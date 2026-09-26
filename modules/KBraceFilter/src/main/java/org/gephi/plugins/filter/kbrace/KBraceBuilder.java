/*
 * Copyright 2013-2016 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gephi.plugins.filter.kbrace;

import java.util.*;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.plugin.AbstractFilter;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = FilterBuilder.class)
public class KBraceBuilder implements FilterBuilder {

    @Override
    public Category getCategory() {
        return FilterLibrary.TOPOLOGY;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(KBraceFilter.class, "KBraceFilter.name");
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(KBraceFilter.class, "KBraceFilter.description");
    }

    @Override
    public Filter getFilter(Workspace workspace) {
        return new KBraceFilter();
    }

    @Override
    public JPanel getPanel(Filter filter) {
        KBraceUI ui = Lookup.getDefault().lookup(KBraceUI.class);
        if (ui != null) {
            return ui.getPanel((KBraceFilter) filter);
        }
        return null;
    }

    @Override
    public void destroy(Filter filter) {
    }

    public static class KBraceFilter extends AbstractFilter implements ComplexFilter {

        private int k = 1;

        public KBraceFilter() {
            super(NbBundle.getMessage(KBraceFilter.class, "KBraceFilter.name"));

            addProperty(Integer.class, "k");
        }

        @Override
        public Graph filter(Graph graph) {
            Graph undirectedGraph = graph.getView().getGraphModel().getUndirectedGraph(graph.getView());
            LinkedList<Edge> queue = new LinkedList<Edge>();
            Map<Edge, Integer> edgeInter = new HashMap<Edge, Integer>();
            for (Edge e : undirectedGraph.getEdges().toArray()) {
                Node node1 = e.getSource();
                Node node2 = e.getTarget();
                int interseciton = neighborsIntersection(undirectedGraph, node1, node2).size();
                edgeInter.put(e, interseciton);
                if (interseciton < k) {
                    queue.add(e);
                    undirectedGraph.removeEdge(e);
                }
            }
            while (!queue.isEmpty()) {
                Edge e = queue.pop();
                Node node1 = e.getSource();
                Node node2 = e.getTarget();
                List<Node> interseciton = neighborsIntersection(undirectedGraph, node1, node2);
                for (Node n : interseciton) {
                    Edge adj1 = undirectedGraph.getEdge(node1, n);
                    int em1 = edgeInter.get(adj1) - 1;
                    edgeInter.put(adj1, em1);
                    if (em1 < k) {
                        queue.add(adj1);
                        undirectedGraph.removeEdge(adj1);
                    }
                    Edge adj2 = undirectedGraph.getEdge(node2, n);
                    int em2 = edgeInter.get(adj2) - 1;
                    edgeInter.put(adj2, em2);
                    if (em2 < k) {
                        queue.add(adj2);
                        undirectedGraph.removeEdge(adj2);
                    }
                }
            }
            for (Node n : undirectedGraph.getNodes().toArray()) {
                if (undirectedGraph.getDegree(n) == 0) {
                    undirectedGraph.removeNode(n);
                }
            }
            return graph;
        }

        private List<Node> neighborsIntersection(Graph graph, Node node1, Node node2) {
            List<Node> intersection = new ArrayList<Node>();
            if (node1 == node2) {
                return intersection;
            }

            for (Node neighbor : graph.getNeighbors(node1)) {
                //Test if neigbor connected to node2
                if (graph.isAdjacent(neighbor, node2)) {
                    intersection.add(neighbor);
                }
            }
            return intersection;
        }

        public Integer getK() {
            return k;
        }

        public void setK(Integer k) {
            this.k = k;
        }
    }
}
