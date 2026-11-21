package net.clementlevallois.ForceAtlas3D;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.junit.Test;

public class ForceAtlas2Test {

    @Test
    public void staticEdgeWeightsAreReadWhenAnotherElementHoldsATime() {
        GraphModel timed = staticWeightsGraph(true);
        // The graph is dynamic because a node holds a time, but the edge weight is a plain number
        assertTrue(timed.isDynamic());
        assertFalse(timed.getEdgeTable().getColumn("weight").isDynamic());

        double[] positions = layOut(timed, 1., 3);

        assertArrayEquals(layOut(staticWeightsGraph(false), 1., 3), positions, 0.);
    }

    @Test
    public void staticEdgeWeightsAreReadWhateverTheEdgeWeightInfluence() {
        for (double influence : new double[]{0., 0.5, 1., 2.}) {
            double[] positions = layOut(staticWeightsGraph(true), influence, 3);

            assertArrayEquals("influence " + influence, layOut(staticWeightsGraph(false), influence, 3), positions, 0.);
        }
    }

    @Test
    public void dynamicEdgeWeightsAreReadLikeStaticOnes() {
        GraphModel dynamic = dynamicWeightsGraph();
        assertTrue(dynamic.isDynamic());
        assertTrue(dynamic.getEdgeTable().getColumn("weight").isDynamic());

        double[] positions = layOut(dynamic, 1., 3);

        // Each weight holds a single value, so the layout is the one of the same weights without times
        assertArrayEquals(layOut(staticWeightsGraph(false), 1., 3), positions, 1e-6);
        // and the weights are what drives it
        double[] withoutWeights = layOut(staticWeightsGraph(false), 0., 3);
        double difference = 0;
        for (int i = 0; i < positions.length; i++) {
            difference = Math.max(difference, Math.abs(positions[i] - withoutWeights[i]));
        }
        assertTrue("The edge weights should change the layout", difference > 1e-3);
    }

    @Test
    public void releasesTheGraphLockWhenAnIterationFails() {
        GraphModel graphModel = staticWeightsGraph(false);
        ForceAtlas2 layout = newLayout(graphModel, 1.);
        layout.initAlgo();
        layout.endAlgo();

        // The thread pool is shut down by endAlgo, so the iteration fails once it holds the graph lock
        try {
            layout.goAlgo();
            fail("Expected the iteration to fail");
        } catch (RejectedExecutionException expected) {
        }

        assertEquals(0, graphModel.getGraphVisible().getLock().getReadHoldCount());
    }

    @Test
    public void stoppingTheLayoutWhileItWaitsForTheRepulsionIsNotReportedAsAnError() throws Exception {
        GraphModel graphModel = staticWeightsGraph(false);
        ForceAtlas2 layout = newLayout(graphModel, 1.);
        layout.initAlgo();
        assertTrue(layout.canAlgo());

        // Keep the only worker busy, so the repulsion tasks wait in the queue and the layout thread waits for them
        ExecutorService pool = poolOf(layout);
        CountDownLatch busy = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        pool.submit(() -> {
            busy.countDown();
            try {
                release.await();
            } catch (InterruptedException ignored) {
            }
        });
        assertTrue(busy.await(10, TimeUnit.SECONDS));

        CrashReporter reporter = new CrashReporter();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        AtomicBoolean interruptKept = new AtomicBoolean();
        AtomicBoolean canContinue = new AtomicBoolean(true);
        AtomicInteger locksHeld = new AtomicInteger(-1);
        Thread layoutThread = new Thread(() -> {
            try {
                // What the layout controller does on its thread, until it checks whether to go on
                layout.goAlgo();
                interruptKept.set(Thread.currentThread().isInterrupted());
                canContinue.set(layout.canAlgo());
                locksHeld.set(graphModel.getGraphVisible().getLock().getReadHoldCount());
            } catch (Throwable t) {
                failure.set(t);
            }
        }, "layout");
        layoutThread.setDaemon(true);
        try {
            layoutThread.start();
            awaitWaitingForATask(layoutThread);

            // The controller does this when a stop request isn't honoured in time
            layoutThread.interrupt();
            layoutThread.join(TimeUnit.SECONDS.toMillis(5));

            assertTrue("Stopping the layout is not an error to report: " + reporter.reported(), reporter.reported().isEmpty());
            assertFalse("The layout should leave the iteration when interrupted, it keeps waiting for the repulsion",
                    layoutThread.isAlive());
        } finally {
            release.countDown();
            reporter.close();
            pool.shutdown();
        }

        assertNull("The layout failed: " + failure.get(), failure.get());
        assertTrue("The interrupt must be kept for the caller", interruptKept.get());
        assertFalse("The controller's loop would go on running the layout", canContinue.get());
        assertEquals("The graph is still locked", 0, locksHeld.get());

        // The repulsion tasks that were still queued were cancelled: they never ran, no node moved
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));
        for (Node n : graphModel.getGraph().getNodes()) {
            ForceAtlas2LayoutData data = n.getLayoutData();
            assertEquals(n.getId() + " got a force", 0., Math.abs(data.dx) + Math.abs(data.dy) + Math.abs(data.dz), 0.);
        }
        layout.endAlgo();
    }

    @Test
    public void aRepulsionTaskStopsWhenItsThreadIsInterrupted() {
        AtomicInteger forces = new AtomicInteger();
        ForceFactory.RepulsionForce counting = ForceFactory.builder.new RepulsionForce() {
            @Override
            public void apply(Node n1, Node n2) {
                forces.incrementAndGet();
            }

            @Override
            public void apply(Node n, Region r) {
                forces.incrementAndGet();
            }

            @Override
            public void apply(Node n, double g) {
                forces.incrementAndGet();
            }
        };
        Node[] nodes = staticWeightsGraph(false).getGraph().getNodes().toArray();
        for (Node n : nodes) {
            n.setLayoutData(new ForceAtlas2LayoutData());
        }

        for (boolean barnesHut : new boolean[]{false, true}) {
            Region region = null;
            if (barnesHut) {
                region = new Region(nodes);
                region.buildSubRegions();
            }
            NodesThread task = new NodesThread(nodes, 0, nodes.length, barnesHut, 1.2, 1., counting, 40., region, counting);

            task.run();
            assertTrue("The task applies forces, barnesHut " + barnesHut, forces.getAndSet(0) > 0);

            Thread.currentThread().interrupt();
            try {
                task.run();
            } finally {
                // Don't leave the test thread interrupted
                Thread.interrupted();
            }
            assertEquals("barnesHut " + barnesHut, 0, forces.get());
        }
    }

    @Test
    public void barnesHutSwitchedOnDuringAnIterationDoesNotFailTheRepulsion() {
        // Read as off to decide whether to build the region, then as on when the repulsion tasks are created
        assertAnIterationUsesOneBarnesHutValue(false, true);
    }

    @Test
    public void barnesHutSwitchedOffDuringAnIterationIsUsedConsistently() {
        assertAnIterationUsesOneBarnesHutValue(true, false);
    }

    /**
     * An iteration has to compute what it does when the option keeps the value it was first read as
     */
    private static void assertAnIterationUsesOneBarnesHutValue(boolean first, boolean then) {
        GraphModel steadyGraph = staticWeightsGraph(false);
        ForceAtlas2 steady = newLayout(steadyGraph, 1.);
        steady.setBarnesHutOptimize(first);
        double[] expected = layOut(steady, steadyGraph, 1);

        // and that has to differ from what the other value computes, or the test shows nothing
        GraphModel otherGraph = staticWeightsGraph(false);
        ForceAtlas2 other = newLayout(otherGraph, 1.);
        other.setBarnesHutOptimize(then);
        assertFalse(Arrays.equals(expected, layOut(other, otherGraph, 1)));

        GraphModel changingGraph = staticWeightsGraph(false);
        ForceAtlas2 changing = newLayout(new ChangingBarnesHut(first, then), changingGraph, 1.);
        CrashReporter reporter = new CrashReporter();
        double[] positions;
        try {
            positions = layOut(changing, changingGraph, 1);
        } finally {
            reporter.close();
        }

        assertTrue("A repulsion task failed and was reported: " + reporter.reported(), reporter.reported().isEmpty());
        assertArrayEquals(expected, positions, 0.);
    }

    /**
     * What Gephi's crash reporter does with the log: every record that carries a Throwable becomes a crash report
     */
    private static final class CrashReporter extends Handler {

        private final List<Throwable> reported = Collections.synchronizedList(new ArrayList<Throwable>());

        CrashReporter() {
            Logger.getLogger("").addHandler(this);
        }

        @Override
        public void publish(LogRecord record) {
            if (record.getThrown() != null) {
                reported.add(record.getThrown());
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() {
            Logger.getLogger("").removeHandler(this);
        }

        List<Throwable> reported() {
            return new ArrayList<Throwable>(reported);
        }
    }

    /**
     * A layout whose Barnes-Hut option answers differently on its successive reads, as it does when the user changes
     * the option while the layout runs
     */
    private static final class ChangingBarnesHut extends ForceAtlas2 {

        private final boolean first;
        private final boolean then;
        private final AtomicInteger reads = new AtomicInteger();

        ChangingBarnesHut(boolean first, boolean then) {
            super(new ForceAtlas2Builder());
            this.first = first;
            this.then = then;
        }

        @Override
        public Boolean isBarnesHutOptimize() {
            return reads.getAndIncrement() == 0 ? first : then;
        }
    }

    private static ExecutorService poolOf(ForceAtlas2 layout) throws Exception {
        Field pool = ForceAtlas2.class.getDeclaredField("pool");
        pool.setAccessible(true);
        return (ExecutorService) pool.get(layout);
    }

    private static void awaitWaitingForATask(Thread thread) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (thread.getState() == Thread.State.WAITING
                    && Arrays.stream(thread.getStackTrace()).anyMatch(f -> f.getClassName().equals(FutureTask.class.getName()))) {
                return;
            }
            Thread.sleep(1);
        }
        fail("The layout never waited for a repulsion task");
    }

    /**
     * Three nodes at distinct positions joined by two edges of weights 2 and 1
     */
    private static GraphModel staticWeightsGraph(boolean withATime) {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Node[] nodes = addNodes(graphModel);
        Graph graph = graphModel.getGraph();
        graph.addEdge(graphModel.factory().newEdge("ab", nodes[0], nodes[1], 0, 2., false));
        graph.addEdge(graphModel.factory().newEdge("bc", nodes[1], nodes[2], 0, 1., false));
        if (withATime) {
            nodes[0].addTimestamp(1.);
        }
        return graphModel;
    }

    /**
     * The same graph, with the weights 2 and 1 held at a time each
     */
    private static GraphModel dynamicWeightsGraph() {
        Configuration configuration = Configuration.builder()
                .edgeWeightType(TimestampDoubleMap.class)
                .timeRepresentation(TimeRepresentation.TIMESTAMP)
                .build();
        GraphModel graphModel = GraphModel.Factory.newInstance(configuration);
        Node[] nodes = addNodes(graphModel);
        Graph graph = graphModel.getGraph();
        Edge ab = graphModel.factory().newEdge("ab", nodes[0], nodes[1], 0, 1., false);
        Edge bc = graphModel.factory().newEdge("bc", nodes[1], nodes[2], 0, 1., false);
        graph.addEdge(ab);
        graph.addEdge(bc);
        ab.setWeight(2., 1.);
        bc.setWeight(1., 2.);
        return graphModel;
    }

    private static Node[] addNodes(GraphModel graphModel) {
        Node[] nodes = {
            node(graphModel, "a", 0f, 0f),
            node(graphModel, "b", 100f, 10f),
            node(graphModel, "c", -30f, 80f)
        };
        graphModel.getGraph().addAllNodes(Arrays.asList(nodes));
        return nodes;
    }

    private static Node node(GraphModel graphModel, String id, float x, float y) {
        Node node = graphModel.factory().newNode(id);
        node.setX(x);
        node.setY(y);
        return node;
    }

    private static ForceAtlas2 newLayout(GraphModel graphModel, double edgeWeightInfluence) {
        return newLayout(new ForceAtlas2(new ForceAtlas2Builder()), graphModel, edgeWeightInfluence);
    }

    private static ForceAtlas2 newLayout(ForceAtlas2 layout, GraphModel graphModel, double edgeWeightInfluence) {
        layout.setGraphModel(graphModel);
        // One thread and distinct positions keep the layout deterministic
        layout.setThreadsCount(1);
        layout.setEdgeWeightInfluence(edgeWeightInfluence);
        return layout;
    }

    /**
     * Runs the layout and returns the finite x, y and z of the nodes
     */
    private static double[] layOut(GraphModel graphModel, double edgeWeightInfluence, int iterations) {
        return layOut(newLayout(graphModel, edgeWeightInfluence), graphModel, iterations);
    }

    private static double[] layOut(ForceAtlas2 layout, GraphModel graphModel, int iterations) {
        layout.initAlgo();
        try {
            for (int i = 0; i < iterations; i++) {
                layout.goAlgo();
            }
        } finally {
            layout.endAlgo();
        }

        double[] positions = new double[3 * graphModel.getGraph().getNodeCount()];
        int i = 0;
        for (Node n : graphModel.getGraph().getNodes()) {
            for (float coordinate : new float[]{n.x(), n.y(), n.z()}) {
                assertTrue(n.getId() + " position " + coordinate, Float.isFinite(coordinate));
                positions[i++] = coordinate;
            }
        }
        return positions;
    }
}
