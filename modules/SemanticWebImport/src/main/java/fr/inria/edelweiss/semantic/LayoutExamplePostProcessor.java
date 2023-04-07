/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic;

import fr.inria.edelweiss.semantic.analyzer.PostProcessor;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

import org.checkerframework.checker.nullness.Opt;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.Lookup;

import javax.annotation.CheckForNull;

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

        Optional<LayoutBuilder> searchBuilder = Optional.empty();
        for (LayoutBuilder layoutBuilder : layoutBuilders) {
            logger.info(layoutBuilder.getName());
            if (layoutBuilder.getName().equals(layoutName)) {
                searchBuilder = Optional.of(layoutBuilder);
                break;
            }
        }

        LayoutBuilder foundBuilder = searchBuilder.orElseThrow(IllegalStateException::new);
        Layout layout = foundBuilder.buildLayout();
        layout.setGraphModel(getModel());
        layout.initAlgo();
        int nbIterations = 0;
        while (layout.canAlgo() && (nbIterations < 10_000)) {
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

        Optional<StatisticsBuilder> searchBuilder = Optional.empty();
        for (StatisticsBuilder statisticsBuilder : statisticsBuilders) {
            logger.info(statisticsBuilder.getName());
            if (statisticsBuilder.getName().equals(statisticName)) {
                searchBuilder = Optional.of(statisticsBuilder);
                break;
            }
        }
        StatisticsBuilder foundBuilder = searchBuilder.orElseThrow();
        Statistics statistics = foundBuilder.getStatistics();
        statistics.execute(getModel());
    }
}
