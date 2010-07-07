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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.gephi.streaming.api.OperationSupport;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.impl.json.parser.JSONException;
import org.gephi.streaming.impl.json.parser.JSONObject;
import org.gephi.streaming.impl.json.parser.JSONConstants.Fields;
import org.gephi.streaming.impl.json.parser.JSONConstants.Types;

/**
 * A stream processor for the GraphStream JSON file format.
 * 
 * @author panisson
 *
 */
public class JSONStreamReader extends StreamReader {

    /**
     * @param operator the OperationSupport to which the operations will be delegated
     */
    public JSONStreamReader(OperationSupport operator) {
        super(operator);
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
            System.out.println("Stream closed");
        }

        System.out.println("Stream finished");
    }

    @Override
    public String toString() {
        return "JSONStreamProcessor";
    }

    private void parse(String content) {
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

                    operator.nodeAdded(id, attributes);
                }

            } else if (Types.CN.value().equals(type)) {

                Iterator i = gObjs.keys();
                while (i.hasNext()) {
                    String id = (String)i.next();
                    JSONObject gObj = (JSONObject)gObjs.get(id);

                    Iterator i2 = gObj.keys();
                    while (i2.hasNext()) {
                        String key = (String)i2.next();
                        Object value = gObj.get(key);
                        operator.nodeAttributeChanged(id, key, value);
                    }
                }

            } else if (Types.DN.value().equals(type)) {
                Iterator i = gObjs.keys();
                while (i.hasNext()) {
                    String id = (String)i.next();
                    operator.nodeRemoved(id);
                }

            } else if (Types.AE.value().equals(type)) {
                Iterator i = gObjs.keys();
                while (i.hasNext()) {
                    String id = (String)i.next();
                    JSONObject gObj = (JSONObject)gObjs.get(id);
                    operator.edgeAdded(id,
                            gObj.getString(Fields.SOURCE.value()),
                            gObj.getString(Fields.TARGET.value()),
                            Boolean.valueOf(gObj.getString(Fields.DIRECTED.value())));

                    Iterator i2 = gObj.keys();
                    while (i2.hasNext()) {
                        String key = (String)i2.next();
                        if (!key.equals(Fields.SOURCE.value()) 
                                && !key.equals(Fields.TARGET.value()) 
                                && !key.equals(Fields.DIRECTED.value())) {
                            Object value = gObj.get(key);
                            operator.edgeAttributeChanged(id, key, value);
                        }
                    }
                }

            } else if (Types.CE.value().equals(type)) {

                Iterator i = gObjs.keys();
                while (i.hasNext()) {
                    String id = (String)i.next();
                    JSONObject gObj = (JSONObject)gObjs.get(id);

                    Iterator i2 = gObj.keys();
                    while (i2.hasNext()) {
                        String key = (String)i2.next();
                        Object value = gObj.get(key);
                        operator.edgeAttributeChanged(id, key, value);
                    }
                }

            } else if (Types.DE.value().equals(type)) {
                Iterator i = gObjs.keys();
                while (i.hasNext()) {
                    String id = (String)i.next();
                    operator.edgeRemoved(id);
                }

            } else if (Types.CG.value().equals(type)) {

            };
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
