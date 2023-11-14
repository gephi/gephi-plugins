/* 
 * Copyright (C) 2016 Michael Henninger <gephi@michihenninger.ch>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.plugins.prestige.util;

import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public final class GraphUtil {

    private GraphUtil() {
    }

    private static Set<Node> getReferencingNeighbourNodes(Node target, Graph graph, Set<Node> alreadyVisited) {
        Set<Node> nextNextNodes = Sets.newHashSet();
        Iterator<Edge> edgeIt = graph.getEdges(target).iterator();
        while (edgeIt.hasNext()) {
            Edge e = edgeIt.next();
            // Only consider indegrees
            if (e.getTarget().equals(target)) {
                // Only consider if not already visited
                Node source = e.getSource();
                if (!alreadyVisited.contains(source)) {
                    nextNextNodes.add(source);
                }
            }
        }
        return nextNextNodes;
    }

    public static Set<Node> getReferencingNodes(Set<Node> targetNodes, Graph graph, Map<Node, Integer> distances) {
        Set<Node> nextNodes = Sets.newHashSet();
        for (Node n : targetNodes) {
            Integer dist = distances.get(n);
            Set<Node> neighbours = getReferencingNeighbourNodes(n, graph, distances.keySet());
            for (Node neighbour : neighbours) {
                distances.put(neighbour, dist + 1);
                nextNodes.add(neighbour);
            }
        }
        return nextNodes;
    }

}
