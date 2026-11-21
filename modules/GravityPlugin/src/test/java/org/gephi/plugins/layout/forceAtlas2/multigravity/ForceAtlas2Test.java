package org.gephi.plugins.layout.forceAtlas2.multigravity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ForceAtlas2Test {

    // The layout reports what goes wrong in a worker through Exceptions.printStackTrace, so the tests watch the log
    private final List<Throwable> logged = Collections.synchronizedList(new ArrayList<>());
    private Handler handler;

    @Before
    public void listenToLoggedThrowables() {
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
    public void stopListening() {
        Logger.getLogger("").removeHandler(handler);
        // Never leave an interrupt behind for the next test
        Thread.interrupted();
    }

    @Test
    public void barnesHutSwitchedOnDuringAPassDoesNotFailTheWorkers() {
        GraphModel model = randomGraph(40);
        ForceAtlas2 layout = new ForceAtlas2(new ForceAtlas2Builder()) {
            private final AtomicInteger reads = new AtomicInteger();

            @Override
            public Boolean isBarnesHutOptimize() {
                // The user ticks Barnes-Hut while the layout runs: off the first time the layout asks, on every time
                // after that
                return reads.getAndIncrement() > 0;
            }
        };
        layout.setGraphModel(model);
        layout.setThreadsCount(2);

        layout.initAlgo();
        try {
            for (int i = 0; i < 3; i++) {
                layout.goAlgo();
            }
        } finally {
            layout.endAlgo();
        }

        assertTrue("A pass must not fail because Barnes-Hut was switched on, got " + logged, logged.isEmpty());
        assertFinitePositions(model);
    }

    @Test
    public void aFailingPassDoesNotLeaveTheGraphLocked() {
        GraphModel model = randomGraph(10);
        ForceAtlas2 layout = new ForceAtlas2(new ForceAtlas2Builder()) {
            @Override
            public Double getScalingRatio() {
                throw new IllegalStateException("failure in the middle of a pass");
            }
        };
        layout.setGraphModel(model);

        layout.initAlgo();
        try {
            try {
                layout.goAlgo();
                fail("The pass was expected to fail");
            } catch (IllegalStateException expected) {
            }

            // Core never calls endAlgo() when a pass throws, so nothing else releases the lock
            assertEquals("The failed pass must release the graph read lock", 0,
                model.getGraphVisible().getLock().getReadHoldCount());
        } finally {
            layout.endAlgo();
        }
    }

    private static GraphModel randomGraph(int nodeCount) {
        GraphModel model = GraphModel.Factory.newInstance();
        Graph graph = model.getGraph();
        Random random = new Random(7);
        Node[] nodes = new Node[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            nodes[i] = model.factory().newNode(String.valueOf(i));
            nodes[i].setX(random.nextFloat() * 1000f);
            nodes[i].setY(random.nextFloat() * 1000f);
            graph.addNode(nodes[i]);
        }
        for (int i = 1; i < nodeCount; i++) {
            graph.addEdge(model.factory().newEdge(nodes[i], nodes[random.nextInt(i)], 0, 1.0, false));
        }
        return model;
    }

    private static void assertFinitePositions(GraphModel model) {
        for (Node n : model.getGraph().getNodes().toArray()) {
            assertTrue(n.getId() + " x=" + n.x() + " y=" + n.y(), Float.isFinite(n.x()) && Float.isFinite(n.y()));
        }
    }
}
