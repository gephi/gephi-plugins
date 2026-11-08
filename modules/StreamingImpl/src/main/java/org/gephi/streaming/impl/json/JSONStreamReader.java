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
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.gephi.streaming.impl.FilterFactory;
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
     private FilterFactory filterFactory = new FilterFactory();

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
        
        InputStreamReader reader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));

        try {
            int read;
            while ((read = reader.read())!=-1) {
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
            
            Double t = null;
            if (jo.has(Fields.T.value())) {
                String tstr = jo.getString(Fields.T.value());
                t = Double.valueOf(tstr);
            }
            
            Iterator<String> keys = jo.keys();
            
            while (keys.hasNext()) {
                String key = keys.next();
                if (Fields.ID.value().equals(key)) continue;
                if (Fields.T.value().equals(key)) continue;
                
                Object gObjs = jo.get(key);
                if (gObjs instanceof JSONObject) {
                    parse(key, (JSONObject)gObjs, id, t);
                } else {
                    throw new IllegalArgumentException("Invalid attribute: "+key);
                    //logger.log(Level.WARNING, "JSON attribute ignored: \"{0}\"", new String[]{key});
                }
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
    
    private void parse(String type, JSONObject gObjs, String eventId, Double t) throws JSONException {

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

                boolean directed = true;
                if (gObj.has(Fields.DIRECTED.value())) {
                    directed = Boolean.valueOf(gObj.getString(Fields.DIRECTED.value()));
                }

                event = eventBuilder.edgeAddedEvent(id,
                        gObj.getString(Fields.SOURCE.value()),
                        gObj.getString(Fields.TARGET.value()),
                        directed, attributes);

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
                if (t!=null) {
                    event.setTimestamp(t);
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
            return filterFactory.getFilter(elementType, filterName, null);
        } else {
            JSONObject filterObj = (JSONObject)filter;
            String filterName = filterObj.keys().next();
            JSONObject filterAttr = filterObj.getJSONObject(filterName);
            return filterFactory.getFilter(elementType, filterName, readAttributes(filterAttr));
        }
    }
}
