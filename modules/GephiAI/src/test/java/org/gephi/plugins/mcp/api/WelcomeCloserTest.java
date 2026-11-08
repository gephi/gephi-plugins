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

package org.gephi.plugins.mcp.api;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JLabel;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

/**
 * Gephi's Welcome window can stay open over the graph when work starts through the API right
 * after launch. The plugin closes it on the first request that does work, but only in the first
 * minutes after startup, so a Welcome the person reopens later from the Help menu is left alone.
 */
class WelcomeCloserTest {

    /** Stands in for Gephi's welcome panel, found by class name as the real one is. */
    static class FakeWelcomePanel extends JPanel {
    }

    @Test
    void theWelcomePanelIsFoundAnywhereInsideAWindow() {
        JPanel outer = new JPanel();
        JPanel middle = new JPanel();
        middle.add(new FakeWelcomePanel());
        outer.add(new JLabel("header"));
        outer.add(middle);

        assertTrue(WelcomeCloser.contains(outer, FakeWelcomePanel.class.getName()));
    }

    @Test
    void windowWithoutItIsLeftAlone() {
        JPanel outer = new JPanel();
        outer.add(new JLabel("Screenshot saved"));

        assertFalse(WelcomeCloser.contains(outer, FakeWelcomePanel.class.getName()));
    }

    @Test
    void itTriesOnlyInTheFirstMinutesAndOnlyUntilItHasClosedOne() {
        assertTrue(WelcomeCloser.shouldTry(60_000, false));
        assertFalse(WelcomeCloser.shouldTry(60_000, true));
        assertFalse(WelcomeCloser.shouldTry(WelcomeCloser.WINDOW_MS + 1, false));
    }
}
