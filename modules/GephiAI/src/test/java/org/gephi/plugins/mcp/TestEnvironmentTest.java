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

package org.gephi.plugins.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import org.junit.jupiter.api.Test;

/**
 * The module's installer runs inside the test JVM and starts the API server. Without these
 * settings it binds 8080, and when a Gephi on the same machine already holds that port the
 * failure opens an error dialog on the desktop in the middle of a test run.
 */
class TestEnvironmentTest {

    @Test
    void testsRunHeadless() {
        assertTrue(GraphicsEnvironment.isHeadless(), "tests must run with java.awt.headless=true");
    }

    @Test
    void theServerStartedByTheInstallerStaysOffPort8080() {
        assertEquals(0, Installer.getPreferredPort(), "tests must bind an ephemeral port, never 8080");
    }
}
