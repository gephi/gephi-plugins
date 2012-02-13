/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uic.cs.compbio.DyNSPK;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.*;
import org.gephi.statistics.spi.DynamicStatistics;
import org.gephi.utils.TempDirUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author zitterbewegung
 */
public class DynamicGraphDensity implements DynamicStatistics {
    public String report;
    private boolean directed = false;
    private ArrayList<Interval<Double>> densities = new ArrayList<Interval<Double>>();
    private int nodecount;
    private ArrayList<Integer> graphsize = new ArrayList<Integer>();
    private GraphModel graphModel;
    private DynamicModel dynamicModel;
    private double window;
    private double tick;
    private Interval bounds;
    private boolean isDirected;
    public DynamicGraphDensity() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
        }
    }

    @Override
    public void execute(GraphModel gm, AttributeModel am) {
            this.graphModel = gm;
            this.isDirected = gm.isDirected();
            this.dynamicModel = Lookup.getDefault().lookup(DynamicController.class).getModel(graphModel.getWorkspace());
    }
    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }
    @Override
    public void loop(GraphView win, Interval intrvl) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraph(win);
        HierarchicalDirectedGraph directedGraph = null;
        if (isDirected) {
            directedGraph = graphModel.getHierarchicalDirectedGraph(win);
        }
        double edgesCount = graph.getTotalEdgeCount();
        double nodesCount = graph.getNodeCount();
        double multiplier = 1;

        if (!directed) {
            multiplier = 2;
        }
       double density = (multiplier * edgesCount) / (nodesCount * nodesCount - nodesCount);
       double low = intrvl.getLow();
       double high = intrvl.getHigh();
      if(Double.isNaN(density)){ 
          densities.add(new Interval(low,high,0.0));
                  }
      else{
         densities.add(new Interval(low,high,density));
      }
    }

    @Override
    public void end() {
       
    }

     public void setBounds(Interval bounds) {
        this.bounds = bounds;
    }

    public void setWindow(double window) {
        this.window = window;
    }

    public void setTick(double tick) {
        this.tick = tick;
    }

    public double getWindow() {
        return window;
    }

    public double getTick() {
        return tick;
    }

    public Interval getBounds() {
        return bounds;
    }

    @Override
    public String getReport() {
         String tableContent = "";
        DefaultCategoryDataset inDataset = new DefaultCategoryDataset();

        for (int i = 0; i < densities.size(); ++i) {
            String interval = "[";
            interval += DynamicUtilities.getXMLDateStringFromDouble(densities.get(i).getLow()).replace('T', ' ').
                    substring(0, 19) + ", ";
            interval += DynamicUtilities.getXMLDateStringFromDouble(densities.get(i).getHigh()).replace('T', ' ').
                    substring(0, 19);
            interval += "]";
            //Creation of the table
            tableContent += "<tr>";
            tableContent += "<td>";
            tableContent += interval;
            tableContent += "</td>";
            tableContent += "<td>";
            tableContent += densities.get(i).getValue();
            tableContent += "</td>";
            tableContent += "<td>";
            tableContent += densities.get(i).getValue();
            tableContent += "</td>";
            tableContent += "</tr>";
        }
        double inMin = Double.POSITIVE_INFINITY;
        double inMax = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;
        double min = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < densities.size(); ++i) {
            if (inMin > densities.get(i).getValue()) {
                inMin = densities.get(i).getValue();
            }
            if (inMax < densities.get(i).getValue()) {
                inMax = densities.get(i).getValue() ;
            }
            String interval = "[";
            interval += DynamicUtilities.getXMLDateStringFromDouble(densities.get(i).getLow()).replace('T', ' ').substring(0, 19) + ", ";
            interval += DynamicUtilities.getXMLDateStringFromDouble(densities.get(i).getHigh()).replace('T', ' ').substring(0, 19);
            interval += "]";
            
            report += Double.toString(densities.get(i).getValue());
            inDataset.addValue(densities.get(i).getValue(), "in degrees", interval);


        }

        String inImage = "";
        try {
            //        for(Integer n: degreelist){
            //            report += n.toString();
            //              }
            //Output to report for debugging purposes
//            report += "Directed:" + this.directed + "\n";
//            report += "Window:" + this.window + "\n";
//            report += "TimeInterval:" + this.timeInterval + "\n";
//            report += "Nodecount:" + this.nodecount + "\n";
//            report += "Length of degreelist" + Integer.toString(degreelist.toArray().length) + "\n";
//            report += nodecount;

            JFreeChart inChart = ChartFactory.createLineChart(
                    "Dynamic Graph Density",
                    "Degrees",
                    "Dynamic Graph Density",
                    inDataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    true);

            inChart.setPadding(new RectangleInsets(0, 80, 0, 0));
            CategoryPlot inPlot = (CategoryPlot) inChart.getPlot();
            inPlot.setBackgroundPaint(Color.WHITE);
            inPlot.setDomainGridlinePaint(Color.GRAY);
            inPlot.setRangeGridlinePaint(Color.GRAY);
            inPlot.setNoDataMessage("ERROR NO DATA");

            CategoryAxis inDomainAxis = inPlot.getDomainAxis();
            inDomainAxis.setLowerMargin(0.0);
            inDomainAxis.setUpperMargin(0.0);
            inDomainAxis.setAxisLineVisible(true);

            inDomainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
            NumberAxis inRangeAxis = (NumberAxis) inPlot.getRangeAxis();
           // inRangeAxis.setTickUnit(new NumberTickUnit(0.00005));
           // inRangeAxis.setRange(inMin - 0.0001 * inMin, inMax + 0.00001 * inMax);
            final LineAndShapeRenderer renderer = (LineAndShapeRenderer) inPlot.getRenderer();
            ChartRenderingInfo inInfo = new ChartRenderingInfo(new StandardEntityCollection());

            TempDirUtils.TempDir inTempDir = TempDirUtils.createTempDir();
            String inFileName = "inDynamicDistribution.png";
            File inFile = inTempDir.createFile(inFileName);
            inImage = "<img src=\"file:" + inFile.getAbsolutePath() + "\" " + "width=\"600\" height=\"600\" "
                    + "border=\"0\" usemap=\"#chart\"></img>";
            ChartUtilities.saveChartAsPNG(inFile, inChart, 600, 600, inInfo);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        report += inImage;
        return report;
    }
    
    
}
