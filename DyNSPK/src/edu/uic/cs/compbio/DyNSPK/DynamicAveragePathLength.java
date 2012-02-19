/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uic.cs.compbio.DyNSPK;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.graph.api.*;
import org.gephi.statistics.spi.DynamicStatistics;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.openide.util.Exceptions;

/**
 *
 * @author zitterbewegung
 */
public class DynamicAveragePathLength implements DynamicStatistics, LongTask  {
     public String report;
    private boolean directed;
    private ArrayList<Interval<Double>> averageDistances = new ArrayList<Interval<Double>>();
     private ArrayList<Interval<Double>> diameters = new ArrayList<Interval<Double>>();
    private int nodecount;
    private ArrayList<Integer> graphsize = new ArrayList<Integer>();
     public static final String BETWEENNESS = "betweenesscentrality";
    public static final String CLOSENESS = "closnesscentrality";
    public static final String ECCENTRICITY = "eccentricity";
    /** */
    private double[] betweenness;
    /** */
    private double[] closeness;
    /** */
    private double[] eccentricity;
    /** */
    private double diameter;
    private double radius;
    /** */
    private double avgDist;
    /** */
    private int N;
    /** */
    private boolean isDirected;
    /** */
    private ProgressTicket progress;
    /** */
    private boolean isCanceled;
    private double shortestPaths;
    private boolean isNormalized;
    private boolean graphtype = false;
    private GraphModel graphModel;
    private double tick;
    private Interval bounds;
    private boolean averageOnly;
    private double window;
    private boolean cancel;

    
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
         this.graphModel = graphModel;
         this.isDirected = graphModel.isDirected();
    }

    public void loop(GraphView window, Interval interval) {
        
        HierarchicalDirectedGraph g = null;
        if (isDirected) {
            g = graphModel.getHierarchicalDirectedGraph(window);
        }
       N = g.getNodeCount();
        betweenness = new double[N];
        eccentricity = new double[N];
        closeness = new double[N];
        isCanceled = false;
        
        diameter = 0;
        avgDist = 0;
        shortestPaths = 0;
        radius = Integer.MAX_VALUE;
        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int index = 0;
        for (Node s : g.getNodes()) {
            indicies.put(s, index);
            index++;
        }

        Progress.start(progress, g.getNodeCount());
        int count = 0;
        for (Node s : g.getNodes()) {
            Stack<Node> S = new Stack<Node>();

            LinkedList<Node>[] P = new LinkedList[N];
            double[] theta = new double[N];
            int[] d = new int[N];
            for (int j = 0; j < N; j++) {
                P[j] = new LinkedList<Node>();
                theta[j] = 0;
                d[j] = -1;
            }

            int s_index = indicies.get(s);

            theta[s_index] = 1;
            d[s_index] = 0;

            LinkedList<Node> Q = new LinkedList<Node>();
            Q.addLast(s);
            while (!Q.isEmpty()) {
                Node v = Q.removeFirst();
                S.push(v);
                int v_index = indicies.get(v);

                EdgeIterable edgeIter = null;
                if (isDirected) {
                    edgeIter = ((HierarchicalDirectedGraph) g).getOutEdgesAndMetaOutEdges(v);
                } else {
                    edgeIter = g.getEdgesAndMetaEdges(v);
                }

                for (Edge edge : edgeIter) {
                    Node reachable = g.getOpposite(v, edge);

                    int r_index = indicies.get(reachable);
                    if (d[r_index] < 0) {
                        Q.addLast(reachable);
                        d[r_index] = d[v_index] + 1;
                    }
                    if (d[r_index] == (d[v_index] + 1)) {
                        theta[r_index] = theta[r_index] + theta[v_index];
                        P[r_index].addLast(v);
                    }
                }
            }
            double reachable = 0;
            for (int i = 0; i < N; i++) {
                if (d[i] > 0) {
                    avgDist += d[i];
                    eccentricity[s_index] = (int) Math.max(eccentricity[s_index], d[i]);
                    closeness[s_index] += d[i];
                    diameter = Math.max(diameter, d[i]);
                    reachable++;
                }
            }

            radius = (int) Math.min(eccentricity[s_index], radius);

            if (reachable != 0) {
                closeness[s_index] /= reachable;
            }

            shortestPaths += reachable;

            double[] delta = new double[N];
            while (!S.empty()) {
                Node w = S.pop();
                int w_index = indicies.get(w);
                ListIterator<Node> iter1 = P[w_index].listIterator();
                while (iter1.hasNext()) {
                    Node u = iter1.next();
                    int u_index = indicies.get(u);
                    delta[u_index] += (theta[u_index] / theta[w_index]) * (1 + delta[w_index]);
                }
                if (w != s) {
                    betweenness[w_index] += delta[w_index];
                }
            }
            count++;
            if (isCanceled) {
                g.readUnlockAll();
                return;
            }
            Progress.progress(progress, count);
        }

        avgDist /= shortestPaths;//mN * (mN - 1.0f);
       
       
    
        averageDistances.add(new Interval(interval.getLow(),interval.getHigh(),avgDist));
        diameters.add(new Interval(interval.getLow(),interval.getHigh(),diameter));
        
    }

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

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public void setAverageOnly(boolean averageOnly) {
        this.averageOnly = averageOnly;
    }

    public boolean isAverageOnly() {
        return averageOnly;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
    }

    public String getReport() {
          String tableContent = "";
        DefaultCategoryDataset inDataset = new DefaultCategoryDataset();

        for (int i = 0; i < averageDistances.size(); ++i) {
            String interval = "[";
            interval += DynamicUtilities.getXMLDateStringFromDouble(averageDistances.get(i).getLow()).replace('T', ' ').
                    substring(0, 19) + ", ";
            interval += DynamicUtilities.getXMLDateStringFromDouble(averageDistances.get(i).getHigh()).replace('T', ' ').
                    substring(0, 19);
            interval += "]";
            //Creation of the table
            tableContent += "<tr>";
            tableContent += "<td>";
            tableContent += interval;
            tableContent += "</td>";
            tableContent += "<td>";
            tableContent += averageDistances.get(i).getValue();
            tableContent += "</td>";
            tableContent += "<td>";
            tableContent += averageDistances.get(i).getValue();
            tableContent += "</td>";
            tableContent += "</tr>";
        }
//        double inMin = Integer.MAX_VALUE;
//        double inMax = Integer.MIN_VALUE;
        double max = Double.POSITIVE_INFINITY;
        double min = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < averageDistances.size(); ++i) {

            String interval = "[";
            interval += DynamicUtilities.getXMLDateStringFromDouble(averageDistances.get(i).getLow()).replace('T', ' ').substring(0, 19) + ", ";
            interval += DynamicUtilities.getXMLDateStringFromDouble(averageDistances.get(i).getHigh()).replace('T', ' ').substring(0, 19);
            interval += "]";
            
            report += Double.toString(averageDistances.get(i).getValue());
            inDataset.addValue(averageDistances.get(i).getValue(), "in degrees", interval);


        }

        String inImage = "";
        if(isDirected()){
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
                    "Dynamic Average Path Length",
                    "Edges",
                    "Dynamic Average Path Length",
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
           //inRangeAxis.setTickUnit(new NumberTickUnit(0.05));
           //inRangeAxis.setRange(min - 0.0001 * min, max + 0.01 * max);
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
        }
        else{
        DefaultCategoryDataset inDataset2 = new DefaultCategoryDataset();

        for (int i = 0; i < diameters.size(); ++i) {
            String interval = "[";
            interval += DynamicUtilities.getXMLDateStringFromDouble(diameters.get(i).getLow()).replace('T', ' ').
                    substring(0, 19) + ", ";
            interval += DynamicUtilities.getXMLDateStringFromDouble(diameters.get(i).getHigh()).replace('T', ' ').
                    substring(0, 19);
            interval += "]";
            //Creation of the table
            tableContent += "<tr>";
            tableContent += "<td>";
            tableContent += interval;
            tableContent += "</td>";
            tableContent += "<td>";
            tableContent += diameters.get(i).getValue();
            tableContent += "</td>";
            tableContent += "<td>";
            tableContent += diameters.get(i).getValue();
            tableContent += "</td>";
            tableContent += "</tr>";
        }
       

        for (int i = 0; i < diameters.size(); ++i) {
//            if (inMin > diameters.get(i).getValue()) {
//                inMin = diameters.get(i).getValue();
//            }
//            if (inMax < diameters.get(i).getValue()) {
//                inMax = diameters.get(i).getValue() ;
//            }
            String interval = "[";
            interval += DynamicUtilities.getXMLDateStringFromDouble(diameters.get(i).getLow()).replace('T', ' ').substring(0, 19) + ", ";
            interval += DynamicUtilities.getXMLDateStringFromDouble(diameters.get(i).getHigh()).replace('T', ' ').substring(0, 19);
            interval += "]";
            
            report += Double.toString(diameters.get(i).getValue()) + " ";
            inDataset2.addValue(diameters.get(i).getValue(), "edges", interval);


        }

     
        try {
          

            JFreeChart inChart = ChartFactory.createLineChart(
                    "Dynamic Average Distance",
                    "Edges",
                    "Dynamic Average Distance",
                    inDataset2,
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
}
        return report;
    }
}
