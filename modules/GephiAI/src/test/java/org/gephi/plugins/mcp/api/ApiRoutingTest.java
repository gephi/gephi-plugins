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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openide.util.Lookup;

/**
 * Every endpoint's argument handling over a real connection: what a caller is told when a
 * required value is missing or of the wrong type, and that the values it does send reach the
 * tool. Each test starts from a fresh project holding a small network built through the API.
 */
class ApiRoutingTest {

    private static GephiAPIServer server;
    private static int port;

    @BeforeAll
    static void start() throws Exception {
        server = new GephiAPIServer(0);
        server.startServer();
        port = server.getListeningPort();
    }

    @AfterAll
    static void stop() {
        server.stop();
    }

    /** A triangle a-b-c with d hanging off c, and e alone; each node has a group. */
    @BeforeEach
    void network() throws Exception {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        ok(call("POST", "/graph/nodes/add", "{\"nodes\": ["
            + "{\"id\": \"a\", \"label\": \"Ada Fennimore\", \"attributes\": {\"group\": \"x\", \"score\": 1}},"
            + "{\"id\": \"b\", \"attributes\": {\"group\": \"x\", \"score\": 2}},"
            + "{\"id\": \"c\", \"attributes\": {\"group\": \"y\", \"score\": 3}},"
            + "{\"id\": \"d\", \"attributes\": {\"group\": \"y\", \"score\": 4}},"
            + "{\"id\": \"e\", \"attributes\": {\"group\": \"x\", \"score\": 5}}]}"));
        ok(call("POST", "/graph/edges/add", "{\"edges\": ["
            + "{\"source\": \"a\", \"target\": \"b\", \"directed\": false, \"weight\": 1},"
            + "{\"source\": \"b\", \"target\": \"c\", \"directed\": false, \"weight\": 2},"
            + "{\"source\": \"c\", \"target\": \"a\", \"directed\": false, \"weight\": 3},"
            + "{\"source\": \"c\", \"target\": \"d\", \"directed\": false, \"weight\": 4}]}"));
    }

    // ─── Plumbing ────────────────────────────────────────────────────

    /** Sends a request and returns the status code followed by the parsed JSON body. */
    private static Reply call(String method, String path, String body) throws Exception {
        StringBuilder r = new StringBuilder(method + " " + path + " HTTP/1.1\r\nHost: 127.0.0.1\r\n");
        byte[] bytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        if (body != null) {
            r.append("Content-Type: application/json; charset=utf-8\r\n")
                .append("Content-Length: ").append(bytes.length).append("\r\n");
        }
        r.append("Connection: close\r\n\r\n");
        try (Socket s = new Socket("127.0.0.1", port)) {
            s.setSoTimeout(30_000);
            OutputStream out = s.getOutputStream();
            out.write(r.toString().getBytes(StandardCharsets.UTF_8));
            out.write(bytes);
            out.flush();
            InputStream in = s.getInputStream();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            byte[] b = new byte[8192];
            int n;
            while ((n = in.read(b)) > 0) {
                buf.write(b, 0, n);
            }
            String response = new String(buf.toByteArray(), StandardCharsets.UTF_8);
            int status = Integer.parseInt(response.split(" ", 3)[1]);
            String json = response.substring(response.indexOf("\r\n\r\n") + 4);
            return new Reply(status, JsonParser.parseString(json).getAsJsonObject());
        }
    }

    private static Reply call(String method, String path) throws Exception {
        return call(method, path, null);
    }

    private static final class Reply {
        final int status;
        final JsonObject json;

        Reply(int status, JsonObject json) {
            this.status = status;
            this.json = json;
        }

        @Override
        public String toString() {
            return status + " " + json;
        }
    }

    private static JsonObject ok(Reply r) {
        assertEquals(200, r.status, r.toString());
        assertTrue(r.json.get("success").getAsBoolean(), r.toString());
        return r.json;
    }

    /** The error text of a refused request, which must be a 4xx or 5xx with a non-empty error. */
    private static String refused(Reply r) {
        assertTrue(r.status >= 400, r.toString());
        assertFalse(r.json.get("success").getAsBoolean(), r.toString());
        assertTrue(r.json.has("error") && !r.json.get("error").isJsonNull(), "no error text: " + r);
        String error = r.json.get("error").getAsString();
        assertFalse(error.isBlank(), r.toString());
        return error;
    }

    private static GraphModel model() {
        return Lookup.getDefault().lookup(GraphController.class).getGraphModel();
    }

    private static String encoded(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    // ─── Missing arguments ──────────────────────────────────────────

    /**
     * A request without its required values is refused with a message naming them, before any
     * tool runs. Each row: method, path, body ("-" for none), text the error must contain.
     */
    @ParameterizedTest(name = "{0} {1} {2}")
    @CsvSource(delimiter = '|', value = {
        "POST | /perspective/switch | {} | 'name'",
        "POST | /project/open | {} | 'file'",
        "POST | /project/save | - | 'file'",
        "POST | /workspace/switch | {} | 'index'",
        "POST | /workspace/duplicate | {} | 'index'",
        "POST | /workspace/rename | {\"index\": 0} | 'name'",
        "DELETE | /workspace/delete?index=-2 | - | 'index'",
        "POST | /graph/node/add | {} | 'id'",
        "POST | /graph/nodes/add | {} | 'nodes'",
        "POST | /graph/nodes/remove | {} | 'ids'",
        "GET | /graph/node/get/ | - | node ID",
        "POST | /graph/node/label | {\"id\": \"a\"} | 'label'",
        "POST | /graph/node/position | {} | 'id'",
        "POST | /graph/nodes/positions | {} | 'positions'",
        "POST | /graph/edge/add | {\"source\": \"a\"} | 'target'",
        "POST | /graph/edges/add | {} | 'edges'",
        "POST | /graph/edge/remove | {\"target\": \"a\"} | 'source'",
        "POST | /graph/edge/weight | {\"source\": \"a\", \"target\": \"b\"} | 'weight'",
        "POST | /graph/edge/label | {\"source\": \"a\", \"target\": \"b\"} | 'label'",
        "POST | /graph/columns/add | {\"name\": \"n\"} | 'type'",
        "POST | /graph/node/attributes | {\"id\": \"a\"} | 'attributes'",
        "POST | /graph/nodes/attributes | {} | 'updates'",
        "POST | /graph/edge/attributes | {\"source\": \"a\", \"target\": \"b\"} | 'attributes'",
        "POST | /appearance/node/color | {} | 'id'",
        "POST | /appearance/node/size | {\"id\": \"a\"} | 'size'",
        "POST | /appearance/edge/color | {\"source\": \"a\"} | 'target'",
        "POST | /appearance/nodes/color | {} | 'nodes'",
        "POST | /appearance/partition/color | {} | 'column'",
        "POST | /appearance/edge/partition-color | {} | 'column'",
        "POST | /appearance/ranking/color | {} | 'column'",
        "POST | /appearance/ranking/size | {} | 'column'",
        "POST | /layout/run | {} | 'algorithm'",
        "GET | /layout/properties | - | 'algorithm'",
        "POST | /layout/properties | {\"algorithm\": \"x\"} | 'properties'",
        "POST | /statistics/run | {} | 'name'",
        "POST | /filter/ego-network | {} | 'node_id'",
        "POST | /filter/apply | {} | 'name'",
        "POST | /filter/combine | {\"filters\": \"Degree Range\"} | 'filters'",
        "POST | /time/from-columns | - | body",
        "POST | /time/slice | {\"start\": 1} | 'end'",
        "POST | /graph/shortest-path | {\"source\": \"a\"} | 'target'",
        "POST | /datalab/column/edit | {\"column\": \"group\"} | 'action'",
        "POST | /datalab/frequencies | {} | 'column'",
        "POST | /datalab/duplicates | {} | 'column'",
        "POST | /datalab/merge-nodes | {} | 'ids'",
        "POST | /datalab/regex-column | {\"column\": \"group\", \"new_column\": \"n\"} | 'regex'",
        "POST | /preview/settings | - | body",
        "POST | /export/format | {\"file\": \"/tmp/x\"} | 'format'",
        "POST | /export/png | {} | 'file'",
        "POST | /export/screenshot | {} | 'file'",
        "POST | /export/pdf | {} | 'file'",
        "POST | /export/svg | {} | 'file'",
        "POST | /export/graphml | {} | 'file'",
        "POST | /export/csv | {} | 'file'",
        "POST | /import/gexf | {} | 'file'",
        "POST | /import/graphml | {} | 'file'",
        "POST | /import/csv | {} | 'file'",
        "POST | /import/file | {} | 'file'",
        "POST | /graph/generate | {\"model\": \"random\"} | 'nodes'",
    })
    void requiredValuesAreNamedWhenMissing(String method, String path, String body, String expected)
        throws Exception {
        String error = refused(call(method, path, "-".equals(body) ? null : body));
        assertTrue(error.contains(expected), error);
        assertEquals(5, model().getGraph().getNodeCount(), "a refused request changed the graph");
    }

    @Test
    void anEndpointCalledWithTheWrongMethodIsUnknown() throws Exception {
        assertTrue(refused(call("GET", "/graph/node/add")).contains("Unknown endpoint: GET /graph/node/add"));
        assertTrue(refused(call("POST", "/graph/nodes", "{}")).contains("Unknown endpoint"));
    }

    // ─── Values of the wrong type ───────────────────────────────────

    @ParameterizedTest(name = "{0} {1}")
    @CsvSource(delimiter = '|', value = {
        "/graph/node/add | {\"id\": {\"nested\": true}}",
        "/workspace/switch | {\"index\": \"first\"}",
        "/graph/nodes/add | {\"nodes\": \"a,b\"}",
        "/appearance/partition/color | {\"column\": \"group\", \"colors\": {\"x\": [0, 114]}}",
        "/graph/edge/add | {\"source\": null, \"target\": \"b\"}",
        "/time/slice | {\"start\": \"spring\", \"end\": 2}",
    })
    void valueOfTheWrongTypeIsReportedWithTheRequestItCameIn(String path, String body) throws Exception {
        Reply r = call("POST", path, body);
        String error = refused(r);
        assertTrue(error.contains("POST " + path), error);
        assertTrue(error.contains("wrong type"), error);
        assertEquals(5, model().getGraph().getNodeCount(), "a refused request changed the graph");
    }

    @Test
    void bodyThatIsNotAnObjectIsRefused() throws Exception {
        String error = refused(call("POST", "/graph/nodes/add", "[1, 2]"));
        assertTrue(error.contains("not a JSON object"), error);
    }

    // ─── Values reach the tool ──────────────────────────────────────

    @Test
    void nodeSearchParametersAreRead() throws Exception {
        JsonObject r = ok(call("GET", "/graph/nodes?column=group&value=y&sort_by=score&descending=false"
            + "&columns=score&limit=1"));
        assertEquals(2, r.get("matches").getAsInt());
        JsonObject first = r.getAsJsonArray("nodes").get(0).getAsJsonObject();
        assertEquals("c", first.get("id").getAsString());
        assertFalse(first.getAsJsonObject("attributes").has("group"));

        JsonObject range = ok(call("GET", "/graph/nodes?column=score&min=2&max=4"));
        assertEquals(3, range.get("matches").getAsInt());

        JsonObject text = ok(call("GET", "/graph/nodes?column=label&contains=" + encoded("fenn")));
        assertEquals(1, text.get("matches").getAsInt());

        // An unreadable limit falls back to the default rather than failing the request.
        assertEquals(5, ok(call("GET", "/graph/nodes?limit=lots")).get("count").getAsInt());
    }

    @Test
    void nodesAreReadEditedAndRemovedByIdInThePath() throws Exception {
        assertEquals("Ada Fennimore", ok(call("GET", "/graph/node/get/a")).getAsJsonObject("node")
            .get("label").getAsString());
        ok(call("POST", "/graph/node/label", "{\"id\": \"b\", \"label\": \"Kowalski\"}"));
        ok(call("POST", "/graph/node/position", "{\"id\": \"b\", \"x\": 12.5, \"y\": -3}"));
        ok(call("POST", "/appearance/node/size", "{\"id\": \"b\", \"size\": 22}"));
        ok(call("POST", "/appearance/node/color", "{\"id\": \"b\", \"r\": 0, \"g\": 114, \"b\": 178}"));
        JsonObject b = ok(call("GET", "/graph/node/get/b")).getAsJsonObject("node");
        assertEquals("Kowalski", b.get("label").getAsString());
        assertEquals(12.5, b.get("x").getAsDouble(), 1e-6);
        assertEquals(22, b.get("size").getAsDouble(), 1e-6);
        assertEquals(114, b.get("g").getAsInt());

        ok(call("DELETE", "/graph/node/e"));
        assertEquals(4, model().getGraph().getNodeCount());
        assertTrue(refused(call("DELETE", "/graph/node/zz")).contains("zz"));
    }

    @Test
    void edgesAreEditedAndPaged() throws Exception {
        ok(call("POST", "/graph/edge/weight", "{\"source\": \"a\", \"target\": \"b\", \"weight\": 9}"));
        ok(call("POST", "/graph/edge/label", "{\"source\": \"a\", \"target\": \"b\", \"label\": \"knows\"}"));
        ok(call("POST", "/appearance/edge/color", "{\"source\": \"a\", \"target\": \"b\", \"r\": 230}"));
        JsonObject page = ok(call("GET", "/graph/edges?limit=2&offset=1"));
        assertEquals(2, page.getAsJsonArray("edges").size());
        ok(call("POST", "/graph/edge/remove", "{\"source\": \"c\", \"target\": \"d\"}"));
        assertEquals(3, model().getGraph().getEdgeCount());
    }

    @Test
    void statisticsRunWithTheirParameters() throws Exception {
        JsonObject r = ok(call("POST", "/statistics/run",
            "{\"name\": \"Modularity\", \"params\": {\"resolution\": 1.0}}"));
        assertFalse(r.has("unapplied_params"), r.toString());
        assertTrue(ok(call("POST", "/statistics/degree", null)).has("average_degree"));
        assertEquals(2, ok(call("POST", "/statistics/connected-components", null))
            .get("connected_components").getAsInt());
        assertTrue(ok(call("GET", "/statistics/available")).toString().contains("Degree"));
    }

    @Test
    void shortestPathOptionsAreRead() throws Exception {
        JsonObject r = ok(call("POST", "/graph/shortest-path", "{\"source\": \"a\", \"target\": \"d\","
            + " \"weighting\": \"distance\", \"follow_direction\": false, \"mark_column\": \"route\"}"));
        assertEquals(2, r.get("steps").getAsInt());
        assertEquals("route", r.get("mark_column").getAsString());
    }

    @Test
    void filterRequestsAreRead() throws Exception {
        JsonObject dry = ok(call("POST", "/filter/degree", "{\"min\": 2, \"dry_run\": true}"));
        assertEquals(2, dry.get("would_remove").getAsInt());
        JsonObject combined = ok(call("POST", "/filter/combine", "{\"filters\": [{\"name\": \"Degree Range\","
            + " \"params\": {\"range\": [2, 100]}}], \"combine\": \"all\", \"dry_run\": true}"));
        assertEquals(3, combined.get("nodes_kept").getAsInt());
        JsonObject col = ok(call("POST", "/filter/apply", "{\"name\": \"Degree Range\","
            + " \"params\": {\"range\": [2, 100]}, \"action\": \"column\", \"column\": \"busy\"}"));
        assertEquals("busy", col.get("column").getAsString());
        assertTrue(ok(call("GET", "/filter/list")).getAsJsonArray("filters").size() > 0);
        ok(call("POST", "/filter/remove-isolates", "{\"dry_run\": true}"));
        assertEquals(5, model().getGraph().getNodeCount(), "a dry run changed the graph");
    }

    @Test
    void stylingRequestsAreRead() throws Exception {
        ok(call("POST", "/appearance/partition/color", "{\"column\": \"group\","
            + " \"colors\": {\"x\": [0, 114, 178], \"y\": [230, 159, 0]}}"));
        assertEquals(0, model().getGraph().getNode("a").getColor().getRed());
        assertEquals(230, model().getGraph().getNode("c").getColor().getRed());
        JsonObject size = ok(call("POST", "/appearance/ranking/size", "{\"column\": \"score\", \"min_size\": 2,"
            + " \"max_size\": 20, \"cap\": null}"));
        assertEquals(5.0, size.get("max_value").getAsDouble());
        assertEquals(20f, model().getGraph().getNode("e").size(), 1e-4);
        ok(call("POST", "/appearance/ranking/color", "{\"column\": \"score\"}"));
        ok(call("POST", "/appearance/edge/thickness-by-weight", "{\"min_thickness\": 1, \"max_thickness\": 4}"));
        ok(call("POST", "/appearance/reset", "{}"));
        assertEquals(153, model().getGraph().getNode("e").getColor().getRed());
        assertEquals(20f, model().getGraph().getNode("e").size(), 1e-4,
            "a colour reset with no size asked for wiped the size ranking");
        ok(call("POST", "/appearance/reset", "{\"size\": 7}"));
        assertEquals(7f, model().getGraph().getNode("e").size(), 1e-4);
    }

    @Test
    void previewSettingsNestedUnderSettingsAreUnwrapped() throws Exception {
        JsonObject r = ok(call("POST", "/preview/settings", "{\"settings\": {\"node.label.show\": true,"
            + " \"node.label.max-char\": 12}}"));
        assertEquals(2, r.get("properties_set").getAsInt(), r.toString());
        JsonObject read = ok(call("GET", "/preview/settings")).getAsJsonObject("settings");
        assertTrue(read.get("node.label.show").getAsBoolean());
        assertEquals(12, read.get("node.label.max-char").getAsInt());
    }

    @Test
    void columnsAndDataLaboratoryRequestsAreRead() throws Exception {
        ok(call("POST", "/graph/columns/add", "{\"name\": \"rank\", \"type\": \"integer\"}"));
        JsonArray cols = ok(call("GET", "/graph/columns?target=node")).getAsJsonArray("columns");
        assertTrue(cols.toString().contains("\"rank\""), cols.toString());
        ok(call("POST", "/graph/node/attributes", "{\"id\": \"a\", \"attributes\": {\"rank\": 3}}"));
        JsonObject freq = ok(call("POST", "/datalab/frequencies", "{\"column\": \"group\"}"));
        assertNotNull(freq);
        JsonObject partial = ok(call("POST", "/datalab/regex-column", "{\"column\": \"label\","
            + " \"new_column\": \"starts_ada\", \"regex\": \"^Ada\"}"));
        assertEquals(0, partial.get("matched").getAsInt());
        assertTrue(partial.get("warning").getAsString().contains("whole value"), partial.toString());
        JsonObject whole = ok(call("POST", "/datalab/regex-column", "{\"column\": \"label\","
            + " \"new_column\": \"is_ada\", \"regex\": \"Ada.*\"}"));
        assertEquals(1, whole.get("matched").getAsInt());
        assertFalse(whole.has("warning"));
        assertEquals(Boolean.TRUE, model().getGraph().getNode("a").getAttribute("is_ada"));
    }

    @Test
    void exportsWriteTheFileTheyAreGiven(@TempDir Path dir) throws Exception {
        JsonObject inline = ok(call("POST", "/export/gexf", "{\"inline\": true, \"visible\": false}"));
        assertTrue(inline.get("content").getAsString().contains("Ada Fennimore"));

        Path csv = dir.resolve("edges.csv");
        ok(call("POST", "/export/csv", "{\"file\": " + jsonString(csv) + ", \"target\": \"edges\","
            + " \"separator\": \";\"}"));
        assertEquals(5, Files.readAllLines(csv).size());

        Path gml = dir.resolve("g.gml");
        ok(call("POST", "/export/format", "{\"file\": " + jsonString(gml) + ", \"format\": \"gml\"}"));
        assertTrue(Files.size(gml) > 0);
    }

    @Test
    void workspacesAreListedAndRenamed() throws Exception {
        JsonArray list = ok(call("GET", "/workspace/list")).getAsJsonArray("workspaces");
        assertEquals(1, list.size());
        ok(call("POST", "/workspace/rename", "{\"index\": 0, \"name\": \"Wójcik network\"}"));
        boolean renamed = false;
        for (JsonElement w : ok(call("GET", "/workspace/list")).getAsJsonArray("workspaces")) {
            renamed |= w.toString().contains("Wójcik network");
        }
        assertTrue(renamed, "a non-ASCII name did not survive the round trip");
    }

    private static String jsonString(Path p) {
        JsonArray a = new JsonArray();
        a.add(p.toString());
        return a.get(0).toString();
    }
}
