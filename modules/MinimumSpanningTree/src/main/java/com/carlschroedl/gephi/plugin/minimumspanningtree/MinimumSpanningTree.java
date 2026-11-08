package com.carlschroedl.gephi.plugin.minimumspanningtree;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * See http://wiki.gephi.org/index.php/HowTo_write_a_metric#Create_Statistics
 *
 * @author Carl Schroedl <carlschroedl@gmail.com>
 */
public class MinimumSpanningTree implements Statistics, LongTask {

    private boolean cancel = false;
    private ProgressTicket progressTicket;
    // Set by the thread running the statistics, and read when cancelling from another
    private volatile MinimumSpanningTreeAlgorithm stAlgorithm;
    
    private boolean directed;
    
    public MinimumSpanningTree(){
    }
    
    @Override
    public void execute(GraphModel graphModel){
        this.stAlgorithm = new KruskalsAlgorithm();
        stAlgorithm.execute(graphModel);
        
    }
    /** Only useful if the algorithm takes graph type into account. */

    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    /** ----------------------------------------------------------- */

    @Override
    public String getReport() { //delegate
        //Write the report HTML string here
        return stAlgorithm.getReport();
    }

    @Override   //delegate
    public boolean cancel() {
        // Can be called right as the run starts, before the algorithm exists
        MinimumSpanningTreeAlgorithm algorithm = stAlgorithm;
        return algorithm != null && algorithm.cancel();
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
