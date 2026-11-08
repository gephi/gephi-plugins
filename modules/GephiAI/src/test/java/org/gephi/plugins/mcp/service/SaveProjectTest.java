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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gephi.project.api.ProjectController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.util.Lookup;

/** "Project saved" is reported only when the file is on disk. */
class SaveProjectTest {

    @Test
    void saveReportsTheWrittenFile(@TempDir Path dir) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        Path target = dir.resolve("saved.gephi");

        JsonObject r = GephiControlService.getInstance().saveProject(target.toString());

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertTrue(Files.isRegularFile(target));
        assertTrue(r.get("bytes").getAsLong() > 0);
    }

    @Test
    void saveIntoAMissingFolderCreatesIt(@TempDir Path dir) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        Path target = dir.resolve("new-folder").resolve("x.gephi");

        JsonObject r = GephiControlService.getInstance().saveProject(target.toString());

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertTrue(Files.isRegularFile(target));
    }

    @Test
    void saveWhereNoFolderCanBeMadeIsReportedAsFailed(@TempDir Path dir) throws Exception {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        Path blocker = Files.writeString(dir.resolve("a-file"), "x");

        JsonObject r = GephiControlService.getInstance()
            .saveProject(blocker.resolve("x.gephi").toString());

        assertFalse(r.get("success").getAsBoolean(), r.toString());
    }

    @Test
    void exportsCreateTheirFolder(@TempDir Path dir) {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        Path gexf = dir.resolve("out").resolve("g.gexf");
        Path csv = dir.resolve("tables").resolve("nodes.csv");

        JsonObject g = GephiControlService.getInstance().exportGexf(gexf.toString(), false);
        JsonObject c = GephiControlService.getInstance().exportCsv(csv.toString(), ",", "nodes");

        assertTrue(g.get("success").getAsBoolean(), g.toString());
        assertTrue(Files.isRegularFile(gexf));
        assertTrue(c.get("success").getAsBoolean(), c.toString());
        assertTrue(Files.isRegularFile(csv));
    }

    @Test
    void fileLeftUnchangedIsNotASave(@TempDir Path dir) throws Exception {
        File old = Files.writeString(dir.resolve("old.gephi"), "x").toFile();
        long stamp = System.currentTimeMillis() - 60_000;
        assertTrue(old.setLastModified(stamp));

        String problem = GephiControlService.savedFileProblem(old, old.lastModified(), System.currentTimeMillis());

        assertTrue(problem != null && problem.contains("unchanged"), String.valueOf(problem));
    }

    @Test
    void missingOrEmptyFileIsNotASave(@TempDir Path dir) throws Exception {
        long now = System.currentTimeMillis();
        assertTrue(GephiControlService.savedFileProblem(dir.resolve("none").toFile(), -1, now).contains("not written"));
        File empty = Files.createFile(dir.resolve("empty.gephi")).toFile();
        assertTrue(GephiControlService.savedFileProblem(empty, -1, now).contains("empty"));
        Files.writeString(empty.toPath(), "data");
        assertEquals(null, GephiControlService.savedFileProblem(empty, -1, now));
    }
}
