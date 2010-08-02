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

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.filters.spi.EdgeFilter;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.filters.spi.NodeFilter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.Issue;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.FilterEvent;
import org.gephi.streaming.api.event.GraphEvent;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.gephi.streaming.impl.json.parser.JSONException;
import org.gephi.streaming.impl.json.parser.JSONObject;
import org.gephi.streaming.impl.json.parser.JSONConstants.Fields;
import org.gephi.streaming.impl.json.parser.JSONConstants.Types;

/**
 * A stream reader for the GraphStream JSON file format.
 * 
 * @author panisson
 *
 */
public class JSONStreamReader extends StreamReader {

     private static final Logger logger =  Logger.getLogger(JSONStreamReader.class.getName());

    /**
     * @param handler the GraphEventHandler to which the events will be delegated
     * @param eventBuilder 
     */
    public JSONStreamReader(GraphEventHandler handler,
            GraphEventBuilder eventBuilder) {
        super(handler, eventBuilder);
    }

    @Override
    public void processStream(InputStream inputStream) throws IOException {

//        this.processStream(Channels.newChannel(inputStream), listener);

         StringBuilder content = new StringBuilder(1024);

        try {
            int read;
            while ((read = inputStream.read())!=-1) {
                char readChar = (char)read;
                if (readChar == '\r') {
                    if (listener!=null) {
                        listener.onDataReceived();
                    }
                    parse(content.toString());
                    content.setLength(0);
                } else {
                    content.append(readChar);
                }
            }
        } catch (IOException e) {
        } finally {
            if (report!=null)
                report.log("Stream closed at "+new Date());
        }




        if (content.length() > 0) {
            parse(content.toString());
        }

        if (listener!=null) {
            listener.onStreamClosed();
        }

    }

    @Override
    public void processStream(ReadableByteChannel channel) throws IOException {

        this.processStream(Channels.newInputStream(channel));

//        StringBuilder content = new StringBuilder();

//        ByteBuffer buffer = ByteBuffer.allocate(4096);
//        CharsetDecoder decoder = Charset.defaultCharset().newDecoder();
//
//        try {
//            int read;
//            while ((read = channel.read(buffer))!=-1) {
//
//                buffer.flip();
//
//                if (listener!=null) {
//                    listener.onDataReceived();
//                }
//                CharBuffer charbuffer = decoder.decode(buffer);
//                content.append(charbuffer.toString());
//
//                int pos;
//                while ((pos=content.indexOf("\r")) > 0) {
//                    parse(content.substring(0, pos+1));
//                    content.delete(0, pos+1);
//                }
//
//                buffer.clear();
//            }
//        } catch (IOException e) {
//        } finally {
//            if (report!=null)
//                report.log("Stream closed at "+new Date());
//        }
//
//        if (content.length() > 0) {
//            parse(content.toString());
//        }
//
//        if (listener!=null) {
//            listener.onStreamClosed();
//        }
    }

    @Override
    public String toString() {
        return "JSONStreamProcessor";
    }

    private void parse(String content) {
        content = content.trim();
        if (content.length() == 0) return;
        
        try {
            JSONObject jo = new JSONObject(content);
            
            String id = null;
            if (jo.has(Fields.ID.value())) {
                id = jo.getString(Fields.ID.value());
            }
            
            Iterator<String> keys = jo.keys();
            
            while (keys.hasNext()) {
                String key = keys.next();
                if (Fields.ID.value().equals(key)) continue;
                JSONObject gObjs = (JSONObject)jo.get(key);
                parse(key, gObjs, id);
            }
            
            if (report!=null) {
                report.incrementEventCounter();
            }
        } catch (JSONException e) {

            if (listener!=null) {
                listener.onError();
            }

            if (report!=null) {
                StringBuilder message = new StringBuilder("JSON object ");
                message.append(report.getEventCounter()+1)
                        .append(" ignored: \"")
                        .append(content)
                        .append("\": ")
                        .append(e.getMessage());

                Issue issue = new Issue(message.toString(), Issue.Level.WARNING, e);
                report.logIssue(issue);
            }
            logger.log(Level.WARNING, "JSON object ignored: \"{0}\": {1}", new String[]{content, e.getMessage()});
        }
    }
    
    private void parse(String type, JSONObject gObjs, String eventId) throws JSONException {

        Types eventType = Types.fromString(type);
        
        if (gObjs.has("filter")) {

            Map<String, Object> attributes = null;
            if (gObjs.has("attributes")) {
                JSONObject attrObj = gObjs.getJSONObject("attributes");
                attributes = readAttributes(attrObj);
            }
            
            handler.handleGraphEvent(
                    new FilterEvent(this, eventType.getEventType(),
                    eventType.getElementType(), getFilter(eventType.getElementType(), gObjs), attributes));
            return;
        }

        if (eventType.equals(Types.CG)) {
            Map<String, Object> attributes = readAttributes(gObjs);
            handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.GRAPH, EventType.CHANGE,
                    null, attributes));
            return;
        }
        
        Iterator<String> it = gObjs.keys();
        while (it.hasNext()) {
            GraphEvent event = null;
            String id = it.next();

            if (eventType.equals(Types.AN)) {
                JSONObject gObj = (JSONObject)gObjs.get(id);
                Map<String, Object> attributes = readAttributes(gObj);
                event = eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, id, attributes);

            } else if (eventType.equals(Types.CN)) {
                JSONObject gObj = (JSONObject)gObjs.get(id);
                Map<String, Object> attributes = readAttributes(gObj);
                event = eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, id, attributes);

            } else if (eventType.equals(Types.DN)) {
                event = eventBuilder.graphEvent(ElementType.NODE, EventType.REMOVE, id, null);

            } else if (eventType.equals(Types.AE)) {
                Map<String, Object> attributes = new HashMap<String, Object>();
                JSONObject gObj = (JSONObject)gObjs.get(id);
                Iterator<String> i2 = gObj.keys();
                while (i2.hasNext()) {
                    String key = i2.next();
                    if (!key.equals(Fields.SOURCE.value())
                            && !key.equals(Fields.TARGET.value())
                            && !key.equals(Fields.DIRECTED.value())) {
                        Object value = gObj.get(key);
                        attributes.put(key, value);
                    }
                }
                event = eventBuilder.edgeAddedEvent(id,
                        gObj.getString(Fields.SOURCE.value()),
                        gObj.getString(Fields.TARGET.value()),
                        Boolean.valueOf(gObj.getString(Fields.DIRECTED.value())), attributes);

            } else if (eventType.equals(Types.CE)) {
                JSONObject gObj = (JSONObject)gObjs.get(id);
                Map<String, Object> attributes = readAttributes(gObj);
                event = eventBuilder.graphEvent(ElementType.EDGE, EventType.CHANGE, id, attributes);

            } else if (eventType.equals(Types.DE)) {
                event = eventBuilder.graphEvent(ElementType.EDGE, EventType.REMOVE, id, null);

            }

            if (event != null) {
                if (eventId!=null) {
                    event.setEventId(eventId);
                }
                handler.handleGraphEvent(event);
            }
        }
    }

    private Map<String, Object> readAttributes(JSONObject gObj) throws JSONException {
        Map<String, Object> attributes = new HashMap<String, Object>();
        Iterator<String> it = gObj.keys();
        while (it.hasNext()) {
            String key = it.next();
            Object value = gObj.get(key);
            attributes.put(key, value);
        }
        return attributes;
    }

    private Filter getFilter(ElementType elementType, JSONObject gObjs) throws JSONException{
        Object filter = gObjs.get("filter");

        if (filter instanceof String) {
            String filterName = (String)filter;
            if (filterName.equalsIgnoreCase("ALL")) {
                if (elementType.equals(ElementType.NODE)) {
                    return new NodeFilter() {

                        @Override
                        public boolean init(Graph graph) {
                            return true;
                        }

                        @Override
                        public boolean evaluate(Graph graph, Node node) {
                            return true;
                        }

                        @Override
                        public void finish() {
                        }

                        @Override
                        public String getName() {
                            return "AllNodes";
                        }

                        @Override
                        public FilterProperty[] getProperties() {
                            return null;
                        }
                    };
                } else if (elementType.equals(ElementType.EDGE)) {
                    return new EdgeFilter() {

                        @Override
                        public boolean init(Graph graph) {
                            return true;
                        }

                        @Override
                        public boolean evaluate(Graph graph, Edge edge) {
                            return true;
                        }

                        @Override
                        public void finish() {
                        }

                        @Override
                        public String getName() {
                            return "AllEdges";
                        }

                        @Override
                        public FilterProperty[] getProperties() {
                            return null;
                        }
                    };
                }
            }
        }
        return null;
    }
}
