/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freetime.plugins.metric.trophiclevels;

import java.util.LinkedList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;

import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.statistics.plugin.ConnectedComponents;
import org.gephi.statistics.spi.Statistics;
import org.openide.util.Lookup;

import org.la4j.Matrix;
import org.la4j.Vector;
import org.la4j.matrix.MatrixFactory;
import org.la4j.vector.dense.BasicVector;
import org.la4j.vector.DenseVector;
import org.la4j.matrix.dense.Basic2DMatrix;

import org.la4j.linear.GaussianSolver;
/**
 *
 * @author wouter
 */

public class TrophicLevels implements Statistics {
    
    public static final String TROPHICLEVEL = "TrophicLevel";
    public static final String COMPONENT = "Component";
    private boolean isDirected;
    private boolean singularWarning = false;
    private Map<Integer, Double> incoherenceMap = new HashMap();
    public String report;
    
    public TrophicLevels() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel() != null) {
            isDirected = graphController.getGraphModel().isDirected();
        }       
    }
    
    @Override
    public void execute(GraphModel graphModel) {
        Graph graph;
        isDirected = graphModel.isDirected();
        if (isDirected) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }
        execute(graph, graphModel);
    }
    
    // This is where the actual work is done.
    public void execute(Graph hgraph, GraphModel graphModel) {
        // Let's make a default report in case something went wrong:
        report = "<HTML? <BODY> <h1>Trophic level</h1> "
            + "<hr>"
            + "<br> Something went wrong and the function did not fully execute. <br />"
            + "<br> <br />"
            + "</BODY></HTML>";
        // We check if the graph is directed. If not, we report this to the user and return.
        if (graphModel.isDirected()) {
            // Add column for trophic levels if not already there
            Table nodeTable = graphModel.getNodeTable();
            Column trophcol = nodeTable.getColumn(TROPHICLEVEL);
            Column compcol = nodeTable.getColumn(COMPONENT);
            if (trophcol == null) {
             trophcol = nodeTable.addColumn(TROPHICLEVEL, "TrophicLevel", Double.class, null);
            } 
            // Lock the graph while we are working with it.
            hgraph.readLock();
            // We want to check if there are weakly connected components.
            // We treat the graph as undirected for this step.
            UndirectedGraph undirectedGraph = graphModel.getUndirectedGraphVisible();
            // Let us just use the built-in Gephi functions for getting components.
            ConnectedComponents componentsModule = new ConnectedComponents();
            HashMap<Node, Integer> indices = componentsModule.createIndicesMap(undirectedGraph);
            LinkedList<LinkedList<Node>> components = componentsModule.computeWeaklyConnectedComponents(undirectedGraph, indices);

            // Add component variable if we have multiple components.
            if (components.size() > 1)
            {
                if (compcol == null) {
                    compcol = nodeTable.addColumn(COMPONENT, "Component", Integer.class, 0);
                }   
                int c = 0;
                for (LinkedList<Node> component : components) {
                    for (Node currentNode : component) {
                        currentNode.setAttribute(compcol, c);
                    }
                    c++;
                }
            }

            // Now we treat each component as a separate network.
            // Let's keep track of component numbers, starting with 1.
            int c = 0;
            for (LinkedList<Node> component : components) {
                // Ignore components that are singular systems
                if (component.size() > 1 )
                {
                    // Now we reconstruct our adjacency matrix
                    // We make an empty matrix first.
                    double[][] adj_mat = new double[component.size()][component.size()];
                    // And let's initialize it to be sure
                    for (int x = 0; x < component.size(); x++) {
                        for (int y = 0; y < component.size(); y++) {
                            adj_mat[x][y] = 0.0;
                         }
                    }

                    // We will create an array of nodes of the current component.
                    Node[] nodes = new Node[component.size()];
                    for (int i =0; i < component.size(); i++) {
                        nodes[i] = component.get(i);
                    }

                    // We can already get the inweight and outweight while we 
                    // make the adjacency matrix
                    double[] inweight = new double[component.size()];
                    double[] outweight = new double[component.size()];

                    // Now we iterate through all nodes
                    for (int i = 0; i < component.size(); i++) {
                        // First initialize the attribute
                        nodes[i].setAttribute(trophcol, 0.0);
                        // Now let us find all targets of this node and iterate through them
                        for (int j = 0; j < component.size(); j++) {
                            if (hgraph.getEdge(nodes[i], nodes[j]) != null) {
                                // Let's get the weight of the edge between these nodes
                                double weight = hgraph.getEdge(nodes[i], nodes[j]).getWeight();
                                outweight[i] += weight;
                                inweight[j] += weight;
                                // And then fill the corresponding cell of the matrix
                                adj_mat[i][j] = weight;
                            }
                        }
                    }

                    // Let us create the v and w variables
                    double[] vA = new double[component.size()];
                    double[] wA = new double[component.size()];
                    for (int i = 0; i < component.size(); i++) {
                        vA[i] = inweight[i] - outweight[i];
                        wA[i] = inweight[i] + outweight[i];
                    }
                    Vector v = new BasicVector(vA);
                    Vector w = new BasicVector(wA);

                    // Let's create the diagonal matrix from w.
                    Matrix diag = new Basic2DMatrix(component.size(),component.size());
                    for (int i = 0; i < component.size(); i++) {
                        diag.set(i, i, w.get(i));
                    }

                    // Let us now create matrices from our results.
                    Matrix adj = new Basic2DMatrix(adj_mat);
                    Matrix sum = adj.add(adj.transpose());

                    // And now we prepare our matrix for the linear solver.
                    Matrix L = diag.subtract(sum);
                    L.set(0, 0, 0.0);

                    // The vector for results
                    Vector h;

                    // The actual solver from the la4j library
                    GaussianSolver solver = new GaussianSolver(L);
                    h = solver.solve(v);
                    h = h.subtract(h.min());
                    DenseVector hD = h.toDenseVector();
                    double[] results = hD.toArray();
                    for (int i = 0; i < component.size(); i++) {
                       nodes[i].setAttribute(trophcol, results[i]);
                    }

                    // An attempt to also calculate the trophic incoherence

                    // First initialize the numinator and denominator
                    double numerator = 0.0;
                    double denominator = 0.0;
                    for (int i = 0; i < component.size(); i++) {
                        for (int j = 0; j < component.size(); j++) {
                            // It only makes sense to do this if there is an edge 
                            if (hgraph.getEdge(nodes[i], nodes[j]) != null) {
                                // Get the weight of the edge between the two nodes
                                double weight = hgraph.getEdge(nodes[i], nodes[j]).getWeight();                
                                // Let's immediately add this to the sum of weights (denominator)
                                denominator += weight;
                                // Get the trophic level of the nodes
                                double hi = results[i];
                                double hj = results[j];
                                // Then we can add to the numerator
                                numerator += weight * ((hj - hi - 1) * (hj -hi - 1));
                            }
                        }
                    }
                    double incoherence= numerator / denominator;

                    // Keep track of trophic incoherence for this component
                    incoherenceMap.put(c, incoherence);
                }
                else {
                    singularWarning = true;
                }
                // Increment component number; Last thing we do in this loop. 
                c++;
            }            
        // What the report is upon success.
        report = "<HTML? <BODY> <h1>Trophic levels</h1> "
        + "<hr>"
        + "<br> The trophic levels of each node are reported in the TrophicLevels column (see data laboratory). <br />";
        // We need to add the trophic incoherence for each component separately.
        Iterator<Map.Entry<Integer, Double>> entrySet = incoherenceMap.entrySet().iterator();
        while (entrySet.hasNext()) {
            Map.Entry<Integer, Double> entry = entrySet.next();
                report += "<br>" + "Trophic Incoherence of component " + entry.getKey() + ":  " + entry.getValue() + "<br />";
        }
        report += "<br> </br>";
        if (singularWarning) { 
            report += "<br> Components with single nodes were found."
            + "These components are skipped in the calculation of trophic levels and trophic incoherence.<br />"
            + "<br> <br />";
        }

        report += "</BODY></HTML>";
        }   
        else {
            // Report if graph is undirected.
            report = "<HTML? <BODY> <h1>Trophic levels</h1> "
            + "<hr>"
            + "<br> The Trophic Levels metric only applies to directed graphs. <br />"
            + "<br> <br />";
            report += "</BODY></HTML>";
        }
        // Unlock the graph after we're finished.q
        hgraph.readUnlock();
    }
    
    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }
    
    @Override
    public String getReport() {
        // Return report.
        return report;
    }
}
