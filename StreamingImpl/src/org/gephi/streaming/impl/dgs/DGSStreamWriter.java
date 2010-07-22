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
package org.gephi.streaming.impl.dgs;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.event.EdgeAddedEvent;
import org.gephi.streaming.api.event.ElementEvent;
import org.gephi.streaming.api.event.GraphEvent;

/**
 * @author panisson
 *
 */
public class DGSStreamWriter extends StreamWriter {

    /**
     * A shortcut to the output.
     */
    protected PrintStream out;

    protected String graphName = "";

    public DGSStreamWriter(OutputStream outputStream) {
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
    
    protected void outputHeader() {
        
        out.printf( "DGS004%n" );
        
        if( graphName.length() <= 0 )
             out.printf( "null 0 0%n" );
        else out.printf( "\"%s\" 0 0%n", graphName );
    }

    protected void outputEndOfFile() {
        // NOP
    }

    public void handleGraphEvent(GraphEvent event) {

        if (event instanceof ElementEvent) {
            ElementEvent elementEvent = (ElementEvent)event;

            switch (event.getElementType()) {
            case NODE:
                switch (event.getEventType()) {
                    case ADD:
                        this.nodeAdded(elementEvent.getElementId(), elementEvent.getAttributes());
                        break;
                    case CHANGE:
                        this.nodeChanged(elementEvent.getElementId(), elementEvent.getAttributes());
                        break;
                    case REMOVE:
                        this.nodeRemoved(elementEvent.getElementId());
                        break;
                }
                break;
            case EDGE:
                switch (event.getEventType()) {
                    case ADD:
                        EdgeAddedEvent eaEvent = (EdgeAddedEvent)event;
                        this.edgeAdded(elementEvent.getElementId(), eaEvent.getSourceId(),
                                eaEvent.getTargetId(), eaEvent.isDirected(),
                                elementEvent.getAttributes());
                        break;
                    case CHANGE:
                        this.edgeChanged(elementEvent.getElementId(),
                                elementEvent.getAttributes());
                        break;
                    case REMOVE:
                        this.edgeRemoved(elementEvent.getElementId());
                        break;
                }
                break;
            }
        }
    }

    private void graphAttributeAdded(String attribute, Object value) {
        graphAttributeChanged(attribute, null, value);
    }

    public void graphAttributeChanged(String attribute, Object oldValue,
            Object newValue) {
        out.printf("cg %s%n", attributeString(attribute, newValue, false));
    }

    private void graphAttributeRemoved(String attribute) {
        out.printf("cg %s%n", attributeString(attribute, null, true));
    }

    private void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed, Map<String, Object> attributes) {
        out.printf("ae \"%s\" \"%s\" %s \"%s\"", edgeId, fromNodeId, directed ? ">" : "", toNodeId);
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                out.printf(" %s", attributeString(entry.getKey(), entry.getValue(), false));
            }
        }
        out.printf("%n");
    }
    
    private void edgeChanged(String edgeId, Map<String, Object> attributes) {
        if (attributes != null && attributes.size() > 0) {
            out.printf("ce \"%s\"", edgeId);
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                out.printf(" \"%s\":\"%s\"", entry.getKey(), entry.getValue());
            }
            out.printf("%n");
        }
    }

    private void edgeRemoved(String edgeId) {
        out.printf("de \"%s\"%n", edgeId);
    }

    private void graphCleared() {
        out.printf("clear%n");
    }

    private void nodeAdded(String nodeId, Map<String, Object> attributes) {
        out.printf("an \"%s\"", nodeId);
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                out.printf(" %s", attributeString(entry.getKey(), entry.getValue(), false));
            }
        }
        out.printf("%n");
    }
    
    private void nodeChanged(String nodeId, Map<String, Object> attributes) {
        if (attributes != null && attributes.size() > 0) {
            out.printf("cn \"%s\"", nodeId);
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                out.printf(" \"%s\":\"%s\"", entry.getKey(), entry.getValue());
            }
            out.printf("%n");
        }
    }

    private void nodeRemoved(String nodeId) {
        out.printf("dn \"%s\"%n", nodeId);
    }

    public void stepBegins(double step) {
        out.printf(Locale.US, "st %f%n", step);
    }
    
 // Utility methods
     
    protected String attributeString(String key, Object value, boolean remove) {
        if (key == null || key.length() == 0) {
            return null;
        }

        if (remove) {
            return String.format(" -\"%s\"", key);
        } else {
            if (value != null && value.getClass().isArray()) {
                Object[] values = (Object[]) value;
                StringBuilder sb = new StringBuilder();

                sb.append(String.format(" \"%s\":", key));

                if (values.length > 0) {
                    sb.append(valueString(values[0]));
                } else {
                    sb.append("\"\"");
                }

                for (int i = 1; i < values.length; ++i) {
                    sb.append(String.format(",%s", valueString(values[i])));
                }

                return sb.toString();
            } else {
                return String.format(" \"%s\":%s", key, valueString(value));
            }
        }
    }
     
    protected String valueString(Object value) {

         if (value instanceof CharSequence)
         {
             return String.format("\"%s\"", (CharSequence) value);
         }
         else if (value instanceof Number)
         {
             if (value instanceof Integer || value instanceof Short || value instanceof Byte || value instanceof Long)
             {
                 return String.format(Locale.US, "%d", ((Number) value).longValue());
             }
             else if (value instanceof Float || value instanceof Double)
             {
                 return String.format(Locale.US, "%f", ((Number) value).doubleValue());
             }
             else if (value instanceof Character)
             {
                 return String.format("\"%c\"", ((Character) value).charValue());
             }
             else if (value instanceof Boolean)
             {
                 return String.format(Locale.US, "\"%b\"", ((Boolean) value));
             }
             else
             {
                 return String.format(Locale.US, " %f", ((Number) value).doubleValue());
             }
         }
         else if (value == null)
         {
             return "\"\"";
         }
         else if (value instanceof Object[])
         {
             Object array[] = (Object[]) value;
             int n = array.length;
             StringBuilder sb = new StringBuilder();

             if (array.length > 0) {
                 sb.append(valueString(array[0]));
             }

             for (int i = 1; i < n; i++)
             {
                 sb.append(",");
                 sb.append(valueString(array[i]));
             }
                 
             return sb.toString();
         }
         else
         {
             return String.format("\"%s\"", value.toString());
         }
     }
     
    protected String hashToString(HashMap<?, ?> hash) {
        StringBuilder sb = new StringBuilder();

        sb.append("[ ");

        for (Object key : hash.keySet()) {
            sb.append(attributeString(key.toString(), hash.get(key), false));
            sb.append(" ");
        }

        sb.append(']');

        return sb.toString();
    }
}
