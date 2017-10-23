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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;

import java.nio.charset.Charset;

import java.util.Base64;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private ProgressTicket progress;
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

                //  count the nodes + edges for progress bar
                int tasks = graph.getNodeCount() + graph.getEdgeCount() + 1;
                Progress.start(progress, tasks);

                //  nodes
                Table nodeTable = graphModel.getNodeTable();
                ArrayList<NodeElement> pnodes = new ArrayList<NodeElement>();
                Node[] gnodes = graph.getNodes().toArray();
                for (Node gnode : gnodes) {
                    NodeElement pnode = new NodeElement();
                    pnode.id = gnode.getId().toString();
                    pnode.attributes.put("name", gnode.getLabel());
                    pnode.x = gnode.x();
                    pnode.y = gnode.y();
                    pnode.size = gnode.size();
                    pnode.color = "rgba(" + (int) (gnode.r() * 255) + "," + (int) (gnode.g() * 255) + "," + (int) (gnode.b() * 255) + "," + (int) (gnode.alpha() * 255) + ")";

                    for (Column col : nodeTable) {
                        String cid = col.getId();
                        if (!cid.equalsIgnoreCase("id") && !cid.equalsIgnoreCase("label")) {
                            Object obj = gnode.getAttribute(col);
                            if (obj != null) {
                                pnode.attributes.put(col.getTitle(), obj.toString());
                            }
                        }
                    }

                    pnodes.add(pnode);

                    if (cancel) {
                        return false;
                    }
                    Progress.progress(progress);
                }

                ArrayList<EdgeElement> pedges = new ArrayList<EdgeElement>();
                Edge[] gedges = graph.getEdges().toArray();
                for (Edge gedge : gedges) {
                    EdgeElement pedge = new EdgeElement();
                    pedge.id = gedge.getId().toString();
                    pedge.source = gedge.getSource().getId().toString();
                    pedge.target = gedge.getTarget().getId().toString();

                    pedge.attributes.put("Name", gedge.getLabel());
                    pedge.attributes.put("Color", "rgba(" + (int) (gedge.r() * 255) + "," + (int) (gedge.g() * 255) + "," + (int) (gedge.b() * 255) + "," + (int) (gedge.alpha() * 255) + ")");

                    Iterator<Column> gedgeAttribute = gedge.getAttributeColumns().iterator();
                    while (gedgeAttribute.hasNext()) {
                        Column col = gedgeAttribute.next();
                        if (!col.isProperty()) {
                            pedge.attributes.put(col.getTitle(), gedge.getAttribute(col).toString());
                        }
                    }

                    pedges.add(pedge);

                    if (cancel) {
                        return false;
                    }
                    Progress.progress(progress);
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
                URL url = new URL("https://www.polinode.com/api/v2/networks");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                String authorization = Base64.getEncoder().encodeToString((polinodePublicKey + ":" + polinodePrivateKey).getBytes(utf8Charset));
                connection.setRequestProperty("Authorization", "Basic " + authorization);

                Gson gson = new Gson();
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(gson.toJson(postdata).getBytes(utf8Charset));

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
                    
                    reader.mark(32768);
                    
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

        Progress.finish(progress);
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
                Object[] options = {"Open in Polinode", "Cancel"};
                int selected = JOptionPane.showOptionDialog(null,
                        "Successfully exported network id "+networkid,
                        "Export to Polinode",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                if( selected==0 ) {
                    try {
                        Desktop.getDesktop().browse(new URL("https://www.polinode.com/networks/explore/"+networkid).toURI());
                    } catch (Exception e) {
                        Logger.getLogger(PolinodeExporter.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            }
        });
    }

    @Override
    public void setWorkspace(Workspace wrkspc
    ) {
        this.workspace = wrkspc;
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
    public void setProgressTicket(ProgressTicket pt
    ) {
        this.progress = pt;
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

