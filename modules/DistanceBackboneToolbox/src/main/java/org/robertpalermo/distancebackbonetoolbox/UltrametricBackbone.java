package org.robertpalermo.distancebackbonetoolbox;

import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;

public class UltrametricBackbone implements Statistics {

    @Override
    public void execute(GraphModel graphModel) {
        System.out.println("Running Ultrametric Backbone...");
        
        DistanceBackboneToolboxProcessor processor = new DistanceBackboneToolboxProcessor(graphModel);
        processor.computeUltrametricBackbone();
    }

    @Override
    public String getReport() {
        return "<html><h1>Ultrametric Backbone</h1><p>The ultrametric backbone has been successfully computed.</p></html>";
    }
}