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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import javax.swing.event.ChangeListener;
import org.gephi.desktop.statistics.api.StatisticsControllerUI;
import org.gephi.desktop.statistics.api.StatisticsModelUI;
import org.gephi.graph.api.GraphModel;
import org.gephi.layout.api.LayoutController;
import org.gephi.layout.api.LayoutModel;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.junit.jupiter.api.Test;
import org.openide.util.Lookup;

/**
 * Statistics and layouts run through Gephi's own controllers so its Statistics and Layout panels
 * show what Gephi AI is doing. These stand-in controllers pin the waiting, stopping, error and
 * call-order logic without the desktop interface.
 */
class PanelExecutionTest {

    private static final Consumer<Runnable> DIRECT = Runnable::run;

    /** A mark read from a fake panel's flags. */
    static GephiControlService.PanelRunMark mark(java.util.function.BooleanSupplier now,
        java.util.function.BooleanSupplier ever) {
        return new GephiControlService.PanelRunMark() {
            @Override public boolean isMarked() {
                return now.getAsBoolean();
            }

            @Override public boolean wasEverMarked() {
                return ever.getAsBoolean();
            }

            @Override public void close() {
            }
        };
    }

    /** A panel that never marks the statistic, as Gephi's does when it declines a run. */
    static GephiControlService.PanelRunMark neverMarked() {
        return mark(() -> false, () -> false);
    }

    /** A statistic that runs until cancelled, or for a fixed time. */
    static class SlowStat implements Statistics, LongTask {
        final long millis;
        final AtomicBoolean cancelled = new AtomicBoolean();
        final AtomicBoolean ran = new AtomicBoolean();

        SlowStat(long millis) {
            this.millis = millis;
        }

        @Override public void execute(GraphModel gm) {
            ran.set(true);
            long end = System.currentTimeMillis() + millis;
            while (!cancelled.get() && System.currentTimeMillis() < end) {
                Thread.onSpinWait();
            }
        }

        @Override public String getReport() {
            return "";
        }

        @Override public boolean cancel() {
            cancelled.set(true);
            return true;
        }

        @Override public void setProgressTicket(ProgressTicket t) {
        }
    }

    /** Runs the statistic on its own thread and reports back, as Gephi's panel controller does. */
    static class FakeStatsUI implements StatisticsControllerUI {
        Throwable failWith;

        @Override public void execute(Statistics s) {
            execute(s, null);
        }

        @Override public void execute(Statistics s, LongTaskListener listener) {
            new Thread(() -> {
                if (failWith != null) {
                    try {
                        Method m = listener.getClass().getMethod("fatalError", Throwable.class);
                        m.invoke(listener, failWith);
                    } catch (ReflectiveOperationException e) {
                        throw new AssertionError(e);
                    }
                    return;
                }
                s.execute(null);
                listener.taskFinished(s instanceof LongTask ? (LongTask) s : null);
            }).start();
        }

        @Override public void setStatisticsUIVisible(StatisticsUI ui, boolean visible) {
        }
    }

    @Test
    void statisticRunThroughThePanelIsWaitedFor() throws Exception {
        SlowStat stat = new SlowStat(150);

        boolean stopped = GephiControlService.executeStatistic(stat, null, 0, new FakeStatsUI(), DIRECT);

        assertFalse(stopped);
        assertTrue(stat.ran.get());
        assertFalse(stat.cancelled.get());
    }

    @Test
    void statisticPastItsDeadlineIsStoppedAndReported() throws Exception {
        SlowStat stat = new SlowStat(60_000);

        long start = System.nanoTime();
        boolean stopped = GephiControlService.executeStatistic(stat, null, 200, new FakeStatsUI(), DIRECT);

        assertTrue(stopped);
        assertTrue(stat.cancelled.get());
        assertTrue((System.nanoTime() - start) / 1_000_000 < 10_000);
    }

    @Test
    void failureReportedByGephiEndsTheWaitWithTheError() {
        FakeStatsUI ui = new FakeStatsUI();
        ui.failWith = new IllegalStateException("boom");

        RuntimeException e = assertThrows(RuntimeException.class,
            () -> GephiControlService.executeStatistic(new SlowStat(60_000), null, 0, ui, DIRECT));

        assertTrue(String.valueOf(e.getCause()).contains("boom"), String.valueOf(e));
    }

    @Test
    void withoutThePanelTheStatisticRunsDirectly() throws Exception {
        SlowStat stat = new SlowStat(10);

        assertFalse(GephiControlService.executeStatistic(stat, null, 0, null, DIRECT));
        assertTrue(stat.ran.get());
    }

    /** Recurses once per node of a long path, as Gephi's connected-components search does. */
    static class DeepStat extends SlowStat {
        DeepStat() {
            super(0);
        }

        @Override public void execute(GraphModel gm) {
            ran.set(true);
            depth(200_000);
        }

        private static int depth(int n) {
            return n == 0 ? 0 : 1 + depth(n - 1);
        }
    }

    /**
     * Gephi 0.11.4 runs statistics on a worker with a 128 MB stack, so a deep recursion does not
     * overflow (gephi#3327). A statistic the plugin runs itself gets the same stack, whatever
     * thread the request arrived on.
     */
    @Test
    void statisticRunDirectlyHasRoomForDeepRecursion() throws Exception {
        DeepStat stat = new DeepStat();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread small = new Thread(null, () -> {
            try {
                GephiControlService.executeStatistic(stat, null, 0, null, DIRECT);
            } catch (Throwable t) {
                failure.set(t);
            }
        }, "small-stack-request", 512 * 1024);
        small.start();
        small.join(60_000);

        assertFalse(small.isAlive(), "the statistic was still running after a minute");
        assertNull(failure.get(), () -> "the statistic failed: " + failure.get());
        assertTrue(stat.ran.get());
    }

    /**
     * A request that is interrupted (its client went away) gives up its claim on the statistic, so
     * the statistic must stop too, or a retry runs alongside a run nobody is waiting for.
     */
    @Test
    void interruptedRequestStopsTheStatisticItStarted() throws Exception {
        SlowStat stat = new SlowStat(60_000);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread request = new Thread(() -> {
            try {
                GephiControlService.executeStatistic(stat, null, 0, null, DIRECT);
            } catch (Throwable t) {
                failure.set(t);
            }
        }, "request");
        request.start();
        long until = System.nanoTime() + 5_000_000_000L;
        while (!stat.ran.get() && System.nanoTime() < until) {
            Thread.onSpinWait();
        }

        request.interrupt();
        request.join(10_000);

        assertFalse(request.isAlive(), "the request did not end");
        assertTrue(stat.cancelled.get(), "the statistic kept running after its request was interrupted");
    }

    @Test
    void stopRequestCancelsTheRunningStatisticAndMarksItStopped() throws Exception {
        SlowStat stat = new SlowStat(60_000);
        GephiControlService.RUNNING_STATISTICS.put(stat, "Slow");
        try {
            Thread run = new Thread(() -> {
                try {
                    GephiControlService.executeStatistic(stat, null, 0, new FakeStatsUI(), DIRECT);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            run.start();
            while (!stat.ran.get()) {
                Thread.onSpinWait();
            }

            final com.google.gson.JsonObject r = GephiControlService.getInstance().stopStatistics();

            run.join(10_000);
            assertFalse(run.isAlive(), "the statistic kept running after the stop");
            assertTrue(stat.cancelled.get());
            assertEquals("Slow", r.getAsJsonArray("stopped").get(0).getAsString());
            assertTrue(GephiControlService.STOP_REQUESTED.contains(stat));
        } finally {
            GephiControlService.RUNNING_STATISTICS.remove(stat);
            GephiControlService.STOP_REQUESTED.remove(stat);
        }
    }

    /** Returns without running anything, as Gephi's panel does before its model is bound. */
    static class DecliningStatsUI extends FakeStatsUI {
        @Override public void execute(Statistics s, LongTaskListener listener) {
        }
    }

    /** Runs the statistic and reports back before execute() returns. */
    static class ImmediateStatsUI extends FakeStatsUI {
        @Override public void execute(Statistics s, LongTaskListener listener) {
            s.execute(null);
            listener.taskFinished(s instanceof LongTask ? (LongTask) s : null);
        }
    }

    /** Counts its runs. */
    static class CountingStat extends SlowStat {
        final java.util.concurrent.atomic.AtomicInteger runs = new java.util.concurrent.atomic.AtomicInteger();

        CountingStat() {
            super(0);
        }

        @Override public void execute(GraphModel gm) {
            runs.incrementAndGet();
            super.execute(gm);
        }
    }

    /**
     * The panel can decline a run without reporting back (its model not yet bound to the
     * workspace, after a workspace change). Waiting for its callback then held the request for
     * the full one-hour cap and blocked every later run of that statistic.
     */
    @Test
    void statisticThePanelDidNotTakeRunsDirectly() {
        CountingStat stat = new CountingStat();

        boolean stopped = org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(
            java.time.Duration.ofSeconds(5),
            () -> GephiControlService.executeStatistic(stat, null, 0, new DecliningStatsUI(), DIRECT,
                s -> neverMarked()));

        assertFalse(stopped);
        assertEquals(1, stat.runs.get(), "the statistic must run once, directly");
    }

    /** A run that finished before the check is not run a second time. */
    @Test
    void statisticThatFinishedBeforeTheCheckIsNotRunAgain() throws Exception {
        CountingStat stat = new CountingStat();

        GephiControlService.executeStatistic(stat, null, 0, new ImmediateStatsUI(), DIRECT,
            s -> neverMarked());

        assertEquals(1, stat.runs.get());
    }

    /**
     * Gephi 0.11.4's real order: mark the statistic running inside execute(), run it on a worker
     * thread, clear the mark, do the panel's own result work, then call back. A callback that
     * never comes stands for that result work failing on the worker thread.
     */
    static class RealOrderStatsUI extends FakeStatsUI {
        final AtomicBoolean marked = new AtomicBoolean();
        final AtomicBoolean everMarked = new AtomicBoolean();
        final long callbackDelayMs;
        final boolean callsBack;
        /** Return from execute() only once the worker has cleared the mark: a fast statistic. */
        boolean finishBeforeReturning;

        RealOrderStatsUI(long callbackDelayMs, boolean callsBack) {
            this.callbackDelayMs = callbackDelayMs;
            this.callsBack = callsBack;
        }

        @Override public void execute(Statistics s, LongTaskListener listener) {
            marked.set(true);
            everMarked.set(true);
            java.util.concurrent.CountDownLatch cleared = new java.util.concurrent.CountDownLatch(1);
            new Thread(() -> {
                s.execute(null);
                marked.set(false);
                cleared.countDown();
                try {
                    Thread.sleep(callbackDelayMs);
                } catch (InterruptedException e) {
                    return;
                }
                if (callsBack) {
                    listener.taskFinished(s instanceof LongTask ? (LongTask) s : null);
                }
            }).start();
            if (finishBeforeReturning) {
                try {
                    cleared.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /** A fast statistic whose callback comes 400 ms after its mark clears is run once. */
    @Test
    void slowCallbackAfterTheMarkClearsDoesNotRunTheStatisticTwice() throws Exception {
        CountingStat stat = new CountingStat();
        RealOrderStatsUI ui = new RealOrderStatsUI(400, true);
        ui.finishBeforeReturning = true;

        GephiControlService.executeStatistic(stat, null, 0, ui, DIRECT,
            s -> mark(ui.marked::get, ui.everMarked::get));

        Thread.sleep(500);
        assertEquals(1, stat.runs.get(), "the statistic ran twice");
    }

    /** A run that ends with no callback is reported as unconfirmed, never as a success. */
    @Test
    void runThatEndsWithoutReportingBackIsReportedUnconfirmed() {
        CountingStat stat = new CountingStat();
        RealOrderStatsUI ui = new RealOrderStatsUI(0, false);

        IllegalStateException e = assertThrows(IllegalStateException.class, () ->
            org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(java.time.Duration.ofSeconds(10),
                () -> GephiControlService.executeStatistic(stat, null, 0, ui, DIRECT,
                    s -> mark(ui.marked::get, ui.everMarked::get))));

        assertTrue(e.getMessage().contains("without reporting back"), e.getMessage());
        assertEquals(1, stat.runs.get());
    }

    /**
     * The interface thread was too busy: the hand-over timed out before the queued step started.
     * The request reports the failure, and the step, when it finally runs, must not start the
     * statistic behind the caller's back.
     */
    @Test
    void handOverThatTimedOutBeforeStartingNeverStartsTheStatisticLater() throws Exception {
        CountingStat stat = new CountingStat();
        AtomicReference<Runnable> queued = new AtomicReference<>();
        Consumer<Runnable> timesOut = r -> {
            queued.set(r);
            throw new RuntimeException("Gephi's UI thread is unresponsive");
        };

        assertThrows(RuntimeException.class,
            () -> GephiControlService.executeStatistic(stat, null, 0, new FakeStatsUI(), timesOut, s -> neverMarked()));
        queued.get().run();
        Thread.sleep(300);

        assertEquals(0, stat.runs.get(), "the statistic started after the request had given up");
    }

    /** A hand-over that timed out after the step had started is waited for, not reported as failed. */
    @Test
    void handOverThatTimedOutAfterStartingIsWaitedFor() throws Exception {
        CountingStat stat = new CountingStat();
        RealOrderStatsUI ui = new RealOrderStatsUI(0, true);
        Consumer<Runnable> startsThenTimesOut = r -> {
            Thread t = new Thread(r, "slow-interface-thread");
            t.start();
            long until = System.nanoTime() + 5_000_000_000L;
            while (!ui.everMarked.get() && System.nanoTime() < until) {
                Thread.onSpinWait();
            }
            throw new RuntimeException("Gephi's UI thread is unresponsive");
        };

        boolean stopped = org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(java.time.Duration.ofSeconds(10),
            () -> GephiControlService.executeStatistic(stat, null, 0, ui, startsThenTimesOut,
                s -> mark(ui.marked::get, ui.everMarked::get)));

        assertFalse(stopped);
        assertEquals(1, stat.runs.get());
    }

    /**
     * Gephi's Statistics model keeps its listeners in a plain list that its worker thread iterates;
     * adding and removing a listener for every run races with that. One listener per model, added
     * once, does not.
     */
    @Test
    void eachStatisticsModelGetsOneListenerForAllRuns() {
        CountingModelUI model = new CountingModelUI();
        CountingStat stat = new CountingStat();

        GephiControlService.watchPanel(stat, List.of(model), List.of()).close();
        GephiControlService.watchPanel(new CountingStat(), List.of(model), List.of()).close();

        assertEquals(1, model.added.get(), "a listener was added for each run");
        assertEquals(0, model.removed.get(), "a listener was removed while Gephi's worker may be iterating");
    }

    /** Counts listener additions and removals. */
    static class CountingModelUI extends FakeModelUI {
        final java.util.concurrent.atomic.AtomicInteger added = new java.util.concurrent.atomic.AtomicInteger();
        final java.util.concurrent.atomic.AtomicInteger removed = new java.util.concurrent.atomic.AtomicInteger();

        @Override public void addChangeListener(ChangeListener l) {
            added.incrementAndGet();
        }

        @Override public void removeChangeListener(ChangeListener l) {
            removed.incrementAndGet();
        }
    }

    /** Stands in for a workspace's Statistics model, reporting one statistic as running. */
    static class FakeModelUI implements StatisticsModelUI {
        Statistics running;

        @Override public String getResult(StatisticsUI ui) {
            return null;
        }

        @Override public boolean isStatisticsUIVisible(StatisticsUI ui) {
            return true;
        }

        @Override public boolean isRunning(StatisticsUI ui) {
            return running != null && ui.getStatisticsClass().equals(running.getClass());
        }

        @Override public Statistics getRunning(StatisticsUI ui) {
            return isRunning(ui) ? running : null;
        }

        @Override public String getReport(Class<? extends Statistics> c) {
            return null;
        }

        @Override public void addChangeListener(ChangeListener l) {
        }

        @Override public void removeChangeListener(ChangeListener l) {
        }
    }

    @Test
    void panelTookTheStatisticOnlyWhenItsModelRunsThatInstance() {
        CountingStat stat = new CountingStat();
        StatisticsUI ui = statisticsUiFor(CountingStat.class);
        FakeModelUI model = new FakeModelUI();

        assertFalse(GephiControlService.panelIsRunning(model, stat, List.of(ui)), "nothing running");
        model.running = new CountingStat();
        assertTrue(GephiControlService.panelIsRunning(model, stat, List.of(ui)),
            "another instance of the class is ambiguous, so the panel is trusted rather than risk a second run");
        model.running = stat;
        assertTrue(GephiControlService.panelIsRunning(model, stat, List.of(ui)));
        assertFalse(GephiControlService.panelIsRunning(null, stat, List.of(ui)), "no model");
        assertTrue(GephiControlService.panelIsRunning(new FakeModelUI(), stat, List.of()),
            "with no panel entry for the statistic there is nothing to check, so the panel is trusted");
    }

    private static StatisticsUI statisticsUiFor(Class<? extends Statistics> c) {
        return (StatisticsUI) java.lang.reflect.Proxy.newProxyInstance(StatisticsUI.class.getClassLoader(),
            new Class<?>[] {StatisticsUI.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getStatisticsClass":
                        return c;
                    case "equals":
                        return proxy == args[0];
                    case "hashCode":
                        return System.identityHashCode(proxy);
                    default:
                        return null;
                }
            });
    }

    @Test
    void stopWithNothingRunningSaysSo() {
        com.google.gson.JsonObject r = GephiControlService.getInstance().stopStatistics();

        assertTrue(r.get("message").getAsString().contains("No statistic"), r.toString());
    }

    /** Records the order of controller calls and what the layout held at each. */
    static class FakeLayoutController implements LayoutController {
        final List<String> calls = new ArrayList<>();
        Layout selected;

        @Override public LayoutModel getModel() {
            return null;
        }

        @Override public LayoutModel getModel(Workspace w) {
            return null;
        }

        @Override public void setLayout(Layout l) {
            selected = l;
            calls.add("setLayout linLog=" + value(l, "linLogMode"));
        }

        @Override public void executeLayout() {
            calls.add("execute");
        }

        @Override public void executeLayout(int n) {
            calls.add("execute " + n);
        }

        public void executeLayout(Layout l) {
            calls.add("executeNow");
        }

        @Override public boolean canExecute() {
            return true;
        }

        @Override public void stopLayout() {
            calls.add("stop");
        }

        @Override public boolean canStop() {
            return false;
        }
    }

    static Object value(Layout layout, String key) {
        for (LayoutProperty p : layout.getProperties()) {
            if (p.getCanonicalName() != null && p.getCanonicalName().contains("." + key + ".")) {
                try {
                    return p.getProperty().getValue();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

    @Test
    void layoutIsSelectedThenGivenItsSettingsThenShownAgainThenRun() {
        FakeLayoutController lc = new FakeLayoutController();
        Layout layout = new ForceAtlas2Builder().buildLayout();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("linLogMode", true);
        props.put("barnesHutOptimize", true);

        List<String> unapplied = GephiControlService.startLayoutThroughController(
            lc, layout, props, 300, DIRECT);

        // Selecting reloads the settings Gephi saved for this layout, so Gephi AI's settings go
        // on after it; selecting again makes the panel show them; then it runs.
        assertEquals(List.of("setLayout linLog=false", "setLayout linLog=true", "execute 300"), lc.calls);
        assertEquals(List.of("barnesHutOptimize"), unapplied);
        assertEquals(true, value(layout, "linLogMode"));
    }

    /**
     * Gephi's event thread: tasks run in the order they were posted, and a task posted while
     * another runs waits its turn. accept() behaves like the service's onEdt: it posts the task and
     * returns once that task has run, leaving anything posted after it still queued.
     */
    static class FakeEdt implements Consumer<Runnable> {
        final java.util.ArrayDeque<Runnable> queue = new java.util.ArrayDeque<>();

        void post(Runnable r) {
            queue.addLast(r);
        }

        @Override public void accept(Runnable r) {
            queue.addLast(r);
            Runnable next;
            do {
                next = queue.removeFirst();
                next.run();
            } while (next != r);
        }

        void drain() {
            while (!queue.isEmpty()) {
                queue.removeFirst().run();
            }
        }
    }

    /**
     * Gephi's Layout panel answers a layout newly selected in a workspace with no saved settings
     * for it by posting a load of the default preset to the event thread; with no preset saved,
     * that load resets the layout to its defaults.
     */
    static class PanelResettingController extends FakeLayoutController {
        final FakeEdt edt;

        PanelResettingController(FakeEdt edt) {
            this.edt = edt;
        }

        @Override public void setLayout(Layout l) {
            boolean newlySelected = l != selected;
            super.setLayout(l);
            if (newlySelected) {
                edt.post(l::resetPropertiesValues);
            }
        }
    }

    @Test
    void theLayoutPanelsQueuedDefaultsLandBeforeTheSettingsNotOnTopOfThem() {
        FakeEdt edt = new FakeEdt();
        PanelResettingController lc = new PanelResettingController(edt);
        Layout layout = new ForceAtlas2Builder().buildLayout();

        GephiControlService.startLayoutThroughController(lc, layout, Map.of("gravity", 5.0), 100, edt);
        edt.drain();

        // In 3 of 6 eval trials the first run reported applied gravity 1.0: the panel's reset ran
        // after the settings went on, and the layout ran on the defaults.
        assertEquals(5.0, value(layout, "gravity"));
        assertEquals("execute 100", lc.calls.get(lc.calls.size() - 1));
    }

    @Test
    void settingsNotGivenStartFromTheLayoutsDefaults() {
        FakeLayoutController lc = new FakeLayoutController();
        Layout layout = new ForceAtlas2Builder().buildLayout();
        layout.resetPropertiesValues();
        for (LayoutProperty p : layout.getProperties()) {
            if (p.getCanonicalName().contains(".linLogMode.")) {
                try {
                    p.getProperty().setValue(true);
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            }
        }

        GephiControlService.startLayoutThroughController(lc, layout, Map.of("gravity", 2.0), 10, DIRECT);

        assertEquals(false, value(layout, "linLogMode"), "a leftover setting must not carry into the run");
    }

    /** A dynamic statistic: Gephi steps it through the network's timeline window by window. */
    static class FakeDynamicStat implements org.gephi.statistics.spi.DynamicStatistics {
        double window = 1;
        double tick = 1;

        FakeDynamicStat() {
        }

        FakeDynamicStat(double window, double tick) {
            this.window = window;
            this.tick = tick;
        }

        @Override public void execute(GraphModel gm) {
        }

        @Override public String getReport() {
            return "";
        }

        @Override public void loop(org.gephi.graph.api.GraphView v, org.gephi.graph.api.Interval i) {
        }

        @Override public void end() {
        }

        @Override public double getWindow() {
            return window;
        }

        @Override public void setWindow(double w) {
            window = w;
        }

        @Override public double getTick() {
            return tick;
        }

        @Override public void setTick(double t) {
            tick = t;
        }

        @Override public org.gephi.graph.api.Interval getBounds() {
            return null;
        }

        @Override public void setBounds(org.gephi.graph.api.Interval b) {
        }
    }

    @Test
    void dynamicStatisticIsRefusedOnANetworkWithoutTimeData() {
        GraphModel gm = GraphModel.Factory.newInstance();
        gm.getDirectedGraph().addNode(gm.factory().newNode("a"));

        String problem = GephiControlService.dynamicStatisticProblem(new FakeDynamicStat(), gm);

        assertTrue(problem != null && problem.contains("time data"), String.valueOf(problem));
    }

    @Test
    void dynamicStatisticIsAllowedWhenTheNetworkHasTimeData() {
        org.gephi.graph.api.Configuration config = org.gephi.graph.api.Configuration.builder()
            .timeRepresentation(org.gephi.graph.api.TimeRepresentation.INTERVAL).build();
        GraphModel gm = GraphModel.Factory.newInstance(config);
        org.gephi.graph.api.Node n = gm.factory().newNode("a");
        n.addInterval(new org.gephi.graph.api.Interval(1, 5));
        gm.getDirectedGraph().addNode(n);

        assertEquals(null, GephiControlService.dynamicStatisticProblem(new FakeDynamicStat(), gm));
    }

    private static GraphModel timedNetwork(double low, double high) {
        org.gephi.graph.api.Configuration config = org.gephi.graph.api.Configuration.builder()
            .timeRepresentation(org.gephi.graph.api.TimeRepresentation.INTERVAL).build();
        GraphModel gm = GraphModel.Factory.newInstance(config);
        org.gephi.graph.api.Node n = gm.factory().newNode("a");
        n.addInterval(new org.gephi.graph.api.Interval(low, high));
        gm.getDirectedGraph().addNode(n);
        return gm;
    }

    @Test
    void dynamicStatisticWithoutAStepIsRefusedAndTheTimeSpanIsGiven() {
        // Gephi's dynamic statistics start with window 0 and tick 0; its settings dialog fills
        // them in. Run with tick 0, Gephi's loop never advances.
        String problem = GephiControlService.dynamicStatisticProblem(new FakeDynamicStat(0, 0),
            timedNetwork(1990, 2000));

        assertTrue(problem != null && problem.contains("tick") && problem.contains("1990")
            && problem.contains("2000"), String.valueOf(problem));
    }

    @Test
    void windowWiderThanTheNetworksTimeSpanIsRefused() {
        String problem = GephiControlService.dynamicStatisticProblem(new FakeDynamicStat(50, 1),
            timedNetwork(1990, 2000));

        assertTrue(problem != null && problem.contains("window"), String.valueOf(problem));
    }

    @Test
    void dynamicStatisticWithAWindowAndStepInsideTheSpanRuns() {
        assertEquals(null, GephiControlService.dynamicStatisticProblem(new FakeDynamicStat(2, 1),
            timedNetwork(1990, 2000)));
    }

    @Test
    void anOrdinaryStatisticIsNeverRefusedForLackingTimeData() {
        GraphModel gm = GraphModel.Factory.newInstance();

        assertEquals(null, GephiControlService.dynamicStatisticProblem(new SlowStat(1), gm));
    }

    @Test
    void thePanelIsUsedOnlyOnceTheStatisticsWindowHasSetUpTheWorkspace() {
        // Before the Statistics window gives the workspace its model, the panel's execute()
        // returns without running anything or reporting back, so waiting on it would hang.
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        Workspace ws = pc.getCurrentWorkspace();
        FakeStatsUI panel = new FakeStatsUI();

        assertNull(GephiControlService.statisticsPanelFor(ws, panel));
        assertNull(GephiControlService.statisticsPanelFor(null, panel));
        assertNull(GephiControlService.statisticsPanelFor(ws, null));

        ws.add(new FakeStatsModel());
        assertSame(panel, GephiControlService.statisticsPanelFor(ws, panel));
    }

    /** What the Statistics window adds to each workspace it has set up. */
    static class FakeStatsModel implements StatisticsModelUI {
        @Override public String getResult(StatisticsUI ui) {
            return null;
        }

        @Override public boolean isStatisticsUIVisible(StatisticsUI ui) {
            return true;
        }

        @Override public boolean isRunning(StatisticsUI ui) {
            return false;
        }

        @Override public Statistics getRunning(StatisticsUI ui) {
            return null;
        }

        @Override public String getReport(Class<? extends Statistics> c) {
            return null;
        }

        @Override public void addChangeListener(ChangeListener l) {
        }

        @Override public void removeChangeListener(ChangeListener l) {
        }
    }

    @Test
    void statisticRunDirectlyPastItsDeadlineIsReportedStopped() throws Exception {
        SlowStat stat = new SlowStat(60_000);

        assertTrue(GephiControlService.executeStatistic(stat, null, 200, null, DIRECT));
        assertTrue(stat.cancelled.get());
    }

    /** A run the panel declined is run directly, and its deadline still applies and is reported. */
    @Test
    void declinedRunPastItsDeadlineIsReportedStopped() throws Exception {
        SlowStat stat = new SlowStat(60_000);

        assertTrue(GephiControlService.executeStatistic(stat, null, 200, new DecliningStatsUI(), DIRECT,
            s -> neverMarked()));
        assertTrue(stat.cancelled.get());
    }

    /** The watch on the panel is released after every run, so its record of the run does not linger. */
    @Test
    void thePanelWatchIsClosedAfterTheRun() throws Exception {
        java.util.concurrent.atomic.AtomicInteger closed = new java.util.concurrent.atomic.AtomicInteger();
        GephiControlService.PanelRunMark watch = new GephiControlService.PanelRunMark() {
            @Override public boolean isMarked() {
                return true;
            }

            @Override public boolean wasEverMarked() {
                return true;
            }

            @Override public void close() {
                closed.incrementAndGet();
            }
        };

        GephiControlService.executeStatistic(new SlowStat(10), null, 0, new FakeStatsUI(), DIRECT, s -> watch);

        assertEquals(1, closed.get());
    }

    /**
     * Statistics run on a daemon worker, so one left running never keeps Gephi from quitting. The
     * request comes from an ordinary thread here, since a new thread copies its creator's setting.
     */
    @Test
    void theStatisticWorkerIsADaemon() throws Exception {
        AtomicBoolean daemon = new AtomicBoolean();
        Thread request = new Thread(() -> GephiControlService.runOnStatisticThread(
            () -> daemon.set(Thread.currentThread().isDaemon()), () -> { }), "request");
        request.setDaemon(false);
        request.start();
        request.join(10_000);

        assertTrue(daemon.get());
    }

    /**
     * A statistic that ignores cancel() but answers an interrupt is interrupted too, and is given time
     * to stop before the request returns; the request keeps its interrupted status.
     */
    @Test
    void interruptedRequestInterruptsTheWorkerAndWaitsForIt() throws Exception {
        AtomicBoolean started = new AtomicBoolean();
        AtomicBoolean workerInterrupted = new AtomicBoolean();
        AtomicBoolean workerDone = new AtomicBoolean();
        AtomicBoolean doneWhenReturned = new AtomicBoolean();
        AtomicBoolean requestStillInterrupted = new AtomicBoolean();
        Thread request = new Thread(() -> {
            try {
                GephiControlService.runOnStatisticThread(() -> {
                    started.set(true);
                    try {
                        Thread.sleep(60_000);
                    } catch (InterruptedException e) {
                        workerInterrupted.set(true);
                        long until = System.nanoTime() + 200_000_000L;
                        while (System.nanoTime() < until) {
                            Thread.onSpinWait();
                        }
                        workerDone.set(true);
                    }
                }, () -> { });
            } catch (RuntimeException expected) {
                doneWhenReturned.set(workerDone.get());
                requestStillInterrupted.set(Thread.currentThread().isInterrupted());
            }
        }, "request");
        request.start();
        long until = System.nanoTime() + 5_000_000_000L;
        while (!started.get() && System.nanoTime() < until) {
            Thread.onSpinWait();
        }

        request.interrupt();
        request.join(10_000);

        assertTrue(workerInterrupted.get(), "the worker was not interrupted");
        assertTrue(doneWhenReturned.get(), "the request returned before the worker stopped");
        assertTrue(requestStillInterrupted.get(), "the request lost its interrupted status");
    }

    /** One listener is added the first time a model is watched. */
    @Test
    void theFirstWatchOfAModelAddsItsListener() {
        CountingModelUI model = new CountingModelUI();

        GephiControlService.watchPanel(new CountingStat(), List.of(model), List.of()).close();

        assertEquals(1, model.added.get());
    }

    /** A model that keeps the listener it is given, and can fire it. */
    static class ListeningModelUI extends FakeModelUI {
        final List<ChangeListener> listeners = new ArrayList<>();

        @Override public void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }

        void fire() {
            for (ChangeListener l : listeners) {
                l.stateChanged(new javax.swing.event.ChangeEvent(this));
            }
        }
    }

    @Test
    void statisticThePanelShowsRunningIsMarked() {
        CountingStat stat = new CountingStat();
        FakeModelUI model = new FakeModelUI();
        model.running = stat;

        GephiControlService.PanelRunMark mark =
            GephiControlService.watchPanel(stat, List.of(model), List.of(statisticsUiFor(CountingStat.class)));
        try {
            assertTrue(mark.isMarked());
        } finally {
            mark.close();
        }
    }

    /** A mark the panel sets and clears between two checks is still seen, through its change event. */
    @Test
    void markSetAndClearedBetweenChecksIsStillSeen() {
        CountingStat stat = new CountingStat();
        ListeningModelUI model = new ListeningModelUI();
        GephiControlService.PanelRunMark mark =
            GephiControlService.watchPanel(stat, List.of(model), List.of(statisticsUiFor(CountingStat.class)));
        try {
            model.running = stat;
            model.fire();
            model.running = null;

            assertFalse(mark.isMarked());
            assertTrue(mark.wasEverMarked(), "the change event was not recorded");
        } finally {
            mark.close();
        }
    }
}
