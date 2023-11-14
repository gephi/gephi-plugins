package com.carlschroedl.gephi.plugin.minimumspanningtree;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public class GraphModelLoader {
    private final ProjectController projectController;
    
    public GraphModelLoader(ProjectController projectController){
        this.projectController = projectController;
    }
    
    public GraphModel fromFile(String path) {
        File file;
        try {
            URL url = this.getClass().getResource(path);
            URI uri = url.toURI();
            file = new File(uri);
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
        return this.fromFile(file);
    }
    /**
     * Loads the specified file and returns the associated Graph. Side effect: 
     * This method creates a new workspace in the current project and opens it.
     * The user of this method is responsible for any necessary cleanup of the 
     * workspace or project after loading.
     * @param file The file containing a graph
     * @return the GraphModel
     */
    public GraphModel fromFile(File file) {
        GraphModel graphModelFromFile = null;
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
        projectController.openWorkspace(workspace);
        Lookup lookup = workspace.getLookup();
        ImportController ic = Lookup.getDefault().lookup(ImportController.class);
        Container container;
        try {
            container = ic.importFile(file);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        ic.process(container, new DefaultProcessor(), workspace);
        graphModelFromFile = lookup.lookup(GraphModel.class);
        return graphModelFromFile;
    }
    
    /**
     * Creates and returns a new empty GraphModel. Side effect: This method 
     * creates a new workspace in the current project and opens it. The user of 
     * this method is responsible for any necessary cleanup of the workspace or 
     * project after loading.
     * @return a new empty GraphModel
     */
    public GraphModel fromScratch(){
        Workspace workspace = projectController.newWorkspace(projectController.getCurrentProject());
        projectController.openWorkspace(workspace);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        return graphModel;
    }
    
}
