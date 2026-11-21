package org.wouterspekkink.plugins.layout.orderedgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wouterspekkink.plugins.layout.orderedgraph.ForceFactory.RepulsionForce;

/**
 * The layout controller stops a layout that does not end in time by interrupting the thread running it, and that is
 * typically a thread waiting for the repulsion tasks. The interrupt status is set before goAlgo() in most tests here:
 * the repulsion tasks of a graph this size are never all done by the time goAlgo() waits for them.
 */
public class OrderedLayoutCancellationTest {

    private static final int NODES = 2000;
    // Only reached when the code under test is wrong, the run is over in a few passes otherwise
    private static final long TIMEOUT_MILLIS = TimeUnit.SECONDS.toMillis(30);

    private final List<Throwable> logged = Collections.synchronizedList(new ArrayList<Throwable>());
    private final Handler recorder = new Handler() {
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

    @Before
    public void recordLoggedThrowables() {
        // Gephi's crash reporter is a handler of the root logger, it reports every Throwable that is logged
        Logger.getLogger("").addHandler(recorder);
    }

    @After
    public void stopRecording() {
        Logger.getLogger("").removeHandler(recorder);
        // Never leave the interrupt status to the next test
        Thread.interrupted();
    }

    @Test
    public void interruptWhileWaitingForTheRepulsionTasksIsNotLogged() {
        OrderedLayout layout = newLayout(orderedGraph(NODES));

        layout.initAlgo();
        Thread.currentThread().interrupt();
        layout.goAlgo();
        Thread.interrupted();
        layout.endAlgo();

        assertTrue("Stopping a layout is not a failure: " + logged, logged.isEmpty());
    }

    @Test
    public void interruptWhileWaitingForTheRepulsionTasksIsKept() {
        OrderedLayout layout = newLayout(orderedGraph(NODES));

        layout.initAlgo();
        Thread.currentThread().interrupt();
        layout.goAlgo();
        boolean interrupted = Thread.interrupted();
        layout.endAlgo();

        assertTrue("The code running the layout must still see the interrupt", interrupted);
    }

    @Test
    public void interruptedLayoutCannotRunAnotherPass() {
        OrderedLayout layout = newLayout(orderedGraph(NODES));

        layout.initAlgo();
        assertTrue(layout.canAlgo());
        Thread.currentThread().interrupt();
        layout.goAlgo();
        // The layout controller loops while canAlgo() is true and its own stop flag is not set
        boolean canAlgo = layout.canAlgo();
        Thread.interrupted();
        layout.endAlgo();

        assertFalse("The pass was interrupted, the run must end", canAlgo);
    }

    @Test
    public void runDrivenByCanAlgoEndsWhenItsThreadIsInterrupted() throws InterruptedException {
        OrderedLayout layout = newLayout(orderedGraph(NODES));
        AtomicInteger passes = new AtomicInteger();
        AtomicBoolean abandoned = new AtomicBoolean();
        Thread runner = new Thread(() -> {
            layout.initAlgo();
            while (layout.canAlgo() && !abandoned.get()) {
                layout.goAlgo();
                passes.incrementAndGet();
            }
            layout.endAlgo();
        });
        runner.start();
        long deadline = System.currentTimeMillis() + TIMEOUT_MILLIS;
        while (passes.get() == 0 && runner.isAlive() && System.currentTimeMillis() < deadline) {
            Thread.sleep(1);
        }

        runner.interrupt();
        runner.join(TIMEOUT_MILLIS);
        boolean ended = !runner.isAlive();
        // Never leave a layout running when the assertions below fail
        abandoned.set(true);
        runner.join(TIMEOUT_MILLIS);

        assertTrue("The run must end when its thread is interrupted", ended);
        assertTrue("Stopping a layout is not a failure: " + logged, logged.isEmpty());
    }

    @Test
    public void interruptedPassReleasesTheGraphLock() {
        GraphModel graphModel = orderedGraph(NODES);
        OrderedLayout layout = newLayout(graphModel);

        layout.initAlgo();
        Thread.currentThread().interrupt();
        layout.goAlgo();
        Thread.interrupted();
        assertEquals(0, graphModel.getGraphVisible().getLock().getReadHoldCount());

        layout.endAlgo();
        assertEquals(0, graphModel.getGraphVisible().getLock().getReadHoldCount());
    }

    @Test
    public void failedPassReleasesTheGraphLock() {
        GraphModel graphModel = orderedGraph(50);
        Column text = graphModel.getNodeTable().addColumn("text", String.class);
        for (Node n : graphModel.getGraph().getNodes().toArray()) {
            n.setAttribute(text, "a");
        }
        OrderedLayout layout = newLayout(graphModel);
        layout.setOrder(text);

        layout.initAlgo();
        try {
            layout.goAlgo();
        } catch (RuntimeException expected) {
            // The order is not a number, so the pass fails once it holds the graph lock
        }
        assertEquals(0, graphModel.getGraphVisible().getLock().getReadHoldCount());

        layout.endAlgo();
        assertEquals(0, graphModel.getGraphVisible().getLock().getReadHoldCount());
    }

    @Test
    public void interruptedRepulsionTaskAppliesNoForces() {
        GraphModel graphModel = orderedGraph(200);
        OrderedLayout layout = newLayout(graphModel);
        layout.initAlgo();
        Node[] nodes = graphModel.getGraph().getNodes().toArray();

        // A task that was cancelled while it runs must stop at once, a layout that ended must not be touched by it
        Thread.currentThread().interrupt();
        repulsionTask(layout, nodes).run();
        Thread.interrupted();

        assertEquals(0, nodesWithForces(nodes));
        layout.endAlgo();
    }

    @Test
    public void repulsionTaskAppliesForces() {
        GraphModel graphModel = orderedGraph(200);
        OrderedLayout layout = newLayout(graphModel);
        layout.initAlgo();
        Node[] nodes = graphModel.getGraph().getNodes().toArray();

        repulsionTask(layout, nodes).run();

        assertTrue(nodesWithForces(nodes) > 0);
        layout.endAlgo();
    }

    @Test
    public void uninterruptedRunStillOrdersTheNodes() {
        GraphModel graphModel = orderedGraph(200);
        OrderedLayout layout = newLayout(graphModel);

        layout.initAlgo();
        for (int i = 0; i < 3 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        layout.endAlgo();

        assertTrue(logged.toString(), logged.isEmpty());
        for (int i = 0; i < 200; i++) {
            Node n = graphModel.getGraph().getNode(String.valueOf(i));
            assertEquals(i * layout.getOrderScale(), n.x(), 1e-3);
        }
    }

    private static OrderedLayout newLayout(GraphModel graphModel) {
        OrderedLayout layout = new OrderedLayoutBuilder().buildLayout();
        layout.setGraphModel(graphModel);
        layout.setThreadsCount(2);
        return layout;
    }

    private static NodesThread repulsionTask(OrderedLayout layout, Node[] nodes) {
        RepulsionForce repulsion = ForceFactory.builder.buildRepulsion(layout.getScalingRatio());
        return new NodesThread(nodes, 0, nodes.length, layout.getGravity(), repulsion, layout.getScalingRatio(), repulsion);
    }

    private static int nodesWithForces(Node[] nodes) {
        int count = 0;
        for (Node n : nodes) {
            OrderedLayoutData data = n.getLayoutData();
            if (data.dx != 0 || data.dy != 0) {
                count++;
            }
        }
        return count;
    }

    private static GraphModel orderedGraph(int nodeCount) {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Column order = graphModel.getNodeTable().addColumn("order", Double.class);
        Graph graph = graphModel.getGraph();
        Random random = new Random(7);
        Node[] nodes = new Node[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            nodes[i] = graphModel.factory().newNode(String.valueOf(i));
            nodes[i].setX(random.nextFloat() * 1000f);
            nodes[i].setY(random.nextFloat() * 1000f);
            nodes[i].setAttribute(order, (double) i);
            graph.addNode(nodes[i]);
        }
        for (int i = 1; i < nodeCount; i++) {
            graph.addEdge(graphModel.factory().newEdge(nodes[i - 1], nodes[i], 0, 1.0, false));
        }
        return graphModel;
    }
}
