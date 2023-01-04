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
import java.util.LinkedList;
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
import org.openide.util.Lookup;

/**
 *
 * @author LEVALLOIS
 */
public class NetworkImporter {

    public static void main(String args[]) throws IOException, Exception {
        new NetworkImporter().importFromFile();
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

        GraphModel gm = graphController.getGraphModel();

        Graph graph = gm.getGraph();

        System.out.println("number of edges:" + graph.getEdgeCount());
        System.out.println("number of nodes:" + graph.getNodeCount());

        Column descriptionAttribute = gm.getNodeTable().getColumn("description");

        NodeIterable nodes = graph.getNodes();

        List<String> descriptions = new ArrayList();

        Iterator<Node> iteratorOnNodes = nodes.iterator();
        while (iteratorOnNodes.hasNext()) {
            Node node = iteratorOnNodes.next();
            String descriptionForOneNode = (String) node.getAttribute(descriptionAttribute);
            if (descriptionForOneNode != null && !descriptionForOneNode.isBlank()) {
                descriptions.add(descriptionForOneNode);
            }
        }

        System.out.println("number of nodes that have a description: " + descriptions.size());

        Multiset<String> textFragments = new Multiset();
        Set<String> languageSpecificLexicon = new HashSet();

        /*
        
        1. we loop through the list of textual descriptions and tokenize them
        
        2. The result is a list of TextFragment objects
        
        3. We keep only the text fragments that are not punctuation signs nor whitespaces

         */
        StopWordsRemover stopWordsRemoverEN = new StopWordsRemover(3, "en");
        StopWordsRemover stopWordsRemoverES = new StopWordsRemover(3, "es");

        for (String description : descriptions) {
            List<TextFragment> textFragmentsForOneDescription = UmigonTokenizer.tokenize(description, languageSpecificLexicon);
            for (TextFragment oneTextFragment : textFragmentsForOneDescription) {
                if (
                        oneTextFragment.getTypeOfTextFragmentEnum() != TypeOfTextFragmentEnum.WHITE_SPACE
                        & oneTextFragment.getTypeOfTextFragmentEnum() != TypeOfTextFragmentEnum.PUNCTUATION
                        & !stopWordsRemoverEN.shouldItBeRemoved(oneTextFragment.getOriginalForm())
                        & !stopWordsRemoverES.shouldItBeRemoved(oneTextFragment.getOriginalForm())
                        & oneTextFragment.getOriginalForm().length() > 4
                        ) {
                    textFragments.addOne(oneTextFragment.getOriginalForm());
                }
            }
        }
        System.out.println("number of text fragments in the description: " + textFragments.getSize());

        List<Map.Entry<String, Integer>> multisetRankedFromTopFrequency = textFragments.sortDesc(textFragments);

        int i = 0;
        for (Map.Entry<String, Integer> tf : multisetRankedFromTopFrequency) {
            System.out.println("top term: " + tf.getKey() + " (appearing " + tf.getValue() + " times).");
            if (i++ > 10) {
                break;
            }
        }

    }

}
