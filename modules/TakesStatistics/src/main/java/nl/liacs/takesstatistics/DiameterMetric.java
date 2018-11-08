package nl.liacs.takesstatistics;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.*;
import org.gephi.graph.api.*;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.gephi.utils.progress.Progress;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author
 */
public class DiameterMetric implements Statistics, LongTask{
    
    // Attribute names
    private final String ECCENTRICITY = "eccentricity";
    private final String IS_PERIPHERY = "isperiphery";
    private final String IS_CENTER = "iscenter";

    // Option Flags
    private boolean isCanceled;
    private boolean eccentricitiesFlag;
    private boolean peripheryFlag;
    private boolean centerFlag;
    
    // Progress
    private ProgressTicket progress;
    
    // Internal usage
    private int[] eccLower;
    private int[] eccUpper;
    private int[] distance;
    private int[] pruned;
    
    // Array mapping Node.getStoreId() -> whether that node is a candidate
    private boolean[] isCandidate;
    
    private int giantComponentSize;
    
    // Results
    private int diameter;
    private int radius;
    private int peripherySize;
    private int centerSize;
    private int iterations;
    
    private LinkedList<Node> giantComponentNodes;
    
    private String test = "";
    
    // Make sure the graph contain relevant attributes, as defined by the flags
    private void initAttributeColumns(Table nodeTable) {
        if (this.eccentricitiesFlag && !nodeTable.hasColumn(ECCENTRICITY)) {
            nodeTable.addColumn(ECCENTRICITY, "Eccentricity", Integer.class, -1);
        }
        if (this.peripheryFlag && !nodeTable.hasColumn(IS_PERIPHERY)) {
            nodeTable.addColumn(IS_PERIPHERY, "Part of periphery", Boolean.class, false);
        }
        if (this.centerFlag && !nodeTable.hasColumn(IS_CENTER)) {
            nodeTable.addColumn(IS_CENTER, "Part of center", Boolean.class, false);
        }
    }

    
    @Override
    public void execute(GraphModel graphModel) {
        isCanceled = false;
        
        initAttributeColumns(graphModel.getNodeTable());
        
        
        
        Graph graph = graphModel.getUndirectedGraph();

        
        initializeStartValues(graph);
        
        boundingDiameters(graph);

    }
    
    //Calculates the eccentricity of a Node u given its graph.
    //return: eccentricity of Node u.
    private int eccentricity (Graph graph, Node u) {
        Node current; 
        int ecc = 0;
        Queue<Node> q = new ArrayDeque<Node>();

        Arrays.fill(distance, -1);

        distance[u.getStoreId()] = 0;
        q.add(u);
        while(!q.isEmpty()) {
            current = q.poll();
            for (Node s : graph.getNeighbors(current)) {
                if (distance[s.getStoreId()] == -1) { //and not pruned
                    distance[s.getStoreId()] = distance[current.getStoreId()] + 1;
                    q.add(s);
                    ecc = Math.max(ecc, distance[s.getStoreId()]);
                }
            }
        }
        return ecc;
    } // eccentricity
    
    private void boundingDiameters (Graph graph) {
        
        Node currentNode = null, maxUpperNode = null,
                minLowerNode = null;

        int candidateTotal = giantComponentSize, current_ecc = 0,
                maxUpper = -1, maxLower = -2, minUpper = -3,
                minLower = -4;

        int d_lower = 0, d_upper = giantComponentSize;

        candidateTotal -= pruning(graph);

        boolean high = true;
        
        Progress.start(progress, candidateTotal);
        int count = 0;
        
        currentNode = giantComponentNodes.getFirst();
        int currentNodeDegree = graph.getDegree(currentNode);
        for (Node s : giantComponentNodes) {
            if (distance[s.getStoreId()] != -1 
                && pruned[s.getStoreId()] < 0
                && graph.getDegree(s) > currentNodeDegree
                    ) {
                currentNode = s;
                currentNodeDegree = graph.getDegree(currentNode);
            }
        }
        
        while (d_lower != d_upper && candidateTotal > 0) {
          
            iterations++;
            
            current_ecc = eccentricity(graph, currentNode);
            //eccentricity fills distance array
            
            maxUpperNode = null;
            minLowerNode = null;
            
            maxLower = 0;
            maxUpper = 0;
            minLower = giantComponentSize;
            minUpper = giantComponentSize;
            
            //Update Bounds
            for (Node s : giantComponentNodes) {
                if (distance[s.getStoreId()] == -1 || pruned[s.getStoreId()] >= 0)
                        continue;
                
                //update eccentricity bounds
                eccLower[s.getStoreId()] = Math.max(eccLower[s.getStoreId()], Math.max(distance[s.getStoreId()], current_ecc - distance[s.getStoreId()]));
                eccUpper[s.getStoreId()] = Math.min(eccUpper[s.getStoreId()], current_ecc + distance[s.getStoreId()]);
                
                //update max values of lower and upper bounds
                maxLower = Math.max(eccLower[s.getStoreId()], maxLower);
                maxUpper = Math.max(eccUpper[s.getStoreId()], maxUpper);
                minLower = Math.min(eccLower[s.getStoreId()], minLower);
                minUpper = Math.min(eccUpper[s.getStoreId()], minUpper);
            }
            
            // update candidate set
            for (Node s : giantComponentNodes) {
                if (!isCandidate[s.getStoreId()] || distance[s.getStoreId()] == -1 || pruned[s.getStoreId()] >= 0)
                        continue;
                
                if (eccLower[s.getStoreId()] == eccUpper[s.getStoreId()]
                    || ( //general check
                        (eccUpper[s.getStoreId()] <= maxLower && eccLower[s.getStoreId()]*2 >= maxUpper) //diameter check
                        && (eccLower[s.getStoreId()] >= minUpper && (eccUpper[s.getStoreId()] + 1) / 2 <= minLower) //radius check
                        && (
                            !this.peripheryFlag || (
                                eccUpper[s.getStoreId()] < maxLower 
                                && (
                                    (maxLower == maxUpper) 
                                    || (eccLower[s.getStoreId()]*2 > maxUpper)
                                )
                            ) 
                        ) //periphery check
                        && (
                            !this.centerFlag || (
                                eccLower[s.getStoreId()] > minUpper 
                                && ((minLower == minUpper) || ((eccUpper[s.getStoreId()] + 1) / 2 < minLower))
                            ) 
                        )
                    )) {//center check
                    
                    isCandidate[s.getStoreId()] = false;
                    candidateTotal--;
                    count++;
                }
                
                // updating maxuppernode and minlowernode for selection in next round
                if (minLowerNode == null) 
                    minLowerNode = s;
                else if (eccLower[s.getStoreId()] == eccLower[minLowerNode.getStoreId()] && graph.getDegree(s) > graph.getDegree(minLowerNode))
                    minLowerNode = s;
                else if (eccLower[s.getStoreId()] < eccLower[minLowerNode.getStoreId()])
                    minLowerNode = s;
                
                if (maxUpperNode == null) 
                    maxUpperNode = s;
                else if (eccUpper[s.getStoreId()] == eccUpper[maxUpperNode.getStoreId()] && graph.getDegree(s) > graph.getDegree(maxUpperNode))
                    maxUpperNode = s;
                else if (eccUpper[s.getStoreId()] > eccUpper[maxUpperNode.getStoreId()])
                    maxUpperNode = s;
                
            }
            if (isCanceled) {
                diameter = maxLower;
                radius = minUpper;
                return;
            }
            
            
            high = !high;
            //Set currentnode for next iteration
            if (high)
                currentNode = maxUpperNode;
            else
                currentNode = minLowerNode;
            
            Progress.progress(progress, count);
        }//while
        
        if (d_upper == d_lower)
            Progress.progress(progress, giantComponentSize);
        
        test = test + iterations + "\n";
        for (Node s : giantComponentNodes) {
            test = test + eccLower[s.getStoreId()] + "-" + eccUpper[s.getStoreId()] + "\n";
        }
        
        for (Node s : graph.getNodes()) {
            if(pruned[s.getStoreId()] >= 0)
                eccLower[s.getStoreId()] = eccLower[(int) pruned[s.getStoreId()]];
        }
        
        diameter = maxLower;
        radius   = minUpper;
        for (Node s : graph.getNodes()) {
            if (this.peripheryFlag && eccLower[s.getStoreId()] == maxLower) {
                peripherySize++;
                s.setAttribute(IS_PERIPHERY, true);
            }
            if (this.centerFlag && eccUpper[s.getStoreId()] == minUpper) {
                centerSize++;
                s.setAttribute(IS_CENTER, true);
            }
            if (this.eccentricitiesFlag) {
                s.setAttribute(ECCENTRICITY, eccLower[s.getStoreId()]);
            }
        }
            
    }
    
    // pruning strategy
    private int pruning(Graph graph) {
        Node prunee;
        int count = 0;
        for (Node s : graph.getNodes())
            pruned[s.getStoreId()] = -1;

        // pruned[i] is going to contain the node number that i has identical ecc to
        for (Node s : graph.getNodes()) {
            if (!giantComponentNodes.contains(s))
                continue;
            prunee = null;
            
            for (Node u : graph.getNeighbors(s)) {
                if (graph.getDegree(u) == 1 && pruned[u.getStoreId()] == -1){
                    if (prunee == null) { // prune all but this one
                        prunee = u;
                    }
                    else {
                        pruned[u.getStoreId()] = prunee.getStoreId(); // [0...n-1] indicates that the node was pruned as it is identical to prunee
                        count++;
                        pruned[s.getStoreId()] = -2; // -2 indicates that its neighbors have been pruned
                    }
                }
            }
        }
        return count;
    } // pruning
    
    private void initializeStartValues(Graph graph) {
        // Initialize arrays, zeroes all entries by default
        final int numNodes = graph.getModel().getMaxNodeStoreId();
        
        eccLower = new int[numNodes];
        eccUpper = new int[numNodes];
        distance = new int[numNodes];
        pruned = new int[numNodes];
        isCandidate = new boolean[numNodes];

        iterations = 0;
        
        ConnectedComponents cc = new ConnectedComponents();
        cc.execute(graph.getModel());
        int giantComponentIndex = cc.getGiantComponent();
        
        int[] componentsSizes = cc.getComponentsSize();
        giantComponentSize = componentsSizes[giantComponentIndex];

        // Non-zero default values
        Arrays.fill(eccUpper, giantComponentSize);
        Arrays.fill(pruned, -1);
        Arrays.fill(isCandidate, true);
        
        giantComponentNodes = new LinkedList<Node>();

        
        for (Node s : graph.getNodes()) {
            if (s.getAttribute("componentnumber").equals(giantComponentIndex))
                giantComponentNodes.add(s);
        }
    }
    
    @Override
    public String getReport() {
        Map<Integer, Integer> eccDist = new HashMap();
        for (int v : eccLower) {
            if (!eccDist.containsKey(v)) {
                eccDist.put(v, 0);
            }
            eccDist.put(v, eccDist.get(v) + 1);
        }

        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(eccDist, "Eccentricity Distribution");

        XYSeriesCollection dataset1 = new XYSeriesCollection();
        dataset1.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Eccentricity Distribution",
                "Eccentricity (number of nodes)",
                "Count",
                dataset1,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String imageFile = ChartUtils.renderChart(chart, "bd-eccentricity-distribution.png");
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
                + (this.eccentricitiesFlag
                    ? ("<br /><br />" + imageFile)
                    : "")
                + "<br>"
                + "<br>"
                + "<h2>Algorithm</h2>"
                + test
                + "</body></html>";

    }
    
    @Override
    public boolean cancel() {
        isCanceled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        progress = progressTicket;
    }
    
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

