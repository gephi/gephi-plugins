/* Copyright 2015 Wouter Spekkink
Authors : Wouter Spekkink <wouterspekkink@gmail.com>
Website : http://www.wouterspekkink.org
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
Copyright 2015 Wouter Spekkink. All rights reserved.
The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License. When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
Contributor(s): Wouter Spekkink

The plugin makes use of the MDSJ library, which is available under the Creative Commons License "by-nc-sa" 3.0.
Link to license: http://creativecommons.org/licenses/by-nc-sa/3.0/
Ref: "Algorithmics Group. MDSJ: Java Library for Multidimensional Scaling (Version 0.2). 
Available at http://www.inf.uni-konstanz.de/algo/software/mdsj/. University of Konstanz, 2009."

*/
package org.wouterspekkink.plugins.metric.mdsstatistics;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Edge;
import org.gephi.statistics.spi.Statistics;
import mdsj.MDSJ;
import mdsj.StressMinimization;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.EdgeIterable;
import org.gephi.graph.api.GraphController;
import org.openide.util.Lookup;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;


/**
 * The plugin makes use of the MDSJ library, which is available under the Creative Commons License "by-nc-sa" 3.0.
 * Link to license: http://creativecommons.org/licenses/by-nc-sa/3.0/
 * Ref: "Algorithmics Group. MDSJ: Java Library for Multidimensional Scaling (Version 0.2). 
 * Available at http://www.inf.uni-konstanz.de/algo/software/mdsj/. University of Konstanz, 2009."
 *
 * For the calculation of shortest paths the plugin uses the algorithm originally used by Gephi as a step in
 * the calculation of centrality metrics.
 * 
 * @author wouter
 */


public class MdsStatistics implements Statistics, LongTask {
    
    /* Currently I am assuming that we're working with only two dimensions
    Later I might want to add an iterator to determine the number of
    dimensions to be used. */
    
    public static final String DIM_1 = "Dimension_1";
    public static final String DIM_2 = "Dimension_2";
    public static final String DIM_3 = "Dimension_3";
    public static final String DIM_4 = "Dimension_4";
    public static final String DIM_5 = "Dimension_5";
    public static final String DIM_6 = "Dimension_6";
    public static final String DIM_7 = "Dimension_7";
    public static final String DIM_8 = "Dimension_8";
    public static final String DIM_9 = "Dimension_9";
    public static final String DIM_10 = "Dimension_10";
    public static final String WEIGHT = "Weight";
    
    private double stress;
        
    private int N;
    
    //Doesn't do anything now.
    private boolean isDirected;

    private boolean useDissimilarity = false;
    private boolean useSimilarity = false;
    private String reportString = "";
    private String reportStringTwo = "";

    //Distance matrix for path distances
    double [][] pathDistances;
    
    //Weight matrix for MDS calculations; check whether it works as expected.
    double [][] weightMatrix; 
    
    //When using weights for distances
    double [][] weightDist;
    
    private ProgressTicket progress;
    private boolean isCanceled;
    
    int distanceWeight = 0;

    int numberDimensions = 2;
    
    public MdsStatistics() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getGraphModel()!= null) {
            isDirected = graphController.getGraphModel().isDirected();
        }
    }

    @Override
    public void execute(GraphModel graphModel) {
        Graph graph = null;
        isDirected = graphModel.isDirected();
        if (isDirected) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }
        execute(graph, graphModel);
    }
  
    public void execute(Graph hgraph, GraphModel graphModel) {
        isCanceled = false;
        //Look if the result column already exist and create it if needed
        Table nodeTable = graphModel.getNodeTable();
        Table edgeTable = graphModel.getEdgeTable();
        Column col1 = nodeTable.getColumn(DIM_1);
        Column col2 = nodeTable.getColumn(DIM_2);
        Column col3 = nodeTable.getColumn(DIM_3);
        Column col4 = nodeTable.getColumn(DIM_4);
        Column col5 = nodeTable.getColumn(DIM_5);
        Column col6 = nodeTable.getColumn(DIM_6);
        Column col7 = nodeTable.getColumn(DIM_7);
        Column col8 = nodeTable.getColumn(DIM_8);
        Column col9 = nodeTable.getColumn(DIM_9);
        Column col10 = nodeTable.getColumn(DIM_10);
        Column weight = edgeTable.getColumn(WEIGHT);
        if (numberDimensions == 2) {
            if(col1 == null) {
                col1 = nodeTable.addColumn(DIM_1, "Dimension_1", Double.class, 0.0);
            }
            if(col2 == null) {
                col2 = nodeTable.addColumn(DIM_2, "Dimension_2", Double.class, 0.0);
            }  
            if(col3 != null) {
                nodeTable.removeColumn(col3);
                col3 = null;
            }
            if(col4 != null) {
                nodeTable.removeColumn(col4);
                col4 = null;
            }
            if(col5 != null) {
                nodeTable.removeColumn(col5);
                col5 = null;
            }
            if(col6 != null) {
                nodeTable.removeColumn(col6);
                col6 = null;
            }
            if(col7 != null) {
                nodeTable.removeColumn(col7);
                col7 = null;
            }
            if(col8 != null) {
                nodeTable.removeColumn(col8);
                col8 = null;
            }
            if(col9 != null) {
                nodeTable.removeColumn(col9);
                col9 = null;
            }
            if(col10 != null) {
                nodeTable.removeColumn(col10);
                col10 = null;
            }
        } else if (numberDimensions == 3) {
            if(col1 == null) {
                col1 = nodeTable.addColumn(DIM_1, "Dimension_1", Double.class, 0.0);
            }
            if(col2 == null)  {   
                col2 = nodeTable.addColumn(DIM_2, "Dimension_2", Double.class, 0.0);
            }
            if(col3 == null) {
                col3 = nodeTable.addColumn(DIM_3, "Dimension_3", Double.class, 0.0);
            }
            if(col4 != null) {
                nodeTable.removeColumn(col4);
                col4 = null;
            }
            if(col5 != null) {
                nodeTable.removeColumn(col5);
                col5 = null;
            }
            if(col6 != null) {
                nodeTable.removeColumn(col6);
                col6 = null;
            }
            if(col7 != null) {
                nodeTable.removeColumn(col7);
                col7 = null;
            }
            if(col8 != null) {
                nodeTable.removeColumn(col8);
                col8 = null;
            }
            if(col9 != null) {
                nodeTable.removeColumn(col9);
                col9 = null;
            }
            if(col10 != null) {
                nodeTable.removeColumn(col10);
                col10 = null;
            }
        } else if (numberDimensions == 4) {
            if(col1 == null) {
                col1 = nodeTable.addColumn(DIM_1, "Dimension_1", Double.class, 0.0);
            }
            if(col2 == null) {
                col2 = nodeTable.addColumn(DIM_2, "Dimension_2", Double.class, 0.0);
            }
            if(col3 == null) {
                col3 = nodeTable.addColumn(DIM_3, "Dimension_3", Double.class, 0.0);
            }
            if(col4 == null) {
                col4 = nodeTable.addColumn(DIM_4, "Dimension_4", Double.class, 0.0);
            }
            if(col5 != null) {
                nodeTable.removeColumn(col5);
                col5 = null;
            }
            if(col6 != null) {
                nodeTable.removeColumn(col6);
                col6 = null;
            }
            if(col7 != null) {
                nodeTable.removeColumn(col7);
                col7 = null;
            }
            if(col8 != null) {
                nodeTable.removeColumn(col8);
                col8 = null;
            }
            if(col9 != null) {
                nodeTable.removeColumn(col9);
                col9 = null;
            }
            if(col10 != null) {
                nodeTable.removeColumn(col10);
                col10 = null;
            }
        } else if (numberDimensions == 5) {
            if(col1 == null) {
                col1 = nodeTable.addColumn(DIM_1, "Dimension_1", Double.class, 0.0);
            }
            if(col2 == null) {
                col2 = nodeTable.addColumn(DIM_2, "Dimension_2", Double.class, 0.0);
            }
            if(col3 == null) {
                col3 = nodeTable.addColumn(DIM_3, "Dimension_3", Double.class, 0.0);
            }
            if(col4 == null) {
                col4 = nodeTable.addColumn(DIM_4, "Dimension_4", Double.class, 0.0);
            }
            if(col5 == null) {
                col5 = nodeTable.addColumn(DIM_5, "Dimension_5", Double.class, 0.0);
            }
            if(col6 != null) {
                nodeTable.removeColumn(col6);
                col6 = null;
            }
            if(col7 != null) {
                nodeTable.removeColumn(col7);
                col7 = null;
            }
            if(col8 != null) {
                nodeTable.removeColumn(col8);
                col8 = null;
            }
            if(col9 != null) {
                nodeTable.removeColumn(col9);
                col9 = null;
            }
            if(col10 != null) {
                nodeTable.removeColumn(col10);
                col10 = null;
            }
        } else if (numberDimensions == 6) {
            if(col1 == null) {    
                col1 = nodeTable.addColumn(DIM_1, "Dimension_1", Double.class, 0.0);
            }
            if(col2 == null) { 
                col2 = nodeTable.addColumn(DIM_2, "Dimension_2", Double.class, 0.0);
            }
            if(col3 == null) { 
                col3 = nodeTable.addColumn(DIM_3, "Dimension_3", Double.class, 0.0);
            }
            if(col4 == null) {
                col4 = nodeTable.addColumn(DIM_4, "Dimension_4", Double.class, 0.0);
            }
            if(col5 == null) {
                col5 = nodeTable.addColumn(DIM_5, "Dimension_5", Double.class, 0.0);
            }
            if(col6 == null) {
                col6 = nodeTable.addColumn(DIM_6, "Dimension_6", Double.class, 0.0);
            }
            if(col7 != null) {
                nodeTable.removeColumn(col7);
                col7 = null;
            }
            if(col8 != null) {
                nodeTable.removeColumn(col8);
                col8 = null;
            }
            if(col9 != null) {
                nodeTable.removeColumn(col9);
                col9 = null;
            }
            if(col10 != null) {
                nodeTable.removeColumn(col10);
                col10 = null;
            }
        } else if (numberDimensions == 7) {
            if(col1 == null) {
                col1 = nodeTable.addColumn(DIM_1, "Dimension_1", Double.class, 0.0);
            }
            if(col2 == null) {
                col2 = nodeTable.addColumn(DIM_2, "Dimension_2", Double.class, 0.0);
            }
            if(col3 == null) {
                col3 = nodeTable.addColumn(DIM_3, "Dimension_3", Double.class, 0.0);
            }
            if(col4 == null) {
                col4 = nodeTable.addColumn(DIM_4, "Dimension_4", Double.class, 0.0);
            }
            if(col5 == null) {
                col5 = nodeTable.addColumn(DIM_5, "Dimension_5", Double.class, 0.0);
            }
            if(col6 == null) {
                col6 = nodeTable.addColumn(DIM_6, "Dimension_6", Double.class, 0.0);
            }
            if(col7 == null) {
                col7 = nodeTable.addColumn(DIM_7, "Dimension_7", Double.class, 0.0);
            }
            if(col8 != null) {
                nodeTable.removeColumn(col8);
                col8 = null;
            }
            if(col9 != null) {
                nodeTable.removeColumn(col9);
                col9 = null;
            }
            if(col10 != null) {
                nodeTable.removeColumn(col10);
                col10 = null;
            }
        } else if (numberDimensions == 8) {
            if(col1 == null) {
                col1 = nodeTable.addColumn(DIM_1, "Dimension_1", Double.class, 0.0);
            }
            if(col2 == null) {
                col2 = nodeTable.addColumn(DIM_2, "Dimension_2", Double.class, 0.0);
            }
            if(col3 == null) {
                col3 = nodeTable.addColumn(DIM_3, "Dimension_3", Double.class, 0.0);
            }
            if(col4 == null) {
                col4 = nodeTable.addColumn(DIM_4, "Dimension_4", Double.class, 0.0);
            }
            if(col5 == null) {
                col5 = nodeTable.addColumn(DIM_5, "Dimension_5", Double.class, 0.0);
            }
            if(col6 == null) {
                col6 = nodeTable.addColumn(DIM_6, "Dimension_6", Double.class, 0.0);
            }
            if(col7 == null) {
                col7 = nodeTable.addColumn(DIM_7, "Dimension_7", Double.class, 0.0);
            }
            if(col8 == null) {
                col8 = nodeTable.addColumn(DIM_8, "Dimension_8", Double.class, 0.0);
            }
            if(col9 != null) {
                nodeTable.removeColumn(col9);
                col9 = null;
            }
            if(col10 != null) {
                nodeTable.removeColumn(col10);
                col10 = null;
            }
        } else if (numberDimensions == 9) {
            if(col1 == null) {
                col1 = nodeTable.addColumn(DIM_1, "Dimension_1", Double.class, 0.0);
            }
            if(col2 == null) {
                col2 = nodeTable.addColumn(DIM_2, "Dimension_2", Double.class, 0.0);
            }
            if(col3 == null) {
                col3 = nodeTable.addColumn(DIM_3, "Dimension_3", Double.class, 0.0);
            }
            if(col4 == null) {
                col4 = nodeTable.addColumn(DIM_4, "Dimension_4", Double.class, 0.0);
            }
            if(col5 == null) {
                col5 = nodeTable.addColumn(DIM_5, "Dimension_5", Double.class, 0.0);
            }
            if(col6 == null) {
                col6 = nodeTable.addColumn(DIM_6, "Dimension_6", Double.class, 0.0);
            }
            if(col7 == null) {
                col7 = nodeTable.addColumn(DIM_7, "Dimension_7", Double.class, 0.0);
            }
            if(col8 == null) {
                col8 = nodeTable.addColumn(DIM_8, "Dimension_8", Double.class, 0.0);
            }
            if(col9 == null) {
                col9 = nodeTable.addColumn(DIM_9, "Dimension_9", Double.class, 0.0);
            }
            if(col10 != null) {
                nodeTable.removeColumn(col10);
                col10 = null;
            }
        } else if (numberDimensions == 10) {
            if(col1 == null) {
                col1 = nodeTable.addColumn(DIM_1, "Dimension_1", Double.class, 0.0);
            }
            if(col2 == null) {
                col2 = nodeTable.addColumn(DIM_2, "Dimension_2", Double.class, 0.0);
            }
            if(col3 == null) {
                col3 = nodeTable.addColumn(DIM_3, "Dimension_3", Double.class, 0.0);
            }
            if(col4 == null) {
                col4 = nodeTable.addColumn(DIM_4, "Dimension_4", Double.class, 0.0);
            }
            if(col5 == null) {
                col5 = nodeTable.addColumn(DIM_5, "Dimension_5", Double.class, 0.0);
            }
            if(col6 == null) {
                col6 = nodeTable.addColumn(DIM_6, "Dimension_6", Double.class, 0.0);
            }
            if(col7 == null) {
                col7 = nodeTable.addColumn(DIM_7, "Dimension_7", Double.class, 0.0);
            }
            if(col8 == null) {
                col8 = nodeTable.addColumn(DIM_8, "Dimension_8", Double.class, 0.0);
            }
            if(col9 == null) {
                col9 = nodeTable.addColumn(DIM_9, "Dimension_9", Double.class, 0.0);
            }
            if(col10 == null) {
                col10 = nodeTable.addColumn(DIM_10, "Dimension_10", Double.class, 0.0);
            }
        }

        //Lock to graph. This is important to have consistent results if another
        //process is currently modifying it.
        hgraph.readLock();
        
        N = hgraph.getNodeCount();
        HashMap<Node, Integer> indicies = createIndiciesMap(hgraph);
                
        pathDistances = new double[N][N];
        weightMatrix = new double[N][N];
        // Initialize the path distances
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                pathDistances[i][j] = 0;
            }
        }
            
        //Check whether the length-method works like it should.
        pathDistances = calculateDistanceMetrics(hgraph, indicies, isDirected);

        //Handle (dis)similarities
        if(useSimilarity || useDissimilarity) {
            double maxValue = 0;
                
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if(pathDistances[i][j] > maxValue) {
                        maxValue = pathDistances[i][j];
                    }
                }    
            }
            double minValue = maxValue;
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if(pathDistances[i][j] < minValue && pathDistances[i][j] > 0) {
                        minValue = pathDistances[i][j];
                    }
                }    
            }
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    if (pathDistances[i][j] > 0) {
                        pathDistances[i][j] = 1 + ((pathDistances[i][j] - minValue) * (2 - 1)) / (maxValue - minValue);
                    }
                }
            }
            if(useSimilarity) {
                for (int i = 0; i < N; i ++) {
                    for (int j = 0; j < N; j++) {
                        if(pathDistances[i][j] > 0) {
                            pathDistances[i][j] = 3 - pathDistances[i][j];
                        }
                    }
                }
            }
        }
       
        //Need something here to calculate coordinates
        weightMatrix = StressMinimization.weightMatrix(pathDistances, distanceWeight);
        double output [][] = MDSJ.stressMinimization(pathDistances, weightMatrix, numberDimensions);
        stress = StressMinimization.normalizedStress(pathDistances, weightMatrix, output);
          
        //Iterate on all nodes and store coordinates
        for (Node n : hgraph.getNodes()) {
            int n_index = indicies.get(n);
            if (numberDimensions == 2) {
                n.setAttribute(col1, output[0][n_index]);
                n.setAttribute(col2, output[1][n_index]);
            } else if (numberDimensions == 3) {
                    n.setAttribute(col1, output[0][n_index]);
                    n.setAttribute(col2, output[1][n_index]);
                    n.setAttribute(col3, output[2][n_index]);
            } else if (numberDimensions == 4) {
                    n.setAttribute(col1, output[0][n_index]);
                    n.setAttribute(col2, output[1][n_index]);
                    n.setAttribute(col3, output[2][n_index]);
                    n.setAttribute(col4, output[3][n_index]);
            } else if (numberDimensions == 5) {
                    n.setAttribute(col1, output[0][n_index]);
                    n.setAttribute(col2, output[1][n_index]);
                    n.setAttribute(col3, output[2][n_index]);
                    n.setAttribute(col4, output[3][n_index]);
                    n.setAttribute(col5, output[4][n_index]);
            }  else if (numberDimensions == 6) {
                    n.setAttribute(col1, output[0][n_index]);
                    n.setAttribute(col2, output[1][n_index]);
                    n.setAttribute(col3, output[2][n_index]);
                    n.setAttribute(col4, output[3][n_index]);
                    n.setAttribute(col5, output[4][n_index]);
            }  else if (numberDimensions == 7) {
                    n.setAttribute(col1, output[0][n_index]);
                    n.setAttribute(col2, output[1][n_index]);
                    n.setAttribute(col3, output[2][n_index]);
                    n.setAttribute(col4, output[3][n_index]);
                    n.setAttribute(col5, output[4][n_index]);
                    n.setAttribute(col6, output[5][n_index]);
                    n.setAttribute(col7, output[6][n_index]);
            }  else if (numberDimensions == 8) {
                    n.setAttribute(col1, output[0][n_index]);
                    n.setAttribute(col2, output[1][n_index]);
                    n.setAttribute(col3, output[2][n_index]);
                    n.setAttribute(col4, output[3][n_index]);
                    n.setAttribute(col5, output[4][n_index]);
                    n.setAttribute(col6, output[5][n_index]);
                    n.setAttribute(col7, output[6][n_index]);
                    n.setAttribute(col8, output[7][n_index]);
            }  else if (numberDimensions == 9) {
                    n.setAttribute(col1, output[0][n_index]);
                    n.setAttribute(col2, output[1][n_index]);
                    n.setAttribute(col3, output[2][n_index]);
                    n.setAttribute(col4, output[3][n_index]);
                    n.setAttribute(col5, output[4][n_index]);
                    n.setAttribute(col6, output[5][n_index]);
                    n.setAttribute(col7, output[6][n_index]);
                    n.setAttribute(col8, output[7][n_index]);
                    n.setAttribute(col9, output[8][n_index]);
            }  else if (numberDimensions == 10) {
                    n.setAttribute(col1, output[0][n_index]);
                    n.setAttribute(col2, output[1][n_index]);
                    n.setAttribute(col3, output[2][n_index]);
                    n.setAttribute(col4, output[3][n_index]);
                    n.setAttribute(col5, output[4][n_index]);
                    n.setAttribute(col6, output[5][n_index]);
                    n.setAttribute(col7, output[6][n_index]);
                    n.setAttribute(col8, output[7][n_index]);
                    n.setAttribute(col9, output[8][n_index]);
                    n.setAttribute(col10, output[9][n_index]);
            }
        }
        hgraph.readUnlock();
    }
        
    // Let's see how I can use the stuff below
    //public Map<String, double[]> calculateDistanceMetrics(Graph hgraph, HashMap<Node, Integer> indicies, boolean directed, boolean normalized) {
    // Does not work yet.
    public double[][] calculateDistanceMetrics(Graph hgraph, HashMap<Node, Integer> indicies, boolean directed) {
        int n = hgraph.getNodeCount();
        
        double [][] distances = new double[n][n];
        
        Progress.start(progress, hgraph.getNodeCount());
        int count = 0;
        
        for (Node s : hgraph.getNodes()) {
            Stack<Node> S = new Stack<Node>();

            LinkedList<Node>[] P = new LinkedList[n];
            double[] d = new double[n];
            
            int s_index = indicies.get(s);
            
            setInitParametersForNode(s, P, d, s_index, n);

            LinkedList<Node> Q = new LinkedList<Node>();
            Q.addLast(s);
            while (!Q.isEmpty()) {
                Node v = Q.removeFirst();
                S.push(v);
                int v_index = indicies.get(v);

                EdgeIterable edgeIter = getEdgeIter(hgraph, v, directed);
                    
                for (Edge edge : edgeIter) {
                    Node reachable = hgraph.getOpposite(v, edge);
                    double currentWeight = edge.getWeight();
                    int r_index = indicies.get(reachable);
                    if (d[r_index] < 0) {
                        Q.addLast(reachable);
                        if (useDissimilarity || useSimilarity) {
                            d[r_index] = d[v_index] + currentWeight;
                        } else {
                            d[r_index] = d[v_index] + 1;
                        }
                    }
                }
            }
            for (int i = 0; i < n; i++) {              
                if (d[i] > 0) {
                    distances[s_index][i] = d[i];
                }
            }
            count++;
            if(isCanceled) {
                hgraph.readUnlockAll();
                return distances;
            }
            Progress.progress(progress, count);
        }
        return distances;
    }
    private void setInitParametersForNode(Node s, LinkedList<Node>[] P, double[] d, int index, int n) {           
            for (int j = 0; j < n; j++) {
                P[j] = new LinkedList<Node>();
                d[j] = -1;
            }
            d[index] = 0;
    }
    
    private EdgeIterable getEdgeIter(Graph hgraph, Node v, boolean directed) {
            EdgeIterable edgeIter = null;
            if (directed) {
                edgeIter = ((DirectedGraph) hgraph).getEdges(v);
            } else {
                edgeIter = hgraph.getEdges(v);
            }
            return edgeIter;
    }
    
    public  HashMap<Node, Integer> createIndiciesMap(Graph hgraph) {
       HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();
        int index = 0;
        for (Node s : hgraph.getNodes()) {
            indicies.put(s, index);
            index++;
        } 
        return indicies;
    }
     
    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }
    
    public void setDissimilarity(boolean useDissimilarity) {
        this.useDissimilarity = useDissimilarity;
        if (useDissimilarity == true) {
            reportString = "Weights interpreted as distances / dissimilarities.";
        }
    }
    
    public void setSimilarity(boolean useSimilarity) {
        this.useSimilarity = useSimilarity;
        if (useSimilarity == true) {
            reportString = "Weights interpreted as proximities / similarities.";
        }
    }
    
    public boolean isDissimilarity() {
        return useDissimilarity;
    }
    
    public boolean isSimilarity() {
        return useSimilarity;
    }
    
    public void setNoWeights(boolean useNoWeights) {
        if(useNoWeights == true) {
            this.useDissimilarity = false;
            this.useSimilarity = false;
            reportString = "Regular (unweighted) path distances used.";
        }
    }
    
    public void setDistanceWeight(int weight) {
        distanceWeight = weight;
        if(weight == 0) {
            reportStringTwo = "(all distances treated equally)";
        } else if (weight == -2) {
            reportStringTwo = "(large distances downweighted and small distances upweighted)";
        }
    }
    
    public int getDistanceWeight() {
        return distanceWeight;
    }
    
    public void setNumberDimensions(int number)  {
        numberDimensions =  number;
    }           
    
    public int getNumberDimensions() {
        return numberDimensions;
    }
      
    @Override
    public String getReport() {
        //This is the HTML report shown when execution ends. 
        //One could add a distribution histogram for instance
        String report = "<HTML> <BODY> <h1>Stress value</h1> "
                + "<hr>"
                + "<br> Stress of this MDS configuration: " + stress +"<br />"
                + "<br>" + reportStringTwo + "<br />"
                + "<br> <br />"
                + "<br>" + reportString + "<br />"
                + "<br> <br />"
                + "<br> Number of dimensions of the resulting configuration: " + numberDimensions + ".<br />"
                + "<br> <br />"
                + "</BODY></HTML>";
        return report;
    }
    
    @Override
    public boolean cancel() {
        this.isCanceled = true;
        return true;
    }
    
    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
    this.progress  = progressTicket;
    }
}
