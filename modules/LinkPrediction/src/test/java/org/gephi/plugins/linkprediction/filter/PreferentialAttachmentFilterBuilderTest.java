package org.gephi.plugins.linkprediction.filter;

import org.gephi.filters.spi.Category;
import org.gephi.graph.api.*;
import org.gephi.plugins.linkprediction.base.LinkPredictionFilterPanel;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatistics;
import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

import static org.gephi.plugins.linkprediction.base.LinkPredictionFilterBuilder.LINK_PREDICTION_CATEGORY;
import static org.junit.Assert.assertEquals;

public class PreferentialAttachmentFilterBuilderTest {
    GraphModel graphModel;

    @Before
    public void setUp() {
        //Init project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        //Get the default graph model
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        //Create nodes
        GraphFactory factory = graphModel.factory();
        Node a = factory.newNode("A");
        a.setLabel("Node A");
        Node b = factory.newNode("B");
        b.setLabel("Node B");
        Node c = factory.newNode("C");
        c.setLabel("Node C");
        Node d = factory.newNode("D");
        d.setLabel("Node D");
        Node e = factory.newNode("E");
        e.setLabel("Node E");
        Node f = factory.newNode("F");
        f.setLabel("Node F");
        Node g = factory.newNode("G");
        g.setLabel("Node G");
        Node h = factory.newNode("H");
        h.setLabel("Node H");
        Node i = factory.newNode("I");
        i.setLabel("Node I");

        //Create edges
        Edge e1 = factory.newEdge("E1", a, b, 1, 1, false);
        Edge e2 = factory.newEdge("E2", a, d, 1, 1, false);
        Edge e3 = factory.newEdge("E3", a, e, 1, 1, false);
        Edge e4 = factory.newEdge("E4", b, d, 1, 1, false);
        Edge e5 = factory.newEdge("E5", b, c, 1, 1, false);
        Edge e6 = factory.newEdge("E6", c, d, 1, 1, false);
        Edge e7 = factory.newEdge("E7", c, f, 1, 1, false);
        Edge e8 = factory.newEdge("E8", e, f, 1, 1, false);
        Edge e9 = factory.newEdge("E9", b, a, 1, 1, false);
        Edge e10 = factory.newEdge("E10", f, g, 1, 1, false);
        Edge e11 = factory.newEdge("E11", g, h, 1, 1, false);
        Edge e12 = factory.newEdge("E12", g, i, 1, 1, false);

        // Add nodes
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        undirectedGraph.addNode(a);
        undirectedGraph.addNode(b);
        undirectedGraph.addNode(c);
        undirectedGraph.addNode(d);
        undirectedGraph.addNode(e);
        undirectedGraph.addNode(f);
        undirectedGraph.addNode(g);
        undirectedGraph.addNode(h);
        undirectedGraph.addNode(i);
        undirectedGraph.addEdge(e1);
        undirectedGraph.addEdge(e2);
        undirectedGraph.addEdge(e3);
        undirectedGraph.addEdge(e4);
        undirectedGraph.addEdge(e5);
        undirectedGraph.addEdge(e6);
        undirectedGraph.addEdge(e7);
        undirectedGraph.addEdge(e8);
        undirectedGraph.addEdge(e9);
        undirectedGraph.addEdge(e10);
        undirectedGraph.addEdge(e11);
        undirectedGraph.addEdge(e12);
    }

    @Test
    public void getName() {
        PreferentialAttachmentFilterBuilder builder = new PreferentialAttachmentFilterBuilder();
        assertEquals(PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME, builder.getName());
    }

    @Test
    public void getDescription() {
        PreferentialAttachmentFilterBuilder builder = new PreferentialAttachmentFilterBuilder();
        assertEquals(PreferentialAttachmentFilterBuilder.PREFERENTIAL_ATTACHMENT_DESC, builder.getDescription());
    }

    @Test
    public void getFilter() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        PreferentialAttachmentFilterBuilder builder = new PreferentialAttachmentFilterBuilder();
        assertEquals(PreferentialAttachmentFilter.class, builder.getFilter(workspace).getClass());
    }

    @Test
    public void getCategory() {
        PreferentialAttachmentFilterBuilder builder = new PreferentialAttachmentFilterBuilder();
        Category category  = builder.getCategory();

        assertEquals(LINK_PREDICTION_CATEGORY, category.getName());
    }

    @Test
    public void getPanel() {
        LinkPredictionStatistics statistic = new PreferentialAttachmentStatistics();
        statistic.execute(graphModel);

        PreferentialAttachmentFilterBuilder builder = new PreferentialAttachmentFilterBuilder();
        PreferentialAttachmentFilter filter = new PreferentialAttachmentFilter();
        assertEquals(LinkPredictionFilterPanel.class, builder.getPanel(filter).getClass());
    }
}
