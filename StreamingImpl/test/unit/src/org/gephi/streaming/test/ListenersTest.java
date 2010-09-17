/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.streaming.test;

import org.gephi.data.attributes.api.AttributeController;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.streaming.api.Graph2EventListener;
import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.GraphUpdaterEventHandler;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEventBuilder;
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
        GraphModel graphModel = graphController.getModel();
        Graph graph = graphModel.getHierarchicalMixedGraph();

        GraphEventHandler printerHandler = new GraphEventHandler() {

            @Override
            public void handleGraphEvent(org.gephi.streaming.api.event.GraphEvent event) {
                System.out.println(event);
            }
        };

        Graph2EventListener listener = new Graph2EventListener(graph, printerHandler);
        graph.getGraphModel().addGraphListener(listener);
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ac.getModel().addAttributeListener(listener);

        GraphUpdaterEventHandler graphUpdaterHandler = new GraphUpdaterEventHandler(graph);
        GraphEventBuilder eventBuilder = new GraphEventBuilder(this);

        graphUpdaterHandler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "A", null));
        graphUpdaterHandler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, "B", null));
        graphUpdaterHandler.handleGraphEvent(eventBuilder.edgeAddedEvent("AB", "A", "B", false, null));

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
