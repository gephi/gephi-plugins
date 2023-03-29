package net.clementlevallois.wordcloud;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.GraphImporter;
import org.junit.Assert;
import org.junit.Test;

public class TopTermExtractorTest {

    @Test
    public void testOnGraph() {
        GraphModel graphModel = GraphImporter.importGraph(TopTermExtractorTest.class, "terms.gexf");

        TopTermExtractor ttt = new TopTermExtractor();
        Assert.assertTrue(ttt.tokenizeSelectedTextualAttributeForTheEntireGraph(graphModel, "terms", "en"));
        Assert.assertEquals(Set.of("gephi"),
            new HashSet<>(DataManager.getMapOfNodeIdsToTheirTextFragments().values().stream().reduce((l1, l2) -> {
                l1.addAll(l2);
                return l1;
            }).get()));
    }
}
