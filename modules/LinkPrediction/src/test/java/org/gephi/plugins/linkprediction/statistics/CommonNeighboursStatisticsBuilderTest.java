package org.gephi.plugins.linkprediction.statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CommonNeighboursStatisticsBuilderTest {

    @Test void getName() {
        CommonNeighboursStatisticsBuilder builder = new CommonNeighboursStatisticsBuilder();
        String name = builder.getName();

        assertEquals("Common Neighbours", name);
    }

    @Test void getStatistics() {
        CommonNeighboursStatisticsBuilder builder = new CommonNeighboursStatisticsBuilder();
        Class statisticClass = builder.getStatistics().getClass();

        assertEquals(CommonNeighboursStatistics.class, statisticClass);
    }
}
