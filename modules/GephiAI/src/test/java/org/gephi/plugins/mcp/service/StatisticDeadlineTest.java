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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.junit.jupiter.api.Test;

/**
 * A statistic run with a deadline is cancelled when the deadline passes, so it releases the graph
 * lock (before Gephi 0.11.4, modularity could loop forever, gephi#1630), and the caller is told it
 * was stopped so a stale column is never read back as a result.
 */
class StatisticDeadlineTest {

    /** Spins until cancelled, the way a statistic that outlasts its deadline does. */
    static class Spinner implements LongTask, Runnable {
        final AtomicBoolean cancelled = new AtomicBoolean();

        @Override public void run() {
            while (!cancelled.get()) {
                Thread.onSpinWait();
            }
        }

        @Override public boolean cancel() {
            cancelled.set(true);
            return true;
        }

        @Override public void setProgressTicket(ProgressTicket t) {
        }
    }

    @Test
    void taskThatNeverFinishesIsStoppedAtTheDeadline() {
        Spinner s = new Spinner();
        long start = System.nanoTime();
        boolean stopped = GephiControlService.runWithDeadline(s, s, 200);
        long ms = (System.nanoTime() - start) / 1_000_000;

        assertTrue(stopped);
        assertTrue(s.cancelled.get());
        assertTrue(ms >= 150 && ms < 5_000, "stopped after " + ms + " ms");
    }

    @Test
    void taskThatFinishesInTimeIsNotReportedAsStoppedOrCancelledLater() throws Exception {
        Spinner s = new Spinner();
        boolean stopped = GephiControlService.runWithDeadline(() -> {
        }, s, 200);
        Thread.sleep(400);

        assertFalse(stopped);
        assertFalse(s.cancelled.get(), "the deadline must be withdrawn once the task finishes");
    }

    @Test
    void noDeadlineMeansNoWatchdog() {
        Spinner s = new Spinner();
        assertFalse(GephiControlService.runWithDeadline(() -> {
        }, s, 0));
        assertFalse(s.cancelled.get());
    }
}
