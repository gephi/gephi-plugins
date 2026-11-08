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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * "Who are the top ten?" is the commonest question asked of a network. Nodes are sorted by a
 * column before paging, so the first page is the top of the whole graph, not of the first ids.
 */
class NodeSortTest {

    private static final Map<String, Object> VALUES = new HashMap<>();

    static {
        VALUES.put("a", 3.0);
        VALUES.put("b", 10);
        VALUES.put("c", null);
        VALUES.put("d", 7.5);
    }

    @Test
    void largestFirstWhenDescending() {
        List<String> sorted = GephiControlService.sortByValue(List.of("a", "b", "c", "d"), VALUES::get, true);
        assertEquals(List.of("b", "d", "a", "c"), sorted);
    }

    @Test
    void smallestFirstWhenAscendingWithMissingValuesStillLast() {
        List<String> sorted = GephiControlService.sortByValue(List.of("a", "b", "c", "d"), VALUES::get, false);
        assertEquals(List.of("a", "d", "b", "c"), sorted);
    }

    @Test
    void textSortsAlphabeticallyIgnoringCase() {
        Map<String, Object> names = Map.of("x", "banana", "y", "Apple", "z", "cherry");
        List<String> sorted = GephiControlService.sortByValue(List.of("x", "y", "z"), names::get, false);
        assertEquals(List.of("y", "x", "z"), sorted);
    }

    @Test
    void requestedColumnIsKeptByIdOrByTitleIgnoringCase() {
        Set<String> wanted = GephiControlService.wantedColumns("pageranks, Betweenness Centrality");
        assertTrue(GephiControlService.isWanted(wanted, "pageranks", "PageRank"));
        assertTrue(GephiControlService.isWanted(wanted, "betweenesscentrality", "Betweenness Centrality"));
        assertFalse(GephiControlService.isWanted(wanted, "eccentricity", "Eccentricity"));
    }

    @Test
    void noRequestedColumnsKeepsEveryColumn() {
        Set<String> wanted = GephiControlService.wantedColumns(null);
        assertTrue(GephiControlService.isWanted(wanted, "eccentricity", "Eccentricity"));
    }
}
