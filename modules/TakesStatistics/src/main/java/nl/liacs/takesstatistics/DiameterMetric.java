package nl.liacs.takesstatistics;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;
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
        
        HashMap<Node, Integer> indices = createIndicesMap(graph);
        
        initializeStartValues(graph, indices);
        
        boundingDiameters(graph, indices);

    }
    
    //Calculates the eccentricity of a Node u given
    //its graph and indices HashMap.
    //return: eccentricity of Node u.
    public int eccentricity (Graph graph, HashMap<Node, Integer> indices, Node u) {
        Node current; 
        int ecc = 0;
        Queue<Node> q = new LinkedList();
        for (Node s : graph.getNodes())
            distance[indices.get(s)] = -1;

        distance[indices.get(u)] = 0;
        q.add(u);
        while(!q.isEmpty()) {
            current = q.poll();
            for (Node s : graph.getNeighbors(current)) {
                if (distance[indices.get(s)] == -1) { //and not pruned
                    distance[indices.get(s)] = distance[indices.get(current)] + 1;
                    q.add(s);
                    ecc = Math.max(ecc, distance[indices.get(s)]);
                }
            }
        }
        return ecc;
    } // eccentricity
    
    public void boundingDiameters (Graph graph, HashMap<Node, Integer> indices) {
        
        Node currentNode = null, maxUpperNode = null,
                minLowerNode = null;
        
        candidates = new LinkedList<Node>();
        candidates.addAll(LWCCNodes);

        int candidateTotal = LWCC, current_ecc = 0,
                maxUpper = -1, maxLower = -2, minUpper = -3,
                minLower = -4;

        int d_lower = 0, d_upper = LWCC;
        for (Node s : LWCCNodes){
            eccLower[indices.get(s)] = 0;
            eccUpper[indices.get(s)] = LWCC;
            distance[indices.get(s)] = 0;
            pruned[indices.get(s)] = -1;
        }

        //candidateTotal -= pruning(graph, indices);

        boolean high = true;
        
        Progress.start(progress, candidateTotal);
        int count = 0;
        
        while (d_lower != d_upper && candidateTotal > 0) {
            
            high = !high;
            if (currentNode == null) {
                currentNode = LWCCNodes.getFirst();
                for (Node s : LWCCNodes) {
                    if (distance[indices.get(s)] == -1 || pruned[indices.get(s)] >= 0)
                        continue;
                    if (graph.getDegree(s) > graph.getDegree(currentNode))
                        currentNode = s;
                }
            }
            else if (high)
                currentNode = maxUpperNode;
            else
                currentNode = minLowerNode;
            
            current_ecc = eccentricity(graph, indices, currentNode);
            //eccentricity should fill distance array
            
            maxUpperNode = null;
            minLowerNode = null;
            
            maxLower = 0;
            maxUpper = 0;
            minLower = LWCC;
            minUpper = LWCC;
            
            //Update Bounds
            for (Node s : LWCCNodes) {
                if (distance[indices.get(s)] == -1 || pruned[indices.get(s)] >= 0)
                        continue;
                
                //update eccentricity bounds
                eccLower[indices.get(s)] = Math.max(eccLower[indices.get(s)], Math.max(distance[indices.get(s)], current_ecc - distance[indices.get(s)]));
                eccUpper[indices.get(s)] = Math.min(eccUpper[indices.get(s)], current_ecc + distance[indices.get(s)]);
                
                //update max values of lower and upper bounds
                maxLower = Math.max(eccLower[indices.get(s)], maxLower);
                maxUpper = Math.max(eccUpper[indices.get(s)], maxUpper);
                minLower = Math.min(eccLower[indices.get(s)], minLower);
                minUpper = Math.min(eccUpper[indices.get(s)], minUpper);
            }
            
            // update candidate set
            // No smart for-loop to avoid exception
            for (int i = 0; i < LWCCNodes.size(); i++) {
                Node s = LWCCNodes.get(i);
                if (!candidates.contains(s) || distance[indices.get(s)] == -1 || pruned[indices.get(s)] >= 0)
                        continue;
                if (candidates.contains(s) && ( //if s is in candidate set
                        (eccLower[indices.get(s)] == eccUpper[indices.get(s)]) ||
                        (eccUpper[indices.get(s)] <= maxLower && eccLower[indices.get(s)]*2 >= maxUpper)) &&
                        (eccLower[i] >= minUpper && (eccUpper[i] + 1) / 2 <= minLower)) {
                    candidates.remove(s);
                    candidateTotal--;
                    count++;
                }
                
                // updating maxuppernode and minlowernode for selection in next round
                if (candidates.contains(s)) { //if s is in candidate set
                    if (minLowerNode == null) 
                        minLowerNode = s;
                    else if (eccLower[indices.get(s)] == eccLower[indices.get(minLowerNode)] && graph.getDegree(s) > graph.getDegree(minLowerNode))
                        minLowerNode = s;
                    else if (eccLower[indices.get(s)] < eccLower[indices.get(minLowerNode)])
                        minLowerNode = s;
                    if (maxUpperNode == null) 
                        maxUpperNode = s;
                    else if (eccUpper[indices.get(s)] == eccUpper[indices.get(maxUpperNode)] && graph.getDegree(s) > graph.getDegree(maxUpperNode))
                        maxUpperNode = s;
                    else if (eccUpper[indices.get(s)] < eccUpper[indices.get(maxUpperNode)])
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
            test = test + eccLower[indices.get(s)] + "-" + eccUpper[indices.get(s)] + "\n";
        }
        
        for (Node s : graph.getNodes()) {
            if(pruned[indices.get(s)] >= 0)
                eccLower[indices.get(s)] = eccLower[(int) pruned[indices.get(s)]];
        }
        
        diameter = maxLower;
        radius = minUpper;
    }
    
    public HashMap<Node, Integer> createIndicesMap(Graph graph) {
        HashMap<Node, Integer> indices = new HashMap();
        int index = 0;
        for (Node s : graph.getNodes()) {
            indices.put(s, index);
            index++;
        }
        return indices;
    }
    
    // pruning strategy
    // TODO: fix pruning, it decreases the resulting diameter
    public int pruning(Graph graph, HashMap<Node, Integer> indices) {
        Node prunee;
        int count = 0;
        for (Node s : graph.getNodes())
            pruned[indices.get(s)] = -1;

        // pruned[i] is going to contain the node number that i has identical ecc to
        for (Node s : graph.getNodes()) {
            if (!LWCCNodes.contains(s))
                continue;
            prunee = null;
            
            for (Node u : graph.getNeighbors(s)) {
                if (graph.getDegree(u) == 1 && pruned[indices.get(u)] == -1){
                    if (prunee == null) { // prune all but this one
                        prunee = u;
                    }
                    else {
                        pruned[indices.get(u)] = indices.get(prunee); // [0...n-1] indicates that the node was pruned as it is identical to prunee
                        count++;
                        pruned[indices.get(u)] = -2; // -2 indicates that its neighbors have been pruned
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
    
    public void initializeStartValues(Graph graph, HashMap<Node, Integer> indices) {
        eccLower = new int[graph.getNodeCount()];
        eccUpper = new int[graph.getNodeCount()];
        distance = new int[graph.getNodeCount()];
        pruned = new int[graph.getNodeCount()];
        
        LWCCNodes = new LinkedList<Node>();

        int giantComponentIndex = cc.getGiantComponent();
        
        int[] componentsSizes = cc.getComponentsSize();
        LWCC = componentsSizes[giantComponentIndex];
        
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

