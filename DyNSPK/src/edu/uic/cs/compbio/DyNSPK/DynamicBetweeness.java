/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uic.cs.compbio.DyNSPK;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.gephi.algorithms.shortestpath.AbstractShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.BellmanFordShortestPathAlgorithm;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.*;
import org.gephi.statistics.plugin.ChartUtils;
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
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author zitterbewegung
 */
public class DynamicBetweeness implements DynamicStatistics{
    
    private Map<Double, Double> degreeTs;
    private Interval bounds;
    private double tick;
    private double window;

    private Map<Double, Double> betweeness;
    private boolean directed;
    private GraphModel graphModel;
    private DynamicModel dynamicModel;
    private String report;
    private ArrayList<Interval<Double>> degreelist = new ArrayList<Interval<Double>>(); 
    private GraphController gc;
    @Override
    public void execute(GraphModel gm, AttributeModel am) {
         this.graphModel = gm;
         this.directed = gm.isDirected();
         this.dynamicModel = Lookup.getDefault().lookup(DynamicController.class).getModel(graphModel.getWorkspace());
         this.degreeTs = new HashMap<Double, Double>();
         this.gc = Lookup.getDefault().lookup(GraphController.class);
    }

    @Override
    public void loop(GraphView gv, Interval intrvl) {
        HashMap<Node, Double> data = new HashMap<Node, Double>();
        double sum = 0.0;
        for (Node n : gv.getGraphModel().getDirectedGraph().getNodes()) {
         
            
             AbstractShortestPathAlgorithm algorithm;
                    if (directed) {
                        DirectedGraph graph = (DirectedGraph) gv.getGraphModel().getGraphVisible();
                        algorithm = new BellmanFordShortestPathAlgorithm(graph, n);
                        algorithm.compute();
                    } else {
                        Graph graph = gv.getGraphModel().getGraphVisible();
                        algorithm = new DijkstraShortestPathAlgorithm(graph, n);
                        algorithm.compute();
                    }
         
             double maxDistance = algorithm.getMaxDistance();
                    if (maxDistance > 0) {
                        for (Map.Entry<Node, Double> entry : algorithm.getDistances().entrySet()) {
                            NodeData node = entry.getKey().getNodeData();
                            
                        
                            if (!Double.isInfinite(entry.getValue())) {
                                sum = sum + entry.getValue();
                               
                            } 
                        }
                    }
            
        }
       degreeTs.put(intrvl.getHigh(), sum);
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
     //Time series
        XYSeries dSeries = ChartUtils.createXYSeries(degreeTs, "Degree Time Series");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Degree Time Series",
                "Time",
                "Average Degree",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String degreeImageFile = ChartUtils.renderChart(chart, "degree-ts.png");

        NumberFormat f = new DecimalFormat("#0.000000");

        String report = "<HTML> <BODY> <h1>Dynamic Degree Report </h1> "
                + "<hr>"
                + "<br> Bounds: from " + f.format(bounds.getLow()) + " to " + f.format(bounds.getHigh())
                + "<br> Window: " + window
                + "<br> Tick: " + tick
                + "<br><br><h2> Average degrees over time: </h2>"
                + "<br /><br />" + degreeImageFile;

        /*for (Interval<Double> average : averages) {
        report += average.toString(dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DOUBLE)) + "<br />";
        }*/
        report += "<br /><br /></BODY></HTML>";
        return report;
    }

    boolean isDirected() {
        return directed;
    }

    void setDirected(boolean directed) {
        this.directed = directed;
    }
    
}
