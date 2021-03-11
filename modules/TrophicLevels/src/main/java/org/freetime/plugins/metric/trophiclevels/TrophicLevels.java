/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.freetime.plugins.metric.trophiclevels;

import java.util.LinkedList;
import java.util.HashMap;

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
    
    public static final String TROPHICLEVELS = "TrophicLevels";
    private boolean isDirected;
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
        report = "<HTML? <BODY> <h1>Trophic levels</h1> "
            + "<hr>"
            + "<br> Something went wrong and the function did not fully execute. <br />"
            + "<br> <br />"
            + "</BODY></HTML>";
        // We check if the graph is directed. If not, we report this to the user and return.
        if (graphModel.isDirected()) {
            // Add column for trophic levels if not already there
            Table nodeTable = graphModel.getNodeTable();
            Column col = nodeTable.getColumn(TROPHICLEVELS);
            if (col == null) {
             col = nodeTable.addColumn(TROPHICLEVELS, "TrophicLevels", Double.class, 0.0);
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

            // Now we treat each component as a separate network.
            for (LinkedList<Node> component : components) {

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
                
                // We can already get the indegree and outdegree while we 
                // make the adjacency matrix
                double[] indegree = new double[component.size()];
                double[] outdegree = new double[component.size()];

                // Now we iterate through all nodes
                for (int i = 0; i < component.size(); i++) {
                    // First initialize the attribute
                    nodes[i].setAttribute(col, 0.0);
                    // Now let us find all targets of this node and iterate through them
                    for (int j = 0; j < component.size(); j++) {
                        if (hgraph.getEdge(nodes[i], nodes[j]) != null) {
                            // Let's get the weight of the edge between these nodes
                            double weight = hgraph.getEdge(nodes[i], nodes[j]).getWeight();
                            outdegree[i] += weight;
                            indegree[j] += weight;
                            // And then fill the corresponding cell of the matrix
                            adj_mat[i][j] = weight;
                        }
                    }
                }

                // Let us create the v and w variables
                double[] vA = new double[component.size()];
                double[] wA = new double[component.size()];
                for (int i = 0; i < component.size(); i++) {
                    vA[i] = indegree[i] - outdegree[i];
                    wA[i] = indegree[i] + outdegree[i];
                }
                Vector v = new BasicVector(vA);
                Vector w = new BasicVector(wA);

                // Let's create the diagonal matrix from w.
                Matrix diag = new Basic2DMatrix(component.size(),component.size());
                for (int i = 0; i < component.size(); i++) {
                    diag.set(i, i, w.get(i));
                }

                // Let us now create sparse matrices from our results.
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
                   nodes[i].setAttribute(col, results[i]);
                }
                // What the report is upon success.
                report = "<HTML? <BODY> <h1>Trophic levels</h1> "
                + "<hr>"
                + "<br> The results are reported in the TrophicLevels column (see data laboratory). <br />"
                + "<br> <br />"
                + "</BODY></HTML>";
                }
            }
        else {
            // Report if graph is undirected.
            report = "<HTML? <BODY> <h1>Trophic levels</h1> "
            + "<hr>"
            + "<br> The Trophic Levels metric only applies to directed graphs. <br />"
            + "<br> <br />"
            + "</BODY></HTML>";
        }
        // Unlock the graph after we're finished.
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
