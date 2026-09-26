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

import java.util.Map;
import org.gephi.statistics.plugin.ChartUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public final class ReportChartUtil {

    private ReportChartUtil() {
    }

    public static String createDistributionGraph(String name, Map<Double, Integer> distribution) {
        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(distribution, name);

        XYSeriesCollection dataset1 = new XYSeriesCollection();
        dataset1.addSeries(dSeries);

        JFreeChart chart1 = ChartFactory.createXYLineChart(
                name,
                "Value",
                "Count",
                dataset1,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);
        ChartUtils.decorateChart(chart1);
        ChartUtils.scaleChart(chart1, dSeries, false);
        return ChartUtils.renderChart(chart1, name.trim().toLowerCase().replaceAll("\\s+", "") + "png");
    }

}
