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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The colour key is drawn only when the named column fully explains the node
 * colours. A key built on a guess would be confidently wrong, which is worse in
 * a published figure than no key at all.
 */
public class ColourKeyTest {

    private static final Color GREEN = new Color(0x00, 0x83, 0x00);
    private static final Color BLUE = new Color(0x2A, 0x78, 0xD6);
    private static final Color ORANGE = new Color(0xED, 0xA1, 0x00);

    @Test
    public void eachGroupGetsItsOwnColourLargestFirst() {
        ColourKey key = ColourKey.read("project",
            Arrays.asList("lab", "field", "field", "field", "lab"),
            Arrays.asList(GREEN, BLUE, BLUE, BLUE, GREEN));

        assertNull(key.problem);
        assertEquals("project", key.title);
        assertEquals(2, key.rows.size());
        assertEquals("field", key.rows.get(0).label);
        assertEquals(BLUE, key.rows.get(0).fill);
        assertEquals("lab", key.rows.get(1).label);
        assertEquals(GREEN, key.rows.get(1).fill);
    }

    @Test
    public void aGroupOfMixedColoursRefusesTheKeyAndSaysWhich() {
        ColourKey key = ColourKey.read("project",
            Arrays.asList("field", "field", "lab"),
            Arrays.asList(BLUE, ORANGE, GREEN));

        assertTrue(key.rows.isEmpty());
        assertNotNull(key.problem);
        assertTrue(key.problem, key.problem.contains("'field'"));
        assertTrue(key.problem, key.problem.contains("more than one colour"));
    }

    @Test
    public void twoGroupsSharingAColourRefuseTheKey() {
        ColourKey key = ColourKey.read("project",
            Arrays.asList("field", "lab", "studio"),
            Arrays.asList(BLUE, BLUE, GREEN));

        assertTrue(key.rows.isEmpty());
        assertTrue(key.problem, key.problem.contains("share"));
    }

    @Test
    public void transparencyDoesNotSplitAGroup() {
        // Per-node opacity varies alpha, not the colour a reader matches to the key.
        ColourKey key = ColourKey.read("project", Arrays.asList("a", "a"),
            Arrays.asList(new Color(42, 120, 214, 255), new Color(42, 120, 214, 90)));

        assertNull(key.problem);
        assertEquals(255, key.rows.get(0).fill.getAlpha());
    }

    @Test
    public void nodesWithNoValueAreARowWhenTheyShareAColour() {
        ColourKey key = ColourKey.read("project", Arrays.asList("a", "a", null),
            Arrays.asList(BLUE, BLUE, ORANGE));

        assertNull(key.problem);
        assertEquals("no value", key.rows.get(key.rows.size() - 1).label);
        assertEquals(ORANGE, key.rows.get(key.rows.size() - 1).fill);
    }

    @Test
    public void nodesWithNoValueInMixedColoursRefuseTheKey() {
        ColourKey key = ColourKey.read("project", Arrays.asList("a", null, null),
            Arrays.asList(BLUE, ORANGE, GREEN));

        assertTrue(key.rows.isEmpty());
        assertTrue(key.problem, key.problem.contains("no value"));
    }

    @Test
    public void aMissingColourRefusesTheKey() {
        ColourKey key = ColourKey.read("project", Arrays.asList("a", "b"), Arrays.asList(BLUE, null));

        assertTrue(key.rows.isEmpty());
        assertNotNull(key.problem);
    }

    @Test
    public void longPartitionsAreCutAndCounted() {
        List<Object> values = new ArrayList<>();
        List<Color> colors = new ArrayList<>();
        for (int i = 0; i < ShapeLegend.MAX_ROWS + 3; i++) {
            values.add("g" + i);
            colors.add(new Color(i * 10, 40, 200 - i * 5));
        }
        ColourKey key = ColourKey.read("group", values, colors);

        assertNull(key.problem);
        assertEquals(ShapeLegend.MAX_ROWS, key.rows.size());
        assertEquals(3, key.hidden);
    }

    @Test
    public void anEmptyColumnHasNothingToKey() {
        ColourKey key = ColourKey.read("project", Arrays.asList(null, null),
            Arrays.asList(BLUE, BLUE));

        assertTrue(key.rows.isEmpty());
        assertTrue(key.problem, key.problem.contains("no values"));
    }
}
