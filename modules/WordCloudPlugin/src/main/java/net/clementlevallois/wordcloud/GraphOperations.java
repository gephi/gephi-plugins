/*
 * author: Clement Levallois
 */
package net.clementlevallois.wordcloud;

import java.util.List;
import javax.swing.DefaultListModel;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

public class GraphOperations {

    public static GraphModel graphInitFromCurrentlyOpenedProject() {
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
