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

import org.junit.jupiter.api.Test;

/** Unit tests for the DNS-rebinding Host-header guard. */
class HostHeaderTest {

    @Test
    void loopbackHostsAreAccepted() {
        assertTrue(GephiAPIServer.isLoopbackHost("127.0.0.1:8080"));
        assertTrue(GephiAPIServer.isLoopbackHost("127.0.0.1"));
        assertTrue(GephiAPIServer.isLoopbackHost("localhost:8080"));
        assertTrue(GephiAPIServer.isLoopbackHost("localhost"));
        assertTrue(GephiAPIServer.isLoopbackHost("LOCALHOST:8080"));
        assertTrue(GephiAPIServer.isLoopbackHost("[::1]:8080"));
        assertTrue(GephiAPIServer.isLoopbackHost("[::1]"));
    }

    @Test
    void missingHostHeaderIsAllowed() {
        // Non-browser clients (e.g. the MCP server) may omit Host; browsers never do,
        // so this does not open a browser bypass.
        assertTrue(GephiAPIServer.isLoopbackHost(null));
        assertTrue(GephiAPIServer.isLoopbackHost(""));
    }

    @Test
    void rebindingAndRemoteHostsAreRejected() {
        assertFalse(GephiAPIServer.isLoopbackHost("evil.com"));
        assertFalse(GephiAPIServer.isLoopbackHost("evil.com:8080"));
        assertFalse(GephiAPIServer.isLoopbackHost("attacker.localhost.evil.com"));
        assertFalse(GephiAPIServer.isLoopbackHost("127.0.0.1.evil.com"));
        assertFalse(GephiAPIServer.isLoopbackHost("192.168.1.5:8080"));
        assertFalse(GephiAPIServer.isLoopbackHost("0.0.0.0:8080"));
    }
}
