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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openide.util.Lookup;

/** A time slice copies what is present in a window into its own workspace. */
class TimeSliceTest {

    private static final String DYNAMIC = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<gexf xmlns=\"http://gexf.net/1.3\" version=\"1.3\"><graph defaultedgetype=\"undirected\""
        + " mode=\"dynamic\" timeformat=\"double\"><nodes>"
        + "<node id=\"a\" start=\"1990\" end=\"2000\"/>"
        + "<node id=\"b\" start=\"1990\" end=\"1995\"/>"
        + "<node id=\"c\" start=\"1998\" end=\"2000\"/>"
        + "</nodes><edges>"
        + "<edge id=\"ab\" source=\"a\" target=\"b\" start=\"1990\" end=\"1992\"/>"
        + "<edge id=\"ac\" source=\"a\" target=\"c\" start=\"1998\" end=\"2000\"/>"
        + "</edges></graph></gexf>";

    @Test
    void presenceOverlapsTheWindow() {
        TimeRepresentation rep = TimeRepresentation.INTERVAL;
        org.gephi.graph.api.GraphModel gm = org.gephi.graph.api.GraphModel.Factory.newInstance(
            org.gephi.graph.api.Configuration.builder().timeRepresentation(rep).build());
        Node n = gm.factory().newNode("x");
        n.addInterval(new Interval(1990, 1995));
        gm.getDirectedGraph().addNode(n);

        assertTrue(GephiControlService.presentIn(n, 1995, 1999, rep));
        assertFalse(GephiControlService.presentIn(n, 1996, 1999, rep));
        assertTrue(GephiControlService.presentIn(gm.factory().newNode("timeless"), 0, 1, rep),
            "no time data means always present");
    }

    @Test
    void sliceOpensInItsOwnWorkspaceAndLeavesTheNetworkAlone(@TempDir Path dir) throws Exception {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();
        Path f = dir.resolve("dyn.gexf");
        Files.writeString(f, DYNAMIC, StandardCharsets.UTF_8);
        GephiControlService.getInstance().importFile(f.toString(), null);
        Workspace source = pc.getCurrentWorkspace();
        org.gephi.graph.api.GraphModel sourceModel =
            Lookup.getDefault().lookup(GraphController.class).getGraphModel(source);
        sourceModel.getGraph().getNode("a").setX(123f);
        sourceModel.getGraph().getNode("a").setY(-45f);

        JsonObject r = GephiControlService.getInstance().timeSlice(1991, 1996);

        assertTrue(r.get("success").getAsBoolean(), r.toString());
        assertNotSame(source, pc.getCurrentWorkspace());
        org.gephi.graph.api.Graph sliced = Lookup.getDefault().lookup(GraphController.class)
            .getGraphModel(pc.getCurrentWorkspace()).getGraph();
        assertEquals(2, sliced.getNodeCount(), "a and b are present, c is not");
        assertEquals(1, sliced.getEdgeCount(), "only a-b is present");
        assertEquals(123f, sliced.getNode("a").x(), "a slice keeps the network's positions");
        assertEquals(-45f, sliced.getNode("a").y());
        assertEquals(3, Lookup.getDefault().lookup(GraphController.class).getGraphModel(source)
            .getGraph().getNodeCount(), "the network itself is untouched");
    }

    @Test
    void networkWithoutTimeDataIsRefused() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.closeCurrentProject();
        pc.newProject();

        JsonObject r = GephiControlService.getInstance().timeSlice(0, 1);

        assertFalse(r.get("success").getAsBoolean());
        assertTrue(r.get("error").getAsString().contains("gephi_set_time_from_columns"), r.toString());
    }
}
