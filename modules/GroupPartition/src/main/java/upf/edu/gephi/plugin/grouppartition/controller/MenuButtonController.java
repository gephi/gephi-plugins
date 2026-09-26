package upf.edu.gephi.plugin.grouppartition.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.gephi.project.api.ProjectController;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
/**
 *
 * @author puig
 */
@ActionID(category = "Tools",
id = "upf.edu.gephi.plugin.grouppartition")
@ActionRegistration(displayName = "#CTL_GroupPartitionAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools", position = 3333)
})
@Messages("CTL_GroupPartitionAction=Generate groups by partition")
public final class MenuButtonController implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace() != null) {
            String[] options = new String[] {"Create", "Overwrite", "Cancel"}; // {0, 1, 2}
            int response = JOptionPane.showOptionDialog(null,
                "De you want to create a new workspace or overwrite the existing one?", "Select",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE,
                null, options, options[0]);

            if (response != 2) {
                PartitionController partition = new PartitionController();

                boolean createNewWorkspace = false;
                if (response == 0) {
                    createNewWorkspace = true;
                }
                //TODO Change boolean to a required parameter class.
                //Starts logic of partition creation
                partition.generatePartition(createNewWorkspace);
            }
        }
    }
}