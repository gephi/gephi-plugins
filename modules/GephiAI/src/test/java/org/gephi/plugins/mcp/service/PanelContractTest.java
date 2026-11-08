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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import javax.swing.JPanel;
import org.gephi.desktop.statistics.StatisticsControllerUIImpl;
import org.gephi.desktop.statistics.StatisticsModelUIImpl;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Runs statistics through Gephi's own Statistics panel, panel model and statistics controller,
 * so the plugin's watch is checked against Gephi's real order of events: the panel marks the
 * statistic inside execute(), the controller runs it on a background thread, and the panel
 * clears the mark, collects the result, and only then calls back.
 */
class PanelContractTest {

    private static final Consumer<Runnable> DIRECT = Runnable::run;

    /** A statistic that counts its runs. */
    public static class CountingStatistic implements Statistics, LongTask {
        static final AtomicInteger RUNS = new AtomicInteger();

        @Override public void execute(GraphModel gm) {
            RUNS.incrementAndGet();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @Override public String getReport() {
            return "counted";
        }

        @Override public boolean cancel() {
            return false;
        }

        @Override public void setProgressTicket(ProgressTicket t) {
        }
    }

    /** Registers the counting statistic with Gephi's statistics controller. */
    @ServiceProvider(service = StatisticsBuilder.class)
    public static class CountingStatisticBuilder implements StatisticsBuilder {
        @Override public String getName() {
            return "Gephi AI counting statistic (test)";
        }

        @Override public Statistics getStatistics() {
            return new CountingStatistic();
        }

        @Override public Class<? extends Statistics> getStatisticsClass() {
            return CountingStatistic.class;
        }
    }

    /** The counting statistic's panel entry; its unsetup can be made to fail. */
    @ServiceProvider(service = StatisticsUI.class)
    public static class CountingStatisticUI implements StatisticsUI {
        static volatile boolean failOnUnsetup;

        @Override public JPanel getSettingsPanel() {
            return null;
        }

        @Override public void setup(Statistics statistics) {
        }

        @Override public void unsetup() {
            if (failOnUnsetup) {
                throw new IllegalStateException("the panel's own completion code failed");
            }
        }

        @Override public Class<? extends Statistics> getStatisticsClass() {
            return CountingStatistic.class;
        }

        @Override public String getValue() {
            return "done";
        }

        @Override public String getDisplayName() {
            return "Counting (test)";
        }

        @Override public String getShortDescription() {
            return "";
        }

        @Override public String getCategory() {
            return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
        }

        @Override public int getPosition() {
            return 10_000;
        }
    }

    private GraphModel gm;
    private StatisticsModelUIImpl model;
    private final StatisticsControllerUIImpl panel = new StatisticsControllerUIImpl();

    @BeforeEach
    void project() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        Workspace ws = pc.getCurrentWorkspace();
        gm = Lookup.getDefault().lookup(GraphController.class).getGraphModel(ws);
        model = new StatisticsModelUIImpl(ws);
        ws.add(model);
        CountingStatistic.RUNS.set(0);
        CountingStatisticUI.failOnUnsetup = false;
    }

    private boolean run(CountingStatistic stat) throws InterruptedException {
        return GephiControlService.executeStatistic(stat, gm, 0, panel, DIRECT,
            s -> GephiControlService.watchPanel(s, List.of(model), Lookup.getDefault().lookupAll(StatisticsUI.class)));
    }

    @Test
    void boundPanelRunsTheStatisticOnceAndReportsBack() {
        panel.setup(model);
        CountingStatistic stat = new CountingStatistic();

        boolean stopped = assertTimeoutPreemptively(Duration.ofSeconds(10), () -> run(stat));

        assertFalse(stopped);
        assertEquals(1, CountingStatistic.RUNS.get());
        StatisticsUI ui = Lookup.getDefault().lookup(CountingStatisticUI.class);
        assertNotNull(model.getResult(ui), "the panel collected the result");
        assertFalse(model.isRunning(ui), "the panel cleared its running mark");
    }

    @Test
    void unboundPanelDeclinesAndThePluginRunsTheStatisticOnce() {
        panel.setup(null);
        CountingStatistic stat = new CountingStatistic();

        boolean stopped = assertTimeoutPreemptively(Duration.ofSeconds(10), () -> run(stat));

        assertFalse(stopped);
        assertEquals(1, CountingStatistic.RUNS.get());
    }

    @Test
    void failureInThePanelsCompletionCodeIsReportedUnconfirmed() {
        panel.setup(model);
        CountingStatisticUI.failOnUnsetup = true;
        CountingStatistic stat = new CountingStatistic();

        IllegalStateException e = assertThrows(IllegalStateException.class,
            () -> assertTimeoutPreemptively(Duration.ofSeconds(15), () -> run(stat)));

        assertTrue(e.getMessage().contains("without reporting back"), e.getMessage());
        assertEquals(1, CountingStatistic.RUNS.get(), "the statistic must not be run a second time");
    }

    /**
     * A workspace selected while the request waits for the interface thread gets its model from
     * the Statistics window, and the panel binds to it before running. The watch must see that
     * model, or the plugin runs the statistic a second time.
     */
    @Test
    void panelBoundToAWorkspaceSelectedDuringTheHandOverRunsTheStatisticOnce() throws Exception {
        panel.setup(model);
        CountingStatistic stat = new CountingStatistic();
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Consumer<Runnable> selectThenRun = r -> {
            Workspace other = pc.newWorkspace(pc.getCurrentProject());
            StatisticsModelUIImpl otherModel = new StatisticsModelUIImpl(other);
            other.add(otherModel);
            panel.setup(otherModel);
            r.run();
        };

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> GephiControlService.executeStatistic(stat, gm, 0, panel,
            selectThenRun, GephiControlService::watchPanel));
        Thread.sleep(500);

        assertEquals(1, CountingStatistic.RUNS.get(), "the statistic ran twice");
    }
}
