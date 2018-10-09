package nl.liacs.takesstatistics;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.statistics.spi.Statistics;
import org.openide.util.Lookup;

public class DiameterMetric implements Statistics {
    // Attribute names
    private final String ECCENTRICITY = "eccentricity";
    private final String IS_PERIPHERY = "isperiphery";
    private final String IS_CENTER = "iscenter";
    
    // Flags controlling computation of several factors
    private boolean eccentricitiesFlag = false;
    private boolean peripheryFlag = false;
    private boolean centerFlag = false;
    
    // Computed Values
    private int diameter;
    private int radius;
    private int peripherySize;
    private int centerSize;
    
    DiameterMetric() {
        
    }
    
    // Make sure the graph contain relevant attributes, as defined by the flags
    private void initAttributeCollumns(Table nodeTable) {
        if (this.eccentricitiesFlag && !nodeTable.hasColumn(ECCENTRICITY)) {
            nodeTable.addColumn(ECCENTRICITY, "Eccentricity", Double.class, new Double(-1));
        }
        if (this.peripheryFlag && !nodeTable.hasColumn(IS_PERIPHERY)) {
            nodeTable.addColumn(IS_PERIPHERY, "Part of periphery", Boolean.class, false);
        }
        if (this.centerFlag && !nodeTable.hasColumn(IS_CENTER)) {
            nodeTable.addColumn(IS_CENTER, "Part of center", Boolean.class, false);
        }
    }
    
    // Compute the Diameter and Radius from the given GraphModel
    // Post: members diameter and radius will be filled with the values computed
    @Override
    public void execute(GraphModel graphmodel) {
        initAttributeCollumns(graphmodel.getNodeTable());
        
        if (centerFlag) {
            boolean b = true;
            for (Node n : graphmodel.getUndirectedGraph().getNodes()) {
                n.setAttribute(IS_CENTER, b);
                b = !b;
            }
        }
        
        this.diameter = 42;
        this.radius = 21;
        if (this.peripheryFlag) {
            this.peripherySize = 9000;
        }
        if (this.centerFlag) {
            this.centerSize = 12;
        }
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
                + "Compute all eccentricities: " 
                    + (this.eccentricitiesFlag ? "Yes" : "No") + "<br>"
                + "Compute the periphery: " 
                    + (this.peripheryFlag ? "Yes" : "No") + "<br>"
                + "Compute the center: " 
                    + (this.centerFlag ? "Yes" : "No") + "<br>"
                + "<br>"
                + "<h2>Results</h2>"
                + "Diameter: " + this.diameter + "<br>"
                + "Radius: " + this.radius + "<br>"
                + (this.peripheryFlag 
                    ? ("Periphery: " + this.peripherySize + " nodes<br>") 
                    : "")
                + (this.centerFlag 
                    ? ("Center: " + this.centerSize + " nodes<br>") 
                    : "")
                + "<br>"
                + "<br>"
                + "<h2>Algorithm</h2>"
                + ""
                + "</body></html>";
    }
    
    // Getters and Setters for Flags
    
    public void setEccentricitiesFlag(boolean b) {
        eccentricitiesFlag = b;
    }
    public boolean getEccentricitiesFlag() {
        return eccentricitiesFlag;
    }
    
    public void setPeripheryFlag(boolean b) {
        peripheryFlag = b;
    }
    public boolean getPeripheryFlag() {
        return peripheryFlag;
    }
    
    public void setCenterFlag(boolean b) {
        centerFlag = b;
    }
    public boolean getCenterFlag() {
        return centerFlag;
    }
    
    // Getters for computed metrics
    public int getDiameter() {
        return this.diameter;
    }
    public int getRadius() {
        return this.radius;
    }
    
}
