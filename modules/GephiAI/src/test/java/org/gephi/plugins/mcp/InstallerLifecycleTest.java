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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/**
 * Guards the startup and shutdown race: restored() delays the server start, and
 * a module disable that lands inside that delay must cancel the start instead
 * of leaving an unreachable server running.
 */
class InstallerLifecycleTest {

    @Test
    void closeDuringStartupDelayCancelsTheStart() {
        Installer installer = new Installer();
        // The module is disabled while the delayed start is still pending.
        installer.close();
        // The delayed start arrives afterwards; it must not construct or bind a server.
        assertNull(installer.startNow());
        assertFalse(installer.isRunningNow());
    }

    @Test
    void closingDuringStartupDelayCancelsTheStart() {
        Installer installer = new Installer();
        installer.closing();
        assertNull(installer.startNow());
        assertFalse(installer.isRunningNow());
    }
}
