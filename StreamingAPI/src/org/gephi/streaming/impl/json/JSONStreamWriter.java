/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.streaming.impl.json;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.impl.json.parser.JSONException;
import org.gephi.streaming.impl.json.parser.JSONObject;
import org.gephi.streaming.impl.json.parser.JSONConstants.Fields;
import org.gephi.streaming.impl.json.parser.JSONConstants.Types;

/**
 * @author panisson
 *
 */
public class JSONStreamWriter extends StreamWriter {
    
    /**
     * @param outputStream
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
    
    /**
     * A shortcut to the output.
     */
    protected PrintStream out;
    
    protected String graphName = "";
    
    protected void outputHeader()
    {
    }

    protected void outputEndOfFile()
    {
    }

    public void edgeAttributeAdded( String edgeId, String attribute, Object value )
    {
        edgeAttributeChanged( edgeId, attribute, value );
    }

    public void edgeAttributeChanged( String edgeId, String attribute, Object newValue )
    {
        try {
            out.print(
                    new JSONObject()
                        .put(Types.CE.value(), new JSONObject()
                            .put(edgeId, new JSONObject()
                                .put(attribute, newValue)
                                )
                            )
                        .toString() + '\r');
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void edgeAttributeRemoved( String edgeId, String attribute )
    {
        try {
            out.print(
                    new JSONObject()
                        .put(Types.CE.value(), new JSONObject()
                            .put(edgeId,new JSONObject()
                                .put(attribute, JSONObject.NULL)
                                )
                            )
                        .toString() + '\r');
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void graphAttributeAdded( String attribute, Object value )
    {
        graphAttributeChanged( attribute, null, value );
    }

    public void graphAttributeChanged( String attribute, Object oldValue,
            Object newValue )
    {
        try {
            out.print(
                    new JSONObject()
                        .put(Types.CG.value(), new JSONObject()
                            .put(attribute, newValue)
                            )
                        .toString() + '\r');
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void graphAttributeRemoved( String attribute )
    {
        try {
            out.print(
                    new JSONObject()
                        .put(Types.CG.value(), new JSONObject()
                            .put(attribute, JSONObject.NULL)
                            )
                        .toString() + '\r');
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void nodeAttributeAdded( String nodeId, String attribute, Object value )
    {
        nodeAttributeChanged( nodeId, attribute, value );
    }

    public void nodeAttributeChanged( String nodeId, String attribute, Object newValue )
    {
        try {
            out.print(
                    new JSONObject()
                        .put(Types.CN.value(), new JSONObject()
                            .put(nodeId, new JSONObject()
                                .put(attribute, newValue)
                                )
                            )
                        .toString() + '\r');
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void nodeAttributeRemoved( String nodeId, String attribute )
    {
        try {
            out.print(
                    new JSONObject()
                        .put(Types.CN.value(), new JSONObject()
                            .put(nodeId, new JSONObject()
                                .put(attribute, JSONObject.NULL)
                                )
                            )
                        .toString() + '\r');
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void edgeAdded( String edgeId, String fromNodeId, String toNodeId,
            boolean directed )
    {
        try {
            out.print(
                    new JSONObject()
                        .put(Types.AE.value(), new JSONObject()
                            .put(edgeId, new JSONObject()
                                .put(Fields.SOURCE.value(), fromNodeId)
                                .put(Fields.TARGET.value(), toNodeId)
                                .put(Fields.DIRECTED.value(), directed)
                                )
                            )
                        .toString() + '\r');
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void edgeRemoved( String edgeId )
    {
        try {
            out.print(
                    new JSONObject()
                        .put(Types.DE.value(), new JSONObject()
                            .put(edgeId, new JSONObject())
                            )
                        .toString() + '\r');
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void nodeAdded( String nodeId, Map<String, Object> attributes )
    {
        try {
            JSONObject attributesJObject = new JSONObject();
            if (attributes!=null && attributes.size()>0) {
                for(Map.Entry<String, Object> entry: attributes.entrySet()) {
                    attributesJObject.put(entry.getKey(), entry.getValue());
                }
            }

            out.print(
                    new JSONObject()
                        .put(Types.AN.value(), new JSONObject()
                            .put(nodeId, attributesJObject)
                            )
                        .toString() + '\r');
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void nodeRemoved( String nodeId )
    {
        try {
            out.print(
                    new JSONObject()
                        .put(Types.DN.value(), new JSONObject()
                            .put(nodeId, new JSONObject())
                            )
                        .toString() + '\r');
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
