
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gaze;

import Controller.MyFileImporter;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.TreeMultimap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Map.Entry;
import java.util.*;
import levallois.clement.utils.Clock;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.gephi.io.importer.api.Issue;

/**
 *
 * @author C. Levallois
 */
public class ParserAndVectorsBuilder {

    private BufferedReader br;
    private final String str;
    private String currLine;
    private HashSet<String> setNodes = new HashSet();
    private HashSet<String> setSources = new HashSet();
    private HashSet<String> setTargets = new HashSet();
    private TreeSet<Integer> setSourcesInteger = new TreeSet();
    public static HashMultiset<Integer> multisetTargets = HashMultiset.create();
    public static HashBiMap<String, Integer> mapNodes = HashBiMap.create();
    public static HashBiMap<String, Integer> mapSources = HashBiMap.create();
    public static HashBiMap<String, Integer> mapTargets = HashBiMap.create();
    public static TreeMultimap<Integer, Integer> map = TreeMultimap.create();
    public static TreeMultimap<Integer, Integer> mapUndirected = TreeMultimap.create();
    public static TreeMultimap<Integer, Integer> mapInverse = TreeMultimap.create();
    private String sourceNode;
    private String targetNode;
    private Float weight;
    private HashMap<Pair<Integer, Integer>, Float> mapEdgeToWeight = new HashMap();
    private int countLines = 0;
    public static SparseVector[] listVectors;
    private static Iterator<Integer> nodesIt;
    private static SparseVector vectorMJT;
    public static HashMap<Integer, Integer> mapBetweenness;

    ParserAndVectorsBuilder(String str) {

        this.str = str;

    }

    SparseVector[] EdgeListToMatrix() throws IOException {

        Integer n = 0;
        Integer s = 0;
        Integer t = 0;

        //***
        //
        //#### 1. reading the edges list and creating indexes and maps from it
        //
        //***
        Clock readingFile = new Clock("reading input file");
        br = new BufferedReader(new FileReader(Controller.filePath));

        //counting total number of lines in the file
        LineNumberReader lnr = new LineNumberReader(new FileReader(Controller.filePath));
        lnr.skip(Long.MAX_VALUE);
        int totalLines = lnr.getLineNumber();

        while ((currLine = br.readLine()) != null) {

            if (currLine.startsWith("//")) {
                continue;
            }

            countLines++;

            //for debugging purposes: the buffered reader will stop reading when it meets a line with "stop" in the edges list file
            if ("stop".equals(currLine)) {
                break;
            }
            try {
                String[] fields = currLine.split(str);
                sourceNode = fields[0].trim();
                targetNode = fields[1].trim();

                //assigns an arbitrary value of 0.5 to each edge if the network is unwieghted
                if (Controller.weightedNetwork) {
                    weight = Float.valueOf(fields[2]);
                } else {
                    weight = (float) 0.5;
                }
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                System.out.println("issue with line: " + currLine);
                continue;
            } catch (java.lang.NumberFormatException f) {
                System.out.println("issue with line: " + currLine);
                Issue issue = new Issue("issue with line: " + currLine + ". Line " + countLines + " should have been a number, not text. The line was skipped", Issue.Level.WARNING);
                MyFileImporter.getStaticReport().logIssue(issue);
                continue;
            }

            //this step detects nodes which have not been indexed yet, and give them one
            //without making a distinction between sources and tagets
            boolean newNode1 = setNodes.add(sourceNode);
            boolean newNode2 = setNodes.add(targetNode);

            if (newNode1) {
                mapNodes.put(sourceNode, n);
                n++;
            }

            if (newNode2) {
                mapNodes.put(targetNode, n);

                n++;
            }

            //this step is to attribute distinct indexes to sources and targets in a directed network
            if (Controller.directedNetwork) {
                boolean newSource = setSources.add(sourceNode);
                boolean newTarget = setTargets.add(targetNode);

                if (newSource) {
                    mapSources.put(sourceNode, s);
                    setSourcesInteger.add(s);
                    s++;

                }
                if (newTarget) {
                    mapTargets.put(targetNode, t);
                    t++;
                }
                //this last line is for the specific purpose of being able to easily count the number of times a node appears as a target in the network
                //this is a bit redundant since this info is already contained in the "map" multimap created just below,
                //but the multimap makes it hard to retrieve a multiset of a specific value. Hence.
                multisetTargets.add(mapTargets.get(targetNode));

            }

//for huge networks, these lines could be useful because they make the matrix much more sparse, at a negligible cost of precision            
            if (weight < 0.0001) {
                weight = (float) 0;
            }

            //creation of different maps for latter reference
            // note that for a directed network, we need both a map with nodes referenced by their indexes as sources and targets,
            // and a map where they are referenced just as general nodes
            if (Controller.directedNetwork) {
                map.put(mapSources.get(sourceNode), mapTargets.get(targetNode));
                mapUndirected.put(mapNodes.get(sourceNode), mapNodes.get(targetNode));
//                System.out.println("put in the map: --key: "+mapSources.get(sourceNode)+" value: "+mapTargets.get(targetNode));
                mapEdgeToWeight.put(new Pair(mapSources.get(sourceNode), mapTargets.get(targetNode)), weight);

            } else {
                map.put(mapNodes.get(sourceNode), mapNodes.get(targetNode));
                mapInverse.put(mapNodes.get(targetNode), mapNodes.get(sourceNode));
                mapEdgeToWeight.put(new Pair(mapNodes.get(sourceNode), mapNodes.get(targetNode)), weight);

            }

        }
        br.close();

        System.out.println("Number of edges Source // Target treated: " + countLines);
        System.out.println("Size of the list of vectors: " + setNodes.size());

        readingFile.closeAndPrintClock();

        //***
        //
        //#### 2. reading the edges list and creating indexes and maps from it
        //
        //***
        Clock matrixCreation = new Clock("creating the adjacency matrix from the file");
        //this creates a list of vectors equal to the number of nodes, or just number of sources,
        //depending on whether the network is directed or not
        // a vector is a list of elements which are going to be the stuff of the similarity calculation.
        if (Controller.directedNetwork) {
            listVectors = new SparseVector[mapSources.size()];
        } else {
            listVectors = new SparseVector[setNodes.size()];
        }

        //not sure these 2 lines make a lot of difference? They are intented do save memory.
        setNodes.clear();
        setTargets.clear();

        //this loops through all nodes, or just the sources, to create the similarity matrix
        //depending on whether the network is directed or not
        if (Controller.directedNetwork) {

            nodesIt = setSourcesInteger.iterator();
        } else {

            nodesIt = mapNodes.values().iterator();
        }

        while (nodesIt.hasNext()) {

            Integer currNode = nodesIt.next();
//            System.out.println("number of targets associated with source " + currNode + ": " + ParserAndVectorsBuilder.map.get(currNode).size());

            SortedSet<Integer> targets = new TreeSet();

            //with this step, one gets all the target nodes corresponding to the current source node (directed network)
            // but if the network is undirected, that's a bit more tricky:
            //one needs to get all the sources corresponding to the current node as a target, + all the targets corresponding to this node as a source.
            // so the name "targets" for the sorted set is misleading, since in the case of undirected networks in includes sources too. Oh, well.
            if (Controller.directedNetwork) {
//                System.out.println("currNode: " + mapSources.inverse().get(currNode) + " (index =" + currNode + ")");
                targets = map.get(currNode);
//                System.out.println("Size of the set of connected nodes for node " + mapSources.inverse().get(currNode) + ": " + targets.size());
//                System.out.println("list of connected nodes:" +targets);

            } else {
//                System.out.println(mapNodes.inverse().get(currNode));

                targets.addAll(map.get(currNode));
                targets.addAll(mapInverse.get(currNode));
//                System.out.println("Size of the set of connected nodes for node " + mapNodes.inverse().get(currNode) + ": " + targets.size());

            }

            //Now, we iterate through this set of "targets" to retrieve the weights of all (currNode, currTargets).
            // this step should be skipped in the case of unweighted networks, no?
            // also, note the 2 sub steps:
            // - the first one is for all networks
            // the second one is for undirected networks only
            Iterator<Integer> targetsIt = targets.iterator();
            TreeMultimap<Float, Integer> setCurrWeights = TreeMultimap.create();
            while (targetsIt.hasNext()) {

                Integer currTarget = targetsIt.next();
//                System.out.println("current connected Node: " + currTarget);

                Float currWeight = mapEdgeToWeight.get(new Pair(currNode, currTarget));

                if (currWeight == null) {

                    continue;
                }

//                System.out.println("currWeight: " + currWeight);
                setCurrWeights.put(currWeight, currTarget);
            }

            if (!Controller.directedNetwork) {
                targetsIt = targets.iterator();
                while (targetsIt.hasNext()) {

                    Integer currTarget = targetsIt.next();
//                    System.out.println("current connected Node (in undirected mode): " + currTarget);

                    Float currWeight = mapEdgeToWeight.get(new Pair(currTarget, currNode));
                    if (currWeight == null) {
                        continue;
                    }
//                    currWeight = mapEdgeToWeight.get(new Pair(currTarget, currNode));
//                    System.out.println("currWeight: " + currWeight);
                    setCurrWeights.put(-currWeight, currTarget);
                }
            }

            //now, we iterate through the set of "targets" and their corresponding weight for the curr node.
            //we take an ordered map, descending, because we want to afford the possibility to limit the number of "targets"
            //to a number specified by the user (for performance purposes). If the number of targets is limited, then we want to keep
            //those which have the highest weights. That's what this does.
            //the values retained by this iteration will be written in the vector corresponding to the node we are current looping on
            //at which position? At the position corresponding to their index as "target".
            // let's be clear: a "target" is a target in directed networks,
            // but a "target" can be any node in an undirected network
            // you can see that in the definition of the size of the vector just below
            int countTargets = 0;
            Iterator<Entry<Float, Integer>> ITsetCurrWeights = setCurrWeights.entries().iterator();

//            System.out.println("nb of targets: " + targets.size());
            if (Controller.directedNetwork) {
                vectorMJT = new SparseVector(multisetTargets.elementSet().size());
//                System.out.println("size of vectorMJT: " + vectorMJT.size());

            } else {
                vectorMJT = new SparseVector(mapNodes.size());
            }

            //this is where the threshold of how many targets are considered for the calculus of the cosine.
            while (ITsetCurrWeights.hasNext()) {
//                System.out.println("iteration...");
                Entry<Float, Integer> currEntry = ITsetCurrWeights.next();
                Integer currTarget = currEntry.getValue();
//                System.out.println("current connected node in the loop: " + currTarget);
                Float currWeight = currEntry.getKey();
//                System.out.println("to which the current weight considered for inclusion is: "+currWeight);
                if (countTargets >= Controller.maxNbTargetsPerSourceConsidered4CosineCalc) {
//                    System.out.println("breaking on " + currWeight);
                    break;
                }

                countTargets++;

                int vectorPos = (int) currTarget;
//                System.out.println("vectorPos: " + vectorPos);
//                System.out.println("currWeight: " + currWeight);
                if (Controller.weightedNetwork) {
                    vectorMJT.set(vectorPos, (double) -currWeight);
                } else {
                    vectorMJT.set(vectorPos, 1.00);
//                    System.out.println("setting a value of 1 in vectorMJT, at position: " + vectorPos);
                }

            }

            //finally, the treatment fot the current node is over and we can put the the vector of its targets/ weights in a list of vectors,
            //over which the cosine calculation will take place (see the CosineCalculation class)
//            System.out.println("count targets: " + countTargets);
            listVectors[currNode] = vectorMJT;
            //System.out.println(vectorMJT.getIndex().length);
        }
        System.out.println("adjacency matrix created!");
        System.out.println("Number of sources (vectors): " + listVectors.length);
        System.out.println("Number of targets (size of a vector): " + listVectors[0].size());

        mapEdgeToWeight.clear();
        matrixCreation.closeAndPrintClock();

        //oh, and before leaving there is the betweeness calculation
        //it has to be done on the original network (the one of the user), not on the final network
        //because it makes little sense to measure "how central" a node is in terms of similarity (I think)
        //whereas it is interesting to know where central nodes (in the original network) land in the similarity network.
        //in particular: do they end up being neighbors, or "chiefs" of separate regional kingdoms?

        return listVectors;

    }
}
