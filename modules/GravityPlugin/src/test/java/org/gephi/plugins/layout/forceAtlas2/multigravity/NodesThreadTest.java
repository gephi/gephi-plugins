package org.gephi.plugins.layout.forceAtlas2.multigravity;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.plugins.layout.forceAtlas2.multigravity.ForceFactory.RepulsionForce;
import org.junit.After;
import org.junit.Test;

public class NodesThreadTest {

    @After
    public void clearInterrupt() {
        // Never leave an interrupt behind for the next test
        Thread.interrupted();
    }

    @Test
    public void aWorkerStopsWhenItsThreadIsInterrupted() {
        for (boolean barnesHut : new boolean[] {false, true}) {
            Node[] nodes = nodesWithLayoutData(50);
            RepulsionForce repulsion = ForceFactory.builder.buildRepulsion(false, 10.0);
            Region rootRegion = null;
            if (barnesHut) {
                rootRegion = new Region(nodes);
                rootRegion.buildSubRegions();
            }
            NodesThread worker = new NodesThread(nodes, 0, nodes.length, barnesHut, 1.2, 1.0, repulsion, 10.0,
                rootRegion, repulsion);

            // A pass that is cancelled interrupts its workers, which must not go on with their nodes
            Thread.currentThread().interrupt();
            worker.run();
            Thread.interrupted();

            for (Node n : nodes) {
                ForceAtlas2LayoutData layoutData = n.getLayoutData();
                assertEquals("Barnes-Hut " + barnesHut + ": no force may be applied", 0.0,
                    Math.abs(layoutData.dx) + Math.abs(layoutData.dy), 0.0);
            }
        }
    }

    private static Node[] nodesWithLayoutData(int nodeCount) {
        GraphModel model = GraphModel.Factory.newInstance();
        Random random = new Random(7);
        for (int i = 0; i < nodeCount; i++) {
            Node n = model.factory().newNode(String.valueOf(i));
            n.setX(random.nextFloat() * 1000f);
            n.setY(random.nextFloat() * 1000f);
            n.setLayoutData(new ForceAtlas2LayoutData());
            model.getGraph().addNode(n);
        }
        return model.getGraph().getNodes().toArray();
    }
}
