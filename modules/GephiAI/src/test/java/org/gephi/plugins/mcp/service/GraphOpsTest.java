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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the graph-mutation cores against a standalone in-memory
 * GraphModel (no NetBeans platform / running Gephi required). These exercise the
 * actual fixes: batch attributes, edge directedness, the negative-value ranking
 * regression, and CSV assembly.
 */
class GraphOpsTest {

    private static GraphModel newModel() {
        return GraphModel.Factory.newInstance();
    }

    /** Build a node map {id, attributes:{...}} from id + alternating attr key/value pairs. */
    private static Map<String, Object> node(String id, Object... attrKv) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        if (attrKv.length > 0) {
            Map<String, Object> attrs = new LinkedHashMap<>();
            for (int i = 0; i + 1 < attrKv.length; i += 2) attrs.put((String) attrKv[i], attrKv[i + 1]);
            m.put("attributes", attrs);
        }
        return m;
    }

    @Test
    void batchAddAppliesPerNodeAttributes() {
        GraphModel gm = newModel();
        JsonObject r = GephiControlService.addNodesToModel(gm,
            List.of(node("a", "team", "red"), node("b", "team", "blue")));
        assertTrue(r.get("success").getAsBoolean());
        assertEquals(2, r.get("added").getAsInt());

        Graph g = gm.getGraph();
        Column team = gm.getNodeTable().getColumn("team");
        assertNotNull(team, "attribute column should be auto-created");
        assertEquals("red", g.getNode("a").getAttribute(team));
        assertEquals("blue", g.getNode("b").getAttribute(team));
    }

    @Test
    void batchAddSkipsDuplicateIds() {
        GraphModel gm = newModel();
        GephiControlService.addNodeToModel(gm, "a", null, null);
        JsonObject r = GephiControlService.addNodesToModel(gm, List.of(node("a"), node("b")));
        assertEquals(1, r.get("added").getAsInt());
        assertEquals(1, r.get("skipped").getAsInt());
    }

    @Test
    void addEdgeRespectsUndirectedFlag() {
        GraphModel gm = newModel();
        GephiControlService.addNodeToModel(gm, "a", null, null);
        GephiControlService.addNodeToModel(gm, "b", null, null);
        JsonObject r = GephiControlService.addEdgeToModel(gm, "a", "b", 2.0, false);
        assertTrue(r.get("success").getAsBoolean());

        Graph g = gm.getGraph();
        Edge e = g.getEdge(g.getNode("a"), g.getNode("b"), 0); // type 0 == undirected
        assertNotNull(e, "undirected edge (type 0) should exist");
        assertFalse(e.isDirected());
        assertEquals(2.0, e.getWeight(), 1e-9);
    }

    @Test
    void addEdgeRejectsDuplicate() {
        GraphModel gm = newModel();
        GephiControlService.addNodeToModel(gm, "a", null, null);
        GephiControlService.addNodeToModel(gm, "b", null, null);
        assertTrue(GephiControlService.addEdgeToModel(gm, "a", "b", 1.0, true).get("success").getAsBoolean());
        assertFalse(GephiControlService.addEdgeToModel(gm, "a", "b", 1.0, true).get("success").getAsBoolean());
    }

    @Test
    void batchAddEdgesHonorsDirectedLabelAndAttributes() {
        GraphModel gm = newModel();
        GephiControlService.addNodesToModel(gm, List.of(node("a"), node("b")));

        Map<String, Object> edge = new LinkedHashMap<>();
        edge.put("source", "a");
        edge.put("target", "b");
        edge.put("directed", false);
        edge.put("label", "knows");
        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("since", 1999);
        edge.put("attributes", attrs);

        JsonObject r = GephiControlService.addEdgesToModel(gm, List.of(edge));
        assertEquals(1, r.get("added").getAsInt());

        Graph g = gm.getGraph();
        Edge e = GephiControlService.findEdge(g, g.getNode("a"), g.getNode("b"));
        assertNotNull(e);
        assertFalse(e.isDirected());
        assertEquals("knows", e.getLabel());
        Column since = gm.getEdgeTable().getColumn("since");
        assertNotNull(since);
        assertEquals(1999, ((Number) e.getAttribute(since)).intValue());
    }

    @Test
    void numericRangeHandlesAllNegativeValues() {
        // The regression that motivated the fix: a column whose values are all negative.
        // The old Double.MIN_VALUE seed left max at a tiny positive number here.
        GraphModel gm = newModel();
        GephiControlService.addNodesToModel(gm,
            List.of(node("a", "score", -10.0), node("b", "score", -2.0), node("c", "score", -7.0)));
        Column score = gm.getNodeTable().getColumn("score");
        double[] mm = GephiControlService.numericRange(gm.getGraph(), score);
        assertNotNull(mm);
        assertEquals(-10.0, mm[0], 1e-9, "min");
        assertEquals(-2.0, mm[1], 1e-9, "max");
    }

    @Test
    void numericRangeIsNullWhenNoNumericValues() {
        GraphModel gm = newModel();
        GephiControlService.addNodesToModel(gm, List.of(node("a", "tag", "x")));
        Column tag = gm.getNodeTable().getColumn("tag");
        assertNull(GephiControlService.numericRange(gm.getGraph(), tag));
    }

    @Test
    void addColumnCreatesAndRejectsDuplicateAndBadType() {
        GraphModel gm = newModel();
        assertTrue(GephiControlService.addColumnToModel(gm, "weight2", "double", "node")
            .get("success").getAsBoolean());
        assertNotNull(gm.getNodeTable().getColumn("weight2"));
        // duplicate name -> error
        assertFalse(GephiControlService.addColumnToModel(gm, "weight2", "double", "node")
            .get("success").getAsBoolean());
        // unknown type -> error
        assertFalse(GephiControlService.addColumnToModel(gm, "other", "notatype", "node")
            .get("success").getAsBoolean());
    }

    // ── the deadlock-safe write lock (reflection linchpin) ──────────────

    @Test
    void writeLockHandleResolvesGephiInternalLock() {
        // If Gephi ever renames GraphLockImpl.writeLock, this returns null and lockWrite
        // silently degrades to the deadlocking blocking lock. This test guards that.
        GraphModel gm = newModel();
        assertNotNull(GephiControlService.writeLockHandle(gm.getGraph()),
            "reflection into the graph's WriteLock must resolve");
    }

    @Test
    void lockWriteAcquiresAndReleasesViaWriteUnlock() {
        GraphModel gm = newModel();
        Graph g = gm.getGraph();
        GephiControlService.lockWrite(g);
        try {
            assertEquals(1, g.getLock().getWriteHoldCount(), "lockWrite must hold the write lock");
        } finally {
            g.writeUnlock();
        }
        assertEquals(0, g.getLock().getWriteHoldCount(), "writeUnlock must release what lockWrite took");
    }

    /**
     * Regression guard for the wedge-by-leak bug: breaking out of a live
     * auto-locked NodeIterable/EdgeIterable before exhaustion leaks a read hold
     * that is never released (and, on a dying request thread, never releasable),
     * after which no writer can ever acquire the lock. Query endpoints must
     * iterate a toArray() snapshot instead. This encodes the graphstore contract
     * both patterns rely on.
     */
    @Test
    void earlyBreakOverToArraySnapshotLeavesNoReadHold() throws Exception {
        GraphModel gm = newModel();
        for (int i = 0; i < 10; i++) {
            gm.getGraph().addNode(gm.factory().newNode("n" + i));
        }
        Graph g = gm.getGraph();

        // the fixed pattern: snapshot, then break early
        int count = 0;
        for (org.gephi.graph.api.Node n : g.getNodes().toArray()) {
            if (count >= 3) break;
            count++;
        }

        java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock wl =
            GephiControlService.writeLockHandle(g);
        assertNotNull(wl, "write lock must be reachable via reflection");
        assertTrue(wl.tryLock(200, java.util.concurrent.TimeUnit.MILLISECONDS),
            "write lock must be immediately acquirable after an early-broken toArray loop");
        wl.unlock();

        // and the trap itself, for documentation: a live-iterable early break leaks
        java.util.Iterator<org.gephi.graph.api.Node> it = g.getNodes().iterator();
        it.next(); // iterator constructor auto-acquired the read lock
        assertFalse(wl.tryLock(50, java.util.concurrent.TimeUnit.MILLISECONDS),
            "an unexhausted live iterator holds the read lock (the leak this guards against)");
        while (it.hasNext()) it.next(); // exhaustion releases it
        assertTrue(wl.tryLock(200, java.util.concurrent.TimeUnit.MILLISECONDS));
        wl.unlock();
    }

    @Test
    void buildCsvQuotesFieldsContainingSeparator() {
        GraphModel gm = newModel();
        Map<String, Object> n = new LinkedHashMap<>();
        n.put("id", "a");
        n.put("label", "Smith, John"); // label contains the separator -> must be quoted
        GephiControlService.addNodesToModel(gm, List.of(n));

        String[] lines = GephiControlService.buildCsv(gm, ",", "nodes").split("\n");
        assertEquals("Id,Label", lines[0]);
        assertEquals("a,\"Smith, John\"", lines[1]);
    }

    // ─── Data Laboratory cores (Group D) ─────────────────────────────

    @Test
    void columnValueFrequenciesCountsPerValue() {
        GraphModel gm = newModel();
        GephiControlService.addNodesToModel(gm, List.of(
            node("a", "team", "red"), node("b", "team", "red"),
            node("c", "team", "blue"), node("d", "team", "red")));

        JsonObject r = GephiControlService.columnValueFrequenciesCore(gm, "node", "team");
        assertTrue(r.get("success").getAsBoolean());
        assertEquals(2, r.get("distinct_values").getAsInt());
        assertEquals(4, r.get("total").getAsInt());
        JsonObject freq = r.getAsJsonObject("frequencies");
        assertEquals(3, freq.get("red").getAsInt());
        assertEquals(1, freq.get("blue").getAsInt());
    }

    @Test
    void columnValueFrequenciesErrorsOnMissingColumn() {
        GraphModel gm = newModel();
        GephiControlService.addNodesToModel(gm, List.of(node("a")));
        JsonObject r = GephiControlService.columnValueFrequenciesCore(gm, "node", "nope");
        assertFalse(r.get("success").getAsBoolean());
    }

    @Test
    void detectDuplicatesGroupsSharedValues() {
        GraphModel gm = newModel();
        GephiControlService.addNodesToModel(gm, List.of(
            node("a", "email", "x@y.com"), node("b", "email", "x@y.com"),
            node("c", "email", "z@y.com"), node("d", "email", "x@y.com")));

        JsonObject r = GephiControlService.detectDuplicatesCore(gm, "node", "email", true);
        assertTrue(r.get("success").getAsBoolean());
        assertEquals(1, r.get("group_count").getAsInt()); // only x@y.com is duplicated
        assertEquals(3, r.getAsJsonArray("duplicate_groups").get(0).getAsJsonArray().size());
    }

    // ─── Typed parallel edges (Group F) ──────────────────────────────

    private static GraphModel modelWithNodes(String... ids) {
        GraphModel gm = newModel();
        java.util.List<Map<String, Object>> ns = new java.util.ArrayList<>();
        for (String id : ids) ns.add(node(id));
        GephiControlService.addNodesToModel(gm, ns);
        return gm;
    }

    @Test
    void untypedDuplicateEdgeStillBlocked() {
        // Regression: the pre-existing single-edge-per-pair rule must be unchanged
        // when no edge_type is given.
        GraphModel gm = modelWithNodes("a", "b");
        assertTrue(GephiControlService.addEdgeToModel(gm, "a", "b", 1.0, true, null).get("success").getAsBoolean());
        assertFalse(GephiControlService.addEdgeToModel(gm, "a", "b", 1.0, true, null).get("success").getAsBoolean());
        assertEquals(1, gm.getGraph().getEdgeCount());
    }

    @Test
    void differentTypedEdgesCoexistBetweenSamePair() {
        GraphModel gm = modelWithNodes("a", "b");
        assertTrue(GephiControlService.addEdgeToModel(gm, "a", "b", 1.0, true, "cites").get("success").getAsBoolean());
        assertTrue(GephiControlService.addEdgeToModel(gm, "a", "b", 1.0, true, "coauthor").get("success").getAsBoolean());
        assertEquals(2, gm.getGraph().getEdgeCount(), "two typed parallel edges should coexist");
        assertTrue(gm.getEdgeTypeCount() >= 2);
    }

    @Test
    void sameTypedEdgeIsStillBlocked() {
        GraphModel gm = modelWithNodes("a", "b");
        assertTrue(GephiControlService.addEdgeToModel(gm, "a", "b", 1.0, true, "cites").get("success").getAsBoolean());
        assertFalse(GephiControlService.addEdgeToModel(gm, "a", "b", 1.0, true, "cites").get("success").getAsBoolean(),
            "a second edge of the SAME type between the same pair is still a duplicate");
        assertEquals(1, gm.getGraph().getEdgeCount());
    }

    @Test
    void batchAddHonorsPerEdgeType() {
        GraphModel gm = modelWithNodes("a", "b");
        Map<String, Object> e1 = new LinkedHashMap<>();
        e1.put("source", "a"); e1.put("target", "b"); e1.put("edge_type", "cites");
        Map<String, Object> e2 = new LinkedHashMap<>();
        e2.put("source", "a"); e2.put("target", "b"); e2.put("edge_type", "coauthor");
        JsonObject r = GephiControlService.addEdgesToModel(gm, List.of(e1, e2));
        assertEquals(2, r.get("added").getAsInt());
        assertEquals(2, gm.getGraph().getEdgeCount());
    }

    @Test
    void detectDuplicatesRespectsCaseInsensitivity() {
        GraphModel gm = newModel();
        GephiControlService.addNodesToModel(gm, List.of(
            node("a", "name", "Alice"), node("b", "name", "alice")));

        assertEquals(0, GephiControlService.detectDuplicatesCore(gm, "node", "name", true)
            .get("group_count").getAsInt());   // case-sensitive: distinct
        assertEquals(1, GephiControlService.detectDuplicatesCore(gm, "node", "name", false)
            .get("group_count").getAsInt());   // case-insensitive: same
    }

    // ── empty-graph edge cases ───────────────────────────────────────────

    @Test
    void buildCsvOnEmptyGraphIsHeadersOnlyWithNoDataRows() {
        GraphModel gm = newModel();
        String csv = GephiControlService.buildCsv(gm, ",", "both");
        String[] sections = csv.split("\n\n");
        assertEquals(2, sections.length, "expected a node section and an edge section");
        String[] nodeLines = sections[0].split("\n");
        assertEquals(1, nodeLines.length, "empty graph must yield the node header and zero rows");
        assertTrue(nodeLines[0].startsWith("Id,Label"), "node header missing: " + nodeLines[0]);
        String[] edgeLines = sections[1].split("\n");
        assertEquals(1, edgeLines.length, "empty graph must yield the edge header and zero rows");
        assertTrue(edgeLines[0].startsWith("Source,Target,Weight"), "edge header missing: " + edgeLines[0]);
    }

    @Test
    void numericRangeOnEmptyGraphIsNull() {
        GraphModel gm = newModel();
        Column score = gm.getNodeTable().addColumn("score", Double.class);
        // No nodes at all (not merely no numeric values): must be null, not [∞, -∞].
        assertNull(GephiControlService.numericRange(gm.getGraph(), score));
    }
}
