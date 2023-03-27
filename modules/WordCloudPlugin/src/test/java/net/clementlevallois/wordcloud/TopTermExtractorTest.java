package net.clementlevallois.wordcloud;

import java.io.IOException;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.GraphImporter;
import org.junit.Assert;
import org.junit.Test;

public class TopTermExtractorTest {

    @Test
    public void testOnGraph() throws IOException {
        GraphModel graphModel = GraphImporter.importGraph(TopTermExtractorTest.class, "terms.gexf");

        TopTermExtractor ttt = new TopTermExtractor();
        Assert.assertTrue(ttt.tokenizeSelectedTextualAttributeForTheEntireGraph(graphModel, "terms", "en"));
    }
}
