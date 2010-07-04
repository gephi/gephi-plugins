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

// Attribute
    
    /**
     * A shortcut to the output.
     */
    protected PrintStream out;
    
    protected String graphName = "";
    
// Command
    
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
						.put(Fields.TYPE.value(), Types.CE.value())
						.put(Fields.ID.value(), edgeId)
						.put(Fields.ATTRIBUTE.value(), attribute)
						.put(Fields.VALUE.value(), newValue)
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
						.put(Fields.TYPE.value(), Types.CE.value())
						.put(Fields.ID.value(), edgeId)
						.put(Fields.ATTRIBUTE.value(), attribute)
						.put(Fields.VALUE.value(), "NULL")
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
						.put(Fields.TYPE.value(), Types.CG.value())
						.put(Fields.ATTRIBUTE.value(), attribute)
						.put(Fields.VALUE.value(), newValue)
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
						.put(Fields.TYPE.value(), Types.CG.value())
						.put(Fields.ATTRIBUTE.value(), attribute)
						.put(Fields.VALUE.value(), "NULL")
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
						.put(Fields.TYPE.value(), Types.CN.value())
						.put(Fields.ID.value(), nodeId)
						.put(Fields.ATTRIBUTE.value(), attribute)
						.put(Fields.VALUE.value(), newValue)
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
						.put(Fields.TYPE.value(), Types.CN.value())
						.put(Fields.ID.value(), nodeId)
						.put(Fields.ATTRIBUTE.value(), attribute)
						.put(Fields.VALUE.value(), "NULL")
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
						.put(Fields.TYPE.value(), Types.AE.value())
						.put(Fields.ID.value(), edgeId)
						.put(Fields.SOURCE.value(), fromNodeId)
						.put(Fields.TARGET.value(), toNodeId)
						.put(Fields.DIRECTED.value(), directed)
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
						.put(Fields.TYPE.value(), Types.DE.value())
						.put(Fields.ID.value(), edgeId)
						.toString() + '\r');
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void nodeAdded( String nodeId )
    {
    	try {
			out.print(
					new JSONObject()
						.put(Fields.TYPE.value(), Types.AN.value())
						.put(Fields.ID.value(), nodeId)
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
						.put(Fields.TYPE.value(), Types.DN.value())
						.put(Fields.ID.value(), nodeId)
						.toString() + '\r');
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
