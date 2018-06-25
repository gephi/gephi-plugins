/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package upf;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
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
    @ActionReference(path = "Menu/Tools", position = 3333)
})
@Messages("CTL_TestAction=Generate groups by partition")
public final class MenuButtonController implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        String[] options = new String[] {"Create", "Overwrite", "Cancel"}; // {0, 1, 2}
        int response = JOptionPane.showOptionDialog(null, "De you want to create a new workspace or overwrite the existing one?", "Select",
            JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
            null, options, options[0]);
        System.out.println("response: " + response);
        
        if (response != 2) {
            PartitionController partition = new PartitionController();
            
            boolean createNewWorkspace = false;
            if (response == 0) { createNewWorkspace = true; }
            
            partition.generatePartition(createNewWorkspace);
        }
        
        // Where response == 0 for Yes, 1 for No, 2 for Maybe and -1 or 3 for Escape/Cancel.
        //Do something, display a message
        /*NotifyDescriptor d = new NotifyDescriptor.Confirmation("DEBUG MESSAGE TO START\nHere will be parameter to select..", "Generate groups by partition",NotifyDescriptor.YES_NO_OPTION);
        
        if(DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION)
        {
            System.out.println("Said yes..");
            //Should be fed some parameters..
            PartitionController partition = new PartitionController();
            partition.generatePartition();
        } else System.out.println("Said no..");*/

        //Do something - for instance display a dialog
        //Dialogs API documentation: http://bits.netbeans.org/dev/javadoc/org-openide-dialogs/index.html?overview-summary.html
       /* DialogDescriptor dd = new DialogDescriptor(new JPanel(), "Init", false, null);
        DialogDisplayer.getDefault().notify(dd);*/
        /*
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace workspace = pc.getCurrentWorkspace();
        GraphModel gmodel = workspace.getLookup().lookup(GraphModel.class);
        
        Graph graph = gmodel.getGraph();
        Node[] cNodes = graph.getNodes().toArray();
        for (Node node : cNodes){
            //System.out.println(node.getLabel());
           // node.getColor()
        }*/
    }
}