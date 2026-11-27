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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.gephi.plugins.mcp.LogCapture;
import org.gephi.project.api.ProjectController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.util.Lookup;

/**
 * A file that cannot be read or written (another program holds it open, the folder is missing or
 * read-only, the disk is full) is not a fault in Gephi. The caller gets the reason in the reply, and
 * nothing with an exception reaches the log: Gephi's crash reporter sends every logged exception, at
 * any level, as a bug report. A failure inside Gephi is still logged with its stack trace.
 */
class IoFailureLogTest {

    @Test
    void lockedFileIsReturnedAsAnErrorWithoutAnException() {
        String reason = "graph.png (The requested operation cannot be performed on a file with a "
            + "user-mapped section open)";
        try (LogCapture log = new LogCapture(GephiControlService.class)) {
            JsonObject r = GephiControlService.failure("Export failed: ", new FileNotFoundException(reason));

            assertFalse(r.get("success").getAsBoolean());
            assertEquals("Export failed: " + reason, r.get("error").getAsString());
            assertFalse(log.records().isEmpty(), "the failure must still leave a line in the log");
            log.assertNoException();
        }
    }

    @Test
    void ioFailureInsideAnotherExceptionIsStillAnIoFailure() {
        // Gephi's export controller wraps what its exporters throw in a RuntimeException, and the stream
        // methods of java.nio.file wrap an I/O failure in an UncheckedIOException.
        Exception wrapped = new RuntimeException("export failed",
            new UncheckedIOException(new IOException("There is not enough space on the disk")));
        try (LogCapture log = new LogCapture(GephiControlService.class)) {
            JsonObject r = GephiControlService.failure("Export failed: ", wrapped);

            assertFalse(r.get("success").getAsBoolean());
            assertEquals("Export failed: There is not enough space on the disk", r.get("error").getAsString());
            log.assertNoException();
        }
    }

    @Test
    void ioFailureWithoutAMessageIsNamedByItsType() {
        try (LogCapture log = new LogCapture(GephiControlService.class)) {
            JsonObject r = GephiControlService.failure("Import failed: ", new IOException());

            assertEquals("Import failed: java.io.IOException", r.get("error").getAsString());
            log.assertNoException();
        }
    }

    @Test
    void exceptionThatIsNotAnIoFailureIsStillLoggedWithItsStackTrace() {
        Exception bug = new RuntimeException("export failed", new IllegalStateException("graph model is missing"));
        try (LogCapture log = new LogCapture(GephiControlService.class)) {
            JsonObject r = GephiControlService.failure("Export failed: ", bug);

            assertEquals("Export failed: export failed", r.get("error").getAsString());
            assertEquals(1, log.records().size());
            LogRecord record = log.records().get(0);
            assertEquals(Level.WARNING, record.getLevel());
            assertSame(bug, record.getThrown(), "the stack trace was not logged");
        }
    }

    @Test
    void causesThatLoopBackOnEachOtherEndTheSearch() {
        Exception a = new IllegalStateException("a");
        Exception b = new IllegalArgumentException("b", a);
        a.initCause(b);

        assertNull(assertTimeoutPreemptively(Duration.ofSeconds(10),
            () -> GephiControlService.findIoException(a)));
    }

    /** The export the user saw fail: Gephi opens the file before it renders anything. */
    @Test
    void exportToAPathThatCannotBeWrittenIsAnErrorWithoutAnException(@TempDir Path dir) throws Exception {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        // A folder cannot be made under a file, so opening the target fails as it does for a locked one.
        Path blocker = Files.writeString(dir.resolve("a-file"), "x");
        GephiControlService service = GephiControlService.getInstance();
        try (LogCapture log = new LogCapture(GephiControlService.class)) {
            JsonObject png = service.exportPng(blocker.resolve("x.png").toString(), 100, 100);
            JsonObject gexf = service.exportGexf(blocker.resolve("x.gexf").toString());

            assertFalse(png.get("success").getAsBoolean(), png.toString());
            assertTrue(png.get("error").getAsString().contains("x.png"), png.toString());
            assertFalse(gexf.get("success").getAsBoolean(), gexf.toString());
            assertTrue(gexf.get("error").getAsString().contains("x.gexf"), gexf.toString());
            log.assertNoException();
        }
    }
}
