/*
 Copyright Polinode, 2017
 *

 Base on code from
 Copyright 2008-2016 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 Portions Copyrighted 2011 Gephi Consortium.
 */
package com.polinode.polinodeexporter;

import com.google.gson.Gson;

import java.net.URL;
import java.net.HttpURLConnection;

import java.io.InputStreamReader;
import java.io.BufferedReader;

import java.nio.charset.Charset;

import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

import com.polinode.polinodeexporter.model.EdgeElement;
import com.polinode.polinodeexporter.model.NodeElement;
import java.awt.Desktop;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class PolinodeExporter implements Exporter, LongTask {

    private Workspace workspace;

    private String networkName;
    private String networkDescription;
    private boolean isNetworkPublic;
    private String polinodePublicKey;
    private String polinodePrivateKey;

    private ProgressTicket progressTicket;
    private boolean cancel = false;

    final Charset utf8Charset = Charset.forName("UTF-8");

    @Override
    public boolean execute() {

        Graph graph = null;

        try {
            try {
                
                //  get and lock the graph from the current workspace
                GraphModel graphModel = workspace.getLookup().lookup(GraphModel.class);
                graph = graphModel.getGraphVisible();
                graph.readLock();

                //  limit size
                
                if( graph.getNodeCount()>50000 || graph.getEdgeCount()>250000 ) {
                    DisplayError("Polinode networks are limited to 50000 nodes (this network has "+graph.getNodeCount()+")\nand 250000 edges (this network has "+graph.getEdgeCount()+").");
                    cancel = true;
                    return false;
                }

                //  count the nodes + edges for progress bar
                int tasks = graph.getNodeCount() + graph.getEdgeCount() + 1;
                Progress.start(progressTicket, tasks);

                //  nodes
                Table nodeTable = graphModel.getNodeTable();
                ArrayList<NodeElement> pnodes = new ArrayList<NodeElement>();
                Node[] gnodes = graph.getNodes().toArray();
                for (Node gnode : gnodes) {
                    NodeElement pnode = new NodeElement();
                    pnode.id = gnode.getId().toString();
                    if( gnode.getLabel()!=null && gnode.getLabel().length()>0 )
                        pnode.attributes.put("name", gnode.getLabel());
                    pnode.x = gnode.x();
                    pnode.y = -gnode.y();
                    pnode.size = gnode.size();
                    if( gnode.getRGBA()!=0 )
                        pnode.color = "rgba(" + (int) (gnode.r() * 255) + "," + (int) (gnode.g() * 255) + "," + (int) (gnode.b() * 255) + "," + gnode.alpha() + ")";

                    for (Column col : nodeTable) {
                        String cid = col.getId();
                        if (!cid.equalsIgnoreCase("id") && !cid.equalsIgnoreCase("label")) {
                            Object attribute = gnode.getAttribute(col);
                            if( attribute!=null ) {
                                pnode.attributes.put(col.getTitle(), attribute);
                            }
                        }
                    }
                    pnodes.add(pnode);

                    if (cancel) {
                        return false;
                    }
                    Progress.progress(progressTicket);
                }

                ArrayList<EdgeElement> pedges = new ArrayList<EdgeElement>();
                Edge[] gedges = graph.getEdges().toArray();
                for (Edge gedge : gedges) {
                    EdgeElement pedge = new EdgeElement();
                    pedge.id = gedge.getId().toString();
                    pedge.source = gedge.getSource().getId().toString();
                    pedge.target = gedge.getTarget().getId().toString();

                    if( gedge.getLabel()!=null && gedge.getLabel().length()>0 )
                        pedge.attributes.put("Name", gedge.getLabel());

                    if( gedge.getRGBA()!=0 )
                        pedge.attributes.put("Color", "rgba(" + (int) (gedge.r() * 255) + "," + (int) (gedge.g() * 255) + "," + (int) (gedge.b() * 255) + "," + gedge.alpha() + ")");

                    if( gedge.getWeight()!=0 )
                        pedge.attributes.put("Weight", gedge.getWeight());

                    Iterator<Column> gedgeAttribute = gedge.getAttributeColumns().iterator();
                    while (gedgeAttribute.hasNext()) {
                        Column col = gedgeAttribute.next();
                        if (!col.isProperty()) {
                            Object attribute = gedge.getAttribute(col);
                            if( attribute!=null ) {
                                pedge.attributes.put(col.getTitle(), attribute);
                            }
                        }
                    }
                    pedges.add(pedge);

                    if (cancel) {
                        return false;
                    }
                    Progress.progress(progressTicket);
                }

                HashMap<String, ArrayList> networkJSON = new HashMap<String, ArrayList>();
                networkJSON.put("nodes", pnodes);
                networkJSON.put("edges", pedges);

                HashMap postdata = new HashMap();
                postdata.put("name", networkName);
                postdata.put("networkJSON", networkJSON);
                postdata.put("description", networkDescription);
                postdata.put("isDirected", graph.isDirected());
                postdata.put("status", isNetworkPublic ? "Public" : "Private");

                //  POST to polinode API
                URL url = new URL("https://app.polinode.com/api/v2/networks");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);

                String authorization = Base64.getEncoder().encodeToString((polinodePublicKey + ":" + polinodePrivateKey).getBytes(utf8Charset));
                connection.setRequestProperty("Authorization", "Basic " + authorization);

                Gson gson = new Gson();
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Content-Encoding", "gzip");
                GZIPOutputStream outputStream = new GZIPOutputStream(connection.getOutputStream());
                outputStream.write(gson.toJson(postdata).getBytes(utf8Charset));
                outputStream.close();

                connection.connect();

                //  get the response from the request
                
                BufferedReader reader = null;
                String errorMessage = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                }
                catch (Exception e1) {
                    errorMessage = e1.getMessage();
                    reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                }
                finally {
                    reader.mark(327680);
                    
                    //  try to interpret response as json
                    
                    try {
                        HashMap result = gson.fromJson(reader, HashMap.class);
                        
                        if( errorMessage!=null ) {
                            if( result.get("message")!=null )
                                errorMessage = result.get("message").toString();
                            DisplayError(errorMessage);
                        }
                        else {
                            String networkID = result.get("_id").toString();
                            DisplaySuccess(networkID);
                        }
                    }
                    catch( Exception e2 ) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        reader.reset();
                        while( (line=reader.readLine())!=null )
                            sb.append(line+"\n");
                        if( sb.toString().length()>0 )
                            errorMessage = sb.toString();
                        DisplayError(errorMessage);
                    }
                    finally {
                        connection.disconnect();
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(PolinodeExporter.class.getName()).log(Level.SEVERE, null, e);
            } finally {
                if (graph != null) {
                    graph.readUnlock();
                }
            }
        } catch (Exception e) {
            Logger.getLogger(PolinodeExporter.class.getName()).log(Level.SEVERE, null, e);
        }

        Progress.finish(progressTicket);
        return !cancel;
    }

    void DisplayError(final String errorMessage) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null,
                errorMessage,
                "Export to Polinode",
                JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    void DisplaySuccess(final String networkid) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Object[] options = {"Open in Polinode", "Close"};
                int selected = JOptionPane.showOptionDialog(null,
                        "Successfully uploaded your network to Polinode",
                        "Export to Polinode",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                if( selected==0 ) {
                    try {
                        Desktop.getDesktop().browse(new URL("https://app.polinode.com/networks/explore/"+networkid).toURI());
                    } catch (Exception e) {
                        Logger.getLogger(PolinodeExporter.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
        });
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
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public void setNetworkDescription(String networkDescription) {
        this.networkDescription = networkDescription;
    }

    public void setPolinodePublicKey(String polinodePublicKey) {
        this.polinodePublicKey = polinodePublicKey;
    }

    public void setPolinodePrivateKey(String polinodePrivateKey) {
        this.polinodePrivateKey = polinodePrivateKey;
    }

    public void setIsNetworkPublic(boolean isNetworkPublic) {
        this.isNetworkPublic = isNetworkPublic;
    }
}

