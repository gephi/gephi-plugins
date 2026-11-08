/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.streaming.impl.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.event.EdgeAddedEvent;
import org.gephi.streaming.api.event.ElementEvent;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.impl.json.parser.JSONConstants;
import org.gephi.streaming.impl.json.parser.JSONException;
import org.gephi.streaming.impl.json.parser.JSONObject;
import org.gephi.streaming.impl.json.parser.JSONConstants.Fields;
import org.gephi.streaming.impl.json.parser.JSONConstants.Types;

/**
 * StreamWriter implementation to output graph data in JSON format.
 *
 * @author panisson
 *
 */
public class JSONStreamWriter extends StreamWriter {

    private static final Logger logger =
            Logger.getLogger(JSONStreamWriter.class.getName());

    private static String EOL = "\r\n";
    
    /**
     * @param outputStream - the OutputStream to send formatted data.
     */
    public JSONStreamWriter(OutputStream outputStream) {
        super(outputStream);
        out = new PrintStream(outputStream, true);
    }
    
    @Override
    public void startStream() {
        outputHeader();
    }

    @Override
    public void endStream() {
        outputEndOfFile();
    }

    public void handleGraphEvent(GraphEvent event) {

        if (event instanceof ElementEvent) {
            ElementEvent elementEvent = (ElementEvent)event;
            JSONObject o = null;

            switch (event.getElementType()) {
            case GRAPH:
                o = graphAttributeChanged(elementEvent.getAttributes());
                break;
            case NODE:
                
                switch (event.getEventType()) {
                    case ADD:
                        o = nodeAdded(elementEvent.getElementId(), elementEvent.getAttributes());
                        break;
                    case CHANGE:
                        o = nodeChanged(elementEvent.getElementId(), elementEvent.getAttributes());
                        break;
                    case REMOVE:
                        o = nodeRemoved(elementEvent.getElementId());
                        break;
                }
                break;
            case EDGE:
                switch (event.getEventType()) {
                    case ADD:
                        EdgeAddedEvent eaEvent = (EdgeAddedEvent)event;
                        o = edgeAdded(elementEvent.getElementId(), eaEvent.getSourceId(),
                                eaEvent.getTargetId(), eaEvent.isDirected(),
                                elementEvent.getAttributes());
                        break;
                    case CHANGE:
                        o = edgeChanged(elementEvent.getElementId(),
                                elementEvent.getAttributes());
                        break;
                    case REMOVE:
                        o = edgeRemoved(elementEvent.getElementId());
                        break;
                }
                break;
            }
            
            if (o != null) {
                
                if (event.getTimestamp() != null) {
                    try {
                        o.put(JSONConstants.Fields.T.value(), event.getTimestamp());
                    } catch (JSONException e) {
                        logger.log(Level.WARNING, "Unable to write timestamp "
                                + "into JSON object '{0}': {1}",
                                new Object[]{o.toString(), e.getMessage()});
                    }
                }
                
                out.print(o.toString() + EOL);
            }
            
        }
        try {
            outputStream.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
//            Logger.getLogger(JSONStreamWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * A shortcut to the output.
     */
    protected PrintStream out;
    
    protected String graphName = "";
    
    protected void outputHeader() {
    }

    protected void outputEndOfFile() {
    }

    private JSONObject graphAttributeChanged(Map<String, Object> attributes) {
        try {
            JSONObject graphData = createGraphData(attributes);
            return new JSONObject()
                        .put(Types.CG.value(), graphData);
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Unable to write JSONObject for "
                    + "graphAttributeChanged event: {0}",
                    new Object[]{e.getMessage()});
            return null;
        }
    }

    private JSONObject edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed, Map<String, Object> attributes) {
        try {
            
            JSONObject edgeData = createEdgeData(edgeId, fromNodeId, toNodeId, directed, attributes);
            
            return new JSONObject()
                        .put(Types.AE.value(), edgeData);
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Unable to write JSONObject for "
                    + "edgeAdded event, edge {0}: {1}",
                    new Object[]{edgeId, e.getMessage()});
            return null;
        }
    }
    
    private JSONObject edgeChanged(String edgeId, Map<String, Object> attributes) {
        try {
            JSONObject edgeData = createEdgeChangedData(edgeId, attributes);
            return new JSONObject()
                        .put(Types.CE.value(), edgeData);
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Unable to write JSONObject for "
                    + "edgeChanged event, edge {0}: {1}",
                    new Object[]{edgeId, e.getMessage()});
            return null;
        }
    }

    private JSONObject edgeRemoved(String edgeId) {
        try {
            return new JSONObject()
                        .put(Types.DE.value(), new JSONObject()
                            .put(edgeId, new JSONObject())
                            );
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Unable to write JSONObject for "
                    + "edgeRemoved event, edge {0}: {1}",
                    new Object[]{edgeId, e.getMessage()});
            return null;
        }
    }

    private JSONObject nodeAdded(String nodeId, Map<String, Object> attributes) {
        try {
            JSONObject nodeData = createNodeData(nodeId, attributes);
            return new JSONObject()
                        .put(Types.AN.value(), nodeData);
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Unable to write JSONObject for "
                    + "nodeAdded event, node {0}: {1}",
                    new Object[]{nodeId, e.getMessage()});
            return null;
        }
    }
    
    private JSONObject nodeChanged(String nodeId, Map<String, Object> attributes) {
        try {
            JSONObject nodeData = createNodeData(nodeId, attributes);
            return new JSONObject()
                        .put(Types.CN.value(), nodeData);
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Unable to write JSONObject for "
                    + "nodeChanged event, node {0}: {1}",
                    new Object[]{nodeId, e.getMessage()});
            return null;
        }
    }

    private JSONObject nodeRemoved(String nodeId) {
        try {
            return new JSONObject()
                        .put(Types.DN.value(), new JSONObject()
                            .put(nodeId, new JSONObject())
                            );
        } catch (JSONException e) {
            logger.log(Level.WARNING, "Unable to write JSONObject for "
                    + "nodeRemoved event, node {0}: {1}",
                    new Object[]{nodeId, e.getMessage()});
            return null;
        }
    }
    
    private JSONObject createGraphData(Map<String, Object> attributes) 
            throws JSONException {

        JSONObject attributesJObject = new JSONObject();
        if (attributes != null && attributes.size() > 0) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                attributesJObject.put(entry.getKey(), entry.getValue());
            }
        }

        return attributesJObject;
    }
    
    private JSONObject createNodeData(String nodeId,
            Map<String, Object> attributes) throws JSONException {

        JSONObject attributesJObject = new JSONObject();
        if (attributes != null && attributes.size() > 0) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                attributesJObject.put(entry.getKey(), entry.getValue());
            }
        }

        JSONObject nodeData = new JSONObject();
        nodeData.put(nodeId, attributesJObject);

        return nodeData;
    }
    
    private JSONObject createEdgeData(String edgeId, String fromNodeId, String toNodeId,
            boolean directed, Map<String, Object> attributes) throws JSONException {

        JSONObject attributesJObject = new JSONObject()
            .put(Fields.SOURCE.value(), fromNodeId)
            .put(Fields.TARGET.value(), toNodeId)
            .put(Fields.DIRECTED.value(), directed);
        
        if (attributes != null && attributes.size() > 0) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                attributesJObject.put(entry.getKey(), entry.getValue());
            }
        }

        JSONObject nodeData = new JSONObject();
        nodeData.put(edgeId, attributesJObject);

        return nodeData;
    }
    
    private JSONObject createEdgeChangedData(String edgeId, Map<String, Object> attributes) throws JSONException {

        JSONObject attributesJObject = new JSONObject();
        
        if (attributes != null && attributes.size() > 0) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                attributesJObject.put(entry.getKey(), entry.getValue());
            }
        }

        JSONObject nodeData = new JSONObject();
        nodeData.put(edgeId, attributesJObject);

        return nodeData;
    }
}
