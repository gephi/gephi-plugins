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

import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Created by song on 16-1-29.
 *
 * Node Color Manager
 *
 * Save node colors to a file, so you can share colors among multiple graphs.
 *
 * The big deal is NodeColorManagerUI, which contains two button and an icon.
 *
 * ColorTextFilter is used to filter files when open an FileChooser, so you
 * should save file with ".txt" as file name extension --- note this is optional.
 *
 * Save/restore mechanism is implement writeFile() / readFile(), other "spaghetti
 * codes" are mostly logic about ui, since I'm new to GUI programming.
 */

@ServiceProvider(service = Tool.class)
public class NodeColorManager implements Tool{

    private final NodeColorManagerUI ui = new NodeColorManagerUI();

    private String filePathName = System.getProperty("user.home");

    public void select() {}

    public void unselect() {}

    public ToolEventListener[] getListeners() {
        return new ToolEventListener[0];
    }

    public ToolUI getUI() {
        return ui;
    }

    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }



    private class NodeColorManagerUI extends Component implements ToolUI{

        public JPanel getPropertiesBar(Tool tool) {
            JPanel panel = new JPanel();

            //Buttons
            JButton saveButton = new JButton("save color");
            saveButton.setToolTipText("Choose a file to save color of nodes in current graph.");
            saveButton.setDefaultCapable(true);
            saveButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        saveToFile();
                    } catch (IOException ex) {
                        Logger.getLogger(NodeColorManager.class.getName()).log(Level.SEVERE, null, ex);
                        notice("Error: unable to save to file. See log for detail.");
                    } catch (NoOpenProjectException e1) {
                        notice("Error: No project open in Gephi.");
                    }
                }

            });

            JButton restoreFromFileToGraphButton = new JButton("restore color");
            restoreFromFileToGraphButton.setToolTipText("Choose a color file to paint nodes in current graph");
            restoreFromFileToGraphButton.setDefaultCapable(true);
            restoreFromFileToGraphButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        restoreFromFile();
                    } catch (IOException ex) {
                        Logger.getLogger(NodeColorManager.class.getName()).log(Level.SEVERE, null, ex);
                        notice("Error: unable to restoreFromFile from file.");
                    } catch (NoOpenProjectException e1) {
                        notice("Error: No project open in Gephi.");
                    } catch (FileFormatException e1) {
                        notice("Restore Failed:\n" +
                                "Parse Error: This is not a valid file saved by [Node Color Manager].\n" +
                                e1.getMessage());
                    }
                }
            });

            panel.add(saveButton);
            panel.add(restoreFromFileToGraphButton);

            return panel;
        }

        public Icon getIcon() {
            return new ImageIcon(getClass().getResource("/nodeColorManagerIcon16x16.png"));
        }

        public String getName() {
            return "share Node color among multiple graph";
        }

        public String getDescription() {
            return "save/restore node color to a file";
        }

        public int getPosition() {
            return 1200;
        }

        public void notice(String content){
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(content));
        }

        private Graph getGraph() throws NoOpenProjectException {
            ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
            if (pc.getCurrentProject() == null) {
                throw new NoOpenProjectException();
            }
            GraphController gc = Lookup.getDefault().lookup(GraphController.class);
            GraphModel graphModel = gc.getGraphModel();
            return graphModel.getGraph();
        }

        public void restoreFromFile() throws IOException, NoOpenProjectException, FileFormatException {
            Graph graph = getGraph();
            File file = new File(filePathName);

            JFileChooser jFileChooser = new JFileChooser(file);
            ColorTextFilter filter = new ColorTextFilter();
            jFileChooser.addChoosableFileFilter(filter);
//            jFileChooser.setFileFilter(filter);
            if(jFileChooser.showOpenDialog(ui)==JFileChooser.APPROVE_OPTION){
                file = jFileChooser.getSelectedFile();
                if (!file.exists()) {
                    notice("file not found!");
                    return;
                }else{
                    String result = readFile(file, graph);
                    notice("Restore Finish:\n"+
                            "File: "+file.getAbsolutePath()+"\n"+result
                    );
                }
            }
        }

        private String readFile(File file, Graph graph) throws FileNotFoundException, FileFormatException {
            Map<String, Node> nodeMap = new HashMap<String, Node>();
            for (Node node : graph.getNodes()) {
                nodeMap.put(node.getLabel(), node);
            }

            int nodeRestoreCount = 0;
            int lineCount = 0;
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String lineContent = sc.nextLine();
                if(lineContent.length()>0){
                    String[] content = lineContent.split(":rgb:");
                    if(content.length!=2){
                        throw new FileFormatException("File: "+file.getAbsolutePath()+"\nLine: "+(lineCount+1));
                    }
                    lineCount++;
                    String nodeName = content[0];
                    String[] position = content[1].split(",");
                    Node node = nodeMap.get(nodeName);
                    if (node == null) {
                        //do nothing;continue next line;
                    } else {
                        nodeRestoreCount++;
                        node.setR(Float.valueOf(position[0]));
                        node.setG(Float.valueOf(position[1]));
                        node.setB(Float.valueOf(position[2]));
                    }
                }
            }
            sc.close();
            return "Node Read from file: "+lineCount+"\nNode restored in graph: "+nodeRestoreCount+"\n";
        }


        private void saveToFile() throws IOException, NoOpenProjectException {
            File file;
            while(true) {
                file = new File(filePathName);
                JFileChooser jFileChooser = new JFileChooser(file);
                ColorTextFilter filter = new ColorTextFilter();
                jFileChooser.addChoosableFileFilter(filter);
//                jFileChooser.setFileFilter(filter);
                if (jFileChooser.showSaveDialog(ui) == JFileChooser.APPROVE_OPTION) {
                    file = jFileChooser.getSelectedFile();
                    if (file.exists()) {
                        if (DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Confirmation(
                                        "file " + file.getAbsolutePath() + " exist, overwrite?",
                                        "Warning",
                                        NotifyDescriptor.OK_CANCEL_OPTION
                                )) == NotifyDescriptor.OK_OPTION) {

                            break;
                        } else {
                            return;
                        }
                    } else {
                        if(!isValidFileName(file.getName()) ||
                                (file.getParentFile() == null) ||
                                (!file.getParentFile().exists())
                                ){
                            notice("Save Failed\ninvalid file name. Should not contains /\\:*?<>");
                            //continue;
                        }else{
                            file.createNewFile();
                            break;
                        }
                    }
                } else {
                    return;
                }
            }
            filePathName = file.getAbsolutePath();
            Graph graph = getGraph();
            int nodeCount = writeFile(file,graph);
            notice(nodeCount+" node color saved to " + file.getAbsolutePath());
        }

        private int writeFile(File file,Graph graph) throws IOException {
            Map<String, Node> nodeMap = new HashMap();
            for (Node node : graph.getNodes()) {
                nodeMap.put(node.getLabel(), node);
            }

            int bufSize = 8192;
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter, bufSize);
            PrintWriter writer = new PrintWriter(bufferedWriter);

            int nodeCount = 0;
            for (Map.Entry<String,Node> entry : nodeMap.entrySet()) {
                Node node = entry.getValue();
                writer.printf("%s:rgb:%f,%f,%f",
                        node.getLabel(), node.r(), node.g(), node.b());
                writer.println();
                nodeCount++;
            }
            writer.flush();
            writer.close();
            return nodeCount;
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

    }

    private static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

    public static class ColorTextFilter extends FileFilter {

        //Accept all directories and all txt files
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            String extension = getExtension(f);
            if (extension != null) {
                if (extension.equals("txt")) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }
        //The description of this filter
        public String getDescription() {
            return "text file (*.txt)";
        }
    }
}