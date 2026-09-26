/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.statistics;

import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author edemairy
 */
@ServiceProvider(service=StatisticsUI.class)
public class SemanticWebStatisticsUI implements StatisticsUI{
    private final String DISPLAY_NAME = "SW Type Statistics";
    private final int POSITION = 666;
    private SemanticWebStatistics statistics;

    public JPanel getSettingsPanel() {
        return new SemanticWebStatisticsPanel(statistics);
    }

    public void setup(Statistics ststcs) {
        statistics = (SemanticWebStatistics) ststcs;
    }

    public void unsetup() {
        statistics = null;
    }

    public Class<? extends Statistics> getStatisticsClass() {
        return SemanticWebStatistics.class;
    }

    public String getValue() {
        return statistics.getSummary();
    }

    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public String getCategory() {
        return CATEGORY_NODE_OVERVIEW;
    }

    public int getPosition() {
        return POSITION;
    }

    @Override
    public String getShortDescription() {
        return "Compute the number of types and relationships in the current dataset.";
    }

}
