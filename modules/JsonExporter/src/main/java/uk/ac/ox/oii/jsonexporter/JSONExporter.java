/*
Copyright (C) 2015  Scott A. Hale
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
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
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

public class JSONExporter implements GraphExporter, LongTask, CharacterExporter {

    private boolean exportVisible = false;
    private Workspace workspace;
    private Writer writer;
    private ProgressTicket progress;
    private boolean cancel = false;
    //private File path;

    @Override
    public boolean execute() {
        Graph graph = null;
        try {
            if (writer != null) {//path.getParentFile().exists()
                GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
                if (exportVisible) {
                    graph = graphModel.getGraphVisible();
                } else {
                    graph = graphModel.getGraph();
                }
                graph.readLock();

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


                Table attModel = graphModel.getNodeTable();
                HashSet<GraphElement> jNodes = new HashSet<GraphElement>();
                Node[] nodeArray = graph.getNodes().toArray();
                
                for (Node n : nodeArray) {

                    String id = n.getId().toString();
                    String label = n.getLabel();
                    float x = n.x();
                    float y = n.y();
                    //float z=n.z();
                    float size = n.size();
                    String color = "rgb(" + (int) (n.r() * 255) + "," + (int) (n.g() * 255) + "," + (int) (n.b() * 255) + ")";

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

                    
                    for (Column col : attModel) {
                            String name = col.getTitle();
                            String cid = col.getId();
                            if (cid.equalsIgnoreCase("id") || cid.equalsIgnoreCase("label")) {
                                continue;
                            }

                            Object valObj = n.getAttribute(col);
                            if (valObj == null) {
                                continue;
                            }
                            String val = valObj.toString();
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

                for (Edge e : edgeArray) {
                    String sourceId = e.getSource().getId().toString();
                    String targetId = e.getTarget().getId().toString();

                    /*if (renumber) {
                     sourceId = nodeIdMap.get(sourceId);
                     targetId = nodeIdMap.get(targetId);
                     }*/


                    GraphEdge jEdge = new GraphEdge(String.valueOf(e.getId()));
                    jEdge.setSource(sourceId);
                    jEdge.setTarget(targetId);
                    jEdge.setSize(e.getWeight());
                    jEdge.setLabel(e.getLabel());

                    float r=e.r();
                    float g=e.g();
                    float b=e.b();

                    Iterator<Column> eAttr = e.getAttributeColumns().iterator();
                    while (eAttr.hasNext()) {
                        Column col = eAttr.next();
                        if (col.isProperty() || "weight".equalsIgnoreCase(col.getId())) {
                            //isProperty() excludes id, label, but not weight
                            continue;
                        }
                        String name = col.getTitle();
                        Object valObj = e.getAttribute(col);
                        if (valObj == null) {
                            continue;
                        }
                        String val = valObj.toString();
                        jEdge.putAttribute(name, val);
                    }
                    
                    String color;
                    if (e.alpha()!=0) {
                        color = "rgb(" + (int) (r* 255) + "," + (int) (g* 255) + "," + (int) (b* 255) + ")";
                    } else {
                        //no colour has been set. Colour will be mix of connected nodes
                        Node n = e.getSource();
                        Color source = new Color(n.r(),n.g(),n.b());
                        n = e.getTarget();
                        Color target = new Color(n.r(),n.g(),n.b());
                        Color result = colorMixer.getColor(null, source, target);
                        color = "rgb(" + result.getRed() + "," + result.getGreen() + "," + result.getBlue() + ")";
                    }
                    jEdge.setColor(color);

                    jEdges.add(jEdge);

                    if (cancel) {
                        return false; //returning false if the task was cancelled
                    }
                    Progress.progress(progress);
                }


                HashMap<String, HashSet<GraphElement>> json = new HashMap<String, HashSet<GraphElement>>();
                json.put("nodes", jNodes);
                json.put("edges", jEdges);

                gson.toJson(json, writer);

                //Finish progress
                Progress.finish(progress);
            } else {
                throw new FileNotFoundException("Writer is null");
            }
        } catch (Exception e) {
            Logger.getLogger(JSONExporter.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            if (graph!=null) {
                graph.readUnlock();
            }
        }
        return !cancel; //true if task has not been cancelled and we've gotten to the end
    }

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
