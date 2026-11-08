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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.util.Lookup;

/**
 * An import gets its own workspace, as in Gephi's own import dialog. A workspace's graph
 * settings (time format, id type) are fixed when it is created, so importing into whatever
 * workspace was open made timestamp and integer-id files fail. Adding to the current workspace
 * is an explicit choice, and the file's import warnings are returned.
 */
class ImportWorkspaceTest {

    private static final String PLAIN = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<gexf xmlns=\"http://gexf.net/1.3\" version=\"1.3\"><graph defaultedgetype=\"undirected\">"
        + "<nodes><node id=\"a\" label=\"A\"/><node id=\"b\" label=\"B\"/></nodes>"
        + "<edges><edge id=\"e1\" source=\"a\" target=\"b\"/></edges></graph></gexf>";

    private static ProjectController freshProject() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        return pc;
    }

    private static Path write(Path dir, String name, String body) throws Exception {
        Path f = dir.resolve(name);
        Files.writeString(f, body, StandardCharsets.UTF_8);
        return f;
    }

    private static GraphModel model(Workspace ws) {
        return Lookup.getDefault().lookup(GraphController.class).getGraphModel(ws);
    }

    private static int workspaceCount(ProjectController pc) {
        Project p = pc.getCurrentProject();
        return p.getWorkspaces().size();
    }

    @Test
    void fileWithTimestampsImports(@TempDir Path dir) throws Exception {
        freshProject();
        Path f = write(dir, "timestamps.gexf", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<gexf xmlns=\"http://gexf.net/1.3\" version=\"1.3\">"
            + "<graph defaultedgetype=\"undirected\" mode=\"dynamic\" timeformat=\"double\""
            + " timerepresentation=\"timestamp\"><nodes>"
            + "<node id=\"a\"><spells><spell timestamp=\"1\"/></spells></node>"
            + "<node id=\"b\"><spells><spell timestamp=\"2\"/></spells></node>"
            + "</nodes><edges><edge id=\"e1\" source=\"a\" target=\"b\"/></edges></graph></gexf>");

        JsonObject r = GephiControlService.getInstance().importFile(f.toString(), null);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(2, r.get("node_count").getAsInt());
    }

    @Test
    void fileWithIntegerIdsImports(@TempDir Path dir) throws Exception {
        freshProject();
        Path f = write(dir, "ints.gexf", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<gexf xmlns=\"http://gexf.net/1.3\" version=\"1.3\">"
            + "<graph defaultedgetype=\"undirected\" idtype=\"integer\"><nodes>"
            + "<node id=\"1\"/><node id=\"2\"/></nodes>"
            + "<edges><edge id=\"1\" source=\"1\" target=\"2\"/></edges></graph></gexf>");

        JsonObject r = GephiControlService.getInstance().importFile(f.toString(), null);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertEquals(2, r.get("node_count").getAsInt());
    }

    @Test
    void anImportOpensItsOwnWorkspaceAndTidiesAwayAnEmptyOne(@TempDir Path dir) throws Exception {
        ProjectController pc = freshProject();
        Workspace empty = pc.getCurrentWorkspace();

        JsonObject r = GephiControlService.getInstance().importFile(write(dir, "plain.gexf", PLAIN).toString(), null);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertNotSame(empty, pc.getCurrentWorkspace());
        assertEquals(1, workspaceCount(pc), "the empty starting workspace should not be left behind");
        assertEquals("new_workspace", r.get("import_mode").getAsString());
        assertEquals("plain.gexf", pc.getCurrentWorkspace().getName(), "named after the file");
    }

    @Test
    void anImportNeverReplacesAWorkspaceThatHasAGraph(@TempDir Path dir) throws Exception {
        ProjectController pc = freshProject();
        GephiControlService.getInstance().importFile(write(dir, "first.gexf", PLAIN).toString(), null);
        Workspace first = pc.getCurrentWorkspace();

        GephiControlService.getInstance().importFile(write(dir, "second.gexf", PLAIN).toString(), null);

        assertNotSame(first, pc.getCurrentWorkspace());
        assertEquals(2, workspaceCount(pc));
        assertEquals(2, model(first).getGraph().getNodeCount(), "the first graph is untouched");
    }

    @Test
    void appendAddsTheFileToTheCurrentWorkspace(@TempDir Path dir) throws Exception {
        ProjectController pc = freshProject();
        GephiControlService.getInstance().importFile(write(dir, "first.gexf", PLAIN).toString(), null);
        Workspace current = pc.getCurrentWorkspace();
        String more = PLAIN.replace("\"a\"", "\"c\"").replace("\"b\"", "\"d\"").replace("\"A\"", "\"C\"")
            .replace("\"B\"", "\"D\"").replace("source=\"c\" target=\"d\"", "source=\"c\" target=\"d\"");

        JsonObject r = GephiControlService.getInstance()
            .importFile(write(dir, "more.gexf", more).toString(), null, "append");

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertSame(current, pc.getCurrentWorkspace());
        assertEquals(4, model(current).getGraph().getNodeCount());
        assertEquals("append", r.get("import_mode").getAsString());
    }

    @Test
    void theFilesImportWarningsAreReturned(@TempDir Path dir) throws Exception {
        freshProject();
        Path f = write(dir, "dangling.gexf", PLAIN.replace("target=\"b\"", "target=\"missing\""));

        JsonObject r = GephiControlService.getInstance().importFile(f.toString(), null);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertTrue(r.has("import_issues") && r.getAsJsonArray("import_issues").size() > 0, r.toString());
        assertFalse(r.getAsJsonArray("import_issues").get(0).getAsJsonObject().get("message").getAsString().isEmpty());
    }

    @Test
    void appendingAFileWhoseTimeDoesNotFitIsRefusedAndChangesNothing(@TempDir Path dir) throws Exception {
        ProjectController pc = freshProject();
        String stamps = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<gexf xmlns=\"http://gexf.net/1.3\" version=\"1.3\"><graph defaultedgetype=\"undirected\""
            + " mode=\"dynamic\" timeformat=\"double\" timerepresentation=\"timestamp\"><nodes>"
            + "<node id=\"a\"><spells><spell timestamp=\"1\"/></spells></node></nodes></graph></gexf>";
        String intervals = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<gexf xmlns=\"http://gexf.net/1.3\" version=\"1.3\"><graph defaultedgetype=\"undirected\""
            + " mode=\"dynamic\" timeformat=\"double\"><nodes>"
            + "<node id=\"b\" start=\"1\" end=\"2\"/></nodes></graph></gexf>";
        GephiControlService.getInstance().importFile(write(dir, "stamps.gexf", stamps).toString(), null);
        Workspace current = pc.getCurrentWorkspace();

        JsonObject r = GephiControlService.getInstance()
            .importFile(write(dir, "intervals.gexf", intervals).toString(), null, "append");

        assertFalse(r.get("success").getAsBoolean(), r.toString());
        assertTrue(r.get("error").getAsString().contains("its own workspace"), r.toString());
        assertSame(current, pc.getCurrentWorkspace());
        assertEquals(1, model(current).getGraph().getNodeCount(), "the refused file was partly added");
    }

    @Test
    void nodesOfAFileWithIntegerIdsAreFoundByTheirIds(@TempDir Path dir) throws Exception {
        freshProject();
        Path f = write(dir, "ints.gexf", "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<gexf xmlns=\"http://gexf.net/1.3\" version=\"1.3\">"
            + "<graph defaultedgetype=\"undirected\" idtype=\"integer\"><nodes>"
            + "<node id=\"1\"/><node id=\"2\"/><node id=\"3\"/></nodes>"
            + "<edges><edge id=\"1\" source=\"1\" target=\"2\"/></edges></graph></gexf>");
        GephiControlService service = GephiControlService.getInstance();
        assertTrue(service.importFile(f.toString(), null).get("success").getAsBoolean());

        JsonObject node = service.getNode("2");
        assertTrue(node.get("success").getAsBoolean(), node.toString());
        JsonObject path = service.findShortestPath("1", "2", null, true, null);
        assertTrue(path.get("found").getAsBoolean(), path.toString());
        JsonObject edge = service.addEdge("2", "3", 1.0, false);
        assertTrue(edge.get("success").getAsBoolean(), edge.toString());
    }
}
