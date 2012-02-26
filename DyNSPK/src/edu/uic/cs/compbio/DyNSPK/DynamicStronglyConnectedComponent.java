/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uic.cs.compbio.DyNSPK;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.gephi.data.attributes.api.*;
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
public class DynamicStronglyConnectedComponent implements DynamicStatistics {
    public String report;
    private boolean directed = false;
    private ArrayList<Interval<Double>> degreelist = new ArrayList<Interval<Double>>();
    private int nodecount;
    private ArrayList<Integer> graphsize = new ArrayList<Integer>();
    public static final String WEAKLY = "componentnumber";
    public static final String STRONG = "strongcompnum";
    private boolean isCanceled;
    private int componentCount;
    private int stronglyCount;
    private int[] componentsSize;
    int count;
    private GraphModel graphModel;
    private AttributeModel attributeModel;
    private DynamicModel dynamicModel;
    private Interval bounds;
    private double window;
    private double tick;

    
    public void weaklyConnected(HierarchicalUndirectedGraph hgraph, AttributeModel attributeModel) {
        isCanceled = false;
        componentCount = 0;
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn componentCol = nodeTable.getColumn(WEAKLY);
        if (componentCol == null) {
            componentCol = nodeTable.addColumn(WEAKLY, "Component ID", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));
        }

        List<Integer> sizeList = new ArrayList<Integer>();

        hgraph.readLock();

        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int index = 0;
        for (Node s : hgraph.getNodes()) {
            indicies.put(s, index);
            index++;
        }


        int N = hgraph.getNodeCount();

        //Keep track of which nodes have been seen
        int[] color = new int[N];

       // Progress.start(progress, hgraph.getNodeCount());
        int seenCount = 0;
        while (seenCount < N) {
            //The search Q
            LinkedList<Node> Q = new LinkedList<Node>();
            //The component-list
            LinkedList<Node> component = new LinkedList<Node>();

            //Seed the seach Q
            NodeIterable iter = hgraph.getNodes();
            for (Node first : iter) {
                if (color[indicies.get(first)] == 0) {
                    Q.add(first);
                    iter.doBreak();
                    break;
                }
            }

            //While there are more nodes to search
            while (!Q.isEmpty()) {
                if (isCanceled) {
                    hgraph.readUnlock();
                    return;
                }
                //Get the next Node and add it to the component list
                Node u = Q.removeFirst();
                component.add(u);

                //Iterate over all of u's neighbors
                EdgeIterable edgeIter = hgraph.getEdgesAndMetaEdges(u);

                //For each neighbor
                for (Edge edge : edgeIter) {
                    Node reachable = hgraph.getOpposite(u, edge);
                    int id = indicies.get(reachable);
                    //If this neighbor is unvisited
                    if (color[id] == 0) {
                        color[id] = 1;
                        //Add it to the search Q
                        Q.addLast(reachable);
                        //Mark it as used

                       // Progress.progress(progress, seenCount);
                    }
                }
                color[indicies.get(u)] = 2;
                seenCount++;
            }
            for (Node s : component) {
                AttributeRow row = (AttributeRow) s.getNodeData().getAttributes();
                row.setValue(componentCol, componentCount);
            }
            sizeList.add(component.size());
            componentCount++;
        }
//        hgraph.readUnlock();

        componentsSize = new int[sizeList.size()];
        for (int i = 0; i < sizeList.size(); i++) {
            componentsSize[i] = sizeList.get(i);
        }
    }

    public void top_tarjans(HierarchicalDirectedGraph hgraph, AttributeModel attributeModel) {
        count = 1;
        stronglyCount = 0;
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn componentCol = nodeTable.getColumn(STRONG);
        if (componentCol == null) {
            componentCol = nodeTable.addColumn(STRONG, "Strongly-Connected ID", AttributeType.INT, AttributeOrigin.COMPUTED, new Integer(0));
        }

        hgraph.readLock();

        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int v = 0;
        for (Node s : hgraph.getNodes()) {
            indicies.put(s, v);
            v++;
        }
        int N = hgraph.getNodeCount();
        int[] index = new int[N];
        int[] low_index = new int[N];

        while (true) {
            //The search Q
            LinkedList<Node> S = new LinkedList<Node>();
            //The component-list
            //LinkedList<Node> component = new LinkedList<Node>();
            //Seed the seach Q
            Node first = null;
            NodeIterable iter = hgraph.getNodes();
            for (Node u : iter) {
                if (index[indicies.get(u)] == 0) {
                    first = u;
                    iter.doBreak();
                    break;
                }
            }
            if (first == null) {
                hgraph.readUnlockAll();
                return;
            }
            tarjans(componentCol, S, hgraph, first, index, low_index, indicies);
        }
    }

    private void tarjans(AttributeColumn col, LinkedList<Node> S, HierarchicalDirectedGraph hgraph, Node f, int[] index, int[] low_index, HashMap<Node, Integer> indicies) {
        int id = indicies.get(f);
        index[id] = count;
        low_index[id] = count;
        count++;
        S.addFirst(f);
        EdgeIterable edgeIter = hgraph.getOutEdgesAndMetaOutEdges(f);
        for (Edge e : edgeIter) {
            Node u = hgraph.getOpposite(f, e);
            int x = indicies.get(u);
            if (index[x] == 0) {
                tarjans(col, S, hgraph, u, index, low_index, indicies);
                low_index[id] = Math.min(low_index[x], low_index[id]);
            } else if (S.contains(u)) {
                low_index[id] = Math.min(low_index[id], index[x]);
            }
        }
        if (low_index[id] == index[id]) {
            Node v = null;
            while (v != f) {
                v = S.removeFirst();
                AttributeRow row = (AttributeRow) v.getNodeData().getAttributes();
                row.setValue(col, stronglyCount);
            }
            stronglyCount++;
        }
    }

    public int getConnectedComponentsCount() {
        return componentCount;
    }



    public int[] getComponentsSize() {
        return componentsSize;
    }

    public int getGiantComponent() {
        int[] sizes = getComponentsSize();
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i] > max) {
                max = sizes[i];
                maxIndex = i;
            }
        }
        return maxIndex;
    }





    protected String getAdditionalParameters() {
        return "Network Interpretation: " + (directed ? "directed" : "undirected");
    }
    public boolean isDirected() {
        return directed;
    }

    public void setDirected(boolean directed) {
        this.directed = directed;
    }
    @Override
    public void execute(GraphModel gm, AttributeModel am) {
          this.graphModel = gm;
            this.directed = gm.isDirected();
            this.dynamicModel = Lookup.getDefault().lookup(DynamicController.class).getModel(graphModel.getWorkspace());
    }

    @Override
    public void loop(GraphView gv, Interval intrvl) {
       HierarchicalGraph g = graphModel.getHierarchicalGraph(gv);
       weaklyConnected((HierarchicalUndirectedGraph)g, attributeModel);
        if (isDirected()) {

            top_tarjans((HierarchicalDirectedGraph)g, attributeModel);
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

        for (int i = 0; i < degreelist.size(); ++i) {
            String interval = "[";
            interval += DynamicUtilities.getXMLDateStringFromDouble(degreelist.get(i).getLow()).replace('T', ' ').
                    substring(0, 19) + ", ";
            interval += DynamicUtilities.getXMLDateStringFromDouble(degreelist.get(i).getHigh()).replace('T', ' ').
                    substring(0, 19);
            interval += "]";
            //Creation of the table
            tableContent += "<tr>";
            tableContent += "<td>";
            tableContent += interval;
            tableContent += "</td>";
            tableContent += "<td>";
            tableContent += degreelist.get(i).getValue();
            tableContent += "</td>";
            tableContent += "<td>";
            tableContent += degreelist.get(i).getValue();
            tableContent += "</td>";
            tableContent += "</tr>";
        }
//        double inMin = Double.POSITIVE_INFINITY;
//        double inMax = Double.NEGATIVE_INFINITY;
        double max = Double.POSITIVE_INFINITY;
        double min = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < degreelist.size(); ++i) {
//            if (inMin > degreelist.get(i).getValue()) {
//                inMin = degreelist.get(i).getValue() / graphsize.get(i);
//            }
//            if (inMax < degreelist.get(i).getValue()) {
//                inMax = degreelist.get(i).getValue() / graphsize.get(i);
//            }
            String interval = "[";
            interval += DynamicUtilities.getXMLDateStringFromDouble(degreelist.get(i).getLow()).replace('T', ' ').substring(0, 19) + ", ";
            interval += DynamicUtilities.getXMLDateStringFromDouble(degreelist.get(i).getHigh()).replace('T', ' ').substring(0, 19);
            interval += "]";
         //  report += interval + degreelist.get(i).getValue();
            double value = 0.0;
            //Sum up every element in the degreelist from the start to the current value of the list.
            for (Interval<Double> interval1 : degreelist.subList(0, i)) {
                if (degreelist.get(i).getLow() == interval1.getLow() && degreelist.get(i).getHigh() == interval1.getHigh()) {
                    value =+ degreelist.get(i).getValue();
                }
            }
            //Compute the average value from the size of the graph and add that to the dataset.
            inDataset.addValue(value / graphsize.get(i), "in degrees", interval);


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
                    "Dynamic Strongly Connected Components", "Components", "Dynamic Strongly Connected Components",
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
           // inRangeAxis.setRange(inMin - 0.001 * inMin, inMax + 0.001 * inMax);
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
