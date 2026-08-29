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

/**
 * Guards against a web page driving the API.
 *
 * <p>The Host-header check alone is not enough. A page the user is merely visiting can call
 * {@code fetch("http://127.0.0.1:8080/graph/clear", {method:"POST", mode:"no-cors"})}: the
 * browser sends {@code Host: 127.0.0.1:8080}, which the loopback check accepts, and a
 * {@code text/plain} body is CORS-safelisted so no preflight is issued. CORS stops the page
 * reading the reply, but the side effect has already happened, which is all an attacker needs
 * to clear a workspace or write a file through an export endpoint.
 *
 * <p>Browsers attach {@code Origin} to such a request and {@code Sec-Fetch-Site} to every
 * request, and page JavaScript cannot forge or suppress either. Non-browser clients (the MCP
 * server, curl) send neither, so rejecting on them costs nothing.
 */
class BrowserOriginTest {

    @Test
    void nonBrowserClientsAreAccepted() {
        // No Origin, no Sec-Fetch-Site: the MCP server, curl, any local process.
        assertTrue(GephiAPIServer.isNonBrowserRequest(null, null));
        assertTrue(GephiAPIServer.isNonBrowserRequest("", ""));
    }

    @Test
    void requestsCarryingAnOriginAreRejected() {
        assertFalse(GephiAPIServer.isNonBrowserRequest("https://evil.example", null));
        assertFalse(GephiAPIServer.isNonBrowserRequest("http://localhost:3000", null));
        assertFalse(GephiAPIServer.isNonBrowserRequest("null", null));
    }

    @Test
    void crossSiteAndSameOriginBrowserFetchesAreRejected() {
        assertFalse(GephiAPIServer.isNonBrowserRequest(null, "cross-site"));
        assertFalse(GephiAPIServer.isNonBrowserRequest(null, "same-site"));
        assertFalse(GephiAPIServer.isNonBrowserRequest(null, "same-origin"));
    }

    @Test
    void userTypedNavigationIsStillRejectedWhenItCarriesAFetchMetadataHeader() {
        // Sec-Fetch-Site: none means the user typed the URL or used a bookmark. That is a
        // browser, and the API is not a browsing surface, so it is refused like any other.
        assertFalse(GephiAPIServer.isNonBrowserRequest(null, "none"));
    }
}
