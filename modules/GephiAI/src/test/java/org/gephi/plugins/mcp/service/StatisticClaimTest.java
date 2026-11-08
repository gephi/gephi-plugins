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
}
