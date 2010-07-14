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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.Issue;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
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
     */
    public JSONStreamReader(GraphEventHandler handler,
            GraphEventBuilder eventBuilder) {
        super(handler, eventBuilder);
    }

    @Override
    public void processStream(InputStream inputStream) throws IOException {

        StringBuilder content = new StringBuilder();

        try {
            int read;
            while ((read = inputStream.read())!=-1) {
                char readChar = (char)read;
                if (readChar == '\r') {
                    parse(content.toString());
                    content.setLength(0);
                } else {
                    content.append(readChar);
                }
            }
        } catch (IOException e) {
            if (report!=null)
                report.log("Stream closed at "+new Date());
        }

        if (content.length() > 0) {
            parse(content.toString());
        }
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
            String type = (String)jo.keys().next();
            JSONObject gObjs = (JSONObject)jo.get(type);

            if (Types.AN.value().equals(type)) {
                Iterator i = gObjs.keys();
                while (i.hasNext()) {
                    String id = (String)i.next();

                    Map<String, Object> attributes = new HashMap<String, Object>();
                    JSONObject gObj = (JSONObject)gObjs.get(id);
                    Iterator i2 = gObj.keys();
                    while (i2.hasNext()) {
                        String key = (String)i2.next();
                        Object value = gObj.get(key);
                        attributes.put(key, value);
                    }

                    handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, id, attributes));
                }

            } else if (Types.CN.value().equals(type)) {

                Iterator i = gObjs.keys();
                while (i.hasNext()) {
                    String id = (String)i.next();
                    
                    Map<String, Object> attributes = new HashMap<String, Object>();
                    JSONObject gObj = (JSONObject)gObjs.get(id);
                    Iterator i2 = gObj.keys();
                    while (i2.hasNext()) {
                        String key = (String)i2.next();
                        Object value = gObj.get(key);
                        attributes.put(key, value);
                    }

                    handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, id, attributes));
                }

            } else if (Types.DN.value().equals(type)) {
                Iterator i = gObjs.keys();
                while (i.hasNext()) {
                    String id = (String)i.next();
                    handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.REMOVE, id, null));
                }

            } else if (Types.AE.value().equals(type)) {
                Iterator i = gObjs.keys();
                while (i.hasNext()) {
                    String id = (String)i.next();
                    
                    Map<String, Object> attributes = new HashMap<String, Object>();
                    JSONObject gObj = (JSONObject)gObjs.get(id);
                    Iterator i2 = gObj.keys();
                    while (i2.hasNext()) {
                        String key = (String)i2.next();
                        if (!key.equals(Fields.SOURCE.value()) 
                                && !key.equals(Fields.TARGET.value()) 
                                && !key.equals(Fields.DIRECTED.value())) {
                            Object value = gObj.get(key);
                            attributes.put(key, value);
                        }
                    }
                    
                    handler.handleGraphEvent(eventBuilder.edgeAddedEvent(id,
                            gObj.getString(Fields.SOURCE.value()),
                            gObj.getString(Fields.TARGET.value()),
                            Boolean.valueOf(gObj.getString(Fields.DIRECTED.value())), attributes));
                }

            } else if (Types.CE.value().equals(type)) {

                Iterator i = gObjs.keys();
                while (i.hasNext()) {
                    String id = (String)i.next();
                    
                    Map<String, Object> attributes = new HashMap<String, Object>();
                    JSONObject gObj = (JSONObject)gObjs.get(id);
                    Iterator i2 = gObj.keys();
                    while (i2.hasNext()) {
                        String key = (String)i2.next();
                        Object value = gObj.get(key);
                        attributes.put(key, value);
                    }

                    handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.EDGE, EventType.CHANGE, id, attributes));
                }

            } else if (Types.DE.value().equals(type)) {
                Iterator i = gObjs.keys();
                while (i.hasNext()) {
                    String id = (String)i.next();
                    handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.EDGE, EventType.REMOVE, id, null));
                }

            } else if (Types.CG.value().equals(type)) {

            }

            if (report!=null) {
                report.incrementEventCounter();
            }
        } catch (JSONException e) {
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
}
