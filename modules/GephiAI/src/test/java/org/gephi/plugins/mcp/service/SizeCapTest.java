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

import org.junit.jupiter.api.Test;

/**
 * A cap keeps a few outliers (a mailing list with 220 contacts among people with 20) from
 * shrinking every other node: values at or above the cap get the largest size, and the rest
 * spread over the whole range.
 */
class SizeCapTest {

    @Test
    void withoutACapTheRangeRunsFromTheSmallestToTheLargestValue() {
        assertEquals(10f, GephiControlService.rankedSize(0, 0, 200, null, 10, 100), 1e-4);
        assertEquals(100f, GephiControlService.rankedSize(200, 0, 200, null, 10, 100), 1e-4);
        assertEquals(19f, GephiControlService.rankedSize(20, 0, 200, null, 10, 100), 1e-4);
    }

    @Test
    void valuesAtOrAboveTheCapGetTheLargestSize() {
        assertEquals(100f, GephiControlService.rankedSize(200, 0, 200, 20.0, 10, 100), 1e-4);
        assertEquals(100f, GephiControlService.rankedSize(20, 0, 200, 20.0, 10, 100), 1e-4);
    }

    @Test
    void valuesBelowTheCapSpreadOverTheWholeRange() {
        assertEquals(55f, GephiControlService.rankedSize(10, 0, 200, 20.0, 10, 100), 1e-4);
    }

    @Test
    void capAboveTheLargestValueChangesNothing() {
        assertEquals(19f, GephiControlService.rankedSize(20, 0, 200, 500.0, 10, 100), 1e-4);
    }
}
