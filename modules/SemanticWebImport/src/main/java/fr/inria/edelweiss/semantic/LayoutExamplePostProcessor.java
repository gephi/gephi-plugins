/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic;

import fr.inria.edelweiss.semantic.analyzer.PostProcessor;
import java.util.Collection;
import java.util.logging.Logger;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.Lookup;

/**
 *
 * @author edemairy
 */
public class LayoutExamplePostProcessor extends PostProcessor {

    private static final Logger logger = Logger.getLogger(LayoutExamplePostProcessor.class.getName());

    @Override
    public void run() {
        applyLayoutAlgorithm("ForceAtlas 2");
        resizeNodes();

    }

    private void applyLayoutAlgorithm(final String layoutName) {
        final Lookup lookup = Lookup.getDefault();
        final Collection<? extends LayoutBuilder> layoutBuilders = lookup.lookupAll(LayoutBuilder.class);

        LayoutBuilder foundBuilder = null;
        for (LayoutBuilder layoutBuilder : layoutBuilders) {
            logger.info(layoutBuilder.getName());
            if (layoutBuilder.getName().equals(layoutName)) {
                foundBuilder = layoutBuilder;
                break;
            }
        }
        Layout layout = foundBuilder.buildLayout();
        layout.setGraphModel(getModel());
        layout.initAlgo();
        int nbIterations = 0;
        while (layout.canAlgo() && (nbIterations < 10000)) {
            layout.goAlgo();
            nbIterations++;
        }
        layout.endAlgo();
    }

    private void resizeNodes() {
    }

    private void computeStatistic(final String statisticName) {
        final Lookup lookup = Lookup.getDefault();
        final Collection<? extends StatisticsBuilder> statisticsBuilders = lookup.lookupAll(StatisticsBuilder.class);

        StatisticsBuilder foundBuilder = null;
        for (StatisticsBuilder statisticsBuilder : statisticsBuilders) {
            logger.info(statisticsBuilder.getName());
            if (statisticsBuilder.getName().equals(statisticName)) {
                foundBuilder = statisticsBuilder;
                break;
            }
        }
        Statistics statistics = foundBuilder.getStatistics();
        statistics.execute(getModel());
    }
}
