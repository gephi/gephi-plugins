/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Gaze;

import java.util.ArrayList;
import java.util.List;
import levallois.clement.utils.Clock;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

/**
 *
 * @author C. Levallois
 */
public class CosineCalculation implements Runnable {

    public static long cellTime;
    private final SparseVector[] listVectors;
    public static Clock matrixClock;
    static public ArrayList<Integer> nonZeroIndexListSource = new ArrayList();
    static public ArrayList<Integer> nonZeroIndexListTarget = new ArrayList();
    static public int numNodes;
    static double normSource;
    static public ArrayList<Double> norms;
    static public int sizeVector;
    private int numCalculations;
    String logText = "";
    String newLine = "\n";
    String interval = "--------------------------\n";
    int[] sourceIndexes;
    int[] targetIndexes;
    List<Integer> listSourceIndex;
    List<Integer> listTargetIndex;

    CosineCalculation(SparseVector[] listVectors) {

        this.listVectors = listVectors;
    }

    @Override
    public void run() {

        //to clarify: sources and targets refer to the 2 elements of a pair of nodes, that's all
        //without a reference to which node was actually a source in the initial edge list, and which was a target
        SparseVector svSource;
        SparseVector svTarget;


        //the number of nodes which will appear in the final similarity network
        //corresponds to the number of vectors contained in the list created in AdjacencyMtrixBuilder
        numNodes = listVectors.length;
//      numTargets = ParserAndVectorsBuilder.setTargetsShort.size();


        //this looks complicated but is simply the number of elements in the networks and all their combinations
        numCalculations = (int)Math.pow(numNodes, 2) / 2;


        //this is where the adjacency matrix for the final network is built
        Controller.similarityMatrix = new FlexCompColMatrix(numNodes, numNodes);

        System.out.println("size of the similarity matrix: " + numNodes + " x " + numNodes);

        //1. iteration through all nodes of the similarityMatrix

        norms = new ArrayList();

        matrixClock = new Clock("clocking the cosine calculus");

        for (int i = 0; i < numNodes; i++) {

            //in the case of undirected networks, the vector can be empty
            //because there...???
            if (ParserAndVectorsBuilder.listVectors[i] == null) {
                continue;
            }
            svSource = ParserAndVectorsBuilder.listVectors[i];
            norms.add(svSource.norm(Vector.Norm.Two));
//            System.out.println("index source: " + i);

            for (int j = 0; j < numNodes; j++) {
                if (ParserAndVectorsBuilder.listVectors[j] == null) {
                    continue;
                }

                if (j < i) {
//                    System.out.println("index target: " + j);

                    Controller.countCalculus++;

                    svTarget = ParserAndVectorsBuilder.listVectors[j];

                    synchronized (Controller.similarityMatrix) {
                        sourceIndexes = svSource.getIndex();
//                        System.out.println("svSource.getIndex().size: " + sourceIndexes.length);
                        listSourceIndex = new ArrayList();
                        for (int s = 0; s < sourceIndexes.length; s++) {
                            listSourceIndex.add(sourceIndexes[s]);
                        }
//                        System.out.println(listSourceIndex.size());
                        targetIndexes = svTarget.getIndex();
//                        System.out.println("svTarget.getIndex().size: " + targetIndexes.length);

                        listTargetIndex = new ArrayList();
                        for (int s = 0; s < targetIndexes.length; s++) {
                            listTargetIndex.add(targetIndexes[s]);
                        }
//                        System.out.println(listTargetIndex.size());

                        listSourceIndex.retainAll(listTargetIndex);
                        if (!listSourceIndex.isEmpty()) {

                            doCalculus(svSource, svTarget, i, j);
                        }
                    }

                } else {
                    Controller.similarityMatrix.set(i, j, 0);
                }

            }
        }
        matrixClock.closeAndPrintClock();
    }


    static void doCalculus(SparseVector source, SparseVector target, int i, int j) {

        double result = source.dot(target) / (CosineCalculation.norms.get(i) * CosineCalculation.norms.get(j));
        //System.out.println("result in the runnable: " + result);
//    Triple similarityResult = new Triple(i,j,result);
        Controller.similarityMatrix.set(i, j, result);
//    long endTime = System.currentTimeMillis();
//    CosineCalculation.cellTime = CosineCalculation.cellTime + endTime-currentTime; 
    }
}
