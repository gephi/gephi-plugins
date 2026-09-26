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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.plugins.prestige.util.GraphUtil;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public class RankCalculator extends CancableCalculation {

    private static final Logger LOG = Logger.getLogger(RankCalculator.class.getName());

    public static final String RANK_KEY = "pr_rank";
    public static final String NORMALIZED_RANK_KEY = "pr_rank_min-max-normalized";
    private final String prominenceAttributeId;
    private final boolean doLogTransformation;
    private final double defaultProminence;
    private final GraphModel graphModel;

    public RankCalculator(String prominenceAttributeId, boolean doLogTransformation, double defaultProminence, GraphModel graphModel) {
        this.prominenceAttributeId = prominenceAttributeId;
        this.doLogTransformation = doLogTransformation;
        this.defaultProminence = defaultProminence;
        this.graphModel = graphModel;
    }

    @Override
    protected String[] getNodePropertyNames() {
        return new String[]{RANK_KEY, NORMALIZED_RANK_KEY};
    }

    @Override
    protected SortedMap<Double, Integer> calculateNodeMetrics(Graph graph, ProgressTicket pt) {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        LOG.info("Start calculating Rank Prestige");
        Progress.setDisplayName(pt, "Rank Prestige calculations");
        graph.readLock();
        Iterator<Node> it = graph.getNodes().iterator();
        LOG.info("Calculating Rank for every node");
        while (!isCanceled() && it.hasNext()) {
            Node node = it.next();
            double rank = calculateRankPrestigeForNode(node, graph);
            if (rank < min) {
                min = rank;
            }
            if (rank > max) {
                max = rank;
            }
        }

        // Do log transformation
        if (!isCanceled() && doLogTransformation) {
            LOG.info("Doing Log-Transfomration for every rank value");
            Iterator<Node> iter = graph.getNodes().iterator();
            while (iter.hasNext()) {
                Node n = iter.next();
                Double rankValue = (Double) n.getAttribute(RANK_KEY);
                if (rankValue != null) {
                    // Values <= 1 are mapped to 0
                    rankValue = rankValue <= 1D ? 0D : Math.log(rankValue);
                    n.setAttribute(RANK_KEY, rankValue);
                }
            }

            // Update min and max value
            min = min <= 1D ? 0D : Math.log(min);
            max = max <= 1D ? 0D : Math.log(max);
        }

        calculateNormalizedRanks(graph, min, max);
        graph.readUnlock();
        LOG.info("Finished calculating Rank Prestige");
        return aggregateStatistics(graph, pt, RANK_KEY);
    }

    private double calculateRankPrestigeForNode(Node node, Graph graph) {
        Map<Node, Integer> known = Maps.newHashMap();
        known.put(node, 0);
        Set<Node> neighbours = GraphUtil.getReferencingNodes(Sets.newHashSet(node), graph, known);
        double val = 0D;
        for (Node neighbour : neighbours) {
            val += prominence(neighbour);
        }
        node.setAttribute(RANK_KEY, val);
        return val;
    }

    private double prominence(Node node) {
        // Unknown attribute
        if (graphModel.getNodeTable().getColumn(prominenceAttributeId) == null) {
            LOG.log(Level.WARNING, "Unknown attribute '{0}' for prominence value, will take default instead", prominenceAttributeId);
            return defaultProminence;
        }

        Class c = graphModel.getNodeTable().getColumn(prominenceAttributeId).getTypeClass();

        // Boolean null is false and will be threated differently
        if (node.getAttribute(prominenceAttributeId) == null && c != Boolean.class) {
            LOG.log(Level.INFO, "No prominence value set on node {0}. Will return default value", node.getId());
            return defaultProminence;
        }

        Double prominence;
        if (c == Integer.class) {
            prominence = ((Integer) node.getAttribute(prominenceAttributeId)).doubleValue();
        } else if (c == Double.class) {
            prominence = (Double) node.getAttribute(prominenceAttributeId);
        } else if (c == Float.class) {
            prominence = ((Float) node.getAttribute(prominenceAttributeId)).doubleValue();
        } else if (c == Short.class) {
            prominence = ((Short) node.getAttribute(prominenceAttributeId)).doubleValue();
        } else if (c == Byte.class) {
            prominence = ((Byte) node.getAttribute(prominenceAttributeId)).doubleValue();
        } else if (c == Long.class) {
            prominence = ((Long) node.getAttribute(prominenceAttributeId)).doubleValue();
        } else if (c == BigInteger.class) {
            prominence = ((BigInteger) node.getAttribute(prominenceAttributeId)).doubleValue();
        } else if (c == BigDecimal.class) {
            prominence = ((BigDecimal) node.getAttribute(prominenceAttributeId)).doubleValue();
        } else if (c == Boolean.class) {
            Boolean b = ((Boolean) node.getAttribute(prominenceAttributeId));
            prominence = (b == null || b == false) ? 0D : 1D;
        } else {
            LOG.log(Level.WARNING, "Unsupported datatype '{0}'. Will return default value", c);
            prominence = defaultProminence;
        }

        if (prominence == null || Double.isNaN(prominence)) {
            LOG.log(Level.WARNING, "Found prominence value is null or NaN, will return default value");
            prominence = defaultProminence;
        }
        return prominence;
    }

    private void calculateNormalizedRanks(Graph graph, double min, double max) {
        Iterator<Node> it = graph.getNodes().iterator();
        LOG.fine("Calculating normalized rank value for every node");
        while (!isCanceled() && it.hasNext()) {
            Node n = it.next();
            Double rank = (Double) n.getAttribute(RANK_KEY);
            if (rank != null) {
                n.setAttribute(NORMALIZED_RANK_KEY, minMaxNormalize(rank, min, max));
            }
        }
    }

    private double minMaxNormalize(double rank, double min, double max) {
        // Avoid NaN if min == max
        if ((Math.abs(max - min) < 0.001)) {
            return 0D;
        }
        return (rank - min) / (max - min);
    }
}
