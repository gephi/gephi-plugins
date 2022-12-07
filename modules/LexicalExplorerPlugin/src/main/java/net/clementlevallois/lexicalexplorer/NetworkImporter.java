/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.clementlevallois.lexicalexplorer;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerUnloader;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.plugin.file.ImporterGEXF;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

/**
 *
 * @author LEVALLOIS
 */
public class NetworkImporter {

    public static void main(String args[]) throws IOException {
        new NetworkImporter().importFromFile();
    }

    public void importFromFile() throws IOException {

        Path exampleGexf = Path.of("G:\\Mon Drive\\Twitch stream\\gephi plugin development\\gephi-plugins\\modules\\LexicalExplorerPlugin\\miserables_extended.gexf");

        String gexfFileAsString = Files.readString(exampleGexf, StandardCharsets.UTF_8);

        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);

        //Import file
        Container container;
        FileImporter fi = new ImporterGEXF();
        //Append imported data to GraphAPI
        container = importController.importFile(new StringReader(gexfFileAsString), fi);
        container.closeLoader();

        DefaultProcessor processor = new DefaultProcessor();
        processor.setWorkspace(pc.getCurrentWorkspace());
        processor.setContainers(new ContainerUnloader[]{container.getUnloader()});
        processor.process();
        GraphModel gm = graphController.getGraphModel();

        System.out.println("number of edges:" + gm.getGraph().getEdgeCount());
        System.out.println("number of nodes:" + gm.getGraph().getNodeCount());

    }

}
