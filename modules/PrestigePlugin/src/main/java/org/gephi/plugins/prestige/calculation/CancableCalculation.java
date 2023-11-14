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

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public abstract class CancableCalculation {

    private static final Logger LOG = Logger.getLogger(CancableCalculation.class.getName());
    private boolean canceled = false;

    /**
     * Returns overall metrics (counts per value) of null if calculation was
     * canceled)
     *
     * @param graphModel
     * @return
     */
    public final SortedMap<Double, Integer> calculate(GraphModel graphModel, ProgressTicket pt) {
        resetAlreadyCalculatedValues(graphModel);
        addNodePropertyIfNotExists(graphModel);
        return calculateNodeMetrics(graphModel.getGraphVisible(), pt);
    }

    private void resetAlreadyCalculatedValues(GraphModel graph) {
        for (String attributeName : getNodePropertyNames()) {
            // Only remove if attribute already exists
            if (graph.getNodeTable().hasColumn(attributeName)) {
                LOG.log(Level.FINE, "Removing existing value in all nodes for attribute: {0}", attributeName);
                Iterator<Node> it = graph.getGraph().getNodes().iterator();
                while (it.hasNext()) {
                    Node next = it.next();
                    next.removeAttribute(attributeName);
                }
            }
        }
    }

    private void addNodePropertyIfNotExists(GraphModel graphModel) {
        Table nodeTable = graphModel.getNodeTable();
        // Only add property once
        for (String propertyName : getNodePropertyNames()) {
            if (!nodeTable.hasColumn(propertyName)) {
                LOG.log(Level.FINE, "Adding property {0} to node-table", propertyName);
                nodeTable.addColumn(propertyName, propertyName, Double.class, 0.0D);
            }
        }
    }

    public final void cancel() {
        LOG.info("Canceling calculation");
        this.canceled = true;
    }

    protected final boolean isCanceled() {
        return canceled;
    }

    protected final SortedMap<Double, Integer> aggregateStatistics(Graph graph, ProgressTicket pt, String aggregationProperty) {
        LOG.log(Level.INFO, "Aggregating results for: {0}", aggregationProperty);

        if (isCanceled()) {
            return null;
        }

        SortedMap<Double, Integer> sorted = new TreeMap<Double, Integer>();
        Iterator<Node> it = graph.getNodes().iterator();
        while (it.hasNext()) {
            Node n = it.next();
            Double val = (Double) n.getAttribute(aggregationProperty);
            if (val != null) {
                Integer counter = sorted.get(val);
                counter = counter == null ? 1 : counter + 1;
                sorted.put(val, counter);
            }
        }
        LOG.log(Level.INFO, "Finished aggregating results for: {0}", aggregationProperty);
        return sorted;
    }

    /*
     * Returns all property names that will be added by the Prestige Calculator
     */
    protected abstract String[] getNodePropertyNames();

    /**
     * Calculates statistics for every node and returns a aggregated statistic
     *
     * @param graph
     * @return Map containing each prestige value as key and counts (number of
     * occurrences) as values. Will return null if calculation was canceled
     */
    protected abstract SortedMap<Double, Integer> calculateNodeMetrics(Graph graph, ProgressTicket pt);

}
