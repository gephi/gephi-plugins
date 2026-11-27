package cz.cvut.fit.gephi.multimode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.junit.Before;
import org.junit.Test;

public class LongTaskTransformationTest {

    private GraphModel model;
    private UndirectedGraph graph;
    private Column kind;

    @Before
    public void openAProject() {
        model = TestGraphs.newProjectModel();
        graph = model.getUndirectedGraph();
        kind = model.getNodeTable().getColumn(TestGraphs.KIND);
    }

    @Test(timeout = 60000)
    public void nodesCanNotBeRemovedByAnotherThreadWhileTheMatricesAreBuilt() throws Exception {
        Node[] a = new Node[3];
        Node[] b = new Node[3];
        List<Node> kindB = new ArrayList<Node>();
        for (int i = 0; i < 3; i++) {
            a[i] = TestGraphs.addNode(model, "a" + i, "A");
            b[i] = TestGraphs.addNode(model, "b" + i, "B");
            kindB.add(b[i]);
        }
        TestGraphs.addNode(model, "c0", "C");
        for (int i = 0; i < 3; i++) {
            TestGraphs.addEdge(model, a[i], b[i], 1.0);
            TestGraphs.addEdge(model, a[i], b[(i + 1) % 3], 1.0);
        }

        AtomicReference<Thread> remover = new AtomicReference<Thread>();
        AtomicBoolean heldBack = new AtomicBoolean();
        GraphModel observed = beforeFirstEdgeLookup(model, () -> {
            // The matrix from the B nodes to the A nodes is being built, another thread removes the B nodes
            Thread thread = new Thread(() -> graph.removeAllNodes(kindB));
            remover.set(thread);
            thread.start();
            // The nodes are either gone, or the thread is waiting for the graph to be released
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            while (thread.isAlive() && thread.getState() != Thread.State.WAITING && System.nanoTime() < deadline) {
                Thread.sleep(5);
            }
            heldBack.set(thread.isAlive());
        });

        // The C node has no neighbour, so the edges are first looked up for the matrix from B to A
        new LongTaskTransformation(kind, "C", "B", "A", 0.0, false, false, false, false).run_offline(observed);

        assertTrue("the nodes were removed while the matrices were built", heldBack.get());
        remover.get().join(10000);
        assertFalse(remover.get().isAlive());
        assertEquals("the nodes are removed once the matrices are built", 4, graph.getNodeCount());
    }

    @Test
    public void graphIsNotLockedAfterACancelledRun() {
        weightedBipartiteGraph();
        LongTaskTransformation task = new LongTaskTransformation(kind, "A", "B", "A", 0.0, false, false, false, false);
        task.cancel();

        task.run_offline(model);

        assertGraphIsNotLocked();
    }

    @Test
    public void graphIsNotLockedAfterAFailedRun() {
        weightedBipartiteGraph();
        // The column of another graph, as the one kept when the workspace changed
        GraphModel other = GraphModel.Factory.newInstance();
        Column foreign = other.getNodeTable().addColumn(TestGraphs.KIND, String.class);
        LongTaskTransformation task = new LongTaskTransformation(foreign, "A", "B", "A", 0.0, false, false, false, false);

        try {
            task.run_offline(model);
            fail("the column does not belong to this graph");
        } catch (IllegalArgumentException expected) {
            // the graph is released all the same
        }

        assertGraphIsNotLocked();
    }

    @Test
    public void projectionMultipliesTheWeightsOfTheEdges() {
        Node[] a = weightedBipartiteGraph();

        new LongTaskTransformation(kind, "A", "B", "A", 0.0, false, false, false, false).run_offline(model);

        assertEquals(5, graph.getNodeCount());
        assertEquals(6, graph.getEdgeCount());
        assertEquals(2 * 3, graph.getEdge(a[0], a[1]).getWeight(), 1e-9);
        assertEquals(1 * 4, graph.getEdge(a[1], a[2]).getWeight(), 1e-9);
        assertNull("a0 and a2 have no B neighbour in common", graph.getEdge(a[0], a[2]));
        assertEquals("A<--->A", graph.getEdge(a[0], a[1]).getAttribute("MMNT-EdgeType"));
        assertGraphIsNotLocked();
    }

    @Test
    public void removeNodesDropsTheNodesOfTheCommonMode() {
        Node[] a = weightedBipartiteGraph();

        new LongTaskTransformation(kind, "A", "B", "A", 0.0, false, true, false, false).run_offline(model);

        assertEquals(3, graph.getNodeCount());
        assertEquals(2, graph.getEdgeCount());
        assertEquals(6.0, graph.getEdge(a[0], a[1]).getWeight(), 1e-9);
        assertEquals(4.0, graph.getEdge(a[1], a[2]).getWeight(), 1e-9);
        assertGraphIsNotLocked();
    }

    @Test
    public void proportionalProjectionComparesTheNeighbours() {
        Node[] a = bipartiteGraph(1, 1, 1, 1);

        new LongTaskTransformation(kind, "A", "B", "A", 0.0, false, false, true, false).run_offline(model);

        assertEquals(5, graph.getNodeCount());
        // a0 and a1 share b0, a1 and a2 share b1: the cosine of their weights is 1 / sqrt(2) for both pairs
        Edge first = graph.getEdge(a[0], a[1]);
        Edge second = graph.getEdge(a[1], a[2]);
        assertEquals(50.0, first.getWeight(), 1e-3);
        assertEquals(50.0, second.getWeight(), 1e-3);
        assertEquals(100 * Math.sqrt(0.5), (Float) first.getAttribute("MM-Strength"), 1e-3);
        assertEquals(100 * Math.sqrt(0.5), (Float) second.getAttribute("MM-Strength"), 1e-3);
        assertNull(graph.getEdge(a[0], a[2]));
        // the share of the B nodes each A node is connected to
        assertEquals(50.0, (Float) a[0].getAttribute("MM-Connections"), 1e-3);
        assertEquals(100.0, (Float) a[1].getAttribute("MM-Connections"), 1e-3);
        assertGraphIsNotLocked();
    }

    @Test
    public void projectionFollowsTheDirectionOfTheEdges() {
        Node a0 = TestGraphs.addNode(model, "a0", "A");
        Node a1 = TestGraphs.addNode(model, "a1", "A");
        Node b0 = TestGraphs.addNode(model, "b0", "B");
        TestGraphs.addEdge(model, a0, b0, 1.0, true);
        TestGraphs.addEdge(model, b0, a1, 1.0, true);

        new LongTaskTransformation(kind, "A", "B", "A", 0.0, false, false, false, true).run_offline(model);

        assertEquals(3, model.getDirectedGraph().getEdgeCount());
        Edge projected = model.getDirectedGraph().getEdge(a0, a1);
        assertNotNull("the a0 to a1 edge goes through b0", projected);
        assertTrue(projected.isDirected());
        assertNull(model.getDirectedGraph().getEdge(a1, a0));
        assertGraphIsNotLocked();
    }

    @Test
    public void newColumnsAndEdgesBelongToTheModelTheRunWorksOn() {
        // Not the model of the workspace which is current
        GraphModel worked = GraphModel.Factory.newInstance();
        Column workedKind = worked.getNodeTable().addColumn(TestGraphs.KIND, String.class);
        Node a0 = TestGraphs.addNode(worked, "a0", "A");
        Node a1 = TestGraphs.addNode(worked, "a1", "A");
        Node b0 = TestGraphs.addNode(worked, "b0", "B");
        TestGraphs.addEdge(worked, a0, b0, 1.0);
        TestGraphs.addEdge(worked, a1, b0, 1.0);

        new LongTaskTransformation(workedKind, "A", "B", "A", 0.0, false, false, false, false).run_offline(worked);

        assertEquals(3, worked.getUndirectedGraph().getEdgeCount());
        Edge projected = worked.getUndirectedGraph().getEdge(a0, a1);
        assertEquals("A<--->A", projected.getAttribute(worked.getEdgeTable().getColumn("MMNT-EdgeType")));
        assertFalse("the current workspace is left alone", model.getEdgeTable().hasColumn("MMNT-EdgeType"));
    }

    /**
     * The A nodes a0 to a2 are connected to the B nodes b0 and b1 by a0-b0 (weight 2), a1-b0 (3), a1-b1 (1)
     * and a2-b1 (4).
     */
    private Node[] weightedBipartiteGraph() {
        return bipartiteGraph(2, 3, 1, 4);
    }

    private Node[] bipartiteGraph(double a0b0, double a1b0, double a1b1, double a2b1) {
        Node[] a = new Node[3];
        for (int i = 0; i < 3; i++) {
            a[i] = TestGraphs.addNode(model, "a" + i, "A");
        }
        Node b0 = TestGraphs.addNode(model, "b0", "B");
        Node b1 = TestGraphs.addNode(model, "b1", "B");
        TestGraphs.addEdge(model, a[0], b0, a0b0);
        TestGraphs.addEdge(model, a[1], b0, a1b0);
        TestGraphs.addEdge(model, a[1], b1, a1b1);
        TestGraphs.addEdge(model, a[2], b1, a2b1);
        return a;
    }

    private void assertGraphIsNotLocked() {
        assertEquals("the read lock is kept", 0, graph.getLock().getReadHoldCount());
        assertEquals("the write lock is kept", 0, graph.getLock().getWriteHoldCount());
    }

    private interface Action {

        void run() throws Exception;
    }

    /**
     * A view of the model which runs the action when the edges between two nodes are looked up for the
     * first time.
     */
    private static GraphModel beforeFirstEdgeLookup(GraphModel real, Action action) {
        UndirectedGraph visible = real.getUndirectedGraphVisible();
        AtomicBoolean done = new AtomicBoolean();
        UndirectedGraph watched = (UndirectedGraph) Proxy.newProxyInstance(UndirectedGraph.class.getClassLoader(),
                new Class<?>[] {UndirectedGraph.class}, (proxy, method, args) -> {
                    if (method.getName().equals("getEdges") && args != null && args.length == 3
                            && done.compareAndSet(false, true)) {
                        action.run();
                    }
                    return invoke(visible, method, args);
                });
        return (GraphModel) Proxy.newProxyInstance(GraphModel.class.getClassLoader(), new Class<?>[] {GraphModel.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getUndirectedGraphVisible")) {
                        return watched;
                    }
                    return invoke(real, method, args);
                });
    }

    private static Object invoke(Object target, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
