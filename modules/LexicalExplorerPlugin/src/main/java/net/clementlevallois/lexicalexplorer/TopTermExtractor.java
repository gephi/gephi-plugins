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
import org.gephi.graph.api.GraphController;
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
import org.gephi.project.api.Workspace;
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
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

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

    public String mineAndSortTextualAttribute(GraphModel gm, String attributeName, String lang, int maxNumberOfTerms) throws IOException {
        
        Graph graph = gm.getGraph();
        
        System.out.println("number of edges:" + graph.getEdgeCount());
        System.out.println("number of nodes:" + graph.getNodeCount());

        // selecting the column corresponding to the attribute we want to analyze
        Column attributeToBeAnalyzed = gm.getNodeTable().getColumn(attributeName);

        // we will iterate / loop on the nodes of the graph
        NodeIterable nodes = graph.getNodes();

        // we will store the text of the attribute for each node in a list
        List<String> textsFromTheAttribute = new ArrayList();

        // doing the iteration now
        Iterator<Node> iteratorOnNodes = nodes.iterator();
        while (iteratorOnNodes.hasNext()) {
            Node node = iteratorOnNodes.next();
            String descriptionForOneNode = (String) node.getAttribute(attributeToBeAnalyzed);
            if (descriptionForOneNode != null && !descriptionForOneNode.isBlank()) {
                textsFromTheAttribute.add(descriptionForOneNode);
            }
        }

        System.out.println("number of nodes that have a description: " + textsFromTheAttribute.size());

        // the multiset will store unique terms from the text we collected and count how many times each term appears
        Multiset<String> textFragments = new Multiset();
        Set<String> languageSpecificLexicon = new HashSet();

        /*
        
        1. we loop through the list of textual descriptions and tokenize them
        
        2. The result is a list of TextFragment objects
        
        3. We keep only the text fragments that are not punctuation signs nor whitespaces

         */
        StopWordsRemover stopWordsRemoverEN = new StopWordsRemover(3, "en");
        StopWordsRemover stopWordsRemoverSECONDLANGUAGE = new StopWordsRemover(3, lang);

        for (String description : textsFromTheAttribute) {
            List<TextFragment> textFragmentsForOneDescription = UmigonTokenizer.tokenize(description, languageSpecificLexicon);
            for (TextFragment oneTextFragment : textFragmentsForOneDescription) {
                if (oneTextFragment.getTypeOfTextFragmentEnum() != TypeOfTextFragmentEnum.WHITE_SPACE
                        & oneTextFragment.getTypeOfTextFragmentEnum() != TypeOfTextFragmentEnum.PUNCTUATION
                        & !stopWordsRemoverEN.shouldItBeRemoved(oneTextFragment.getOriginalForm())
                        & !stopWordsRemoverSECONDLANGUAGE.shouldItBeRemoved(oneTextFragment.getOriginalForm())
                        & oneTextFragment.getOriginalForm().length() > 4) {
                    textFragments.addOne(oneTextFragment.getOriginalForm());
                }
            }
        }
        System.out.println("number of text fragments in the description: " + textFragments.getSize());

        // once we have all the terms and their counts in a multiset, we can sort the terms from the most to the least frequent and select the top n
        List<Map.Entry<String, Integer>> multisetRankedFromTopFrequency = textFragments.sortDesc(textFragments);

        int i = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        for (Map.Entry<String, Integer> tf : multisetRankedFromTopFrequency) {
            sb
                    .append(tf.getKey())
                    .append(" (")
                    .append(tf.getValue())
                    .append(")")
                    .append("<br/>");
            System.out.println("top term: " + tf.getKey() + " (appearing " + tf.getValue() + " times).");
            if (i++ > maxNumberOfTerms) {
                break;
            }
        }
        sb.append("</body></html>");
        return sb.toString();

    }

}
