package net.clementlevallois.web.publish.plugin.controller;

import net.clementlevallois.web.publish.plugin.github.PublishingActions;
import net.clementlevallois.web.publish.plugin.exceptions.EmptyGraphException;
import net.clementlevallois.web.publish.plugin.exceptions.FileAboveMaxGithubSizeException;
import net.clementlevallois.web.publish.plugin.exceptions.NoOpenProjectException;
import org.gephi.graph.GraphGenerator;
import org.junit.Assert;
import org.junit.Test;

public class PublishingActionsTest {

    @Test (expected = NoOpenProjectException.class)
    public void testNoProject() throws NoOpenProjectException, EmptyGraphException, FileAboveMaxGithubSizeException {
        PublishingActions.getGexfAsString();
    }

    @Test (expected = EmptyGraphException.class)
    public void testEmptyGraph() throws EmptyGraphException, FileAboveMaxGithubSizeException {
        GraphGenerator graphGenerator = GraphGenerator.build().withWorkspace();
        PublishingActions.getGexfAsStringFromWorkspace(graphGenerator.getWorkspace());
    }

    @Test
    public void testSuccess() throws EmptyGraphException, FileAboveMaxGithubSizeException {
        GraphGenerator graphGenerator = GraphGenerator.build().withWorkspace().generateTinyGraph();
        String result = PublishingActions.getGexfAsStringFromWorkspace(graphGenerator.getWorkspace());
        Assert.assertNotNull(result);
    }
}
