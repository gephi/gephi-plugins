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
 * Calculates Domain Prestige
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public class DomainCalculator extends CancableCalculation {

    private static final Logger LOG = Logger.getLogger(DomainCalculator.class.getName());
    public static final String DOMAIN_KEY = "pr_domain";

    @Override
    protected String[] getNodePropertyNames() {
        return new String[]{DOMAIN_KEY};
    }

    @Override
    protected SortedMap<Double, Integer> calculateNodeMetrics(Graph graph, ProgressTicket pt) {
        LOG.info("Start calculating domain prestige");
        Progress.setDisplayName(pt, "Domain Prestige calculations");
        graph.readLock();
        Iterator<Node> it = graph.getNodes().iterator();
        while (!isCanceled() && it.hasNext()) {
            Node node = it.next();
            calculateDomainPrestigeForNode(node, graph);
        }
        graph.readUnlock();
        LOG.info("Finished calculating domain prestige");
        return aggregateStatistics(graph, pt, DOMAIN_KEY);
    }

    private void calculateDomainPrestigeForNode(Node node, Graph graph) {
        Map<Node, Integer> known = Maps.newHashMap();

        // Attention, adding source node to avoid loops.
        known.put(node, 0);
        Set<Node> nextNodes = Sets.newHashSet(node);
        while (!isCanceled() && !nextNodes.isEmpty()) {
            nextNodes = GraphUtil.getReferencingNodes(nextNodes, graph, known);
        }

        double domain = 0D;
        // Calculate if there are referencing nodes (exept source node itself)
        if (known.size() > 1) {
            domain = (double) (known.size() - 1) / (double) (graph.getNodeCount() - 1);
        }

        node.setAttribute(DOMAIN_KEY, domain);
    }

}
