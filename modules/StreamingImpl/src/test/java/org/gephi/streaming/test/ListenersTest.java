/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */

package org.gephi.streaming.test;

import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.TableObserver;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.Graph2EventListener;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.GraphUpdaterEventHandler;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEventBuilder;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author panisson
 */
public class ListenersTest {

    @Test
    public void runTest() {
        // Get active graph instance - Project and Graph API
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Project project = projectController.getCurrentProject();
        if (project==null)
            projectController.newProject();
        Workspace workspace = projectController.getCurrentWorkspace();
        if (workspace==null)
            workspace = projectController.newWorkspace(projectController.getCurrentProject());
//        projectController.openWorkspace(workspace);

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();
        Graph graph = graphModel.getGraph();
        graph.clear();

        GraphEventHandler printerHandler = new GraphEventHandler() {

            @Override
            public void handleGraphEvent(org.gephi.streaming.api.event.GraphEvent event) {
                System.out.println(event);
            }
        };

        Graph2EventListener listener = new Graph2EventListener(graph, printerHandler);
        
        GraphObserver graphObserver = graphModel.createGraphObserver(graph, true);

        GraphUpdaterEventHandler graphUpdaterHandler = new GraphUpdaterEventHandler(graph);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(this);

        graphUpdaterHandler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "A", null));
        graphUpdaterHandler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "B", null));
        graphUpdaterHandler.handleGraphEvent(eventBuilder.edgeAddedEvent("AB", "A", "B", false, null));
        
        assertTrue(graphObserver.hasGraphChanged());
        listener.graphChanged(graphObserver.getDiff());
        
        TableObserver nodeObserver = graphModel.getNodeTable().createTableObserver(true);
        Map<String,Object> attributes = new HashMap<String,Object>();
        attributes.put("bbb", 2.0);
        graphUpdaterHandler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, "A", attributes));
        
        assertTrue(nodeObserver.hasTableChanged());
        listener.attributesChanged(nodeObserver.getDiff());
    }

}
