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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Boots the real server on an ephemeral port and exercises it over HTTP, so the
 * seam that ships is the seam that is tested. Covers the /health liveness probe
 * and the DELETE /workspace/delete contract, whose index is a query parameter
 * (request bodies are parsed for POST and PUT only).
 */
class ApiSmokeTest {

    @Test
    void healthAnswersAndWorkspaceDeleteRequiresTheIndexQueryParameter() throws Exception {
        GephiAPIServer server = new GephiAPIServer(0);
        server.startServer();
        try {
            int port = server.getListeningPort();

            HttpURLConnection health = open(port, "/health", "GET");
            assertEquals(200, health.getResponseCode());
            String healthBody = read(health.getInputStream());
            assertTrue(healthBody.contains("\"success\""), healthBody);
            assertTrue(healthBody.contains("running"), healthBody);

            HttpURLConnection delete = open(port, "/workspace/delete", "DELETE");
            assertEquals(400, delete.getResponseCode());
            String deleteBody = read(delete.getErrorStream());
            assertTrue(deleteBody.contains("query parameter"), deleteBody);

            HttpURLConnection deleteWithParam = open(port, "/workspace/delete?index=abc", "DELETE");
            assertEquals(400, deleteWithParam.getResponseCode());
            String badIndexBody = read(deleteWithParam.getErrorStream());
            assertTrue(badIndexBody.contains("query parameter"), badIndexBody);
        } finally {
            // NanoHTTPD's own stop(); avoids shutting down the shared service
            // singleton that other tests in the suite may still use.
            server.stop();
        }
    }

    private static HttpURLConnection open(int port, String path, String method) throws Exception {
        URL url = new URL("http://127.0.0.1:" + port + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        return conn;
    }

    private static String read(InputStream in) throws Exception {
        if (in == null) {
            return "";
        }
        try (InputStream is = in) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }
}
