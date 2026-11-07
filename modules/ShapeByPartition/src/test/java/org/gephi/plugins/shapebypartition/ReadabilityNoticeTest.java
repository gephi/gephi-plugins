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
package org.gephi.plugins.shapebypartition;

import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the warning the renderer raises when a partition is too large for
 * shape to carry it.
 *
 * @author Matt Artz
 */
public class ReadabilityNoticeTest {

    @Test
    public void saysNothingAboutAPartitionShapeCanCarry() {
        for (int n = 0; n <= 5; n++) {
            assertNull("nothing to report for " + n + " categories",
                ShapeByPartitionRenderer.readabilityNotice("team", n));
        }
    }

    @Test
    public void warnsWhenTheOutlinesStopBeingDistinguishable() {
        // Six to eight categories still get a distinct shape each, so this is a
        // caution about the reader rather than about the mapping.
        for (int n = 6; n <= NodeShape.values().length; n++) {
            String m = ShapeByPartitionRenderer.readabilityNotice("team", n);
            assertNotNull("expected a caution for " + n + " categories", m);
            assertTrue(m, m.contains("separates about"));
        }
    }

    @Test
    public void warnsHarderWhenTwoCategoriesShareAShape() {
        int over = NodeShape.values().length + 1;
        String m = ShapeByPartitionRenderer.readabilityNotice("team", over);

        assertNotNull(m);
        assertTrue("must say the shapes repeat, which is a correctness problem "
            + "rather than a legibility one: " + m, m.contains("repeat"));
    }

    @Test
    public void namesTheColumnSoTheWarningIsActionable() {
        String m = ShapeByPartitionRenderer.readabilityNotice("account_type", 12);

        assertTrue(m, m.contains("account_type"));
        assertTrue(m, m.contains("12"));
    }

    @Test
    public void theBoundaryBetweenTheTwoWarningsIsTheVocabularySize() {
        int v = NodeShape.values().length;

        assertTrue(ShapeByPartitionRenderer.readabilityNotice("c", v).contains("separates about"));
        assertTrue(ShapeByPartitionRenderer.readabilityNotice("c", v + 1).contains("repeat"));
    }
}
