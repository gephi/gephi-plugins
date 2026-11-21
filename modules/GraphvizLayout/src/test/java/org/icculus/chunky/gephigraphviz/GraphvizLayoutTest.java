package org.icculus.chunky.gephigraphviz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.junit.Assume;
import org.junit.Test;

public class GraphvizLayoutTest {

    // Ids that need quoting in the dot language, or that dot writes back differently.
    private static final String[] NODE_IDS = {"42", "a b", "say \"hi\"", "\u00e9t\u00e9"};

    // Output of Graphviz 10 (dot -Tdot) for the input written by toDot() for n0 -> n1 -> n2 -> n2.
    private static final String GRAPHVIZ_OUTPUT = "digraph g {\n"
            + "\tgraph [bb=\"0,0,234,54\",\n"
            + "\t\tlayout=dot,\n"
            + "\t\toverlap=false,\n"
            + "\t\trankdir=LR\n"
            + "\t];\n"
            + "\tnode [label=\"\\N\"];\n"
            + "\tn0\t[height=0.5,\n"
            + "\t\tpos=\"27,18\",\n"
            + "\t\twidth=0.75];\n"
            + "\tn1\t[height=0.5,\n"
            + "\t\tpos=\"117,18\",\n"
            + "\t\twidth=0.75];\n"
            + "\tn0 -> n1\t[pos=\"e,89.616,18 54.403,18 61.886,18 70.183,18 78.199,18\",\n"
            + "\t\tweight=1.0];\n"
            + "\tn2\t[height=0.5,\n"
            + "\t\tpos=\"207,18\",\n"
            + "\t\twidth=0.75];\n"
            + "\tn1 -> n2\t[pos=\"e,179.62,18 144.4,18 151.89,18 160.18,18 168.2,18\",\n"
            + "\t\tweight=1.0];\n"
            + "\tn2 -> n2\t[pos=\"e,222.23,33.167 191.77,33.167 187.16,43.664 192.23,54 207,54 216.23,54 221.67,49.963 223.33,44.372\",\n"
            + "\t\tweight=1.0];\n"
            + "}\n";

    @Test
    public void toDotNamesNodesByIndex() {
        GraphModel graphModel = chain(true, NODE_IDS);
        GraphvizLayout layout = newLayout(graphModel);

        String dot = layout.toDot(graphModel.getGraphVisible().getNodes().toArray());

        assertTrue(dot, dot.startsWith("digraph g {\n"));
        assertTrue(dot, dot.contains("\nn0 [pos=\"0.0,0.0\"];\n"));
        assertTrue(dot, dot.contains("\nn3 [pos=\"0.0,0.0\"];\n"));
        assertTrue(dot, dot.contains("\nn0 -> n1 [weight=1.0];\n"));
        assertTrue(dot, dot.contains("\nn2 -> n3 [weight=1.0];\n"));
        for (String id : NODE_IDS) {
            assertFalse(dot, dot.contains(id));
        }
    }

    @Test
    public void toDotWritesUndirectedEdges() {
        GraphModel graphModel = chain(false, "a", "b");
        GraphvizLayout layout = newLayout(graphModel);

        String dot = layout.toDot(graphModel.getGraphVisible().getNodes().toArray());

        assertTrue(dot, dot.startsWith("graph g {\n"));
        assertTrue(dot, dot.contains("\nn0 -- n1 [weight=1.0];\n"));
    }

    @Test
    public void applyPositionsReadsGraphvizOutput() {
        Node[] nodes = chain(true, "a", "b", "c").getGraphVisible().getNodes().toArray();

        assertEquals(3, GraphvizLayout.applyPositions(GRAPHVIZ_OUTPUT, nodes));

        assertPosition(nodes[0], 27f, 18f);
        assertPosition(nodes[1], 117f, 18f);
        assertPosition(nodes[2], 207f, 18f);
    }

    @Test
    public void applyPositionsSkipsUnknownNodes() {
        Node[] nodes = chain(true, "a", "b").getGraphVisible().getNodes().toArray();

        assertEquals(2, GraphvizLayout.applyPositions(GRAPHVIZ_OUTPUT, nodes));

        assertPosition(nodes[0], 27f, 18f);
        assertPosition(nodes[1], 117f, 18f);
    }

    @Test
    public void layoutWithGraphviz() {
        Assume.assumeTrue("dot is not installed", isDotInstalled());
        GraphModel graphModel = chain(true, NODE_IDS);
        GraphvizLayout layout = newLayout(graphModel);

        layout.initAlgo();
        layout.goAlgo();
        layout.endAlgo();

        Set<String> positions = new HashSet<>();
        for (Node n : graphModel.getGraphVisible().getNodes()) {
            positions.add(n.x() + "," + n.y());
        }
        assertEquals(positions.toString(), NODE_IDS.length, positions.size());
        assertTrue(layout.isConverged());
    }

    private static GraphModel chain(boolean directed, String... ids) {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Graph graph = directed ? graphModel.getDirectedGraph() : graphModel.getUndirectedGraph();
        Node previous = null;
        for (String id : ids) {
            Node n = graphModel.factory().newNode(id);
            graph.addNode(n);
            if (previous != null) {
                graph.addEdge(graphModel.factory().newEdge(previous, n, directed));
            }
            previous = n;
        }
        return graphModel;
    }

    private static GraphvizLayout newLayout(GraphModel graphModel) {
        GraphvizLayout layout = new GraphvizLayout(null);
        layout.setGraphModel(graphModel);
        layout.initAlgo();
        return layout;
    }

    private static void assertPosition(Node n, float x, float y) {
        assertEquals(x, n.x(), 0f);
        assertEquals(y, n.y(), 0f);
    }

    private static boolean isDotInstalled() {
        try {
            return new ProcessBuilder("dot", "-V").start().waitFor() == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
