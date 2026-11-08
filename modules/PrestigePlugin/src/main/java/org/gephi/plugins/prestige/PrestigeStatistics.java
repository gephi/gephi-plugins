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
package org.gephi.plugins.prestige;

import org.gephi.plugins.prestige.calculation.IndegreeCalculator;
import org.gephi.plugins.prestige.calculation.DomainCalculator;
import org.gephi.plugins.prestige.calculation.ProximityCalculator;
import java.util.SortedMap;
import java.util.logging.Logger;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.plugins.prestige.calculation.RankCalculator;
import org.gephi.plugins.prestige.ui.PrestigeSettingsPanel;
import org.gephi.plugins.prestige.util.ReportChartUtil;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public class PrestigeStatistics implements Statistics, LongTask {

    private static final Logger LOG = Logger.getLogger(PrestigeStatistics.class.getName());

    // Settings
    private boolean indegree = false;
    private boolean proximity = false;
    private boolean domain = false;
    private boolean rank = false;
    private boolean rankDoLogTransformation = true;
    private double rankDefaultValueIfNan = 0D;
    private String prominenceAttributeId;

    // Calculators
    private final IndegreeCalculator indegreeCalculator = new IndegreeCalculator();
    private final DomainCalculator domainCalculator = new DomainCalculator();
    private final ProximityCalculator proximityCalculator = new ProximityCalculator();
    private RankCalculator rankCalculator; // Constructor required additional attributes and is called before calculation

    // Result maps
    private SortedMap<Double, Integer> indegreeResultDistribution;
    private SortedMap<Double, Integer> domainResultDistribution;
    private SortedMap<Double, Integer> proximityResultDistribution;
    private SortedMap<Double, Integer> rankResultDistribution;

    private ProgressTicket processTicket;

    public void execute(GraphModel gm) {
        if (!gm.isDirected()) {
            LOG.warning("Can not calculate prestige metrics on undirected graphs.");
            return;
        }

        Graph graph = gm.getGraph();

        graph.readLock();
        try {
            Progress.start(processTicket, 4);
            if (indegree) {
                indegreeResultDistribution = indegreeCalculator.calculate(gm, processTicket);
            }
            Progress.progress(processTicket);

            if (domain) {
                domainResultDistribution = domainCalculator.calculate(gm, processTicket);
            }
            Progress.progress(processTicket);

            if (proximity) {
                proximityResultDistribution = proximityCalculator.calculate(gm, processTicket);
            }
            Progress.progress(processTicket);

            if (rank) {
                if (prominenceAttributeId == null) {
                    LOG.severe("No prominence attribute selected. Can not calculate rank prestige.");
                } else {
                    rankCalculator = new RankCalculator(prominenceAttributeId, rankDoLogTransformation, rankDefaultValueIfNan, gm);
                    rankResultDistribution = rankCalculator.calculate(gm, processTicket);
                }
            }
            Progress.progress(processTicket);
        } finally {
            graph.readUnlockAll();
            Progress.finish(processTicket);
        }
    }

    public String getReport() {
        String template = "<h2>%s</h2><br>%s<br>%s<br/>%s<hr>";
        StringBuilder sb = new StringBuilder("<HTML><BODY><h1>Prestige Report</h1>");
        if (indegreeResultDistribution != null) {
            String algDesc = "Number of directly linked nodes";
            addToReport(sb, template, "Indegree Prestige", algDesc, indegreeResultDistribution, "");
        }

        if (domainResultDistribution != null) {
            String algDesc = "Share of total nodes which can reach a node";
            addToReport(sb, template, "Domain Prestige", algDesc, domainResultDistribution, "");
        }

        if (proximityResultDistribution != null) {
            String algDesc = "Considers directly and indirectly linked nodes and path-lengths";
            addToReport(sb, template, "Proximity Prestige", algDesc, proximityResultDistribution, "");
        }

        if (rankResultDistribution != null) {
            String algDesc = "Considers specified prominence value from in-degree nodes";
            String details = "Using Log-Transformation: " + rankDoLogTransformation + "<br>Default for NA Values: " + rankDefaultValueIfNan;
            addToReport(sb, template, "Rank Prestige", algDesc, rankResultDistribution, details);
        }

        sb.append("<h2>Algorithms</h2>For detailed information about the algorithms visit: <i>" + PrestigeSettingsPanel.DESCRIPTION_URL + "</i> </BODY></HTML>");
        return sb.toString();
    }

    private void addToReport(StringBuilder sb, String template, String name, String algDescription, SortedMap<Double, Integer> distribution, String additionalInfo) {
        String image = ReportChartUtil.createDistributionGraph("Distribution", distribution);
        sb.append(String.format(template, name, algDescription, additionalInfo, image));
    }

    public boolean cancel() {
        if (indegreeCalculator != null) {
            indegreeCalculator.cancel();
        }
        if (proximityCalculator != null) {
            proximityCalculator.cancel();
        }

        if (domainCalculator != null) {
            domainCalculator.cancel();
        }

        if (rankCalculator != null) {
            rankCalculator.cancel();
        }
        return true;
    }

    /*
    Getter and Setter
     */
    public void setProgressTicket(ProgressTicket pt) {
        this.processTicket = pt;
    }

    public void setCalculateIndegree(boolean indegree) {
        this.indegree = indegree;
    }

    public void setCalculateProximity(boolean proximity) {
        this.proximity = proximity;
    }

    public void setCalculateDomain(boolean domain) {
        this.domain = domain;
    }

    public void setCalculateRank(boolean rank) {
        this.rank = rank;
    }

    public void setRankDoLogTransformation(boolean flag) {
        this.rankDoLogTransformation = flag;
    }

    public void setRankDefaultValueIfNan(double defaultValue) {
        this.rankDefaultValueIfNan = defaultValue;
    }

    public void setRankProminenceAttributeId(String id) {
        this.prominenceAttributeId = id;
    }
}
