package net.clementlevallois.wordcloud;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.io.importer.GraphImporter;
import org.junit.Assert;
import org.junit.Test;

public class TopTermExtractorTest {

    @Test
    public void testTermsExtractionOnEntireGraph() {
        GraphModel graphModel = GraphImporter.importGraph(TopTermExtractorTest.class, "terms.gexf");

        TopTermExtractor ttt = new TopTermExtractor();
        List<String> selectedLanguages = null;
        Assert.assertTrue(ttt.tokenizeSelectedTextualAttributeForTheEntireGraph(graphModel, "terms", selectedLanguages));
        Assert.assertEquals(Set.of("gephi", "pendant", "actuellement", "allez"),
                new HashSet<>(DataManager.getMapOfNodeIdsToTheirTextFragments().values().stream().reduce((l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                }).get()));

    }

    @Test
    public void testSelectionOfTermsOnlyForSelectedNodes() {
        GraphModel graphModel = GraphImporter.importGraph(TopTermExtractorTest.class, "terms.gexf");

        TopTermExtractor ttt = new TopTermExtractor();
        List<String> selectedLanguages = null;
        ttt.tokenizeSelectedTextualAttributeForTheEntireGraph(graphModel, "terms", selectedLanguages);
        List<Node> selectedNodes = new ArrayList();
        NodeIterable nodes = graphModel.getGraph().getNodes();
        for (Node next : nodes) {
            if (next.getLabel().equals("Node 3")) {
                selectedNodes.add(next);
            }
        }
        String topTermsExtractorFromSelectedNodes = ttt.topTermsExtractorFromSelectedNodes(selectedNodes, 5);
        Assert.assertTrue(topTermsExtractorFromSelectedNodes.contains("gephi"));
        Assert.assertTrue(!topTermsExtractorFromSelectedNodes.contains("hello"));
        Assert.assertTrue(!topTermsExtractorFromSelectedNodes.contains("world"));

    }
    @Test
    public void testStopWordsRemovalOnSeveralLanguages() {
        GraphModel graphModel = GraphImporter.importGraph(TopTermExtractorTest.class, "terms.gexf");

        TopTermExtractor ttt = new TopTermExtractor();
        List<String> selectedLanguages = List.of("en", "fr");
        ttt.tokenizeSelectedTextualAttributeForTheEntireGraph(graphModel, "terms", selectedLanguages);
        List<Node> selectedNodes = new ArrayList();
        NodeIterable nodes = graphModel.getGraph().getNodes();
        for (Node next : nodes) {
                selectedNodes.add(next);
        }
        String topTermsExtractorFromSelectedNodes = ttt.topTermsExtractorFromSelectedNodes(selectedNodes, 5);
        Assert.assertTrue(!topTermsExtractorFromSelectedNodes.contains("actuellement"));
        Assert.assertTrue(!topTermsExtractorFromSelectedNodes.contains("allez"));
        Assert.assertTrue(!topTermsExtractorFromSelectedNodes.contains("pendant"));
        Assert.assertTrue(topTermsExtractorFromSelectedNodes.contains("gephi"));

    }
}
