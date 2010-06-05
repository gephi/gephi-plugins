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

import java.io.IOException;
import java.io.InputStream;

import org.gephi.streaming.api.OperationSupport;
import org.gephi.streaming.api.StreamReader;

/**
 * A stream processor for the GraphStream DSG file format.
 * 
 * @author panisson
 *
 */
public class DGSStreamReader extends StreamReader implements DGSParserListener {
    
    /**
     * @param operator the OperationSupport to which the operations will be delegated
     */
    public DGSStreamReader(OperationSupport operator) {
        super(operator);
    }

    @Override
    public void processStream(InputStream inputStream) {
        
        DGSParser parser = new DGSParser(inputStream, this);
        try {
            parser.parse();
        } catch (IOException e) {
            this.onStreamClosed();
        }

        System.out.println("Stream finished");
    }

    private void onStreamClosed() {
      //TODO
        System.out.println("Stream closed");
    }

    @Override
    public void onEdgeAdded(String graphName, String edgeId, String fromTag,
            String toTag, boolean directed) {
        operator.edgeAdded(edgeId, fromTag, toTag, directed);
    }

    @Override
    public void onEdgeAttributeAdded(String graphName, String tag,
            String attribute, Object value) {
        operator.edgeAttributeAdded(tag, attribute, value);
    }

    @Override
    public void onEdgeAttributeChanged(String graphName, String tag,
            String attribute, Object object, Object value) {
        operator.edgeAttributeChanged(tag, attribute, value);
    }

    @Override
    public void onEdgeAttributeRemoved(String sourceId, String edgeId,
            String attribute) {
        operator.edgeAttributeRemoved(edgeId, attribute);
    }

    @Override
    public void onEdgeRemoved(String sourceId, String edgeId) {
        operator.edgeRemoved(edgeId);
    }

    @Override
    public void onGraphAttributeAdded(String sourceId, String attribute,
            Object value) {
        //TODO
        System.out.println("onGraphAttributeAdded: Not implemented");
    }

    @Override
    public void onGraphAttributeChanged(String sourceId, String attribute,
            Object oldValue, Object newValue) {
      //TODO
        System.out.println("onGraphAttributeChanged: Not implemented");
    }

    @Override
    public void onGraphAttributeRemoved(String sourceId, String attribute) {
      //TODO
        System.out.println("onGraphAttributeRemoved: Not implemented");
    }

    @Override
    public void onNodeAdded(String sourceId, String nodeId) {
        operator.nodeAdded(nodeId);
    }

    @Override
    public void onNodeAttributeAdded(String sourceId, String nodeId,
            String attribute, Object value) {
        operator.nodeAttributeAdded(nodeId, attribute, value);
    }

    @Override
    public void onNodeAttributeChanged(String sourceId, String nodeId,
            String attribute, Object oldValue, Object newValue) {
        operator.nodeAttributeChanged(nodeId, attribute, newValue);
    }

    @Override
    public void onNodeAttributeRemoved(String sourceId, String nodeId,
            String attribute) {
        operator.nodeAttributeRemoved(nodeId, attribute);
    }

    @Override
    public void onNodeRemoved(String sourceId, String nodeId) {
        operator.nodeRemoved(nodeId);
    }

    @Override
    public void onStepBegins(String graphName, double time) {
      //TODO
        System.out.println("onStepBegins: Not implemented");
    }

    @Override
    public String toString() {
        return "DGSStreamProcessor";
    }
}
