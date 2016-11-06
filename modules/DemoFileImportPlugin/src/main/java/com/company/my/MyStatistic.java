package com.company.my;

import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;

public class MyStatistic implements Statistics {

    public void execute(GraphModel graphModel) {
        graphModel.getGraph();
    }

    public String getReport() {
        return "";
    }
    
}
