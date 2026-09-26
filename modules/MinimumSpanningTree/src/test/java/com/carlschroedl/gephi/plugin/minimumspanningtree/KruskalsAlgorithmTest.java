package com.carlschroedl.gephi.plugin.minimumspanningtree;

import org.gephi.filters.plugin.attribute.AttributeEqualBuilder;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;
import org.gephi.filters.api.FilterController;
import org.gephi.filters.api.Query;
import org.gephi.graph.api.Column;

/**
 *
 * @author carlschroedl
 */
public class KruskalsAlgorithmTest {

    KruskalsAlgorithm instance;
    GraphModelLoader loader;
    ProjectController projectController;
    private static final String INITIAL_GRAPH_PATH = "/com/carlschroedl/gephi/plugin/minimumspanningtree/initial/wiki_kruskal_example_initial.graphml";
    private static final String FINAL_GRAPH_PATH = "/com/carlschroedl/gephi/plugin/minimumspanningtree/mst/wiki_kruskal_example_mst.graphml";

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
        Graph actualMinimumSpanningTree = filterOutNonMinimumSpanningTreeEdges(initialGraphModel);
        
        GraphModel expectedGraphModel = loader.fromFile(FINAL_GRAPH_PATH);
        Graph expectedMinimumSpanningTree = filterOutNonMinimumSpanningTreeEdges(expectedGraphModel);
        
        assertTrue(
                GraphTopologyEquals.graphsHaveSameTopology(actualMinimumSpanningTree, expectedMinimumSpanningTree)
        );
    }
    
    /**
     * Filters out edges that are not part of the minimum spanning tree. Side 
     * Effect: modifies the visible graph of the parameterized GraphModel so 
     * that only minimum spanning tree edges are visible.
     * @param graphModel
     * @return a Graph, the minimum spanning tree
     */
    private Graph filterOutNonMinimumSpanningTreeEdges(GraphModel graphModel){
        Column column = graphModel.getEdgeTable().getColumn(KruskalsAlgorithm.SPANNING_TREE_COLUMN_ID);
        AttributeEqualBuilder.EqualNumberFilter.Edge filter = new AttributeEqualBuilder.EqualNumberFilter.Edge(column);
        filter.setMatch(KruskalsAlgorithm.IN_SPANNING_TREE);
        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        Query query = filterController.createQuery(filter);
        GraphView view = filterController.filter(query);
        graphModel.setVisibleView(view);
        Graph minimumSpanningTree = graphModel.getGraphVisible();
        return minimumSpanningTree;
    }

}
