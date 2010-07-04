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
import java.util.Iterator;

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
			JSONObject o = new JSONObject(content);
			
			String type = (String)o.get(Fields.TYPE.value());
			if (Types.AN.value().equals(type)) {
				operator.nodeAdded(o.getString(Fields.ID.value()));
				
			} else if (Types.CN.value().equals(type)) {
				String id = o.getString(Fields.ID.value());
				Iterator i = o.keys();
				while (i.hasNext()) {
					String key = (String)i.next();
					if (!key.equals(Fields.ID.value()) && !key.equals(Fields.TYPE.value())) {
						Object value = o.get(key);
						operator.nodeAttributeChanged(id, key, value);
					}
					
				}
				
			} else if (Types.DN.value().equals(type)) {
				operator.nodeRemoved(o.getString(Fields.ID.value()));
				
			} else if (Types.AE.value().equals(type)) {
				operator.edgeAdded(o.getString(Fields.ID.value()),
						o.getString(Fields.SOURCE.value()),
						o.getString(Fields.TARGET.value()),
						Boolean.valueOf(o.getString(Fields.DIRECTED.value())));
				
			} else if (Types.CE.value().equals(type)) {
				String id = o.getString(Fields.ID.value());
				Iterator i = o.keys();
				while (i.hasNext()) {
					String key = (String)i.next();
					if (!key.equals(Fields.ID.value()) && !key.equals(Fields.TYPE.value())) {
						Object value = o.get(key);
						operator.edgeAttributeChanged(id, key, value);
					}
					
				}
				
			} else if (Types.DE.value().equals(type)) {
				operator.edgeRemoved(o.getString(Fields.ID.value()));
				
			} else if (Types.CG.value().equals(type)) {
				
			};
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
