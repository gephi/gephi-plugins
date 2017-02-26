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
package org.gephi.plugins.prestige.builder;

import org.gephi.plugins.prestige.PrestigeStatistics;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class PrestigeBuilder implements StatisticsBuilder {

    @Override
    public String getName() {
        return "Calculates Prestige";
    }

    @Override
    public Statistics getStatistics() {
        return new PrestigeStatistics();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return PrestigeStatistics.class;
    }
}
