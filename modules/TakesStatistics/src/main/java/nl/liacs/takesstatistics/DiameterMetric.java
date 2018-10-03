package nl.liacs.takesstatistics;

import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;
import org.openide.util.Lookup;

public class DiameterMetric implements Statistics {
    // Whether the network should be interpreted as directed
    private boolean isDirected;
    
    // Computed Values
    private int diameter;
    private int radius;
    
    DiameterMetric() {
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        this.isDirected = gc.getGraphModel().isDirected();
    }
    
    // Compute the Diameter and Radius from the given GraphModel
    // Post: members diameter and radius will be filled with the values computed
    @Override
    public void execute(GraphModel graphmodel) {
        this.diameter = 42;
        this.radius = 21;
    }

    // Fetch the result
    @Override
    public String getReport() {
        //Todo include some kind of eccentricity distribution graph in the report?
        return 
                "<html><body>"
                + "<h1>Diameter and Radius Report</hi>"
                + "<br>"
                + "<hr>"
                + "<h2>Parameters</h2>"
                + "Network Interpretation: " 
                    + (this.isDirected ? "Undirected" : "Directed") + "<br>"
                + "<br>"
                + "<h2>Results</h2>"
                + "Diameter: " + this.diameter + "<br>"
                + "Radius: " + this.radius + "<br>"
                + "<br>"
                + "<br>"
                + "<h2>Algorithm</h2>"
                + ""
                + "</body></html>";
    }
    
    // Whether the network should be interpreted as a directed graph
    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }
    
    // Return whether the network will be interpreted as a directed graph
    public boolean isDirected() {
        return this.isDirected;
    }
    public int getDiameter() {
        return this.diameter;
    }
    public int getRadius() {
        return this.radius;
    }
    
}
