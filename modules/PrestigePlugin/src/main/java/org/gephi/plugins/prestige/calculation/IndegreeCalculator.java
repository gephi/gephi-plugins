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
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 * Calculates Indegree Prestige
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public class IndegreeCalculator extends CancableCalculation {

    private static final Logger LOG = Logger.getLogger(IndegreeCalculator.class.getName());

    // Constants
    public static final String INDEGREE_KEY = "pr_indegree";
    public static final String INDEGREE_NORMALIZED_KEY = "pr_indegree_normalized";

    @Override
    protected String[] getNodePropertyNames() {
        return new String[]{INDEGREE_KEY, INDEGREE_NORMALIZED_KEY};
    }

    @Override
    protected SortedMap<Double, Integer> calculateNodeMetrics(Graph graph, ProgressTicket pt) {
        LOG.info("Start indegree calculations");
        Progress.setDisplayName(pt, "Indegree Prestige calculations");
        graph.readLock();
        Iterator<Edge> it = graph.getEdges().iterator();
        Map<Node, Set<Node>> alreadyKnownSourceNodes = Maps.newHashMap();
        while (!isCanceled() && it.hasNext()) {
            Edge e = it.next();

            Set<Node> known = alreadyKnownSourceNodes.get(e.getTarget());
            if (known == null) {
                known = Sets.newHashSet(e.getTarget()); // Add target to targets source nodes to avoid counting self-loops
                alreadyKnownSourceNodes.put(e.getTarget(), known);
            }
            if (!known.contains(e.getSource())) {
                updateIndegreeCounter(e.getTarget(), graph.getNodeCount());
                known.add(e.getSource());
            }
        }
        graph.readUnlock();
        LOG.info("Finished indegree calculations");
        return aggregateStatistics(graph, pt, INDEGREE_KEY);
    }

    private void updateIndegreeCounter(Node target, int numNodes) {
        Double counter = (Double) target.getAttribute(INDEGREE_KEY);
        counter = counter == null ? 1 : 1 + counter;
        target.setAttribute(INDEGREE_KEY, counter);
        target.setAttribute(INDEGREE_NORMALIZED_KEY, counter / (numNodes - 1));
    }
}
