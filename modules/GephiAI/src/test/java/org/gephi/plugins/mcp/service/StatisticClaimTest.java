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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.Modularity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * One run of a statistic at a time. A second request for a statistic already running is refused
 * rather than started alongside it, where it would repeat the work and keep the graph's read lock
 * held for both runs.
 */
class StatisticClaimTest {

    @AfterEach
    void clear() {
        GephiControlService.RUNNING_STATISTICS.clear();
        GephiControlService.STOP_REQUESTED.clear();
    }

    @Test
    void secondRunOfTheSameStatisticIsRefused() {
        assertNull(GephiControlService.claimStatistic(new Modularity(), "Modularity"));
        String refused = GephiControlService.claimStatistic(new Modularity(), "Modularity");
        assertNotNull(refused, "a second Modularity run was allowed to start");
        assertTrue(refused.contains("already running"), refused);
    }

    @Test
    void differentStatisticMayRunAlongside() {
        assertNull(GephiControlService.claimStatistic(new Modularity(), "Modularity"));
        assertNull(GephiControlService.claimStatistic(new Degree(), "Degree"));
    }

    @Test
    void theStatisticCanRunAgainOnceTheFirstRunEnds() {
        Modularity first = new Modularity();
        assertNull(GephiControlService.claimStatistic(first, "Modularity"));
        GephiControlService.RUNNING_STATISTICS.remove(first);
        assertNull(GephiControlService.claimStatistic(new Modularity(), "Modularity"));
    }

    /** A stop that reaches a run after it was released would leave a mark nothing ever clears. */
    @Test
    void stopDoesNotMarkARunThatHasEnded() {
        Modularity stat = new Modularity();
        assertNull(GephiControlService.claimStatistic(stat, "Modularity"));
        assertFalse(GephiControlService.releaseStatistic(stat));

        assertFalse(GephiControlService.markStopRequested(stat));
        assertTrue(GephiControlService.STOP_REQUESTED.isEmpty());
    }

    @Test
    void stopMarksARunStillClaimedAndTheReleaseReportsIt() {
        Modularity stat = new Modularity();
        assertNull(GephiControlService.claimStatistic(stat, "Modularity"));

        assertTrue(GephiControlService.markStopRequested(stat));
        assertTrue(GephiControlService.releaseStatistic(stat), "the release did not report the stop");
        assertTrue(GephiControlService.STOP_REQUESTED.isEmpty());
    }

    /**
     * A request that gives up on a statistic still running in Gephi keeps it recorded as running:
     * a stop request must still reach it, and a second run of it must still be refused.
     */
    @Test
    void statisticStillRunningStaysClaimedUntilItEnds() throws Exception {
        Modularity stat = new Modularity();
        assertNull(GephiControlService.claimStatistic(stat, "Modularity"));
        java.util.concurrent.CountDownLatch ended = new java.util.concurrent.CountDownLatch(1);

        final Thread watch = GephiControlService.releaseWhenEnded(stat,
            new GephiControlService.StatisticStillRunningException("still running", ended::await));

        Thread.sleep(100);
        assertNotNull(GephiControlService.claimStatistic(new Modularity(), "Modularity"),
            "a second run started beside one still running");
        assertTrue(GephiControlService.markStopRequested(stat), "a stop could not reach the running statistic");

        ended.countDown();
        watch.join(5_000);
        assertFalse(watch.isAlive());
        assertNull(GephiControlService.claimStatistic(new Modularity(), "Modularity"));
        assertFalse(GephiControlService.STOP_REQUESTED.contains(stat));
    }
}
