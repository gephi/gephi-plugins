package net.clementlevallois.web.publish.plugin.github;

import java.io.IOException;
import net.clementlevallois.web.publish.plugin.exceptions.EmptyGraphException;
import net.clementlevallois.web.publish.plugin.exceptions.NoOpenProjectException;
import org.gephi.graph.GraphGenerator;
import org.junit.Assert;
import org.junit.Test;

public class PublishRunnableTest {

    @Test (expected = NoOpenProjectException.class)
    public void testNoProject() throws IOException {
        PublishRunnable.getGexfAsString();
    }

    @Test (expected = EmptyGraphException.class)
    public void testEmptyGraph() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().withWorkspace();
        PublishRunnable.getGexfAsStringFromWorkspace(graphGenerator.getWorkspace());
    }

    @Test
    public void testSuccess() throws IOException {
        GraphGenerator graphGenerator = GraphGenerator.build().withWorkspace().generateTinyGraph();
        String result = PublishRunnable.getGexfAsStringFromWorkspace(graphGenerator.getWorkspace());
        Assert.assertNotNull(result);
    }
}
