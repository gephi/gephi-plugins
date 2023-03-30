package net.clementlevallois.wordcloud;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.io.importer.GraphImporter;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class TopTermExtractorTest {

    @Test
    public void testTermsExtractionOnEntireGraph() {
        GraphModel graphModel = GraphImporter.importGraph(TopTermExtractorTest.class, "terms.gexf");

        TopTermExtractor ttt = new TopTermExtractor();
        Assert.assertTrue(ttt.tokenizeSelectedTextualAttributeForTheEntireGraph(graphModel, "terms", "en"));
        Assert.assertEquals(Set.of("gephi"),
                new HashSet<>(DataManager.getMapOfNodeIdsToTheirTextFragments().values().stream().reduce((l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                }).get()));

    }

    @Test
    public void testSelectionOfTermsOnlyForSelectedNodes() {
        GraphModel graphModel = GraphImporter.importGraph(TopTermExtractorTest.class, "terms.gexf");

        TopTermExtractor ttt = new TopTermExtractor();
        ttt.tokenizeSelectedTextualAttributeForTheEntireGraph(graphModel, "terms", "en");
        List<Node> selectedNodes = new ArrayList();
        NodeIterable nodes = graphModel.getGraph().getNodes();
        Iterator<Node> it = nodes.iterator();
        while (it.hasNext()) {
            Node next = it.next();
            if (next.getLabel().equals("Node 3")) {
                selectedNodes.add(next);
            }
        }
        String topTermsExtractorFromSelectedNodes = ttt.topTermsExtractorFromSelectedNodes(selectedNodes, 3);
        Assert.assertTrue(topTermsExtractorFromSelectedNodes.contains("gephi"));
        Assert.assertTrue(!topTermsExtractorFromSelectedNodes.contains("hello"));
        Assert.assertTrue(!topTermsExtractorFromSelectedNodes.contains("world"));

    }
}
