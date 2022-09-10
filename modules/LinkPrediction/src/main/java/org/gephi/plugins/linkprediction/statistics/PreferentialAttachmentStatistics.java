package org.gephi.plugins.linkprediction.statistics;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.Node;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.plugins.linkprediction.util.Complexity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME;

/**
 * Class to calculate link predictions based on preferential attachment algorithm.
 *
 *  @author Saskia Schueler
 *  @see LinkPredictionStatistics
 */
public class PreferentialAttachmentStatistics extends LinkPredictionStatistics {
    // Console logger
    private static Logger consoleLogger = Logger.getLogger(PreferentialAttachmentStatistics.class.getName());

    static {
        complexity = Complexity.QUADRATIC;
    }


    /**
     * Gets the name of the respective algorithm.
     *
     * @return Algorithm name
     */
    @Override public String getAlgorithmName() {
        return PREFERENTIAL_ATTACHMENT_NAME;
    }

    /**
     * Iterates over all nodes twice to initially calculate prediction values.
     *
     * @param factory Factory to create new edges
     */
    protected void calculateAll(GraphFactory factory) {
        // Iterate on all nodes for first execution
        consoleLogger.log(Level.FINE,"Initial calculation");
        ArrayList<Node> nodesA = new ArrayList<Node>(Arrays.asList(graph.getNodes().toArray()));
        ArrayList<Node> nodesB = new ArrayList<Node>(Arrays.asList(graph.getNodes().toArray()));

        for (Node a : nodesA) {
            consoleLogger.log(Level.FINE, () -> "Calculation for node " + a.getId());

            // Remove self from neighbours
            nodesB.remove(a);

            // Get neighbours of a
            ArrayList<Node> aNeighbours = getNeighbours(a);

            // Calculate preferential attachment
            for (Node b : nodesB) {
                // Get neighbours of b
                consoleLogger.log(Level.FINE, () -> "Calculation for node " + b.getId());
                ArrayList<Node> bNeighbours = getNeighbours(b);

                // Calculate prediction value
                int totalNeighboursCount = aNeighbours.size() * bNeighbours.size();
                consoleLogger.log(Level.FINE, () -> "Total neighbours product: " + totalNeighboursCount);

                // Temporary save calculated
                // value if edge does not exist
                if (isNewEdge(a, b, PREFERENTIAL_ATTACHMENT_NAME)) {
                    saveCalculatedValue(factory, a, b, totalNeighboursCount);
                }
            }
        }
    }

    /**
     * Recalculates the link prediction probability for neighbours of affected nodes.
     *
     * @param factory Factory to create new edges
     * @param a Center node
     */
    @Override
    protected void recalculateProbability(GraphFactory factory, Node a) {
        consoleLogger.log(Level.FINE,"Recalculate probability for affected nodes");
        // Get neighbours of a
        List<Node> aNeighbours = getNeighbours(a);

        // Get edges and remove
        // self from potential neighbours
        List<Node> nodesB = new ArrayList<>(Arrays.asList(graph.getNodes().toArray()));
        nodesB.remove(a);

        // Iterate over other nodes
        // that could become new neighbours
        for (Node b : nodesB) {

            // Update temporary saved values
            // if edge does not exist
            if (isNewEdge(a, b, PREFERENTIAL_ATTACHMENT_NAME)) {
                consoleLogger.log(Level.FINE, () -> "Calculation for edge new between " + a.getId() + " and " + b.getId());
                List<Node> bNeighbours = getNeighbours(b);
                int totalNeighboursCount = aNeighbours.size() * bNeighbours.size();

                // Update saved and calculated values
                consoleLogger.log(Level.FINE, () -> "Update value to " + totalNeighboursCount);
                updateCalculatedValue(factory, a, b, totalNeighboursCount);
            }
        }
    }
}
