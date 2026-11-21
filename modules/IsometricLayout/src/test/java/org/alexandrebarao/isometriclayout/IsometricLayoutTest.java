package org.alexandrebarao.isometriclayout;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.junit.Test;

public class IsometricLayoutTest {

    @Test
    public void runsAfterAColumnWasRemoved() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Table nodeTable = graphModel.getNodeTable();
        // Removing a column leaves its slot empty, columns are no longer numbered 0..countColumns()-1
        nodeTable.addColumn("removed", Integer.class);
        nodeTable.addColumn("kept", Integer.class);
        nodeTable.removeColumn("removed");
        addNodes(graphModel, 3);

        run(graphModel, 0);

        Column result = nodeTable.getColumn(IsometricLayout.ISOMETRIC_Z_VALUE);
        assertNotNull(result);
        for (Node node : graphModel.getGraph().getNodes()) {
            assertEquals(0, node.getAttribute(result));
        }
    }

    @Test
    public void usesAZColumnStoredAfterARemovedColumn() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Table nodeTable = graphModel.getNodeTable();
        nodeTable.addColumn("removed", Integer.class);
        Column z = nodeTable.addColumn("height", "Height [z]", Integer.class, 0);
        nodeTable.removeColumn("removed");
        Node[] nodes = addNodes(graphModel, 3);
        for (int i = 0; i < nodes.length; i++) {
            nodes[i].setAttribute(z, i);
        }

        run(graphModel, 2);

        Column result = nodeTable.getColumn(IsometricLayout.ISOMETRIC_Z_VALUE);
        for (int i = 0; i < nodes.length; i++) {
            assertEquals(i, nodes[i].getAttribute(result));
        }
    }

    @Test
    public void findingTheZColumnDoesNotLeaveTheTableLocked() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Table nodeTable = graphModel.getNodeTable();
        nodeTable.addColumn("height", "Height [z]", Integer.class, 0);
        nodeTable.addColumn("other", Integer.class);
        addNodes(graphModel, 2);

        run(graphModel, 1);

        assertEquals(0, nodeTable.getLock().getHoldCount());
        assertEquals(0, graphModel.getGraph().getLock().getReadHoldCount());
    }

    @Test
    public void emptyCellsOfATextZColumnCountAsZero() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Table nodeTable = graphModel.getNodeTable();
        Column z = nodeTable.addColumn("height", "Height [z]", String.class, null);
        Node[] nodes = addNodes(graphModel, 3);
        nodes[0].setAttribute(z, "2");
        nodes[1].setAttribute(z, "4");

        run(graphModel, 2);

        Column result = nodeTable.getColumn(IsometricLayout.ISOMETRIC_Z_VALUE);
        assertEquals(1, nodes[0].getAttribute(result));
        assertEquals(2, nodes[1].getAttribute(result));
        assertEquals(0, nodes[2].getAttribute(result));
    }

    @Test
    public void releasesTheGraphLockWhenTheLayoutFails() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        graphModel.getNodeTable().addColumn("height", "Height [z]", Integer.class, 0);
        addNodes(graphModel, 2);
        IsometricLayout failing = new IsometricLayout(new IsometricLayoutBuilder()) {
            @Override
            public double getDataLaboratoryValue(Node n, Column col) {
                throw new IllegalStateException("failure while the graph is locked");
            }
        };

        try {
            run(failing, graphModel, 1);
            fail("The layout should have failed");
        } catch (IllegalStateException expected) {
            // The graph must be unlocked all the same
        }

        assertEquals(0, graphModel.getGraph().getLock().getReadHoldCount());
    }

    private static Node[] addNodes(GraphModel graphModel, int count) {
        Graph graph = graphModel.getGraph();
        Node[] nodes = new Node[count];
        for (int i = 0; i < count; i++) {
            nodes[i] = graphModel.factory().newNode("n" + i);
            graph.addNode(nodes[i]);
        }
        return nodes;
    }

    private static void run(GraphModel graphModel, int zLevels) {
        run(new IsometricLayout(new IsometricLayoutBuilder()), graphModel, zLevels);
    }

    private static void run(IsometricLayout layout, GraphModel graphModel, int zLevels) {
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        layout.setZlevels(zLevels);
        layout.initAlgo();
        while (layout.canAlgo()) {
            layout.goAlgo();
        }
        layout.endAlgo();
    }
}
