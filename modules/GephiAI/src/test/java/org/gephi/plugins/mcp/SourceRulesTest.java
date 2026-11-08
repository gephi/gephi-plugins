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

package org.gephi.plugins.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Rules the plugin's source must keep, read from the source itself. Each guards against a way a
 * Gephi plugin can freeze Gephi or slow its interface: a graph lock that is never released, a lock
 * held by a loop over a live graph iterator, work the interface thread must not do, and threads
 * that could keep Gephi from shutting down.
 */
class SourceRulesTest {

    private static final Path MAIN = Paths.get("src", "main", "java");

    /** Methods whose job is to take a lock for the caller, who then releases it in a finally block. */
    private static final Set<String> LOCK_HELPERS = Set.of("lockRead", "lockWrite");

    /** The only places allowed to create threads, as "File#member". */
    private static final Set<String> THREAD_OWNERS = Set.of(
        "Installer#restored",
        "Installer#stopNow",
        "GephiAPIServer#startServer",
        "GephiControlService#DEADLINES",
        "GephiControlService#runOnStatisticThread");

    private static final Pattern LOCK_CALL =
        Pattern.compile("^\\s*(?:(lockRead|lockWrite)\\(\\w+\\)|\\w+\\.(readLock|writeLock)\\(\\));");
    private static final Pattern UNLOCK = Pattern.compile("readUnlock\\(\\)|writeUnlock\\(\\)|unlockWrite\\(");
    private static final Pattern LIVE_LOOP = Pattern.compile(
        "for\\s*\\([^:]*:[^)]*\\.(getNodes|getEdges|getNeighbors|getOutEdges|getInEdges|getSelfLoops)\\(");
    private static final Pattern EDT_CALL = Pattern.compile("\\b(runOnEDT|onEdt|onEdt\\.accept|invokeLater)\\(");
    private static final Pattern NOT_ON_EDT = Pattern.compile(
        "\\b(newProject|openProject|closeCurrentProject|newWorkspace|openWorkspace|deleteWorkspace"
            + "|duplicateWorkspace|renameWorkspace|saveProject|cleanWorkspace)\\("
            + "|\\block(Read|Write)\\(|\\.(readLock|writeLock)\\(\\)|\\.get(Nodes|Edges)\\(\\)");
    private static final Pattern THREAD_START = Pattern.compile("new Thread\\(|Executors\\.new");
    private static final Pattern MEMBER = Pattern.compile(
        "^ {4}(?!return |throw |if |for |while |else|new )(?:[\\w<>\\[\\],.?]+ )+(\\w+)\\s*(\\(|=|;)");

    private static Map<String, String> sources;

    @BeforeAll
    static void readSources() throws IOException {
        sources = new TreeMap<>();
        try (Stream<Path> files = Files.walk(MAIN)) {
            for (Path p : files.filter(f -> f.toString().endsWith(".java")).collect(Collectors.toList())) {
                sources.put(p.getFileName().toString(), Files.readString(p, StandardCharsets.UTF_8));
            }
        }
    }

    @Test
    void everyGraphLockIsReleasedInAFinallyBlock() {
        List<String> found = new ArrayList<>();
        sources.forEach((file, text) -> found.addAll(unreleasedLocks(file, text)));
        assertEquals(List.of(), found, "Take a graph lock, then open a try at once and release it in"
            + " finally; anything between the two can throw and leave the lock held for good");
    }

    @Test
    void noLoopRunsOverALiveGraphIterator() {
        List<String> found = new ArrayList<>();
        sources.forEach((file, text) -> {
            String[] lines = text.split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                if (!isComment(lines[i]) && LIVE_LOOP.matcher(lines[i]).find() && !lines[i].contains("toArray()")) {
                    found.add(file + ":" + (i + 1));
                }
            }
        });
        assertEquals(List.of(), found, "Loop over getNodes().toArray(): a live iterator holds the graph's"
            + " read lock until it is exhausted, so leaving the loop early leaks the lock");
    }

    @Test
    void caughtExceptionIsLoggedBeforeItIsReturned() {
        Pattern catchAll = Pattern.compile("catch \\((Exception|RuntimeException|Throwable) \\w+\\) \\{");
        Pattern bareReturn = Pattern.compile("return error\\(.*\\+ \\w+\\.getMessage\\(\\)\\);");
        List<String> found = new ArrayList<>();
        sources.forEach((file, text) -> {
            String[] lines = text.split("\n", -1);
            for (int i = 1; i < lines.length; i++) {
                if (bareReturn.matcher(lines[i]).find() && catchAll.matcher(lines[i - 1]).find()) {
                    found.add(file + ":" + (i + 1));
                }
            }
        });
        assertEquals(List.of(), found, "Return failure(prefix, e), which logs the stack trace; a bare"
            + " message leaves nothing in Gephi's log when the failure is inside Gephi");
    }

    @Test
    void nothingWaitsOnTheInterfaceThread() {
        List<String> found = new ArrayList<>();
        sources.forEach((file, text) -> {
            String[] lines = text.split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                if (!isComment(lines[i]) && lines[i].contains("invokeAndWait")) {
                    found.add(file + ":" + (i + 1));
                }
            }
        });
        assertEquals(List.of(), found, "invokeAndWait blocks forever when the interface thread is busy;"
            + " use runOnEDT, which waits with a limit");
    }

    @Test
    void theInterfaceThreadNeitherChangesProjectsNorReadsTheGraph() {
        List<String> found = new ArrayList<>();
        sources.forEach((file, text) -> {
            Matcher m = EDT_CALL.matcher(text);
            while (m.find()) {
                String body = parenthesised(text, m.end() - 1);
                Matcher bad = NOT_ON_EDT.matcher(body);
                if (bad.find()) {
                    found.add(file + ":" + lineOf(text, m.start()) + " " + bad.group());
                }
            }
        });
        assertEquals(List.of(), found, "Project and workspace changes and graph reads run on the calling"
            + " thread, as Gephi's own interface runs them; only Swing work belongs on the interface thread");
    }

    @Test
    void threadsAreCreatedOnlyInKnownPlacesAndNeverBlockShutdown() {
        List<String> found = new ArrayList<>();
        sources.forEach((file, text) -> {
            String[] lines = text.split("\n", -1);
            for (int i = 0; i < lines.length; i++) {
                if (isComment(lines[i]) || !THREAD_START.matcher(lines[i]).find()) {
                    continue;
                }
                int start = memberStart(lines, i);
                String owner = file.replace(".java", "") + "#" + memberName(lines[start]);
                String member = String.join("\n", List.of(lines).subList(start, memberEnd(lines, start)));
                if (!THREAD_OWNERS.contains(owner)) {
                    found.add(owner + " creates a thread");
                } else if (!member.contains("setDaemon(true)")) {
                    found.add(owner + " creates a thread that is not a daemon");
                } else if (lines[i].contains("new Thread(") && !namedThread(member)) {
                    found.add(owner + " creates an unnamed thread");
                }
            }
        });
        assertEquals(List.of(), found, "Threads belong to a known owner, carry a name, and are daemons, so"
            + " a thread dump says whose they are and Gephi can always quit");
    }

    @Test
    void everySourceFileStartsWithTheLicenseHeader() throws IOException {
        Pattern header = Pattern.compile("^/\\*\n \\* Copyright \\d{4} Matt Artz\n \\*\n"
            + " \\* Licensed under the Apache License, Version 2\\.0 \\(the \"License\"\\);\n");
        List<String> missing = new ArrayList<>();
        for (Path root : List.of(MAIN, Paths.get("src", "test", "java"))) {
            try (Stream<Path> files = Files.walk(root)) {
                for (Path p : files.filter(f -> f.toString().endsWith(".java")).collect(Collectors.toList())) {
                    if (!header.matcher(Files.readString(p, StandardCharsets.UTF_8)).find()) {
                        missing.add(p.toString());
                    }
                }
            }
        }
        assertEquals(List.of(), missing, "Every Java file opens with the Apache 2.0 header the other files carry");
    }

    @Test
    void theLockRuleAcceptsTryFinallyAndRejectsAGap() {
        String good = "class A {\n    void m(Graph g) {\n        g.readLock();\n        try {\n"
            + "            work();\n        } finally {\n            g.readUnlock();\n        }\n    }\n}\n";
        String gap = good.replace("g.readLock();\n", "g.readLock();\n        prepare();\n");
        String noFinally = "class A {\n    void m(Graph g) {\n        g.readLock();\n        try {\n"
            + "            work();\n        } catch (Exception e) {\n"
            + "            g.readUnlock();\n        }\n    }\n}\n";
        assertEquals(List.of(), unreleasedLocks("A.java", good));
        assertEquals(List.of("A.java:3 is not followed at once by try"), unreleasedLocks("A.java", gap));
        assertEquals(List.of("A.java:3 is not released in the try's finally block"),
            unreleasedLocks("A.java", noFinally));
    }

    @Test
    void threadsNeedANameArgument() {
        assertEquals(true, namedThread("Thread t = new Thread(r, \"Gephi AI worker\");"));
        assertEquals(false, namedThread("Thread t = new Thread(r);"));
        assertEquals(true, namedThread("Thread t = new Thread(() -> {\n    run();\n}, \"Gephi AI starter\");"));
    }

    // ---- rule mechanics, package-private so the rules themselves are tested above ----

    static List<String> unreleasedLocks(String file, String text) {
        List<String> found = new ArrayList<>();
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            if (isComment(lines[i]) || !LOCK_CALL.matcher(lines[i]).find()) {
                continue;
            }
            if (LOCK_HELPERS.contains(memberName(lines[memberStart(lines, i)]))) {
                continue;
            }
            int next = i + 1;
            while (next < lines.length && (lines[next].isBlank() || isComment(lines[next]))) {
                next++;
            }
            if (next >= lines.length || !lines[next].trim().startsWith("try")) {
                found.add(file + ":" + (i + 1) + " is not followed at once by try");
                continue;
            }
            String finallyBlock = finallyOf(text, offsetOf(lines, next));
            if (finallyBlock == null || !UNLOCK.matcher(finallyBlock).find()) {
                found.add(file + ":" + (i + 1) + " is not released in the try's finally block");
            }
        }
        return found;
    }

    /** The body of the finally block of the try statement starting at {@code at}, or null. */
    static String finallyOf(String text, int at) {
        int i = text.indexOf('{', at);
        while (i >= 0) {
            int end = matching(text, i, '{', '}');
            int j = end + 1;
            while (j < text.length() && Character.isWhitespace(text.charAt(j))) {
                j++;
            }
            if (text.startsWith("finally", j)) {
                int open = text.indexOf('{', j);
                return text.substring(open, matching(text, open, '{', '}') + 1);
            }
            if (!text.startsWith("catch", j)) {
                return null;
            }
            i = text.indexOf('{', j);
        }
        return null;
    }

    /** True when every {@code new Thread(...)} in {@code member} passes a name as its last argument. */
    static boolean namedThread(String member) {
        int at = member.indexOf("new Thread(");
        while (at >= 0) {
            String call = parenthesised(member, at + "new Thread".length());
            // The name is the last argument, or the one before a stack size.
            if (!call.matches("(?s).*,\\s*\"[^\"]+\"\\s*(,\\s*[\\w.]+\\s*)?\\)")) {
                return false;
            }
            at = member.indexOf("new Thread(", at + 1);
        }
        return true;
    }

    private static boolean isComment(String line) {
        String t = line.trim();
        return t.startsWith("//") || t.startsWith("*") || t.startsWith("/*");
    }

    private static int memberStart(String[] lines, int i) {
        for (int j = i; j >= 0; j--) {
            if (MEMBER.matcher(lines[j]).find()) {
                return j;
            }
        }
        return 0;
    }

    private static int memberEnd(String[] lines, int start) {
        for (int j = start + 1; j < lines.length; j++) {
            if (MEMBER.matcher(lines[j]).find()) {
                return j;
            }
        }
        return lines.length;
    }

    private static String memberName(String line) {
        Matcher m = MEMBER.matcher(line);
        return m.find() ? m.group(1) : "?";
    }

    private static int offsetOf(String[] lines, int line) {
        int offset = 0;
        for (int i = 0; i < line; i++) {
            offset += lines[i].length() + 1;
        }
        return offset;
    }

    private static int lineOf(String text, int offset) {
        return (int) text.substring(0, offset).chars().filter(c -> c == '\n').count() + 1;
    }

    private static String parenthesised(String text, int open) {
        return text.substring(open, matching(text, open, '(', ')') + 1);
    }

    /** Index of the bracket closing the one at {@code open}, skipping string and character literals. */
    private static int matching(String text, int open, char left, char right) {
        int depth = 0;
        for (int i = open; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '"' || c == '\'') {
                for (i++; i < text.length() && text.charAt(i) != c; i++) {
                    if (text.charAt(i) == '\\') {
                        i++;
                    }
                }
            } else if (c == left) {
                depth++;
            } else if (c == right && --depth == 0) {
                return i;
            }
        }
        return text.length() - 1;
    }

    /**
     * Gephi 0.11.4 gives GraphLock a timed read and its live counters, so only the write lock,
     * which has no timed acquisition in the public API, may still be reached by reflection.
     */
    @Test
    void onlyTheWriteLockIsReachedByReflection() {
        String svc = sources.get("GephiControlService.java");
        org.junit.jupiter.api.Assertions.assertFalse(svc.contains("getDeclaredField(\"readLock\")"),
            "the read lock must use GraphLock.tryReadLock");
        org.junit.jupiter.api.Assertions.assertFalse(svc.contains("getDeclaredField(\"readWriteLock\")"),
            "the lock counters must use the GraphLock API");
        org.junit.jupiter.api.Assertions.assertTrue(svc.contains("getDeclaredField(\"writeLock\")"),
            "lockWrite still needs its timed write handle");
    }

    @Test
    void threadNameMayBeFollowedByAStackSize() {
        org.junit.jupiter.api.Assertions.assertTrue(namedThread("new Thread(r, \"Worker\")"));
        org.junit.jupiter.api.Assertions.assertTrue(namedThread("new Thread(null, r, \"Worker\", STACK_BYTES)"));
        org.junit.jupiter.api.Assertions.assertFalse(namedThread("new Thread(r)"));
        org.junit.jupiter.api.Assertions.assertFalse(namedThread("new Thread(null, r, STACK_BYTES)"));
    }
}
