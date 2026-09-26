package org.robertpalermo.distancebackbonetoolbox;

import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;

public class MetricBackbone implements Statistics {

    @Override
    public void execute(GraphModel graphModel) {
        System.out.println("Running Metric Backbone...");
        
        DistanceBackboneToolboxProcessor processor = new DistanceBackboneToolboxProcessor(graphModel);
        processor.computeMetricBackbone();
    }

    @Override
    public String getReport() {
        return "<html><h1>Metric Backbone</h1><p>The metric backbone has been successfully computed.</p></html>";
    }
}
