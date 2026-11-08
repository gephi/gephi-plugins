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

 */
package org.wouterspekkink.plugins.metric.lineage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.gephi.graph.api.Column;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;

import org.gephi.graph.api.NodeIterable;
import org.gephi.statistics.spi.Statistics;
import org.openide.util.Lookup;

/**
 * @author wouter
 */
public class Lineage implements Statistics {

    public static final String LINEAGE = "Lineage";
    public static final String ORIGIN = "IsOrigin";
    public static final String ANCESTOR = "IsAncestor";
    public static final String DESCENDANT = "IsDescendant";
    public static final String ADISTANCE = "DistanceAncestor";
    public static final String DDISTANCE = "DistanceDescendant";
    private String originName = "";
    private int counterA = -1;
    private int counterD = 1;
    Node origin;
    private boolean isDirected;
    boolean[] nodeAncestors;
    boolean foundNode = false;
    private boolean nodesLeftAnc = true;
    private boolean nodesLeftDes = true;

    public Lineage() {
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

    // Create all variable columns and initialize them
    // Columns will be removed (overwritten) if they are alread there.
    public void execute(Graph hgraph, GraphModel graphModel) {
        //Look if the result column already exist and create it if needed
        Table nodeTable = graphModel.getNodeTable();
        Column col = nodeTable.getColumn(LINEAGE);
        Column col1 = nodeTable.getColumn(ORIGIN);
        Column col2 = nodeTable.getColumn(ANCESTOR);
        Column col3 = nodeTable.getColumn(DESCENDANT);
        Column col4 = nodeTable.getColumn(ADISTANCE);
        Column col5 = nodeTable.getColumn(DDISTANCE);

        if (col == null) {
            col = nodeTable.addColumn(LINEAGE, "Lineage", String.class, "Unrelated");
        }
        if (col1 == null) {
            col1 = nodeTable.addColumn(ORIGIN, "IsOrigin", Boolean.class, false);
        }
        if (col2 == null) {
            col2 = nodeTable.addColumn(ANCESTOR, "IsAncestor", Boolean.class, false);
        }
        if (col3 == null) {
            col3 = nodeTable.addColumn(DESCENDANT, "IsDescendant", Boolean.class, false);
        }
        if (col4 == null) {
            col4 = nodeTable.addColumn(ADISTANCE, "DistanceAncestor", Integer.class, 0);
        }
        if (col5 == null) {
            col5 = nodeTable.addColumn(DDISTANCE, "DistanceDescendant", Integer.class, 0);
        }

        hgraph.readLock();

        for (Node n : hgraph.getNodes()) {
            n.setAttribute(col, "Unrelated");
            n.setAttribute(col2, false);
            n.setAttribute(col3, false);
            n.setAttribute(col4, 0);
            n.setAttribute(col5, 0);
        }

        // First let's find the origin that is submitted by the user and we'll only run the rest of the plugin if the origin is found.
        for (Node n : hgraph.getNodes()) {
            //NodeData info = n.getNodeData();
            //String tempName = info.getId();
            String tempName = (String) n.getId();
            if (tempName == null ? originName == null : tempName.equals(originName)) {
                origin = n;
                foundNode = true;
            }
        }

        // We only run the algorithm if an appropriate origin node was submitted by the user.
        if (foundNode) {
            origin.setAttribute(col, "Origin");
            origin.setAttribute(col1, true);
            List<Node> doNodesAnc = new CopyOnWriteArrayList<Node>();
            List<Node> doNodesDes = new CopyOnWriteArrayList<Node>();

            NodeIterable nodeIterAnc = getNodeIterAnc(hgraph, origin);
            NodeIterable nodeIterDes = getNodeIterDes(hgraph, origin);

            for (Node node : nodeIterAnc) {
                if (node.getAttribute(LINEAGE).equals("Unrelated")) {
                    doNodesAnc.add(node);
                    node.setAttribute(col, "Ancestor");
                    node.setAttribute(col2, true);
                    node.setAttribute(col4, counterA);
                } else if (node.getAttribute(LINEAGE).equals("Descendant")) {
                    node.setAttribute(col, "Hybrid");
                    node.setAttribute(col2, true);
                    node.setAttribute(col4, counterA);
                }
            }

            for (Node node : nodeIterDes) {
                if (node.getAttribute(LINEAGE).equals("Unrelated")) {
                    doNodesDes.add(node);
                    node.setAttribute(col, "Descendant");
                    node.setAttribute(col3, true);
                    node.setAttribute(col5, counterD);
                } else if (node.getAttribute(LINEAGE).equals("Ancestor")) {
                    node.setAttribute(col, "Hybrid");
                    node.setAttribute(col3, true);
                    node.setAttribute(col5, counterD);
                }
            }

            while (nodesLeftAnc) {
                if (doNodesAnc.isEmpty()) {
                    nodesLeftAnc = false;
                } else {
                    counterA -= 1;
                    for (Node node : doNodesAnc) {
                        NodeIterable nodeIterTwo = getNodeIterAnc(hgraph, node);
                        for (Node nodeTwo : nodeIterTwo) {
                            if (nodeTwo.getAttribute(LINEAGE).equals("Unrelated")) {
                                nodeTwo.setAttribute(col, "Ancestor");
                                nodeTwo.setAttribute(col2, true);
                                nodeTwo.setAttribute(col4, counterA);
                                doNodesAnc.add(nodeTwo);
                            } else if (nodeTwo.getAttribute(LINEAGE).equals("Descendant")) {
                                nodeTwo.setAttribute(col, "Hybrid");
                                nodeTwo.setAttribute(col2, true);
                                nodeTwo.setAttribute(col4, counterA);
                                doNodesDes.add(nodeTwo);
                            }
                        }
                        doNodesAnc.remove(node);
                    }
                }
            }

            while (nodesLeftDes) {
                if (doNodesDes.isEmpty()) {
                    nodesLeftDes = false;
                } else {
                    counterD += 1;
                    for (Node node : doNodesDes) {

                        NodeIterable nodeIterTwo = getNodeIterDes(hgraph, node);
                        for (Node nodeTwo : nodeIterTwo) {
                            if (nodeTwo.getAttribute(LINEAGE).equals("Unrelated")) {
                                nodeTwo.setAttribute(col, "Descendant");
                                nodeTwo.setAttribute(col3, true);
                                nodeTwo.setAttribute(col5, counterD);
                                doNodesDes.add(nodeTwo);
                            } else if (nodeTwo.getAttribute(LINEAGE).equals("Ancestor")) {
                                nodeTwo.setAttribute(col, "Hybrid");
                                nodeTwo.setAttribute(col3, true);
                                nodeTwo.setAttribute(col5, counterD);
                                doNodesDes.add(nodeTwo);
                            }
                        }
                        doNodesDes.remove(node);
                    }
                }
            }

        }
        hgraph.readUnlock();
    }

    private NodeIterable getNodeIterDes(Graph thisGraph, Node n) {
        NodeIterable nodeIter;
        nodeIter = ((DirectedGraph) thisGraph).getSuccessors(n);
        return nodeIter;
    }

    private NodeIterable getNodeIterAnc(Graph thisGraph, Node n) {
        NodeIterable nodeIter;
        nodeIter = ((DirectedGraph) thisGraph).getPredecessors(n);
        return nodeIter;
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public void setOrigin(String inputOrigin) {
        originName = inputOrigin;
    }

    public String getOrigin() {
        return originName;
    }

    @Override
    public String getReport() {
        //This is the HTML report shown when execution ends. 
        //One could add a distribution histogram for instance
        String report = "<HTML> <BODY> <h1>Stress value</h1> "
                + "<hr>"
                + "<br> The results are reported in the Lineage column (see data laboratory)<br />"
                + "<br> <br />"
                + "</BODY></HTML>";
        return report;
    }

}
