/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package upf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Lookup;
import org.gephi.project.api.Workspace;
/**
 *
 * @author puig
 */
@ActionID(category = "File",
id = "org.gephi.desktop.filters.TestAction")
@ActionRegistration(displayName = "#CTL_TestAction")
@ActionReferences({
    @ActionReference(path = "Menu/Plugins", position = 3333)
})
@Messages("CTL_TestAction=Testing...")
public final class MenuButtonController implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        //Do something, display a message
        NotifyDescriptor d = new NotifyDescriptor.Message("Reading nodes..", NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);

        //Do something - for instance display a dialog
        //Dialogs API documentation: http://bits.netbeans.org/dev/javadoc/org-openide-dialogs/index.html?overview-summary.html
        DialogDescriptor dd = new DialogDescriptor(new JPanel(), "Init", false, null);
        DialogDisplayer.getDefault().notify(dd);
        
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace = pc.getCurrentWorkspace();
        GraphModel gmodel = workspace.getLookup().lookup(GraphModel.class);
        
        Graph graph = gmodel.getGraph();
        Node[] cNodes = graph.getNodes().toArray();
        for (Node node : cNodes){
            System.out.println(node.getLabel());
           // node.getColor()
        }
    }
}