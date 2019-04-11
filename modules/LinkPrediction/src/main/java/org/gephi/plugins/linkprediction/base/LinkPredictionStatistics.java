package org.gephi.plugins.linkprediction.base;

import org.gephi.plugins.linkprediction.util.Complexity;
import org.gephi.statistics.spi.Statistics;

/**
 * Statistic that predicts the next edge based on different link-prediction
 * algorithm implementations.
 * <p>
 * Per iteration a new edge is added to the existing network. After every
 * iteration, a new situation is assumed and the next edge is calculated
 * on the basis of this new, expanded network.
 * <p>
 * The number of iterations can be limited using the UI.
 *
 * @author Saskia Schueler
 * @see LinkPredictionStatisticsBuilder
 */
public abstract class LinkPredictionStatistics implements Statistics {
    /** Default number of iteration used to predict next edges **/
    public static final int ITERATION_LIMIT_DEFAULT = 1;

    // Number of edge prediction iterations
    protected int iterationLimit = ITERATION_LIMIT_DEFAULT;
    // Big o complexity of algorithm
    protected Complexity complexity;

    /**
     * Generates a report after link prediction calculation has finished.
     *
     * @return HTML report
     */
    public String getReport() {
        //This is the HTML report shown when execution ends.
        //One could add a distribution histogram for instance
        return "<HTML> <BODY> <h1>Count Common Neighbours</h1> " + "<hr>"
                + "<br> No global results to show. Check Data Laboratory for results" + "<br />" + "</BODY></HTML>";
    }

    /**
     * Gives an estimate of the assumed duration.
     *
     * @return If the calculation will takes a long time
     */
    public boolean longRuntimeExpected(){
        // TODO Implement
        return false;
    }

    public int getIterationLimit() {
        return iterationLimit;
    }

    public void setIterationLimit(int iterationLimit) {
        this.iterationLimit = iterationLimit;
    }

    public Complexity getComplexity() {
        return complexity;
    }

    public void setComplexity(Complexity complexity) {
        this.complexity = complexity;
    }
}
