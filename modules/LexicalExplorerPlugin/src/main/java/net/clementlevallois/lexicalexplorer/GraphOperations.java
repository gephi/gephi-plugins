/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.clementlevallois.lexicalexplorer;

import java.util.List;
import javax.swing.DefaultListModel;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public class GraphOperations {

    public static GraphModel graphInitFromCurrentlyOpendProject() {
        //Init a project by grasping the one currently opened- and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentProject() == null) {
            return null;
        }
        Workspace workspace = pc.getCurrentWorkspace();
        GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getGraphModel(workspace);
        return gm;
    }

    public static DefaultListModel<String> returnTextualNodeAttributesAsListOfNames(GraphModel gm) {
        List<Column> listOfNodeAttributes = gm.getNodeTable().toList();
        DefaultListModel<String> listModel = new DefaultListModel();
        for (Column column : listOfNodeAttributes) {
            if (column.getTypeClass() == String.class) {
                listModel.addElement(column.getId());
            }
        }

        return listModel;
    }

}
