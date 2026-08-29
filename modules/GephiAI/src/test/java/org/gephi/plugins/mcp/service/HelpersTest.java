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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the pure helpers in GephiControlService — CSV quoting, type-string
 * resolution, and value coercion. These need no Gephi runtime.
 */
class HelpersTest {

    // ── CSV (RFC 4180) escaping ──────────────────────────────────────────

    @Test
    void csvLeavesPlainValuesUnquoted() {
        assertEquals("hello", GephiControlService.csv("hello", ","));
        assertEquals("123", GephiControlService.csv("123", ","));
    }

    @Test
    void csvQuotesValuesContainingSeparator() {
        assertEquals("\"a,b\"", GephiControlService.csv("a,b", ","));
    }

    @Test
    void csvDoublesInternalQuotes() {
        assertEquals("\"she said \"\"hi\"\"\"", GephiControlService.csv("she said \"hi\"", ","));
    }

    @Test
    void csvQuotesNewlines() {
        assertEquals("\"line1\nline2\"", GephiControlService.csv("line1\nline2", ","));
    }

    @Test
    void csvRespectsCustomSeparator() {
        // a ';' is safe under a ',' separator but must be quoted under a ';' separator
        assertEquals("a;b", GephiControlService.csv("a;b", ","));
        assertEquals("\"a;b\"", GephiControlService.csv("a;b", ";"));
    }

    @Test
    void csvHandlesNull() {
        assertEquals("", GephiControlService.csv(null, ","));
    }

    // ── type string -> class ─────────────────────────────────────────────

    @Test
    void typeStringToClassKnownTypes() {
        assertEquals(String.class, GephiControlService.typeStringToClass("string"));
        assertEquals(Integer.class, GephiControlService.typeStringToClass("INT"));
        assertEquals(Integer.class, GephiControlService.typeStringToClass("integer"));
        assertEquals(Double.class, GephiControlService.typeStringToClass("double"));
        assertEquals(Boolean.class, GephiControlService.typeStringToClass("bool"));
        assertEquals(Long.class, GephiControlService.typeStringToClass("long"));
    }

    @Test
    void typeStringToClassUnknownIsNull() {
        assertNull(GephiControlService.typeStringToClass("nope"));
        assertNull(GephiControlService.typeStringToClass(null));
    }

    // ── value coercion to a column's type ────────────────────────────────

    @Test
    void convertToColumnTypeParsesNumbers() {
        assertEquals(7, GephiControlService.convertToColumnType("7.9", Integer.class)); // truncates
        assertEquals(3.5, GephiControlService.convertToColumnType("3.5", Double.class));
        assertEquals(true, GephiControlService.convertToColumnType("true", Boolean.class));
    }

    @Test
    void convertToColumnTypePassesThroughMatchingType() {
        assertEquals(42, GephiControlService.convertToColumnType(42, Integer.class));
    }

    @Test
    void convertToColumnTypeFallsBackToStringOnGarbage() {
        assertEquals("abc", GephiControlService.convertToColumnType("abc", Integer.class));
    }

    // ── layout property coercion (e.g. "100.0" -> int 100) ───────────────

    @Test
    void convertLayoutPropertyHandlesNumericStrings() {
        assertEquals(100, GephiControlService.convertLayoutProperty("100.0", int.class));
        assertEquals(2.5, GephiControlService.convertLayoutProperty("2.5", double.class));
        assertEquals(true, GephiControlService.convertLayoutProperty("true", boolean.class));
        assertEquals(1.5f, GephiControlService.convertLayoutProperty("1.5", float.class));
    }

    @Test
    void convertLayoutPropertyReturnsNullOnGarbage() {
        assertNull(GephiControlService.convertLayoutProperty("xyz", int.class));
    }

    // ── layout name matching (real Gephi builder names) ──────────────────

    private static final List<String> LAYOUTS = List.of(
        "Yifan Hu", "Yifan Hu Proportional", "Force Atlas", "ForceAtlas 2",
        "Fruchterman Reingold", "Label Adjust", "Noverlap", "OpenOrd", "Random Layout");

    @Test
    void layoutMatchFoldsSpacesForDocumentedShortNames() {
        // The names the skill/docs use must resolve to the real builders.
        assertEquals("ForceAtlas 2", LAYOUTS.get(GephiControlService.bestLayoutMatch(LAYOUTS, "forceatlas2")));
        assertEquals("Yifan Hu", LAYOUTS.get(GephiControlService.bestLayoutMatch(LAYOUTS, "yifanhu")));
        assertEquals("Fruchterman Reingold", LAYOUTS.get(GephiControlService.bestLayoutMatch(LAYOUTS, "fruchterman")));
    }

    @Test
    void layoutMatchPrefersExactOverSubstring() {
        // "Force Atlas" must not be hijacked by "ForceAtlas 2" (and vice-versa).
        assertEquals("Force Atlas", LAYOUTS.get(GephiControlService.bestLayoutMatch(LAYOUTS, "Force Atlas")));
        assertEquals("ForceAtlas 2", LAYOUTS.get(GephiControlService.bestLayoutMatch(LAYOUTS, "ForceAtlas 2")));
    }

    @Test
    void layoutMatchReturnsMinusOneWhenNoMatch() {
        assertEquals(-1, GephiControlService.bestLayoutMatch(LAYOUTS, "nonexistent"));
        assertEquals(-1, GephiControlService.bestLayoutMatch(LAYOUTS, null));
    }

    @Test
    void layoutMatchFallsBackToFirstSubstringMatch() {
        // "atlas" matches no name exactly; the FIRST substring match ("Force Atlas",
        // which precedes "ForceAtlas 2" in the registry order) must win.
        assertEquals("Force Atlas", LAYOUTS.get(GephiControlService.bestLayoutMatch(LAYOUTS, "atlas")));
        // A single-name substring resolves to that name.
        assertEquals("OpenOrd", LAYOUTS.get(GephiControlService.bestLayoutMatch(LAYOUTS, "openo")));
        // Space folding applies to substring matching too: "chtermanrein" only matches
        // "Fruchterman Reingold" once the space is folded out of the candidate name.
        assertEquals("Fruchterman Reingold",
            LAYOUTS.get(GephiControlService.bestLayoutMatch(LAYOUTS, "chtermanrein")));
    }

    // ── screenshot helpers (gephi_export_screenshot's async-completion detection) ─

    @Test
    void pollForNewFileFindsAFileWrittenAfterPollingStarts() throws Exception {
        File dir = Files.createTempDirectory("poll-test-").toFile();
        try {
            Thread writer = new Thread(() -> {
                try {
                    Thread.sleep(50);
                    new File(dir, "shot.png").createNewFile();
                } catch (Exception ignored) { }
            });
            writer.start();
            File found = GephiControlService.pollForNewFile(dir, 2_000);
            writer.join();
            assertEquals("shot.png", found.getName());
        } finally {
            deleteRecursively(dir);
        }
    }

    @Test
    void pollForNewFileReturnsNullOnTimeoutWhenDirStaysEmpty() throws Exception {
        File dir = Files.createTempDirectory("poll-test-").toFile();
        try {
            assertNull(GephiControlService.pollForNewFile(dir, 100));
        } finally {
            deleteRecursively(dir);
        }
    }

    @Test
    void waitForStableFileSizeTrueOnceWritesStop() throws Exception {
        File f = File.createTempFile("stable-test-", ".png");
        try {
            writeBytes(f, new byte[]{1, 2, 3});
            assertTrue(GephiControlService.waitForStableFileSize(f, 1_000));
        } finally {
            f.delete();
        }
    }

    @Test
    void waitForStableFileSizeFalseOnEmptyFile() throws Exception {
        File f = File.createTempFile("stable-test-empty-", ".png");
        try {
            assertFalse(GephiControlService.waitForStableFileSize(f, 150));
        } finally {
            f.delete();
        }
    }

    @Test
    void deleteDirQuietlyRemovesFilesAndDirectory() throws Exception {
        File dir = Files.createTempDirectory("cleanup-test-").toFile();
        writeBytes(new File(dir, "a.png"), new byte[]{1});
        writeBytes(new File(dir, "b.png"), new byte[]{2});
        assertEquals(2, dir.listFiles().length);

        GephiControlService.deleteDirQuietly(dir);

        assertFalse(dir.exists());
    }

    @Test
    void deleteDirQuietlyToleratesAlreadyEmptyDirectory() throws Exception {
        File dir = Files.createTempDirectory("cleanup-test-empty-").toFile();
        GephiControlService.deleteDirQuietly(dir);
        assertFalse(dir.exists());
    }

    private static void writeBytes(File f, byte[] data) throws IOException {
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(data);
        }
    }

    private static void deleteRecursively(File f) {
        File[] children = f.listFiles();
        if (children != null) for (File c : children) deleteRecursively(c);
        f.delete();
    }
}
