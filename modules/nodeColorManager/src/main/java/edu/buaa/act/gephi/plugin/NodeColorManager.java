package edu.buaa.act.gephi.plugin;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by song on 16-1-29.
 */

@ServiceProvider(service = Tool.class)
public class NodeColorManager implements Tool{

    private final NodeColorManagerUI ui = new NodeColorManagerUI();

    public NodeColorManager(){
//        System.out.println("[Node Color Manager] Plugin initialized...");
    }

    public void select() {

    }

    public void unselect() {

    }

    public ToolEventListener[] getListeners() {
        return new ToolEventListener[0];
    }

    public ToolUI getUI() {
        return ui;
    }

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }

    private boolean isValidFileName(String fileName){
        if(fileName.matches("^[^.\\\\/:*?\"<>|]?[^\\\\/:*?\"<>|]*")){
            String newFileName = fileName.replaceAll("^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*", "");
            if(newFileName.length()>0){
                return true;
            }
        }
        return false;
    }

    private void operate(String op) throws UnsupportedEncodingException, IOException {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentProject() == null) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("No project opened in Gephi."));
            return;
        }

        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = gc.getGraphModel();
        Graph graph = graphModel.getGraph();

        //System.out.println(System.getProperty("user.dir"));
        String fileName = ui.getText();
        if(!isValidFileName(fileName)){
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("invalid file name! Should not contains :/\\<>[]|?*"));
            return;
        }

        File file = new File(fileName);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (op.equals("restore")) {
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("node color will be restore from "+file.getAbsolutePath()));
            String lineContent;
            Map<String, Node> nodeMap = new HashMap<String, Node>();
            for (Node node : graph.getNodes()) {
                nodeMap.put(node.getLabel(), node);
            }
            while ((lineContent = br.readLine()) != null) {
                String[] content = lineContent.split(":rgb:");
                String nodeName = content[0];
                String[] position = content[1].split(",");
                Node node = nodeMap.get(nodeName);
                if (node == null) {
                    Logger.getLogger(NodeColorManager.class.getName()).log(Level.SEVERE, node+" is null");
                } else {
                    node.setR(Float.valueOf(position[0]));
                    node.setG(Float.valueOf(position[1]));
                    node.setB(Float.valueOf(position[2]));
                }
            }
            br.close();
        } else {
            if(file.exists()){
                if(!ui.overwriteWithoutWarning){
                    if(DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Confirmation(
                                    "file "+file.getAbsolutePath()+" exist, overwrite? (check \"overwrite\" box to " +
                                            "ignore this warning)",
                                    "Warning",NotifyDescriptor.OK_CANCEL_OPTION))==NotifyDescriptor.OK_OPTION){
                        //do nothing
                    }else{
                        return;
                    }
                }else{
                    //do nothing
                }
            }else{
                file.createNewFile();
            }
            StringBuilder str = new StringBuilder();
            for (Node node : graph.getNodes().toArray()) {
                str.append(node.getLabel())
                        .append(":rgb:")
                        .append(node.r()).append(",")
                        .append(node.g()).append(",")
                        .append(node.b()).append("\n");
            }
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(str.toString().getBytes("UTF-8"));
            bos.close();
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("node color saved to "+file.getAbsolutePath()));
        }
    }

    private class NodeColorManagerUI implements ToolUI{

        private JTextField fileInput;
        private boolean overwriteWithoutWarning=false;
        public String getText() {
            return fileInput.getText();
        }

        public JPanel getPropertiesBar(Tool tool) {
            final NodeColorManager myTool = (NodeColorManager) tool;
            JPanel panel = new JPanel();

            //Buttons
            JButton saveToColumnButton = new JButton("save color");
            saveToColumnButton.setDefaultCapable(true);
            saveToColumnButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        myTool.operate("save");
                    } catch (IOException ex) {
                        Logger.getLogger(NodeColorManager.class.getName()).log(Level.SEVERE, null, ex);
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Error: unable to save to file. See log for detail."));
                    }
                }
            });
            final JCheckBox jCheckBox = new JCheckBox("overwrite", false);
            jCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        overwriteWithoutWarning = true;
                    } else {
                        overwriteWithoutWarning=false;
                    }
                }
            });
            JButton applyToLayoutButton = new JButton("restore color");
            applyToLayoutButton.setDefaultCapable(true);
            applyToLayoutButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        myTool.operate("restore");
                    } catch (IOException ex) {
                        Logger.getLogger(NodeColorManager.class.getName()).log(Level.SEVERE, null, ex);
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message("Error: unable to restore from file."));
//                        DialogDisplayer.getDefault().
                    }
                }
            });
            fileInput = new JTextField("save to/restore from file:", 16);
            panel.add(saveToColumnButton);
            panel.add(jCheckBox);
            panel.add(applyToLayoutButton);
            panel.add(fileInput);
            return panel;
        }

        public Icon getIcon() {
            return new ImageIcon(getClass().getResource("/nodeColorManagerIcon16x16.png"));
//            return new ImageIcon(getClass().getResource("/plus.png"));
        }

        public String getName() {
            return "share Node color among multi graphs";
        }

        public String getDescription() {
            return "save/restore node color to a file";
        }

        public int getPosition() {
            return 1200;
        }

        public NodeColorManagerUI(){
//            System.out.println("[Node Color Manager] UI initialized");
        }
    }
}
