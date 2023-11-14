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
package org.gephi.plugins.prestige.calculation;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Logger;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.plugins.prestige.util.GraphUtil;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public class ProximityCalculator extends CancableCalculation {

    private static final Logger LOG = Logger.getLogger(ProximityCalculator.class.getName());
    public static final String PROXIMITY_KEY = "pr_proximity";

    @Override
    protected String[] getNodePropertyNames() {
        return new String[]{PROXIMITY_KEY};
    }

    @Override
    protected SortedMap<Double, Integer> calculateNodeMetrics(Graph graph, ProgressTicket pt) {
        LOG.info("Start calculating proximity");
        Progress.setDisplayName(pt, "Proximity Prestige calculations");
        graph.readLock();
        Iterator<Node> it = graph.getNodes().iterator();
        while (!isCanceled() && it.hasNext()) {
            Node node = it.next();
            calculateProximityPrestigeForNode(node, graph);
        }
        graph.readUnlock();
        LOG.info("Finished calculating proximity");
        return aggregateStatistics(graph, pt, PROXIMITY_KEY);
    }

    private void calculateProximityPrestigeForNode(Node node, Graph graph) {
        Map<Node, Integer> distances = Maps.newHashMap();
        // Attention, adding source node to avoid loops. Consider later!
        distances.put(node, 0);
        Set<Node> nextNodes = Sets.newHashSet(node);
        while (!isCanceled() && !nextNodes.isEmpty()) {
            nextNodes = GraphUtil.getReferencingNodes(nextNodes, graph, distances);
        }
        // All incomming nodes with shortest distances stored in distance-map
        // |I| / N-1
        double prox = 0D;
        // Calculate if there are referencing nodes
        if (distances.keySet().size() > 1) {
            double above = (double) (distances.keySet().size() - 1) / (double) (graph.getNodeCount() - 1);
            double below = (double) (pathLength(distances)) / (double) (distances.keySet().size() - 1);
            prox = above / below;
        }

        node.setAttribute(PROXIMITY_KEY, prox);
    }

    private int pathLength(Map<Node, Integer> distances) {
        int pathLength = 0;
        for (Integer dist : distances.values()) {
            pathLength += dist;
        }
        return pathLength;
    }

}
