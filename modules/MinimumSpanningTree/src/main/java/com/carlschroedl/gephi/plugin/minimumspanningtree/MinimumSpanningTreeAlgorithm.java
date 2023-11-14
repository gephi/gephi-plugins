package com.carlschroedl.gephi.plugin.minimumspanningtree;

import javax.swing.JPanel;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;

/**
 *
 * @author Carl Schroedl <carlschroedl@gmail.com>
 */

/*
 * Implementations must provide the following @ServiceProvider Annotation:
 *
 * @ServiceProvider(service = MinimumSpanningTreeAlgorithm.class) 
 * 
 * Failure to add this annotation will prevent the implementation from being
 * detected and used at runtime. In essence, the UI will not be aware of the 
 * algorithm.
 */

public abstract class MinimumSpanningTreeAlgorithm implements LongTask, Statistics{
              
    public abstract void execute(GraphModel graphModel);
    public abstract JPanel getOptions();

    
    //These 2 methods return the name shown to the user in the interface:
    public abstract String getName();
    
    @Override
    public final String toString(){
        return this.getName();
    }
    
    
}
