/*
 Copyright (C) 2012  Scott A. Hale
 Website: http://www.scotthale.net/

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/license
 */
package uk.ac.ox.oii.jsonexporter;

import com.google.gson.Gson;
import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.io.exporter.spi.CharacterExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import uk.ac.ox.oii.jsonexporter.model.GraphEdge;
import uk.ac.ox.oii.jsonexporter.model.GraphElement;
import uk.ac.ox.oii.jsonexporter.model.GraphNode;

/**
 *
 * @author shale
 */
public class JSONExporter implements GraphExporter, LongTask, CharacterExporter {

    private boolean exportVisible = false;
    private Workspace workspace;
    private Writer writer;
    private ProgressTicket progress;
    private boolean cancel = false;
    //private File path;

    @Override
    public boolean execute() {
        try {
            if (writer != null) {//path.getParentFile().exists()
                GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
                Graph graph = null;
                if (exportVisible) {
                    graph = graphModel.getGraphVisible();
                } else {
                    graph = graphModel.getGraph();
                }

                //Count the number of tasks (nodes + edges) and start the progress
                int tasks = graph.getNodeCount() + graph.getEdgeCount();
                Progress.start(progress, tasks);

                //FileWriter fwriter = new  FileWriter(writer);

                Gson gson = new Gson();
                EdgeColor colorMixer = new EdgeColor(EdgeColor.Mode.MIXED);

                //HashMap<String, String> nodeIdMap = new HashMap<String, String>();
                //int nodeId = 0;
                //EdgeColor colorMixer = new EdgeColor(EdgeColor.Mode.MIXED);
                //Write data.json



                HashSet<GraphElement> jNodes = new HashSet<GraphElement>();
                Node[] nodeArray = graph.getNodes().toArray();
                for (int i = 0; i < nodeArray.length; i++) {

                    Node n = nodeArray[i];
                    NodeData nd = n.getNodeData();
                    String id = nd.getId();
                    String label = nd.getLabel();
                    float x = nd.x();
                    float y = nd.y();
                    float size = nd.getSize();
                    String color = "rgb(" + (int) (nd.r() * 255) + "," + (int) (nd.g() * 255) + "," + (int) (nd.b() * 255) + ")";

                    /*if (renumber) {
                     String newId=String.valueOf(nodeId);
                     nodeIdMap.put(id,newId); 
                     id=newId;
                     nodeId++;
                     }*/

                    GraphNode jNode = new GraphNode(id);
                    jNode.setLabel(label);
                    jNode.setX(x);
                    jNode.setY(y);
                    jNode.setSize(size);
                    jNode.setColor(color);

                    AttributeRow nAttr = (AttributeRow) nd.getAttributes();
                    for (int j = 0; j < nAttr.countValues(); j++) {
                        Object valObj = nAttr.getValue(j);
                        if (valObj == null) {
                            continue;
                        }
                        String val = valObj.toString();
                        AttributeColumn col = nAttr.getColumnAt(j);
                        if (col == null) {
                            continue;
                        }
                        String name = col.getTitle();
                        if (name.equalsIgnoreCase("Id") || name.equalsIgnoreCase("Label")
                                || name.equalsIgnoreCase("uid")) {
                            continue;
                        }
                        jNode.putAttribute(name, val);

                    }

                    jNodes.add(jNode);

                    if (cancel) {
                        return false;
                    }
                    Progress.progress(progress);
                }


                //Export edges. Progress is incremented at each step.
                HashSet<GraphElement> jEdges = new HashSet<GraphElement>();
                Edge[] edgeArray = graph.getEdges().toArray();
                for (int i = 0; i < edgeArray.length; i++) {
                    Edge e = edgeArray[i];
                    String sourceId = e.getSource().getNodeData().getId();
                    String targetId = e.getTarget().getNodeData().getId();

                    /*if (renumber) {
                     sourceId = nodeIdMap.get(sourceId);
                     targetId = nodeIdMap.get(targetId);
                     }*/


                    GraphEdge jEdge = new GraphEdge(String.valueOf(e.getId()));
                    jEdge.setSource(sourceId);
                    jEdge.setTarget(targetId);
                    jEdge.setSize(e.getWeight());

                    EdgeData ed = e.getEdgeData();
                    boolean mixColors=false;
                    String color="";
                    if (ed!=null) {
                        jEdge.setLabel(ed.getLabel());
                        
                        float r=ed.r();
                        float g=ed.g();
                        float b=ed.b();

                        if (r==-1 || g==-1 || b==-1) {
                            //Mix colors
                            mixColors=true;
                        } else {
                            color = "rgb(" + (int) (r* 255) + "," + (int) (g* 255) + "," + (int) (b* 255) + ")";
                        }

                        AttributeRow eAttr = (AttributeRow) ed.getAttributes();
                        if (eAttr!=null) {
                            for (int j = 0; j < eAttr.countValues(); j++) {
                                Object valObj = eAttr.getValue(j);
                                if (valObj == null) {
                                    continue;
                                }
                                String val = valObj.toString();
                                AttributeColumn col = eAttr.getColumnAt(j);
                                if (col == null) {
                                    continue;
                                }
                                String name = col.getTitle();
                                if (name.equalsIgnoreCase("Id") || name.equalsIgnoreCase("Label")
                                        || name.equalsIgnoreCase("uid")) {
                                    continue;
                                }
                                jEdge.putAttribute(name, val);
                            }
                        }
                    }
                    
                    if (mixColors) {
                        //Source
                        NodeData nd = e.getSource().getNodeData();
                        Color source = new Color(nd.r(),nd.g(),nd.b());
                        nd = e.getTarget().getNodeData();
                        Color target = new Color(nd.r(),nd.g(),nd.b());
                        Color result = colorMixer.getColor(null, source, target);
                        color = "rgb(" + result.getRed() + "," + result.getGreen() + "," + result.getBlue() + ")";
                    }
                    jEdge.setColor(color);

                    jEdges.add(jEdge);

                    if (cancel) {
                        return false;
                    }
                    Progress.progress(progress);
                }


                HashMap<String, HashSet<GraphElement>> json = new HashMap<String, HashSet<GraphElement>>();
                json.put("nodes", jNodes);
                json.put("edges", jEdges);

                gson.toJson(json, writer);

                //Finish progress
                Progress.finish(progress);
                return true;
            } else {
                throw new FileNotFoundException("Writer is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            /*try {
             /if (writer != null) {
             writer.close();
             }
             } catch (java.io.IOException e) {
             // failed to close file
             System.err.println(e);
             }*/
        }
    }

    /*     public File getPath() {
     return path;
     }
    
     public void setPath(File path) {
     this.path = path;
     }*/
    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void setExportVisible(boolean exportVisible) {
        this.exportVisible = exportVisible;
    }

    @Override
    public boolean isExportVisible() {
        return exportVisible;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this.progress = pt;
    }
}