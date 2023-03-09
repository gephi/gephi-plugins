/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.clementlevallois.lexicalexplorer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.clementlevallois.stopwords.StopWordsRemover;
import net.clementlevallois.umigon.model.TextFragment;
import net.clementlevallois.umigon.model.TypeOfTextFragment.TypeOfTextFragmentEnum;
import net.clementlevallois.umigon.tokenizer.controller.UmigonTokenizer;
import net.clementlevallois.utils.Multiset;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.file.ImporterGEXF;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author LEVALLOIS
 */
public class TopTermExtractor {

    public static void main(String args[]) throws IOException, Exception {
        new TopTermExtractor().importFromFile();
    }

    public void importFromFile() throws IOException, Exception {

        Path exampleGexf = Path.of("G:\\Mon Drive\\Twitch stream\\gephi plugin development\\gephi-plugins\\modules\\LexicalExplorerPlugin\\qatar user network.gexf");

        String gexfFileAsString = Files.readString(exampleGexf, StandardCharsets.UTF_8);

        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        //Import file
        Container container;
        FileImporter fi = new ImporterGEXF();

        //Append imported data to GraphAPI
        container = importController.importFile(new StringReader(gexfFileAsString), fi);
        container.closeLoader();

        DefaultProcessor processor = new DefaultProcessor();
        processor.setWorkspace(pc.getCurrentWorkspace());
        processor.setContainers(new ContainerUnloader[]{container.getUnloader()});
        processor.process();

    }

    public Boolean tokenizeSelectedTextualAttributeForTheEntireGraph(GraphModel gm, String attributeName, String lang) {

        Graph graph = gm.getGraph();

        // selecting the column corresponding to the attribute we want to analyze
        Column attributeToBeAnalyzed = gm.getNodeTable().getColumn(attributeName);

        // we will iterate / loop on the nodes of the graph
        NodeIterable nodes = graph.getNodes();

        // we will store the text of the attribute for each node in a list
        Map<String, String> textsFromTheAttribute = new HashMap();


        // doing the iteration now
        Iterator<Node> iteratorOnNodes = nodes.iterator();
        while (iteratorOnNodes.hasNext()) {
            Node node = iteratorOnNodes.next();
            String descriptionForOneNode = (String) node.getAttribute(attributeToBeAnalyzed);
            if (descriptionForOneNode != null && !descriptionForOneNode.isBlank()) {
                textsFromTheAttribute.put((String) node.getId(), descriptionForOneNode.toLowerCase());
            }
        }

        Set<String> languageSpecificLexicon = new HashSet();

        /*
        1. we loop through the list of textual descriptions and tokenize them
        2. The result is a list of TextFragment objects
        3. We keep only the text fragments that are not punctuation signs nor whitespaces
         */
        Map<String, List<String>> mapOfNodeIdsToTheirTextFragments = new HashMap();

        StopWordsRemover stopWordsRemoverEN = new StopWordsRemover(3, "en");
        StopWordsRemover stopWordsRemoverSECONDLANGUAGE = new StopWordsRemover(3, lang);

        Iterator<Map.Entry<String, String>> iteratorOnNodesAndTheirTextualAttribute = textsFromTheAttribute.entrySet().iterator();
        while (iteratorOnNodesAndTheirTextualAttribute.hasNext()) {
            try {
                Map.Entry<String, String> next = iteratorOnNodesAndTheirTextualAttribute.next();
                String textualAttribute = next.getValue();
                String nodeId = next.getKey();
                List<TextFragment> textFragmentsForOneDescription = UmigonTokenizer.tokenize(textualAttribute, languageSpecificLexicon);
                for (TextFragment oneTextFragment : textFragmentsForOneDescription) {
                    if (oneTextFragment.getTypeOfTextFragmentEnum() != TypeOfTextFragmentEnum.WHITE_SPACE
                            & oneTextFragment.getTypeOfTextFragmentEnum() != TypeOfTextFragmentEnum.PUNCTUATION
                            & !stopWordsRemoverEN.shouldItBeRemoved(oneTextFragment.getOriginalForm())
                            & !stopWordsRemoverSECONDLANGUAGE.shouldItBeRemoved(oneTextFragment.getOriginalForm())
                            & oneTextFragment.getOriginalForm().length() > 4) {

                        if (mapOfNodeIdsToTheirTextFragments.containsKey(nodeId)) {
                            List<String> textFragmentsForThisNode = mapOfNodeIdsToTheirTextFragments.get(nodeId);
                            textFragmentsForThisNode.add(oneTextFragment.getOriginalForm());
                            mapOfNodeIdsToTheirTextFragments.put(nodeId, textFragmentsForThisNode);
                        } else {
                            List<String> textFragmentsForThisNode = new ArrayList();
                            textFragmentsForThisNode.add(oneTextFragment.getOriginalForm());
                            mapOfNodeIdsToTheirTextFragments.put(nodeId, textFragmentsForThisNode);
                        }
                    }
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        }
        DataManager.setMapOfNodeIdsToTheirTextFragments(mapOfNodeIdsToTheirTextFragments);
        return true;
    }

    public String topTermsExtractorFromSelectedNodes(List<String> nodeIds, int maxNumberOfTerms) {
        // the multiset will store unique terms from the text we collected and count how many times each term appears
        Multiset<String> allTextFragmentsFromAllSelectedNodes = new Multiset();

        Map<String, List<String>> mapOfNodeIdsToTheirTextFragments = DataManager.getMapOfNodeIdsToTheirTextFragments();

        for (String nodeId : nodeIds) {
            List<String> textFragmentsForTheSelectedNode = mapOfNodeIdsToTheirTextFragments.get(nodeId);
            if (textFragmentsForTheSelectedNode == null) {
                continue;
            }
            allTextFragmentsFromAllSelectedNodes.addAllFromListOrSet(textFragmentsForTheSelectedNode);
        }

        if (allTextFragmentsFromAllSelectedNodes.getSize() == 0) {
            return "";
        }

        // once we have all the terms and their counts in a multiset, we can sort the terms from the most to the least frequent and select the top n
        List<Map.Entry<String, Integer>> multisetRankedFromTopFrequency = allTextFragmentsFromAllSelectedNodes.sortDesc(allTextFragmentsFromAllSelectedNodes);

        int i = 1;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> tf : multisetRankedFromTopFrequency) {
            sb
                    .append(tf.getKey())
                    .append(" (")
                    .append(tf.getValue())
                    .append(")")
                    .append("<br/>");
//            System.out.println("top term: " + tf.getKey() + " (appearing " + tf.getValue() + " times).");
            if (i >= maxNumberOfTerms) {
                break;
            }
            i++;
        }
        return sb.toString();

    }
}
