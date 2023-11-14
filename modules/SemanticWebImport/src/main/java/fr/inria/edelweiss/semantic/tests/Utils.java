/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.tests;

import fr.inria.edelweiss.semantic.SemanticWebImportMainWindowTopComponent;
import fr.inria.edelweiss.sparql.SparqlRequester;
import java.io.IOException;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.Project;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class Utils {

    private SemanticWebImportMainWindowTopComponent topComponent;
    private static Workspace workspace;

    public void initGephi() {
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        projectController.newProject();
        workspace = projectController.getCurrentWorkspace();
    }

    /**
     *
     * @param configurationName Name of a configuration, e.g. "Humans".
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Workspace[] whenUsingConfiguration(final String configurationName) throws IOException, InterruptedException {
        topComponent = new SemanticWebImportMainWindowTopComponent();
        topComponent.setConfigurationAction(configurationName);
        Workspace workspaces[] = whenCreatingGraphs(topComponent);
        return workspaces;
    }

    public Workspace[] whenCreatingGraphs(SemanticWebImportMainWindowTopComponent topComponent) throws InterruptedException {
        topComponent.startCreateGraphs();
        topComponent.waitCreateGraphs();

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        Project project = projectController.getCurrentProject();
        WorkspaceProvider workspaceprovider = project.getLookup().lookup(WorkspaceProvider.class);
        return workspaceprovider.getWorkspaces();
    }

    public SparqlRequester getSparqlRequester() {
        return (SparqlRequester)topComponent;
    }

    public SparqlRequester getDriver() {
        return topComponent.getDriver();
    }
}
