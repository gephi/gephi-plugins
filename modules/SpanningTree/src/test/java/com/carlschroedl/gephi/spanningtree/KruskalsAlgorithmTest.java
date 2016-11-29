package com.carlschroedl.gephi.spanningtree;

import org.gephi.filters.plugin.attribute.AttributeEqualBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.impl.GraphModelImpl;
import org.gephi.graph.impl.utils.MapDeepEquals;
import org.gephi.io.importer.api.Container;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.impl.ImportControllerImpl;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.io.processor.plugin.MultiProcessor;
import org.gephi.io.processor.spi.Processor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.filters.api.Range;
import org.gephi.filters.plugin.attribute.AttributeRangeBuilder;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.api.Column;

/**
 *
 * @author carlschroedl
 */
public class KruskalsAlgorithmTest {

    KruskalsAlgorithm instance;
    GraphModelLoader loader;
    ProjectController projectController;
    private static final String INITIAL_GRAPH_PATH = "/com/carlschroedl/gephi/spanningtree/initial/wiki_kruskal_example_initial.graphml";
    private static final String FINAL_GRAPH_PATH = "/com/carlschroedl/gephi/spanningtree/mst/wiki_kruskal_example_mst.graphml";

    public KruskalsAlgorithmTest() {
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Before
    public void setUp() {
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        loader = new GraphModelLoader(projectController);
        instance = new KruskalsAlgorithm();
    }

    @After
    public void tearDown() {
        projectController.closeCurrentProject();
    }

    /**
     * Test of execute method, of class KruskalsAlgorithm.
     */
    @org.junit.Test
    public void testExecute() {
        GraphModel initialGraphModel = loader.fromFile(INITIAL_GRAPH_PATH);
        instance.execute(initialGraphModel);
        Column column = initialGraphModel.getEdgeTable().getColumn(KruskalsAlgorithm.SPANNING_TREE_COLUMN_ID);
        AttributeEqualBuilder.EqualBooleanFilter.Edge filter = new AttributeEqualBuilder.EqualBooleanFilter.Edge(column);
        filter.setMatch(true);
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        Query query = filterController.createQuery(filter);
        GraphView view = filterController.filter(query);
        initialGraphModel.setVisibleView(view);
        Graph actualMinimumSpanningTree = initialGraphModel.getUndirectedGraphVisible();

        GraphModel expectedGraphModel = loader.fromFile(FINAL_GRAPH_PATH);
        assertTrue(
                GraphTopologyEquals.graphsHaveSameTopology(actualMinimumSpanningTree, expectedGraphModel.getGraph())
        );
    }

}
