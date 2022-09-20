package net.clementlevallois.web.publish.plugin.controller;

import com.google.gson.JsonObject;
import org.gephi.graph.GraphGenerator;
import org.junit.Assert;
import org.junit.Test;

public class PublishingActionsTest {

    @Test
    public void testNoProject() {
        JsonObject obj = PublishingActions.getGexfAsString();
        Assert.assertTrue(obj.has(GlobalConfigParams.ERROR_CODE_NO_OPEN_PROJECT));
    }

    @Test
    public void testEmptyGraph() {
        GraphGenerator graphGenerator = GraphGenerator.build().withWorkspace();
        JsonObject obj = PublishingActions.getGexfAsString(graphGenerator.getWorkspace());
        Assert.assertTrue(obj.has(GlobalConfigParams.ERROR_CODE_EMPTY_NETWORK));
    }

    @Test
    public void testSuccess() {
        GraphGenerator graphGenerator = GraphGenerator.build().withWorkspace().generateTinyGraph();
        JsonObject obj = PublishingActions.getGexfAsString(graphGenerator.getWorkspace());
        Assert.assertTrue(obj.has(GlobalConfigParams.SUCCESS_CODE));
    }
}
