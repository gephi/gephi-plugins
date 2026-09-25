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
package org.gephi.plugins.mcp.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Unit tests for the port field validation in the server control dialog. */
class PortParseTest {

    @Test
    void validPortsAreAccepted() {
        assertEquals(8080, ServerControlPanel.parsePort("8080"));
        assertEquals(1024, ServerControlPanel.parsePort("1024"));
        assertEquals(65535, ServerControlPanel.parsePort("65535"));
        assertEquals(8080, ServerControlPanel.parsePort("  8080  "));
    }

    @Test
    void invalidPortsAreRejected() {
        assertEquals(-1, ServerControlPanel.parsePort(null));
        assertEquals(-1, ServerControlPanel.parsePort(""));
        assertEquals(-1, ServerControlPanel.parsePort("abc"));
        assertEquals(-1, ServerControlPanel.parsePort("-1"));
        assertEquals(-1, ServerControlPanel.parsePort("0"));
        assertEquals(-1, ServerControlPanel.parsePort("80"));
        assertEquals(-1, ServerControlPanel.parsePort("1023"));
        assertEquals(-1, ServerControlPanel.parsePort("65536"));
        assertEquals(-1, ServerControlPanel.parsePort("8080.5"));
    }
}
