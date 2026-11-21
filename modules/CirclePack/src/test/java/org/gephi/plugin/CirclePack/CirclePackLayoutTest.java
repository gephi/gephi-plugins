package org.gephi.plugin.CirclePack;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.TreeMap;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.spi.LayoutProperty;
import org.junit.Test;

public class CirclePackLayoutTest {

    private static final int NODE_COUNT = 6;

    private enum Edges {
        NONE, DIRECTED, UNDIRECTED, MIXED
    }

    @Test
    public void levelNamingAColumnTheGraphLacksIsIgnored() {
        GraphModel graphModel = newGraphModel(false, Edges.NONE);
        CirclePackLayout layout = newLayout(graphModel);
        layout.setHierarchy1("missing_column");

        run(layout);

        assertArrayEquals(positions(ungrouped(Edges.NONE)), positions(graphModel), 0f);
        assertFinitePositions(graphModel);
    }

    @Test
    public void levelNamingAColumnDeletedAfterItWasChosenIsIgnoredOnTheNextRun() {
        GraphModel graphModel = newGraphModel(true, Edges.NONE);
        CirclePackLayout layout = newLayout(graphModel);
        layout.setHierarchy1("region");
        run(layout);
        assertFalse("grouping by the column applies while it exists",
            Arrays.equals(positions(ungrouped(Edges.NONE)), positions(graphModel)));

        graphModel.getNodeTable().removeColumn("region");
        run(layout);

        assertArrayEquals(positions(ungrouped(Edges.NONE)), positions(graphModel), 0f);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void levelAppliedLikeAPresetIsIgnoredWhenTheColumnIsMissing() throws Exception {
        GraphModel graphModel = newGraphModel(false, Edges.NONE);
        CirclePackLayout layout = newLayout(graphModel);
        for (LayoutProperty property : layout.getProperties()) {
            if (property.getCanonicalName().equals("Hierarchy2")) {
                property.getProperty().setValue("missing_column");
            }
        }

        run(layout);

        assertEquals("missing_column", layout.getHierarchy2());
        assertArrayEquals(positions(ungrouped(Edges.NONE)), positions(graphModel), 0f);
    }

    @Test
    public void missingLevelDoesNotDiscardTheOtherLevels() {
        GraphModel onlyFirst = newGraphModel(true, Edges.NONE);
        CirclePackLayout first = newLayout(onlyFirst);
        first.setHierarchy1("region");
        run(first);

        GraphModel withMissing = newGraphModel(true, Edges.NONE);
        CirclePackLayout both = newLayout(withMissing);
        both.setHierarchy1("missing_column");
        both.setHierarchy2("region");
        both.setHierarchy3("other_missing_column");
        run(both);

        assertArrayEquals(positions(onlyFirst), positions(withMissing), 0f);
        assertFalse("grouping by the existing column still applies",
            Arrays.equals(positions(ungrouped(Edges.NONE)), positions(withMissing)));
    }

    @Test
    public void columnNamesAreMatchedLikeNodeGetAttribute() {
        GraphModel lower = newGraphModel(true, Edges.NONE);
        CirclePackLayout a = newLayout(lower);
        a.setHierarchy1("region");
        run(a);

        GraphModel upper = newGraphModel(true, Edges.NONE);
        CirclePackLayout b = newLayout(upper);
        b.setHierarchy1("REGION");
        run(b);

        assertArrayEquals(positions(lower), positions(upper), 0f);
        assertFalse(Arrays.equals(positions(ungrouped(Edges.NONE)), positions(upper)));
    }

    @Test
    public void inAndOutDegreeLevelsAreIgnoredOnAnUndirectedGraph() {
        GraphModel graphModel = newGraphModel(false, Edges.UNDIRECTED);
        CirclePackLayout layout = newLayout(graphModel);
        layout.setHierarchy1("InDegree");
        layout.setHierarchy2("OutDegree");

        run(layout);

        assertArrayEquals(positions(ungrouped(Edges.UNDIRECTED)), positions(graphModel), 0f);
    }

    @Test
    public void inAndOutDegreeLevelsStillGroupDirectedAndMixedGraphs() {
        for (Edges edges : new Edges[] {Edges.DIRECTED, Edges.MIXED}) {
            for (String level : new String[] {"InDegree", "OutDegree"}) {
                GraphModel graphModel = newGraphModel(false, edges);
                CirclePackLayout layout = newLayout(graphModel);
                layout.setHierarchy1(level);

                run(layout);

                assertFalse(edges + " graph grouped by " + level,
                    Arrays.equals(positions(ungrouped(edges)), positions(graphModel)));
                assertFinitePositions(graphModel);
            }
        }
    }

    @Test
    public void degreeAndNodeIdLevelsStillGroupTheNodes() {
        GraphModel graphModel = newGraphModel(false, Edges.UNDIRECTED);
        CirclePackLayout layout = newLayout(graphModel);
        layout.setHierarchy1("Degree");
        layout.setHierarchy2("NodeID");

        run(layout);

        assertFalse(Arrays.equals(positions(ungrouped(Edges.UNDIRECTED)), positions(graphModel)));
        assertFinitePositions(graphModel);
    }

    @Test
    public void resetKeepsTheHierarchyLevelsThatWereSet() {
        // The toolkit's AutoLayout calls resetPropertiesValues() after the caller's setters, when it starts the layout
        GraphModel graphModel = newGraphModel(true, Edges.NONE);
        CirclePackLayout layout = newLayout(graphModel);
        layout.setHierarchy1("region");

        layout.resetPropertiesValues();

        assertEquals("region", layout.getHierarchy1());
        run(layout);
        assertFalse(Arrays.equals(positions(ungrouped(Edges.NONE)), positions(graphModel)));
    }

    @Test
    public void editorShowsNoSelectionForAValueThatIsNotAnOption() {
        AbstractComboBoxEditor editor = new AbstractComboBoxEditor() {
            {
                comboValues = new TreeMap<>();
                comboValues.put("No Selection", "No Selection");
                comboValues.put("region", "Region (Attribute)");
            }
        };

        editor.setValue("region");
        assertEquals("Region (Attribute)", editor.getAsText());
        editor.setValue("missing_column");
        assertEquals("No Selection", editor.getAsText());
        editor.setValue(null);
        assertEquals("No Selection", editor.getAsText());
    }

    private static CirclePackLayout newLayout(GraphModel graphModel) {
        CirclePackLayout layout = new CirclePackLayout(new CirclePackLayoutBuilder());
        layout.setGraphModel(graphModel);
        layout.resetPropertiesValues();
        return layout;
    }

    private static GraphModel ungrouped(Edges edges) {
        GraphModel graphModel = newGraphModel(false, edges);
        run(newLayout(graphModel));
        return graphModel;
    }

    private static GraphModel newGraphModel(boolean regionColumn, Edges edges) {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        if (regionColumn) {
            Column region = graphModel.getNodeTable().addColumn("Region", String.class);
            assertEquals("region", region.getId());
        }
        Graph graph = graphModel.getGraph();
        Node[] nodes = new Node[NODE_COUNT];
        for (int i = 0; i < NODE_COUNT; i++) {
            nodes[i] = graphModel.factory().newNode("n" + i);
            nodes[i].setSize(10f + i);
            if (regionColumn) {
                nodes[i].setAttribute("Region", i % 3 == 0 ? "north" : "south");
            }
            graph.addNode(nodes[i]);
        }
        if (edges != Edges.NONE) {
            // A path with a shortcut, so that nodes have different degrees
            int[][] ends = {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {0, 2}};
            for (int i = 0; i < ends.length; i++) {
                boolean directed = edges == Edges.DIRECTED || (edges == Edges.MIXED && i % 2 == 0);
                graph.addEdge(graphModel.factory().newEdge(nodes[ends[i][0]], nodes[ends[i][1]], 0, 1.0, directed));
            }
            assertEquals(edges == Edges.DIRECTED, graphModel.isDirected());
            assertEquals(edges == Edges.UNDIRECTED, graphModel.isUndirected());
            assertEquals(edges == Edges.MIXED, graphModel.isMixed());
        }
        return graphModel;
    }

    private static void run(CirclePackLayout layout) {
        layout.initAlgo();
        for (int i = 0; i < 10 && layout.canAlgo(); i++) {
            layout.goAlgo();
        }
        layout.endAlgo();
    }

    private static float[] positions(GraphModel graphModel) {
        float[] positions = new float[2 * NODE_COUNT];
        for (int i = 0; i < NODE_COUNT; i++) {
            Node node = graphModel.getGraph().getNode("n" + i);
            positions[2 * i] = node.x();
            positions[2 * i + 1] = node.y();
        }
        return positions;
    }

    private static void assertFinitePositions(GraphModel graphModel) {
        for (Node node : graphModel.getGraph().getNodes()) {
            assertTrue(node.getId() + " x=" + node.x(), Float.isFinite(node.x()));
            assertTrue(node.getId() + " y=" + node.y(), Float.isFinite(node.y()));
        }
    }
}
