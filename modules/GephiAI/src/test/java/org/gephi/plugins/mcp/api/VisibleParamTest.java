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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The {@code visible} switch that lets a caller choose between the full graph and the
 * filtered view. The defaults matter more than the parsing: read endpoints have always
 * returned the full graph and the export endpoints have always written the filtered one,
 * and wiring this parameter must not change either, or every existing caller silently
 * changes meaning.
 */
class VisibleParamTest {

    private static Map<String, String> params(String key, String value) {
        Map<String, String> m = new HashMap<>();
        if (key != null) m.put(key, value);
        return m;
    }

    private static JsonObject body(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    void absentParameterKeepsTheEndpointsHistoricalView() {
        assertFalse(GephiAPIServer.visibleParam(params(null, null), false), "reads default to the full graph");
        assertTrue(GephiAPIServer.visibleParam(params(null, null), true), "exports default to the visible graph");
        assertFalse(GephiAPIServer.visibleParam(null, false), "a null map must not throw");
        assertFalse(GephiAPIServer.visibleParam(params("visible", "   "), false), "blank is treated as absent");
    }

    @Test
    void queryParameterIsHonouredInBothDirections() {
        assertTrue(GephiAPIServer.visibleParam(params("visible", "true"), false));
        assertTrue(GephiAPIServer.visibleParam(params("visible", "TRUE"), false));
        assertTrue(GephiAPIServer.visibleParam(params("visible", "1"), false));
        assertFalse(GephiAPIServer.visibleParam(params("visible", "false"), true));
        assertFalse(GephiAPIServer.visibleParam(params("visible", "0"), true));
    }

    @Test
    void garbageFallsBackToTheDefaultRatherThanGuessing() {
        assertFalse(GephiAPIServer.visibleParam(params("visible", "yes"), false));
        assertTrue(GephiAPIServer.visibleParam(params("visible", "banana"), true));
    }

    @Test
    void bodyFlagIsHonouredAndDefaultsSafely() {
        assertTrue(GephiAPIServer.visibleBody(body("{}"), true), "absent keeps the export default");
        assertFalse(GephiAPIServer.visibleBody(body("{}"), false));
        assertFalse(GephiAPIServer.visibleBody(body("{\"visible\":false}"), true));
        assertTrue(GephiAPIServer.visibleBody(body("{\"visible\":true}"), false));
        assertTrue(GephiAPIServer.visibleBody(body("{\"visible\":null}"), true), "explicit null is absent");
        assertTrue(GephiAPIServer.visibleBody(null, true), "a null body must not throw");
    }

    @Test
    void aNonBooleanBodyValueFallsBackRatherThanThrowing() {
        assertTrue(GephiAPIServer.visibleBody(body("{\"visible\":{\"a\":1}}"), true));
        assertFalse(GephiAPIServer.visibleBody(body("{\"visible\":[1,2]}"), false));
    }
}
