package com.carlschroedl.gephi.plugin.minimumspanningtree;

import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.utils.progress.ProgressTicket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openide.util.Lookup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * KruskalsAlgorithm reports the failures it runs into by logging them instead of throwing them, so these tests
 * watch the logged throwables as well as the result.
 */
public class KruskalsAlgorithmCornerCasesTest {

    private ProjectController projectController;
    private GraphModelLoader loader;
    private final List<Throwable> logged = new CopyOnWriteArrayList<>();
    private Handler handler;

    @Before
    public void setUp() {
        projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        loader = new GraphModelLoader(projectController);
        handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getThrown() != null) {
                    logged.add(record.getThrown());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        Logger.getLogger("").addHandler(handler);
    }

    @After
    public void tearDown() {
        Logger.getLogger("").removeHandler(handler);
        projectController.closeCurrentProject();
    }

    @Test
    public void testGraphWithAnIsolatedNodeGetsASpanningForest() {
        GraphModel graphModel = loader.fromScratch();
        Node[] nodes = addNodes(graphModel, 4);
        addEdge(graphModel, "a-b", nodes[0], nodes[1], 1);
        addEdge(graphModel, "b-c", nodes[1], nodes[2], 2);
        addEdge(graphModel, "a-c", nodes[0], nodes[2], 3);

        execute(graphModel);

        assertNoFailureLogged();
        assertEquals(ids("a-b", "b-c"), edgesInSpanningTree(graphModel));
    }

    @Test
    public void testEachComponentGetsItsOwnTree() {
        GraphModel graphModel = loader.fromScratch();
        Node[] nodes = addNodes(graphModel, 5);
        addEdge(graphModel, "a-b", nodes[0], nodes[1], 1);
        addEdge(graphModel, "b-c", nodes[1], nodes[2], 2);
        addEdge(graphModel, "a-c", nodes[0], nodes[2], 3);
        addEdge(graphModel, "d-e", nodes[3], nodes[4], 5);

        execute(graphModel);

        assertNoFailureLogged();
        assertEquals(ids("a-b", "b-c", "d-e"), edgesInSpanningTree(graphModel));
    }

    @Test
    public void testNodesWithoutEdges() {
        GraphModel graphModel = loader.fromScratch();
        addNodes(graphModel, 3);

        execute(graphModel);

        assertNoFailureLogged();
        assertTrue(edgesInSpanningTree(graphModel).isEmpty());
    }

    @Test
    public void testConnectedGraphWithSelfLoopAndParallelEdges() {
        GraphModel graphModel = loader.fromScratch();
        Node[] nodes = addNodes(graphModel, 3);
        addEdge(graphModel, "loop", nodes[0], nodes[0], 0.5);
        addEdge(graphModel, "a-b heavy", nodes[0], nodes[1], 2);
        addEdge(graphModel, "a-b light", nodes[0], nodes[1], 1);
        addEdge(graphModel, "b-c", nodes[1], nodes[2], 3);

        execute(graphModel);

        assertNoFailureLogged();
        assertEquals(ids("a-b light", "b-c"), edgesInSpanningTree(graphModel));
    }

    @Test
    public void testCancelledFirstRunRemovesTheColumnItCreated() {
        GraphModel graphModel = loader.fromScratch();
        Node[] nodes = addNodes(graphModel, 4);
        addEdge(graphModel, "a-b", nodes[0], nodes[1], 1);
        addEdge(graphModel, "b-c", nodes[1], nodes[2], 2);
        addEdge(graphModel, "c-d", nodes[2], nodes[3], 3);
        addEdge(graphModel, "a-d", nodes[0], nodes[3], 4);

        KruskalsAlgorithm algorithm = new KruskalsAlgorithm();
        // Cancels from the first progress report, as the cancel button does while the algorithm runs
        algorithm.setProgressTicket((ProgressTicket) Proxy.newProxyInstance(
                ProgressTicket.class.getClassLoader(), new Class<?>[]{ProgressTicket.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("progress")) {
                        algorithm.cancel();
                    }
                    return null;
                }));
        algorithm.execute(graphModel);

        assertNoFailureLogged();
        assertNull(graphModel.getEdgeTable().getColumn(KruskalsAlgorithm.SPANNING_TREE_COLUMN_ID));
    }

    private void execute(GraphModel graphModel) {
        new KruskalsAlgorithm().execute(graphModel);
    }

    private void assertNoFailureLogged() {
        assertTrue("The algorithm logged a failure: " + logged, logged.isEmpty());
    }

    private Node[] addNodes(GraphModel graphModel, int count) {
        Node[] nodes = new Node[count];
        for (int i = 0; i < count; i++) {
            nodes[i] = graphModel.factory().newNode("n" + i);
            graphModel.getGraph().addNode(nodes[i]);
        }
        return nodes;
    }

    private void addEdge(GraphModel graphModel, String id, Node source, Node target, double weight) {
        Edge edge = graphModel.factory().newEdge(id, source, target, 0, weight, false);
        assertTrue("Edge " + id + " was not added", graphModel.getGraph().addEdge(edge));
    }

    private Set<String> ids(String... ids) {
        Set<String> set = new HashSet<>();
        for (String id : ids) {
            set.add(id);
        }
        return set;
    }

    private Set<String> edgesInSpanningTree(GraphModel graphModel) {
        Column column = graphModel.getEdgeTable().getColumn(KruskalsAlgorithm.SPANNING_TREE_COLUMN_ID);
        assertNotNull(column);
        Set<String> inTree = new HashSet<>();
        for (Edge edge : graphModel.getGraph().getEdges()) {
            if (Integer.valueOf(KruskalsAlgorithm.IN_SPANNING_TREE).equals(edge.getAttribute(column))) {
                inTree.add(String.valueOf(edge.getId()));
            }
        }
        return inTree;
    }
}
