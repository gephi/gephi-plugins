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
package org.gephi.plugins.prestige.ui;

import org.gephi.plugins.prestige.PrestigeStatistics;
import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
@ServiceProvider(service = StatisticsUI.class)
public class PrestigeUI implements StatisticsUI {

    private static final double RANK_DEFAULT_PROMISE = 0d;

    private PrestigeStatistics prestigeStats;
    private PrestigeSettingsPanel settings;

    @Override
    public JPanel getSettingsPanel() {
        settings = new PrestigeSettingsPanel();
        return settings;
    }

    @Override
    public void setup(Statistics ststcs) {
        this.prestigeStats = (PrestigeStatistics) ststcs;
    }

    @Override
    public void unsetup() {
        // Take settings from panel
        prestigeStats.setCalculateIndegree(settings.isIndegree());
        prestigeStats.setCalculateProximity(settings.isProximity());
        prestigeStats.setCalculateDomain(settings.isDomain());
        prestigeStats.setCalculateRank(settings.isRank());
        if (settings.isRank()) {
            prestigeStats.setRankDoLogTransformation(settings.isRankLogTransformation());
            prestigeStats.setRankProminenceAttributeId(settings.getProminenceAttributeId());

            Double defaultIfNan = extractDefaultIfNan();
            prestigeStats.setRankDefaultValueIfNan(defaultIfNan);
        }

        // Reset settings panel
        prestigeStats = null;
        settings = null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return PrestigeStatistics.class;
    }

    @Override
    public String getValue() {
        return "Done";
    }

    @Override
    public String getDisplayName() {
        return "Prestige";
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 10 * 000;
    }

    @Override
    public String getShortDescription() {
        return "Calculates prestiges metrics";
    }

    private Double extractDefaultIfNan() {
        String text = settings.getDefaultIfNan().trim();
        if (text.isEmpty()) {
            return RANK_DEFAULT_PROMISE;
        }

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException nfe) {
            return RANK_DEFAULT_PROMISE;
        }
    }
}
