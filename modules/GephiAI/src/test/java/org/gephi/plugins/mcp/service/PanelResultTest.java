/*
 * Copyright 2026 Matt Artz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gephi.plugins.mcp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import javax.swing.JPanel;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.junit.jupiter.api.Test;

/**
 * The result line Gephi's Statistics panel shows for a statistic that just ran, read from the
 * statistic's own UI. It gives a headline for statistics the plugin has no getter for, such as
 * those from other Gephi plugins.
 */
class PanelResultTest {

    static class Quality implements Statistics {
        final double value;

        Quality(double value) {
            this.value = value;
        }

        @Override public void execute(GraphModel graphModel) {
        }

        @Override public String getReport() {
            return "";
        }
    }

    static class Other implements Statistics {
        @Override public void execute(GraphModel graphModel) {
        }

        @Override public String getReport() {
            return "";
        }
    }

    static class QualityUI implements StatisticsUI {
        private final Class<? extends Statistics> forClass;
        private final String shown;
        private final boolean fails;

        QualityUI(Class<? extends Statistics> forClass, String shown, boolean fails) {
            this.forClass = forClass;
            this.shown = shown;
            this.fails = fails;
        }

        @Override public String getValue(Statistics statistics) {
            if (fails) {
                throw new IllegalStateException("not set up");
            }
            return shown == null || shown.isBlank() ? shown : shown + " for " + ((Quality) statistics).value;
        }

        @Override public String getValue() {
            return "stale";
        }

        @Override public JPanel getSettingsPanel() {
            return null;
        }

        @Override public void setup(Statistics statistics) {
        }

        @Override public void unsetup() {
        }

        @Override public Class<? extends Statistics> getStatisticsClass() {
            return forClass;
        }

        @Override public String getDisplayName() {
            return "Quality";
        }

        @Override public String getShortDescription() {
            return "";
        }

        @Override public String getCategory() {
            return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
        }

        @Override public int getPosition() {
            return 0;
        }
    }

    @Test
    void theUiForTheStatisticGivesItsResultForThisRun() {
        List<StatisticsUI> uis = List.of(new QualityUI(Other.class, "wrong", false),
            new QualityUI(Quality.class, "Q", false));
        assertEquals("Q for 0.5", GephiControlService.panelResult(new Quality(0.5), uis));
    }

    @Test
    void noResultWhenNoUiMatchesOrItShowsNothing() {
        assertNull(GephiControlService.panelResult(new Quality(0.5),
            List.of(new QualityUI(Other.class, "wrong", false))));
        assertNull(GephiControlService.panelResult(new Quality(0.5),
            List.of(new QualityUI(Quality.class, null, false))));
        assertNull(GephiControlService.panelResult(new Quality(0.5),
            List.of(new QualityUI(Quality.class, "  ", false))));
    }

    @Test
    void uiThatFailsLeavesTheRunsOwnResultAlone() {
        assertNull(GephiControlService.panelResult(new Quality(0.5),
            List.of(new QualityUI(Quality.class, "Q", true))));
    }
}
