package net.clementlevallois.ForceAtlas3D;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.concurrent.RejectedExecutionException;
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
        ForceAtlas2 layout = new ForceAtlas2(new ForceAtlas2Builder());
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
        ForceAtlas2 layout = newLayout(graphModel, edgeWeightInfluence);
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
