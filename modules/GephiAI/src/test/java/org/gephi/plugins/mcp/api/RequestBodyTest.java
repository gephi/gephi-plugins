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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

/**
 * Request bodies are JSON, and JSON is UTF-8. NanoHTTPD's own parseBody decodes a body as
 * US-ASCII when the Content-Type names no charset, which turned "Tomás" into "Tom��s"
 * for every node, label, and attribute sent by a client that does not add one.
 */
class RequestBodyTest {

    private static String read(String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return GephiAPIServer.readBody(new ByteArrayInputStream(bytes), String.valueOf(bytes.length));
    }

    @Test
    void nonAsciiTextSurvivesWithoutACharsetInTheContentType() throws IOException {
        String json = "{\"id\":\"Tomás\",\"label\":\"Zoë Wójcik 東京\"}";
        assertEquals(json, read(json));
    }

    @Test
    void theLengthIsCountedInBytesNotCharacters() throws IOException {
        String json = "{\"id\":\"ąęółżźćń\"}";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        String extra = "trailing bytes that belong to no request";
        byte[] stream = (json + extra).getBytes(StandardCharsets.UTF_8);
        assertEquals(json, GephiAPIServer.readBody(new ByteArrayInputStream(stream), String.valueOf(bytes.length)));
    }

    @Test
    void missingOrEmptyLengthReadsNothing() throws IOException {
        assertEquals("", GephiAPIServer.readBody(new ByteArrayInputStream(new byte[0]), null));
        assertEquals("", GephiAPIServer.readBody(new ByteArrayInputStream(new byte[0]), " "));
    }
}
