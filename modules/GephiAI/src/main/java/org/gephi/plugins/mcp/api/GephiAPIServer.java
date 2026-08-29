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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fi.iki.elonen.NanoHTTPD;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.plugins.mcp.service.GephiControlService;

public class GephiAPIServer extends NanoHTTPD {

    private static final Logger LOGGER = Logger.getLogger(GephiAPIServer.class.getName());
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final GephiControlService service;

    public GephiAPIServer(int port) {
        super("127.0.0.1", port);
        this.service = GephiControlService.getInstance();
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        // FINE, not INFO: a client polling /health or /layout/status would otherwise
        // fill messages.log for the whole session. Start and stop stay at INFO.
        LOGGER.fine(() -> "Gephi AI API: " + method + " " + uri);

        // Defense against DNS-rebinding: the API is reachable only from local
        // processes (the MCP server is a local Python process, not a browser).
        // Reject any request whose Host header points at a non-local name, which
        // is how a malicious web page would try to reach 127.0.0.1 via a rebound
        // hostname. Requests with no Host header (e.g. raw curl) are allowed.
        if (!isNonBrowserRequest(session.getHeaders().get("origin"),
                                 session.getHeaders().get("sec-fetch-site"))) {
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("error", "Forbidden: this API is not reachable from a browser");
            return newFixedLengthResponse(Response.Status.FORBIDDEN, "application/json", GSON.toJson(error));
        }

        if (!isLocalHost(session)) {
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("error", "Forbidden: requests must target localhost");
            return newFixedLengthResponse(Response.Status.FORBIDDEN, "application/json", GSON.toJson(error));
        }

        if (Method.OPTIONS.equals(method)) {
            return newFixedLengthResponse(Response.Status.OK, "text/plain", "");
        }

        try {
            JsonObject requestBody = null;
            if (Method.POST.equals(method) || Method.PUT.equals(method)) {
                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                String body = files.get("postData");
                if (body != null && !body.isEmpty()) {
                    requestBody = JsonParser.parseString(body).getAsJsonObject();
                }
            }

            JsonObject result = routeRequest(uri, method, session.getParms(), requestBody);

            Response.Status status = result.has("success") && result.get("success").getAsBoolean()
                ? Response.Status.OK : Response.Status.BAD_REQUEST;

            return newFixedLengthResponse(status, "application/json", GSON.toJson(result));

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "API error", e);
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("error", e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json", GSON.toJson(error));
        }
    }

    /**
     * True when the request did not come from a browser.
     *
     * <p>The Host check below stops DNS rebinding, but not a page the user is merely visiting
     * calling {@code fetch("http://127.0.0.1:8080/...", {mode:"no-cors"})}. That request carries
     * a loopback Host and a CORS-safelisted content type, so it passes every other check, and
     * although CORS hides the reply the side effect has already happened. Destructive endpoints
     * and the export endpoints, which write to a caller-supplied path, are reachable that way.
     *
     * <p>Browsers set {@code Origin} on such a request and {@code Sec-Fetch-Site} on every
     * request, and page JavaScript can neither forge nor suppress them. Legitimate clients here
     * are local processes, which send neither, so refusing on either header costs nothing and
     * closes the hole. Kept package-private and static so it is unit-testable without a server.
     */
    static boolean isNonBrowserRequest(String origin, String secFetchSite) {
        boolean hasOrigin = origin != null && !origin.trim().isEmpty();
        boolean hasFetchSite = secFetchSite != null && !secFetchSite.trim().isEmpty();
        return !hasOrigin && !hasFetchSite;
    }

    /** True when the request's Host header is absent or resolves to the loopback interface. */
    private boolean isLocalHost(IHTTPSession session) {
        return isLoopbackHost(session.getHeaders().get("host"));
    }

    /**
     * True when {@code host} (a raw HTTP Host header value, possibly null or with a
     * port and/or IPv6 brackets) names the loopback interface. A null/empty header is
     * allowed (non-browser clients like the MCP server may omit it); any non-loopback
     * name is rejected, which is what blocks DNS-rebinding attacks from a web page.
     * Package-private and static so it can be unit-tested without a live server.
     */
    static boolean isLoopbackHost(String host) {
        if (host == null || host.isEmpty()) return true;
        String name = host;
        int colon = name.lastIndexOf(':');
        if (colon > -1 && name.indexOf(']') < colon) name = name.substring(0, colon);
        name = name.replace("[", "").replace("]", "").trim().toLowerCase();
        return name.equals("127.0.0.1") || name.equals("localhost") || name.equals("::1");
    }

    @SuppressWarnings("unchecked")
    private JsonObject routeRequest(String uri, Method method, Map<String, String> params, JsonObject body) {

        // ─── Health ──────────────────────────────────────────────────

        if ("/health".equals(uri) || "/".equals(uri)) {
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("service", "Gephi AI API");
            result.addProperty("version", moduleVersion());
            result.addProperty("status", "running");
            // "busy" here (persistently) means Gephi is wedged and needs a restart.
            result.addProperty("graph_lock", service.graphLockProbe());
            result.add("graph_lock_stats", service.graphLockStats());
            // No side effects here: /health is a pure liveness probe that clients may
            // poll at any frequency. The node click listener is installed once at
            // plugin startup by Installer, as soon as the visualization controller is
            // available, and again defensively by /selection (GephiControlService.
            // getSelection calls ensureClickListener itself).
            return result;
        }

        // ─── Human selection journal ─────────────────────────────────

        if ("/selection".equals(uri) && Method.GET.equals(method)) {
            // Default false, matching the Python tool: peeking must not consume.
            boolean clear = "true".equalsIgnoreCase(params.get("clear"));
            return service.getSelection(clear);
        }

        // ─── View / camera (teaching mode) ───────────────────────────

        if ("/view/focus".equals(uri) && Method.POST.equals(method)) {
            String mode = body != null && body.has("mode") ? body.get("mode").getAsString() : "graph";
            String id = body != null && body.has("id") ? body.get("id").getAsString() : null;
            String source = body != null && body.has("source") ? body.get("source").getAsString() : null;
            String target = body != null && body.has("target") ? body.get("target").getAsString() : null;
            Double x = body != null && body.has("x") ? body.get("x").getAsDouble() : null;
            Double y = body != null && body.has("y") ? body.get("y").getAsDouble() : null;
            Double w = body != null && body.has("w") ? body.get("w").getAsDouble() : null;
            Double h = body != null && body.has("h") ? body.get("h").getAsDouble() : null;
            Double zoom = body != null && body.has("zoom") ? body.get("zoom").getAsDouble() : null;
            java.util.List<String> select = null;
            if (body != null && body.has("select")) {
                select = new java.util.ArrayList<>();
                for (com.google.gson.JsonElement el : body.get("select").getAsJsonArray()) {
                    select.add(el.getAsString());
                }
            }
            return service.focusView(mode, id, source, target, x, y, w, h, zoom, select);
        }

        if ("/view/selection".equals(uri) && Method.POST.equals(method)) {
            String mode = body != null && body.has("mode") ? body.get("mode").getAsString() : "rectangle";
            return service.setSelectionMode(mode);
        }

        if ("/perspective".equals(uri) && Method.GET.equals(method)) {
            return service.getPerspective();
        }

        if ("/perspective/switch".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("name")) return errorResult("Missing 'name'");
            return service.switchPerspective(body.get("name").getAsString());
        }

        // ─── Project ─────────────────────────────────────────────────

        if ("/project/new".equals(uri) && Method.POST.equals(method)) {
            String name = body != null && body.has("name") ? body.get("name").getAsString() : "New Project";
            return service.createProject(name);
        }

        if ("/project/open".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file' parameter");
            return service.openProject(body.get("file").getAsString());
        }

        if ("/project/save".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file' parameter");
            return service.saveProject(body.get("file").getAsString());
        }

        if ("/project/info".equals(uri) && Method.GET.equals(method)) {
            return service.getProjectInfo();
        }

        // ─── Workspace ───────────────────────────────────────────────

        if ("/workspace/new".equals(uri) && Method.POST.equals(method)) {
            return service.newWorkspace();
        }

        if ("/workspace/list".equals(uri) && Method.GET.equals(method)) {
            return service.listWorkspaces();
        }

        if ("/workspace/switch".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("index")) return errorResult("Missing 'index'");
            return service.switchWorkspace(body.get("index").getAsInt());
        }

        if ("/workspace/delete".equals(uri) && Method.DELETE.equals(method)) {
            // The workspace index arrives as a query parameter (?index=N). Request
            // bodies are parsed for POST and PUT only, so a JSON body on this DELETE
            // was never readable; the query parameter is the supported form.
            int index = parseIntParam(params.get("index"), -1);
            if (index < 0) return errorResult("Missing 'index' query parameter");
            return service.deleteWorkspace(index);
        }

        if ("/workspace/duplicate".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("index")) return errorResult("Missing 'index'");
            return service.duplicateWorkspace(body.get("index").getAsInt());
        }

        if ("/workspace/rename".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("index") || !body.has("name")) return errorResult("Missing 'index' or 'name'");
            return service.renameWorkspace(body.get("index").getAsInt(), body.get("name").getAsString());
        }

        // ─── Nodes ───────────────────────────────────────────────────

        if ("/graph/node/add".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("id")) return errorResult("Missing 'id' parameter");
            String id = body.get("id").getAsString();
            String label = body.has("label") ? body.get("label").getAsString() : null;
            Map<String, Object> attrs = null;
            if (body.has("attributes") && body.get("attributes").isJsonObject()) {
                attrs = GSON.fromJson(body.get("attributes"), Map.class);
            }
            return service.addNode(id, label, attrs);
        }

        if ("/graph/nodes/add".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("nodes")) return errorResult("Missing 'nodes' array");
            List<Map<String, Object>> nodes = GSON.fromJson(body.get("nodes"), List.class);
            return service.addNodes(nodes);
        }

        if (uri.startsWith("/graph/node/") && uri.length() > "/graph/node/".length() && Method.DELETE.equals(method)) {
            String nodeId = uri.substring("/graph/node/".length());
            if (nodeId.isEmpty()) return errorResult("Missing node ID");
            return service.removeNode(nodeId);
        }

        if ("/graph/nodes/remove".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("ids")) return errorResult("Missing 'ids' array");
            List<String> ids = GSON.fromJson(body.get("ids"), List.class);
            return service.bulkRemoveNodes(ids);
        }

        if ("/graph/nodes".equals(uri) && Method.GET.equals(method)) {
            int limit = parseIntParam(params.get("limit"), 100);
            int offset = parseIntParam(params.get("offset"), 0);
            return service.queryNodes(null, null, limit, offset, visibleParam(params, false));
        }

        if (uri.startsWith("/graph/node/get/") && Method.GET.equals(method)) {
            String nodeId = uri.substring("/graph/node/get/".length());
            if (nodeId.isEmpty()) return errorResult("Missing node ID");
            return service.getNode(nodeId);
        }

        if ("/graph/node/label".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("id") || !body.has("label")) return errorResult("Missing 'id' or 'label'");
            return service.setNodeLabel(body.get("id").getAsString(), body.get("label").getAsString());
        }

        if ("/graph/node/position".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("id")) return errorResult("Missing 'id'");
            float x = body.has("x") ? body.get("x").getAsFloat() : 0;
            float y = body.has("y") ? body.get("y").getAsFloat() : 0;
            return service.setNodePosition(body.get("id").getAsString(), x, y);
        }

        if ("/graph/nodes/positions".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("positions")) return errorResult("Missing 'positions' array");
            List<Map<String, Object>> positions = GSON.fromJson(body.get("positions"), List.class);
            return service.batchSetPositions(positions);
        }

        // ─── Edges ───────────────────────────────────────────────────

        if ("/graph/edge/add".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("source") || !body.has("target"))
                return errorResult("Missing 'source' or 'target'");
            String source = body.get("source").getAsString();
            String target = body.get("target").getAsString();
            Double weight = body.has("weight") ? body.get("weight").getAsDouble() : 1.0;
            boolean directed = !body.has("directed") || body.get("directed").getAsBoolean();
            String edgeType = body.has("edge_type") ? body.get("edge_type").getAsString() : null;
            return service.addEdge(source, target, weight, directed, edgeType);
        }

        if ("/graph/edges/add".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("edges")) return errorResult("Missing 'edges' array");
            List<Map<String, Object>> edges = GSON.fromJson(body.get("edges"), List.class);
            return service.addEdges(edges);
        }

        if ("/graph/edge/remove".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("source") || !body.has("target"))
                return errorResult("Missing 'source' or 'target'");
            return service.removeEdge(body.get("source").getAsString(), body.get("target").getAsString());
        }

        if ("/graph/edge/weight".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("source") || !body.has("target") || !body.has("weight"))
                return errorResult("Missing 'source', 'target', or 'weight'");
            return service.setEdgeWeight(
                body.get("source").getAsString(),
                body.get("target").getAsString(),
                body.get("weight").getAsDouble()
            );
        }

        if ("/graph/edge/label".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("source") || !body.has("target") || !body.has("label"))
                return errorResult("Missing 'source', 'target', or 'label'");
            return service.setEdgeLabel(
                body.get("source").getAsString(),
                body.get("target").getAsString(),
                body.get("label").getAsString()
            );
        }

        if ("/graph/edges".equals(uri) && Method.GET.equals(method)) {
            int limit = parseIntParam(params.get("limit"), 100);
            int offset = parseIntParam(params.get("offset"), 0);
            return service.queryEdges(limit, offset, visibleParam(params, false));
        }

        // ─── Graph Stats & Type ──────────────────────────────────────

        if ("/graph/stats".equals(uri) && Method.GET.equals(method)) {
            return service.getGraphStats(visibleParam(params, false));
        }

        if ("/graph/type".equals(uri) && Method.GET.equals(method)) {
            return service.getGraphType();
        }

        // ─── Attributes / Columns ────────────────────────────────────

        if ("/graph/columns".equals(uri) && Method.GET.equals(method)) {
            String target = params.getOrDefault("target", "node");
            return service.getColumns(target);
        }

        if ("/graph/columns/add".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("name") || !body.has("type"))
                return errorResult("Missing 'name' or 'type'");
            String target = body.has("target") ? body.get("target").getAsString() : "node";
            return service.addColumn(body.get("name").getAsString(), body.get("type").getAsString(), target);
        }

        if ("/graph/node/attributes".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("id") || !body.has("attributes"))
                return errorResult("Missing 'id' or 'attributes'");
            Map<String, Object> attrs = GSON.fromJson(body.get("attributes"), Map.class);
            return service.setNodeAttributes(body.get("id").getAsString(), attrs);
        }

        if ("/graph/nodes/attributes".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("updates")) return errorResult("Missing 'updates' array");
            List<Map<String, Object>> updates = GSON.fromJson(body.get("updates"), List.class);
            return service.batchSetNodeAttributes(updates);
        }

        if ("/graph/edge/attributes".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("source") || !body.has("target") || !body.has("attributes"))
                return errorResult("Missing 'source', 'target', or 'attributes'");
            Map<String, Object> attrs = GSON.fromJson(body.get("attributes"), Map.class);
            return service.setEdgeAttributes(
                body.get("source").getAsString(),
                body.get("target").getAsString(),
                attrs
            );
        }

        // ─── Appearance ──────────────────────────────────────────────

        if ("/appearance/node/color".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("id")) return errorResult("Missing 'id'");
            int r = body.has("r") ? body.get("r").getAsInt() : 0;
            int g = body.has("g") ? body.get("g").getAsInt() : 0;
            int b = body.has("b") ? body.get("b").getAsInt() : 0;
            int a = body.has("a") ? body.get("a").getAsInt() : 255;
            return service.setNodeColor(body.get("id").getAsString(), r, g, b, a);
        }

        if ("/appearance/node/size".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("id") || !body.has("size")) return errorResult("Missing 'id' or 'size'");
            return service.setNodeSize(body.get("id").getAsString(), body.get("size").getAsFloat());
        }

        if ("/appearance/edge/color".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("source") || !body.has("target"))
                return errorResult("Missing 'source' or 'target'");
            int r = body.has("r") ? body.get("r").getAsInt() : 0;
            int g = body.has("g") ? body.get("g").getAsInt() : 0;
            int b = body.has("b") ? body.get("b").getAsInt() : 0;
            int a = body.has("a") ? body.get("a").getAsInt() : 255;
            return service.setEdgeColor(body.get("source").getAsString(), body.get("target").getAsString(), r, g, b, a);
        }

        if ("/appearance/nodes/color".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("nodes")) return errorResult("Missing 'nodes' array");
            List<Map<String, Object>> nodes = GSON.fromJson(body.get("nodes"), List.class);
            return service.batchSetNodeColors(nodes);
        }

        if ("/appearance/reset".equals(uri) && Method.POST.equals(method)) {
            int r = body != null && body.has("r") ? body.get("r").getAsInt() : 153;
            int g = body != null && body.has("g") ? body.get("g").getAsInt() : 153;
            int b = body != null && body.has("b") ? body.get("b").getAsInt() : 153;
            float size = body != null && body.has("size") ? body.get("size").getAsFloat() : 10f;
            return service.resetAppearance(r, g, b, size);
        }

        if ("/appearance/partition/color".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("column")) return errorResult("Missing 'column'");
            String column = body.get("column").getAsString();
            Map<String, int[]> colorMap = null;
            if (body.has("colors") && body.get("colors").isJsonObject()) {
                colorMap = new HashMap<>();
                JsonObject colors = body.getAsJsonObject("colors");
                for (String key : colors.keySet()) {
                    List<Number> rgb = GSON.fromJson(colors.get(key), List.class);
                    colorMap.put(key, new int[]{rgb.get(0).intValue(), rgb.get(1).intValue(), rgb.get(2).intValue()});
                }
            }
            return service.colorByPartition(column, colorMap);
        }

        if ("/appearance/edge/partition-color".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("column")) return errorResult("Missing 'column'");
            String column = body.get("column").getAsString();
            Map<String, int[]> colorMap = null;
            if (body.has("colors") && body.get("colors").isJsonObject()) {
                colorMap = new HashMap<>();
                JsonObject colors = body.getAsJsonObject("colors");
                for (String key : colors.keySet()) {
                    List<Number> rgb = GSON.fromJson(colors.get(key), List.class);
                    colorMap.put(key, new int[]{rgb.get(0).intValue(), rgb.get(1).intValue(), rgb.get(2).intValue()});
                }
            }
            return service.colorEdgesByPartition(column, colorMap);
        }

        if ("/appearance/ranking/color".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("column")) return errorResult("Missing 'column'");
            String column = body.get("column").getAsString();
            int rMin = body.has("r_min") ? body.get("r_min").getAsInt() : 255;
            int gMin = body.has("g_min") ? body.get("g_min").getAsInt() : 255;
            int bMin = body.has("b_min") ? body.get("b_min").getAsInt() : 200;
            int rMax = body.has("r_max") ? body.get("r_max").getAsInt() : 255;
            int gMax = body.has("g_max") ? body.get("g_max").getAsInt() : 0;
            int bMax = body.has("b_max") ? body.get("b_max").getAsInt() : 0;
            return service.colorByRanking(column, rMin, gMin, bMin, rMax, gMax, bMax);
        }

        if ("/appearance/ranking/size".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("column")) return errorResult("Missing 'column'");
            float minSize = body.has("min_size") ? body.get("min_size").getAsFloat() : 5f;
            float maxSize = body.has("max_size") ? body.get("max_size").getAsFloat() : 50f;
            return service.sizeByRanking(body.get("column").getAsString(), minSize, maxSize);
        }

        // ─── Layout ──────────────────────────────────────────────────

        if ("/layout/run".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("algorithm")) return errorResult("Missing 'algorithm'");
            String algo = body.get("algorithm").getAsString();
            int iterations = body.has("iterations") ? body.get("iterations").getAsInt() : 1000;
            // Inline properties: configure and run in one step.
            if (body.has("properties") && body.get("properties").isJsonObject()) {
                Map<String, Object> properties = GSON.fromJson(body.get("properties"), Map.class);
                return service.runLayout(algo, iterations, properties);
            }
            return service.runLayout(algo, iterations);
        }

        if ("/layout/stop".equals(uri) && Method.POST.equals(method)) {
            return service.stopLayout();
        }

        if ("/layout/status".equals(uri) && Method.GET.equals(method)) {
            return service.getLayoutStatus();
        }

        if ("/layout/available".equals(uri) && Method.GET.equals(method)) {
            return service.getAvailableLayouts();
        }

        if ("/layout/properties".equals(uri) && Method.GET.equals(method)) {
            String algo = params.get("algorithm");
            if (algo == null || algo.isEmpty()) return errorResult("Missing 'algorithm' parameter");
            return service.getLayoutProperties(algo);
        }

        if ("/layout/properties".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("algorithm") || !body.has("properties"))
                return errorResult("Missing 'algorithm' or 'properties'");
            String algo = body.get("algorithm").getAsString();
            Map<String, Object> properties = GSON.fromJson(body.get("properties"), Map.class);
            int iterations = body.has("iterations") ? body.get("iterations").getAsInt() : 1000;
            return service.setLayoutProperties(algo, properties, iterations);
        }

        // ─── Statistics ──────────────────────────────────────────────

        if ("/statistics/modularity".equals(uri) && Method.POST.equals(method)) {
            double res = body != null && body.has("resolution") ? body.get("resolution").getAsDouble() : 1.0;
            return service.computeModularity(res);
        }

        if ("/statistics/available".equals(uri) && Method.GET.equals(method)) {
            return service.listStatistics();
        }

        if ("/statistics/run".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("name")) return errorResult("Missing 'name'");
            Map<String, Object> statParams = null;
            if (body.has("params") && body.get("params").isJsonObject()) {
                statParams = GSON.fromJson(body.get("params"), Map.class);
            }
            return service.runStatisticByName(body.get("name").getAsString(), statParams);
        }

        if ("/statistics/degree".equals(uri) && Method.POST.equals(method)) {
            return service.computeDegree();
        }

        if ("/statistics/betweenness".equals(uri) && Method.POST.equals(method)) {
            return service.computeBetweenness();
        }

        if ("/statistics/pagerank".equals(uri) && Method.POST.equals(method)) {
            return service.computePageRank();
        }

        if ("/statistics/connected-components".equals(uri) && Method.POST.equals(method)) {
            return service.computeConnectedComponents();
        }

        if ("/statistics/clustering-coefficient".equals(uri) && Method.POST.equals(method)) {
            return service.computeClusteringCoefficient();
        }

        if ("/statistics/avg-path-length".equals(uri) && Method.POST.equals(method)) {
            return service.computeAvgPathLength();
        }

        if ("/statistics/hits".equals(uri) && Method.POST.equals(method)) {
            return service.computeHITS();
        }

        if ("/statistics/eigenvector".equals(uri) && Method.POST.equals(method)) {
            return service.computeEigenvectorCentrality();
        }

        // ─── Graph Operations ────────────────────────────────────────

        if ("/graph/clear".equals(uri) && Method.POST.equals(method)) {
            return service.clearGraph();
        }

        // ─── Filters ─────────────────────────────────────────────────

        if ("/filter/degree".equals(uri) && Method.POST.equals(method)) {
            int min = body != null && body.has("min") ? body.get("min").getAsInt() : 0;
            int max = body != null && body.has("max") ? body.get("max").getAsInt() : 0;
            boolean dryRun = body != null && body.has("dry_run") && body.get("dry_run").getAsBoolean();
            return service.filterByDegreeRange(min, max, dryRun);
        }

        if ("/filter/edge-weight".equals(uri) && Method.POST.equals(method)) {
            double min = body != null && body.has("min") ? body.get("min").getAsDouble() : 0;
            double max = body != null && body.has("max") ? body.get("max").getAsDouble() : 0;
            boolean dryRun = body != null && body.has("dry_run") && body.get("dry_run").getAsBoolean();
            return service.filterByEdgeWeight(min, max, dryRun);
        }

        if ("/filter/remove-isolates".equals(uri) && Method.POST.equals(method)) {
            return service.removeIsolates();
        }

        if ("/filter/ego-network".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("node_id")) return errorResult("Missing 'node_id'");
            String nodeId = body.get("node_id").getAsString();
            int depth = body.has("depth") ? body.get("depth").getAsInt() : 1;
            return service.extractEgoNetwork(nodeId, depth);
        }

        if ("/filter/giant-component".equals(uri) && Method.POST.equals(method)) {
            return service.extractGiantComponent();
        }

        if ("/filter/reset".equals(uri) && Method.POST.equals(method)) {
            return service.resetFilters();
        }

        if ("/filter/list".equals(uri) && Method.GET.equals(method)) {
            return service.listFilters();
        }

        if ("/filter/apply".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("name")) return errorResult("Missing 'name'");
            String fname = body.get("name").getAsString();
            Map<String, Object> filterParams = body.has("params") ? GSON.fromJson(body.get("params"), Map.class) : null;
            String action = body.has("action") ? body.get("action").getAsString() : "select";
            String column = body.has("column") ? body.get("column").getAsString() : null;
            return service.applyFilter(fname, filterParams, action, column);
        }

        // ─── Data Laboratory ─────────────────────────────────────────

        if ("/datalab/frequencies".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("column")) return errorResult("Missing 'column'");
            String target = body.has("target") ? body.get("target").getAsString() : "node";
            return service.columnValueFrequencies(target, body.get("column").getAsString());
        }

        if ("/datalab/duplicates".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("column")) return errorResult("Missing 'column'");
            String target = body.has("target") ? body.get("target").getAsString() : "node";
            boolean cs = body.has("case_sensitive") && body.get("case_sensitive").getAsBoolean();
            return service.detectDuplicates(target, body.get("column").getAsString(), cs);
        }

        if ("/datalab/merge-nodes".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("ids")) return errorResult("Missing 'ids'");
            java.util.List<String> ids = GSON.fromJson(body.get("ids"), java.util.List.class);
            String into = body.has("into") ? body.get("into").getAsString() : null;
            return service.mergeNodes(ids, into);
        }

        if ("/datalab/regex-column".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("column") || !body.has("new_column") || !body.has("regex"))
                return errorResult("Missing 'column', 'new_column', or 'regex'");
            String target = body.has("target") ? body.get("target").getAsString() : "node";
            return service.createRegexColumn(target, body.get("column").getAsString(),
                body.get("new_column").getAsString(), body.get("regex").getAsString());
        }

        // ─── Timeline (read-only) ────────────────────────────────────
        // NOTE: there is deliberately NO write endpoint here. Driving Gephi's
        // timeline from outside (setInterval/setEnabled, or a time-derived
        // setVisibleView) wedges the EDT, and Gephi's own shutdown runs on the
        // EDT — so a wedged timeline op makes the app impossible to quit
        // normally (Force Quit only). getTimeline is a pure read and is safe.

        if ("/timeline".equals(uri) && Method.GET.equals(method)) {
            return service.getTimeline();
        }

        // ─── Edge Appearance ────────────────────────────────────────

        if ("/appearance/edge/thickness-by-weight".equals(uri) && Method.POST.equals(method)) {
            float minThickness = body != null && body.has("min_thickness") ? body.get("min_thickness").getAsFloat() : 1f;
            float maxThickness = body != null && body.has("max_thickness") ? body.get("max_thickness").getAsFloat() : 5f;
            return service.setEdgeThicknessByWeight(minThickness, maxThickness);
        }

        // ─── Preview ─────────────────────────────────────────────────

        if ("/preview/settings".equals(uri) && Method.GET.equals(method)) {
            return service.getPreviewSettings();
        }

        if ("/preview/settings".equals(uri) && Method.POST.equals(method)) {
            if (body == null) return errorResult("Missing request body");
            // Body shape is flat {property: value}; unwrap the common client
            // mistake of nesting everything under a "settings" key so it does
            // not get stored as a junk preview property named "settings".
            JsonObject effective = body;
            if (body.size() == 1 && body.has("settings") && body.get("settings").isJsonObject()) {
                effective = body.getAsJsonObject("settings");
            }
            Map<String, Object> settings = GSON.fromJson(effective, Map.class);
            return service.setPreviewSettings(settings);
        }

        // ─── Export ──────────────────────────────────────────────────

        if ("/export/gexf".equals(uri) && Method.POST.equals(method)) {
            // no "file" (or inline:true) -> return the GEXF as a string in "content"
            if (body == null || !body.has("file")
                    || (body.has("inline") && body.get("inline").getAsBoolean())) {
                return service.exportGexfContent(visibleBody(body, true));
            }
            return service.exportGexf(body.get("file").getAsString(), visibleBody(body, true));
        }

        if ("/export/format".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file") || !body.has("format"))
                return errorResult("Missing 'file' or 'format'");
            return service.exportByFormat(body.get("file").getAsString(), body.get("format").getAsString(), visibleBody(body, true));
        }

        if ("/export/png".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file'");
            String file = body.get("file").getAsString();
            int w = body.has("width") ? body.get("width").getAsInt() : 1920;
            int h = body.has("height") ? body.get("height").getAsInt() : 1080;
            return service.exportPng(file, w, h);
        }

        if ("/export/screenshot".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file'");
            String file = body.get("file").getAsString();
            int scale = body.has("scale") ? body.get("scale").getAsInt() : 2;
            boolean transparent = body.has("transparent_background")
                && body.get("transparent_background").getAsBoolean();
            return service.exportScreenshot(file, scale, transparent);
        }

        if ("/export/pdf".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file'");
            String file = body.get("file").getAsString();
            int w = body.has("width") ? body.get("width").getAsInt() : 0;
            int h = body.has("height") ? body.get("height").getAsInt() : 0;
            return service.exportPdf(file, w, h);
        }

        if ("/export/svg".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file'");
            return service.exportSvg(body.get("file").getAsString());
        }

        if ("/export/graphml".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file'");
            return service.exportGraphml(body.get("file").getAsString(), visibleBody(body, true));
        }

        if ("/export/csv".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file'");
            String file = body.get("file").getAsString();
            String separator = body.has("separator") ? body.get("separator").getAsString() : ",";
            String target = body.has("target") ? body.get("target").getAsString() : "nodes";
            return service.exportCsv(file, separator, target);
        }

        // ─── Import ──────────────────────────────────────────────────

        if ("/import/gexf".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file'");
            return service.importFile(body.get("file").getAsString(), floatOrNull(body, "max_node_size"));
        }

        if ("/import/graphml".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file'");
            return service.importFile(body.get("file").getAsString(), floatOrNull(body, "max_node_size"));
        }

        if ("/import/csv".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file'");
            return service.importFile(body.get("file").getAsString(), floatOrNull(body, "max_node_size"));
        }

        if ("/import/file".equals(uri) && Method.POST.equals(method)) {
            if (body == null || !body.has("file")) return errorResult("Missing 'file'");
            return service.importFile(body.get("file").getAsString(), floatOrNull(body, "max_node_size"));
        }

        return errorResult("Unknown endpoint: " + method + " " + uri);
    }

    /**
     * Reads an optional {@code visible} query parameter. When absent the endpoint keeps the
     * view it has always used, so wiring this changes no existing caller. Every affected
     * response also states which view it used and whether a filter is active, so the two
     * can no longer disagree silently.
     */
    static boolean visibleParam(Map<String, String> params, boolean dflt) {
        String v = params == null ? null : params.get("visible");
        if (v == null) return dflt;
        String t = v.trim();
        if (t.isEmpty()) return dflt;
        if ("true".equalsIgnoreCase(t) || "1".equals(t)) return true;
        if ("false".equalsIgnoreCase(t) || "0".equals(t)) return false;
        // Anything else is not a decision. Falling back to the endpoint's default beats
        // reading unrecognised text as "false", which would silently switch an export
        // from the filtered graph to the whole graph on a typo.
        return dflt;
    }

    /** The {@code visible} flag from a JSON body, defaulting to the endpoint's historical view. */
    static boolean visibleBody(JsonObject body, boolean dflt) {
        if (body == null || !body.has("visible") || body.get("visible").isJsonNull()) return dflt;
        com.google.gson.JsonElement e = body.get("visible");
        // Only a real JSON boolean decides. Gson would read the string "banana" as false,
        // which is the same silent-switch trap the query parameter avoids above.
        if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean()) {
            return e.getAsBoolean();
        }
        return dflt;
    }

    /**
     * The module's own version, read from the manifest the build generates from the POM.
     * A literal here drifts from the POM the first time someone bumps one and not the other,
     * and the drift is invisible until a user reports the wrong version.
     */
    static String moduleVersion() {
        try {
            org.openide.modules.ModuleInfo info =
                org.openide.modules.Modules.getDefault().ownerOf(GephiAPIServer.class);
            if (info != null && info.getSpecificationVersion() != null) {
                return info.getSpecificationVersion().toString();
            }
        } catch (Throwable t) {
            // Not running inside the platform (unit tests, or a future API change).
        }
        String fromPackage = GephiAPIServer.class.getPackage() == null
            ? null : GephiAPIServer.class.getPackage().getImplementationVersion();
        return fromPackage != null ? fromPackage : "unknown";
    }

    /** An optional float from a JSON body, or null when absent or not a number. */
    static Float floatOrNull(JsonObject body, String key) {
        if (body == null || !body.has(key) || body.get(key).isJsonNull()) return null;
        try {
            return body.get(key).getAsFloat();
        } catch (RuntimeException e) {
            return null;
        }
    }

    private int parseIntParam(String value, int defaultValue) {
        if (value == null) return defaultValue;
        try { return Integer.parseInt(value); }
        catch (NumberFormatException e) { return defaultValue; }
    }

    private JsonObject errorResult(String message) {
        JsonObject result = new JsonObject();
        result.addProperty("success", false);
        result.addProperty("error", message);
        return result;
    }

    public void startServer() throws IOException {
        // daemon=true: the listener thread must never keep the JVM alive on Gephi
        // shutdown. With a non-daemon listener, a missed/slow stop() would hang close.
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
        LOGGER.info("Gephi AI API started on http://127.0.0.1:" + getListeningPort());
    }

    public void stopServer() {
        stop();
        service.shutdown();
        LOGGER.info("Gephi AI API stopped");
    }
}
