package nl.liacs.takesstatistics;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import org.gephi.graph.api.*;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.gephi.utils.progress.Progress;

/**
 *
 * @author
 */
public class DiameterMetric implements Statistics, LongTask{
    
    // Attribute names
    private final String ECCENTRICITY = "eccentricity";
    private final String IS_PERIPHERY = "isperiphery";
    private final String IS_CENTER = "iscenter";

    
    private boolean isCanceled;
    private boolean eccentricitiesFlag;
    private boolean peripheryFlag;
    private boolean centerFlag;
    private ProgressTicket progress;
    
    private LinkedList<Node> candidates;
    private int[] eccLower;
    private int[] eccUpper;
    private int[] distance;
    private int[] pruned;
    
    private int LWCC;
    
    private int diameter;
    private int radius;
    private int peripherySize;
    private int centerSize;
    
    private ConnectedComponents cc; 
    
    private LinkedList<Node> LWCCNodes;
    
    private String test = "";
    
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

    
    @Override
    public void execute(GraphModel graphModel) {
        isCanceled = false;
        
        initAttributeCollumns(graphModel.getNodeTable());
        
        cc = new ConnectedComponents();
        cc.execute(graphModel);
        
        Graph graph = graphModel.getUndirectedGraph();

        
        initializeStartValues(graph);
        
        boundingDiameters(graph);

    }
    
    //Calculates the eccentricity of a Node u given its graph.
    //return: eccentricity of Node u.
    private int eccentricity (Graph graph, Node u) {
        Node current; 
        int ecc = 0;
        Queue<Node> q = new LinkedList();

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
        
        candidates = new LinkedList<Node>();
        candidates.addAll(LWCCNodes);

        int candidateTotal = LWCC, current_ecc = 0,
                maxUpper = -1, maxLower = -2, minUpper = -3,
                minLower = -4;

        int d_lower = 0, d_upper = LWCC;

        //candidateTotal -= pruning(graph);

        boolean high = true;
        
        Progress.start(progress, candidateTotal);
        int count = 0;
        
        while (d_lower != d_upper && candidateTotal > 0) {
            
            high = !high;
            if (currentNode == null) {
                currentNode = LWCCNodes.getFirst();
                for (Node s : LWCCNodes) {
                    if (distance[s.getStoreId()] == -1 || pruned[s.getStoreId()] >= 0)
                        continue;
                    if (graph.getDegree(s) > graph.getDegree(currentNode))
                        currentNode = s;
                }
            }
            else if (high)
                currentNode = maxUpperNode;
            else
                currentNode = minLowerNode;
            
            current_ecc = eccentricity(graph, currentNode);
            //eccentricity should fill distance array
            
            maxUpperNode = null;
            minLowerNode = null;
            
            maxLower = 0;
            maxUpper = 0;
            minLower = LWCC;
            minUpper = LWCC;
            
            //Update Bounds
            for (Node s : LWCCNodes) {
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
            // No smart for-loop to avoid exception
            for (int i = 0; i < LWCCNodes.size(); i++) {
                Node s = LWCCNodes.get(i);
                if (!candidates.contains(s) || distance[s.getStoreId()] == -1 || pruned[s.getStoreId()] >= 0)
                        continue;
                if (candidates.contains(s) && ( //if s is in candidate set
                        (eccLower[s.getStoreId()] == eccUpper[s.getStoreId()]) ||
                        (eccUpper[s.getStoreId()] <= maxLower && eccLower[s.getStoreId()]*2 >= maxUpper)) &&
                        (eccLower[i] >= minUpper && (eccUpper[i] + 1) / 2 <= minLower)) {
                    candidates.remove(s);
                    candidateTotal--;
                    count++;
                }
                
                // updating maxuppernode and minlowernode for selection in next round
                if (candidates.contains(s)) { //if s is in candidate set
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
                    else if (eccUpper[s.getStoreId()] < eccUpper[maxUpperNode.getStoreId()])
                        maxUpperNode = s;
                }
            }
            if (isCanceled) {
                diameter = maxLower;
                radius = minUpper;
                return;
            }
            Progress.progress(progress, count);
        }//while
        if (d_upper == d_lower)
            Progress.progress(progress, LWCC);
        
        for (Node s : LWCCNodes) {
            test = test + eccLower[s.getStoreId()] + "-" + eccUpper[s.getStoreId()] + "\n";
        }
        
        for (Node s : graph.getNodes()) {
            if(pruned[s.getStoreId()] >= 0)
                eccLower[s.getStoreId()] = eccLower[(int) pruned[s.getStoreId()]];
        }
        
        diameter = maxLower;
        radius = minUpper;
    }
    
    // pruning strategy
    // TODO: fix pruning, it decreases the resulting diameter
    private int pruning(Graph graph) {
        Node prunee;
        int count = 0;
        for (Node s : graph.getNodes())
            pruned[s.getStoreId()] = -1;

        // pruned[i] is going to contain the node number that i has identical ecc to
        for (Node s : graph.getNodes()) {
            if (!LWCCNodes.contains(s))
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
                        pruned[u.getStoreId()] = -2; // -2 indicates that its neighbors have been pruned
                    }
                }
            }
        }
        return count;
        /*
        for(int i = 0; i < nodes(FULL); i++) {
            if(!inScope(i, LWCC))
                continue;

            z = neighbors(i).size();
            prunee = -1;

            for(int j = 0; j < z; j++) {
                if(neighbors(neighbors(i)[j]).size() == 1 && pruned[neighbors(i)[j]] == -1) {
                    if(prunee == -1) { // prune all but this one
                        prunee = neighbors(i)[j];
                    } else {
                        pruned[neighbors(i)[j]] = prunee; // [0...n-1] indicates that the node was pruned as it is identical to prunee
                        pruned[prunee] = -2; // -2 indicates that its neighbors have been pruned			
                    }
                }
            }
        }*/
    } // pruning
    
    private void initializeStartValues(Graph graph) {
        // Initialize arrays, zeroes all entries by default
        eccLower = new int[graph.getNodeCount()];
        eccUpper = new int[graph.getNodeCount()];
        distance = new int[graph.getNodeCount()];
        pruned = new int[graph.getNodeCount()];
        
        int giantComponentIndex = cc.getGiantComponent();
        
        int[] componentsSizes = cc.getComponentsSize();
        LWCC = componentsSizes[giantComponentIndex];
        
        // Non-zero default values
        Arrays.fill(eccUpper, LWCC);
        Arrays.fill(pruned, -1);
        
        LWCCNodes = new LinkedList<Node>();

        
        
        for (Node s : graph.getNodes()) {
            if (s != null)
                LWCCNodes.add(s);
        }
    }
    
    @Override
    public String getReport() {
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

