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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.util.Lookup;

/**
 * Importing a GEXF or GraphML file keeps the positions, sizes and colors the file carries.
 *
 * <p>This runs Gephi's real import path in a plain JVM: the GEXF and GraphML importers, the import
 * container, and the default processor, all found through Lookup. Gephi's containers
 * auto-scale by default, which recenters every node and rescales sizes into 4 to 100, so
 * a graph exported and imported again would come back with different positions and sizes.
 * The import turns auto-scale off.
 */
class ImportRoundTripTest {

    /** id, x, y, size, r, g, b. Positions span thousands of units off-center; sizes 1 to 100. */
    private static final Object[][] NODES = {
        {"a", 1036.5f, 257.25f, 18.44f, 230, 25, 75},
        {"b", 6200.0f, -3400.0f, 1.0f, 60, 180, 75},
        {"c", -2800.0f, 4100.0f, 100.0f, 0, 130, 200},
        {"d", 3500.0f, 9000.0f, 42.5f, 245, 130, 48},
    };

    private static final float TOLERANCE = 1e-3f;

    @Test
    void importKeepsTheFilePositionsSizesAndColors(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("styled.gexf");
        Files.write(file, gexf().getBytes(StandardCharsets.UTF_8));
        assertImportKeepsTheFileLayout(file);
    }

    /** GraphML carries the same layout as node data keys, and must come back unchanged too. */
    @Test
    void graphmlImportKeepsTheFilePositionsSizesAndColors(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("styled.graphml");
        Files.write(file, graphml().getBytes(StandardCharsets.UTF_8));
        assertImportKeepsTheFileLayout(file);
    }

    private static void assertImportKeepsTheFileLayout(Path file) throws Exception {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        assertNotNull(pc, "a ProjectController must be registered for the import to run");
        pc.newProject();

        JsonObject r = GephiControlService.getInstance().importFile(file.toString(), null);
        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(NODES.length, r.get("node_count").getAsInt(), r.toString());
        assertEquals(1, r.get("edge_count").getAsInt(), r.toString());

        Workspace ws = pc.getCurrentWorkspace();
        Graph g = Lookup.getDefault().lookup(GraphController.class).getGraphModel(ws).getGraph();
        StringBuilder mismatches = new StringBuilder();
        for (Object[] row : NODES) {
            Node n = g.getNode((String) row[0]);
            assertNotNull(n, "node " + row[0] + " must be imported");
            float x = (Float) row[1];
            float y = (Float) row[2];
            float size = (Float) row[3];
            if (Math.abs(n.x() - x) > TOLERANCE || Math.abs(n.y() - y) > TOLERANCE
                || Math.abs(n.size() - size) > TOLERANCE) {
                mismatches.append(String.format(
                    "%n  %s: file (%.2f, %.2f) size %.2f, imported (%.2f, %.2f) size %.2f",
                    row[0], x, y, size, n.x(), n.y(), n.size()));
            }
            assertEquals((Integer) row[4], n.getColor().getRed(), "red of " + row[0]);
            assertEquals((Integer) row[5], n.getColor().getGreen(), "green of " + row[0]);
            assertEquals((Integer) row[6], n.getColor().getBlue(), "blue of " + row[0]);
        }
        assertTrue(mismatches.length() == 0,
            "import changed the file's positions or sizes:" + mismatches);
    }

    /**
     * With auto-scale off, a file that carries no positions or sizes still imports as a
     * usable graph: the container spreads the nodes at random and the processor gives
     * each one the default size.
     */
    @Test
    void fileWithoutPositionsStillGetsSpreadOutNodes(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("bare.gexf");
        Files.write(file, ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<gexf xmlns=\"http://gexf.net/1.3\" version=\"1.3\">\n"
            + "  <graph defaultedgetype=\"undirected\">\n"
            + "    <nodes><node id=\"p\"/><node id=\"q\"/><node id=\"r\"/></nodes>\n"
            + "    <edges><edge id=\"0\" source=\"p\" target=\"q\"/></edges>\n"
            + "  </graph>\n"
            + "</gexf>\n").getBytes(StandardCharsets.UTF_8));

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        JsonObject r = GephiControlService.getInstance().importFile(file.toString(), null);
        assertTrue(r.get("success").getAsBoolean(), r.toString());

        Graph g = Lookup.getDefault().lookup(GraphController.class)
            .getGraphModel(pc.getCurrentWorkspace()).getGraph();
        assertEquals(3, g.getNodeCount());
        java.util.Set<String> positions = new java.util.HashSet<>();
        for (Node n : g.getNodes()) {
            assertTrue(n.x() != 0f || n.y() != 0f, "node " + n.getId() + " was left at the origin");
            assertEquals(10f, n.size(), TOLERANCE, "default size of " + n.getId());
            positions.add(n.x() + "," + n.y());
        }
        assertEquals(3, positions.size(), "nodes must not share one position");
    }

    /** The keys Gephi's GraphML importer reads as position, size and color: x, y, size, r, g, b. */
    private static String graphml() {
        StringBuilder s = new StringBuilder();
        s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\">\n");
        for (String[] key : new String[][] {
                {"x", "float"}, {"y", "float"}, {"size", "float"},
                {"r", "int"}, {"g", "int"}, {"b", "int"}}) {
            s.append(String.format(java.util.Locale.ROOT,
                "  <key id=\"%s\" for=\"node\" attr.name=\"%s\" attr.type=\"%s\"/>%n",
                key[0], key[0], key[1]));
        }
        s.append("  <graph id=\"G\" edgedefault=\"undirected\">\n");
        for (Object[] row : NODES) {
            s.append(String.format(java.util.Locale.ROOT,
                "    <node id=\"%s\">%n"
                    + "      <data key=\"x\">%s</data>%n"
                    + "      <data key=\"y\">%s</data>%n"
                    + "      <data key=\"size\">%s</data>%n"
                    + "      <data key=\"r\">%d</data>%n"
                    + "      <data key=\"g\">%d</data>%n"
                    + "      <data key=\"b\">%d</data>%n"
                    + "    </node>%n",
                row[0], row[1], row[2], row[3], row[4], row[5], row[6]));
        }
        s.append("    <edge id=\"0\" source=\"a\" target=\"b\"/>\n")
            .append("  </graph>\n")
            .append("</graphml>\n");
        return s.toString();
    }

    private static String gexf() {
        StringBuilder s = new StringBuilder();
        s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            .append("<gexf xmlns=\"http://gexf.net/1.3\" xmlns:viz=\"http://gexf.net/1.3/viz\" version=\"1.3\">\n")
            .append("  <graph defaultedgetype=\"undirected\" mode=\"static\">\n")
            .append("    <nodes>\n");
        for (Object[] row : NODES) {
            s.append(String.format(java.util.Locale.ROOT,
                "      <node id=\"%s\" label=\"%s\">%n"
                    + "        <viz:size value=\"%s\"/>%n"
                    + "        <viz:position x=\"%s\" y=\"%s\" z=\"0.0\"/>%n"
                    + "        <viz:color r=\"%d\" g=\"%d\" b=\"%d\"/>%n"
                    + "      </node>%n",
                row[0], row[0], row[3], row[1], row[2], row[4], row[5], row[6]));
        }
        s.append("    </nodes>\n")
            .append("    <edges>\n")
            .append("      <edge id=\"0\" source=\"a\" target=\"b\"/>\n")
            .append("    </edges>\n")
            .append("  </graph>\n")
            .append("</gexf>\n");
        return s.toString();
    }
}
