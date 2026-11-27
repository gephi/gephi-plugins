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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import org.gephi.plugins.mcp.LogCapture;
import org.junit.jupiter.api.Test;

/**
 * A client that closed the connection before the reply was written, or while its request was still
 * being read (it timed out, or its process ended), is not a fault in Gephi. Nothing can be sent to
 * it, and Gephi's crash reporter sends every logged exception, at any level, as a bug report: the
 * lost connection must not be logged with its exception, nor answered with a second reply.
 */
class ClientGoneTest {

    @Test
    void replyToAClientThatIsGoneIsNeitherLoggedAsAnExceptionNorSentTwice() {
        DroppedClient exchange = new DroppedClient("GET", "/no/such/thing");
        try (LogCapture log = new LogCapture(GephiAPIServer.class)) {
            IOException escaped = handle(exchange);

            assertAll(
                log::assertNoException,
                () -> assertEquals(1, exchange.replyAttempts, "a second reply cannot reach a connection that is gone"),
                () -> assertNull(escaped, "nothing may escape the handler"));
        }
    }

    @Test
    void requestCutOffWhileItsBodyIsReadIsNeitherLoggedAsAnExceptionNorAnswered() {
        DroppedClient exchange = new DroppedClient("POST", "/graph/nodes/add");
        try (LogCapture log = new LogCapture(GephiAPIServer.class)) {
            IOException escaped = handle(exchange);

            assertAll(
                log::assertNoException,
                () -> assertEquals(0, exchange.replyAttempts, "there is nobody left to answer"),
                () -> assertNull(escaped, "nothing may escape the handler"));
        }
    }

    /** Runs the handler and returns what escaped it, as the HTTP server would see it. */
    private static IOException handle(HttpExchange exchange) {
        try {
            new GephiAPIServer(0).handle(exchange);
            return null;
        } catch (IOException e) {
            return e;
        }
    }

    /** An exchange whose client has gone: reading its request and writing its reply fail as on a reset connection. */
    private static final class DroppedClient extends HttpExchange {
        private final String method;
        private final URI uri;
        private final Headers requestHeaders = new Headers();
        private final Headers responseHeaders = new Headers();
        int replyAttempts;

        DroppedClient(String method, String path) {
            this.method = method;
            this.uri = URI.create(path);
        }

        @Override
        public Headers getRequestHeaders() {
            return requestHeaders;
        }

        @Override
        public Headers getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public URI getRequestURI() {
            return uri;
        }

        @Override
        public String getRequestMethod() {
            return method;
        }

        @Override
        public HttpContext getHttpContext() {
            return null;
        }

        @Override
        public void close() {
        }

        @Override
        public InputStream getRequestBody() {
            return new InputStream() {
                @Override
                public int read() throws IOException {
                    throw new IOException("Connection reset");
                }
            };
        }

        @Override
        public OutputStream getResponseBody() {
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    throw new IOException("Connection reset by peer");
                }

                @Override
                public void close() throws IOException {
                    // What the JDK's server adds when the body was cut short: the first failure keeps it as
                    // a suppressed exception, as in the report.
                    throw new IOException("insufficient bytes written to stream");
                }
            };
        }

        @Override
        public void sendResponseHeaders(int status, long length) throws IOException {
            if (++replyAttempts > 1) {
                throw new IOException("headers already sent");
            }
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public int getResponseCode() {
            return -1;
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public void setAttribute(String name, Object value) {
        }

        @Override
        public void setStreams(InputStream in, OutputStream out) {
        }

        @Override
        public HttpPrincipal getPrincipal() {
            return null;
        }
    }
}
