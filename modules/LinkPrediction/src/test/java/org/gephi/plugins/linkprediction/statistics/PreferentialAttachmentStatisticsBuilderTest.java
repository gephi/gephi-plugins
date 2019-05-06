package org.gephi.plugins.linkprediction.statistics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreferentialAttachmentStatisticsBuilderTest {

    @Test void getName() {
        PreferentialAttachmentStatisticsBuilder builder = new PreferentialAttachmentStatisticsBuilder();
        String name = builder.getName();

        assertEquals("Preferential Attachment", name);
    }

    @Test void getStatistics() {
        PreferentialAttachmentStatisticsBuilder builder = new PreferentialAttachmentStatisticsBuilder();
        Class statisticClass = builder.getStatisticsClass();

        assertEquals(PreferentialAttachmentStatistics.class, statisticClass);
    }
}
