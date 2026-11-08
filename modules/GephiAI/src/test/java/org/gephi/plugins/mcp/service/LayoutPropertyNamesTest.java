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

package org.gephi.plugins.mcp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutProperty;
import org.junit.jupiter.api.Test;

/**
 * A layout setting whose name matches no property used to be dropped without a word, so a
 * misspelled key ran the layout on its defaults. Every name that matched nothing is reported.
 */
class LayoutPropertyNamesTest {

    private static Object value(Layout layout, String key) throws Exception {
        for (LayoutProperty p : layout.getProperties()) {
            if (p.getCanonicalName() != null && p.getCanonicalName().contains("." + key + ".")) {
                return p.getProperty().getValue();
            }
        }
        throw new AssertionError("No such property: " + key);
    }

    @Test
    void namesThatMatchNothingAreReportedAndTheRestApplied() throws Exception {
        Layout layout = new ForceAtlas2Builder().buildLayout();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("linLogMode", true);
        props.put("barnesHutOptimize", true);
        props.put("preventOverlap", true);
        props.put("strongGravityMode", true);

        List<String> unapplied = GephiControlService.applyLayoutProperties(layout, props);

        assertEquals(List.of("barnesHutOptimize", "preventOverlap"), unapplied);
        assertEquals(true, value(layout, "linLogMode"));
        assertEquals(true, value(layout, "strongGravityMode"));
    }

    /**
     * A run reports the value each matched setting holds after it was set, read back from the
     * layout under the property's key, so the caller can confirm what was used.
     */
    @Test
    void appliedSettingsAreReadBackFromTheLayoutUnderTheirKeys() throws Exception {
        Layout layout = new ForceAtlas2Builder().buildLayout();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("gravity", 5);
        props.put("LinLog mode", true);
        props.put("preventOverlap", true);

        List<String> unapplied = GephiControlService.applyLayoutProperties(layout, props);
        com.google.gson.JsonObject applied = GephiControlService.appliedLayoutSettings(layout, props);

        assertEquals(List.of("preventOverlap"), unapplied);
        assertEquals(5.0, applied.get("gravity").getAsDouble(), 1e-9);
        assertEquals(true, applied.get("linLogMode").getAsBoolean());
        assertEquals(2, applied.size(), applied.toString());
    }

    /** The run, the status after it, and a staged setting all report the value Gephi holds. */
    @Test
    void runStatusAndStagingReportTheSettingsUsed() throws Exception {
        org.gephi.project.api.ProjectController pc =
            org.openide.util.Lookup.getDefault().lookup(org.gephi.project.api.ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        GephiControlService service = GephiControlService.getInstance();
        service.generateGraph("preferential_attachment", 30, 1, null, 1L);

        com.google.gson.JsonObject staged = service.setLayoutProperties("ForceAtlas 2", Map.of("gravity", 3), 0);
        assertEquals(3.0, staged.getAsJsonObject("staged").get("gravity").getAsDouble(), 1e-9, staged.toString());

        com.google.gson.JsonObject run = service.runLayout("ForceAtlas 2", 5, Map.of("gravity", 5));
        assertTrue(run.get("success").getAsBoolean(), run.toString());
        assertEquals(5.0, run.getAsJsonObject("applied").get("gravity").getAsDouble(), 1e-9, run.toString());
        // Agents read a bare "applied" as the server repeating the request and set out to verify
        // it by other means; the reply says where the values come from.
        assertTrue(run.get("applied_note").getAsString().contains("read back from the layout"), run.toString());
        assertTrue(run.get("applied_note").getAsString().contains("gephi_get_layout_properties"), run.toString());
        for (int i = 0; i < 200 && service.getLayoutStatus().get("running").getAsBoolean(); i++) {
            Thread.sleep(25);
        }

        com.google.gson.JsonObject last = service.getLayoutStatus().getAsJsonObject("last_run");
        assertEquals("ForceAtlas 2", last.get("layout").getAsString(), last.toString());
        assertEquals(5, last.get("iterations").getAsInt());
        assertEquals(5.0, last.getAsJsonObject("applied").get("gravity").getAsDouble(), 1e-9, last.toString());
        assertTrue(last.get("applied_note").getAsString().contains("read back from the layout"), last.toString());
    }

    /**
     * The listing gives the short key run_layout takes and says its values are defaults: agents
     * read "ForceAtlas2.gravity.name = 1.0" after a gravity-5 run as proof the setting was lost.
     */
    @Test
    void thePropertyListingGivesTheKeyToPassAndSaysItShowsDefaults() {
        com.google.gson.JsonObject r = GephiControlService.getInstance().getLayoutProperties("ForceAtlas 2");

        assertTrue(r.get("values_are").getAsString().startsWith("defaults"), r.toString());
        boolean found = false;
        for (com.google.gson.JsonElement e : r.getAsJsonArray("properties")) {
            com.google.gson.JsonObject p = e.getAsJsonObject();
            if ("gravity".equals(p.has("key") ? p.get("key").getAsString() : null)) {
                found = true;
            }
        }
        assertTrue(found, "no property with key \"gravity\": " + r);
    }

    @Test
    void displayNamesAndOtherCasesStillCountAsMatches() throws Exception {
        Layout layout = new ForceAtlas2Builder().buildLayout();
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("LinLog mode", true);
        props.put("SCALINGRATIO", 0.5);

        List<String> unapplied = GephiControlService.applyLayoutProperties(layout, props);

        assertTrue(unapplied.isEmpty(), "unexpected: " + unapplied);
        assertEquals(true, value(layout, "linLogMode"));
        assertEquals(0.5, ((Number) value(layout, "scalingRatio")).doubleValue(), 1e-9);
    }
}
