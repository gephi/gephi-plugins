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
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The HTTP server over a real connection. Requests are written by hand on a socket, so a test
 * can send what a browser or a rebinding page would send, including a Host header that Java's
 * own HTTP client refuses to set.
 */
class HttpServerTest {

    private static GephiAPIServer server;
    private static int port;

    @BeforeAll
    static void start() throws Exception {
        server = new GephiAPIServer(0);
        server.startServer();
        port = server.getListeningPort();
    }

    @AfterAll
    static void stop() {
        server.stop();
    }

    /** Sends a raw request and returns the status code and the body. */
    private static String[] send(String request) throws Exception {
        try (Socket s = new Socket("127.0.0.1", port)) {
            s.setSoTimeout(5000);
            OutputStream out = s.getOutputStream();
            out.write(request.getBytes(StandardCharsets.UTF_8));
            out.flush();
            InputStream in = s.getInputStream();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] b = new byte[4096];
            int n;
            while ((n = in.read(b)) > 0) {
                buf.write(b, 0, n);
            }
            String response = new String(buf.toByteArray(), StandardCharsets.UTF_8);
            String status = response.split(" ", 3)[1];
            int split = response.indexOf("\r\n\r\n");
            return new String[]{status, split < 0 ? "" : response.substring(split + 4)};
        }
    }

    private static String get(String path, String... headers) throws Exception {
        StringBuilder r = new StringBuilder("GET " + path + " HTTP/1.1\r\nHost: 127.0.0.1\r\n");
        for (String h : headers) {
            r.append(h).append("\r\n");
        }
        return String.join("\n", send(r.append("Connection: close\r\n\r\n").toString()));
    }

    @Test
    void requestFromABrowserIsRefused() throws Exception {
        assertTrue(get("/health", "Origin: https://example.com").startsWith("403\n"));
        assertTrue(get("/health", "Sec-Fetch-Site: cross-site").startsWith("403\n"));
    }

    @Test
    void requestForAnotherHostIsRefused() throws Exception {
        String[] r = send("GET /health HTTP/1.1\r\nHost: attacker.example:8080\r\nConnection: close\r\n\r\n");
        assertEquals("403", r[0]);
        assertTrue(r[1].contains("localhost"), r[1]);
    }

    @Test
    void localRequestIsAnswered() throws Exception {
        String r = get("/health");
        assertTrue(r.startsWith("200\n") && r.contains("running"), r);
    }

    @Test
    void healthReportsTheGephiVersion() throws Exception {
        // The server compares it with the Gephi the newest plugin needs before advising an update.
        String r = get("/health");
        assertTrue(r.matches("(?s).*\"gephi_version\": \"\\d+\\.\\d+(\\.\\d+)?\".*"), r);
    }

    @Test
    void optionsIsAnsweredWithNoBody() throws Exception {
        String[] r = send("OPTIONS /health HTTP/1.1\r\nHost: 127.0.0.1\r\nConnection: close\r\n\r\n");
        assertEquals("200", r[0]);
        assertEquals("", r[1]);
    }

    @Test
    void anUnknownEndpointIsAJsonError() throws Exception {
        String r = get("/no/such/thing");
        assertTrue(r.startsWith("400\n") && r.contains("Unknown endpoint"), r);
    }

    @Test
    void malformedJsonIsAJsonErrorNotADroppedConnection() throws Exception {
        String body = "{not json";
        String[] r = send("POST /graph/nodes/add HTTP/1.1\r\nHost: 127.0.0.1\r\nContent-Type: application/json\r\n"
            + "Content-Length: " + body.length() + "\r\nConnection: close\r\n\r\n" + body);
        assertEquals("500", r[0]);
        assertTrue(r[1].contains("\"success\": false"), r[1]);
    }

    @Test
    void chunkedBodyIsRead() throws Exception {
        // Read as empty, this request would be refused as "Missing 'nodes' array".
        String json = "{\"nodes\": []}";
        String[] r = send("POST /graph/nodes/add HTTP/1.1\r\nHost: 127.0.0.1\r\nContent-Type: application/json\r\n"
            + "Transfer-Encoding: chunked\r\nConnection: close\r\n\r\n"
            + Integer.toHexString(json.length()) + "\r\n" + json + "\r\n0\r\n\r\n");
        assertTrue(!r[1].contains("Missing") && !r[0].equals("500"), r[0] + " " + r[1]);
    }

    @Test
    void queryValuesAreDecoded() {
        Map<String, String> q = GephiAPIServer.parseQuery("name=Tom%C3%A1s+Soto&flag&&empty=&bad=%zz");
        assertEquals("Tomás Soto", q.get("name"));
        assertEquals("", q.get("flag"));
        assertEquals("", q.get("empty"));
        assertEquals("%zz", q.get("bad"));
        assertTrue(GephiAPIServer.parseQuery(null).isEmpty());
    }
}
