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
import java.util.concurrent.atomic.AtomicInteger;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.streaming.api.ContainerLoader;
import org.gephi.streaming.api.GraphEventContainer;
import org.gephi.streaming.api.GraphEventContainerFactory;
import org.gephi.streaming.api.StreamProcessor;
import org.openide.util.Lookup;

/**
 * A stream processor for the GraphStream DSG file format.
 * 
 * @author panisson
 *
 */
public class DGSStreamProcessor implements StreamProcessor, DGSParserListener {
    
    private ContainerLoader containerLoader;
    private GraphEventContainer container;
    private AttributeModel attributeModel;
    
    public DGSStreamProcessor() {
        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        this.attributeModel = attributeController.getModel();
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
    
    public void stop() {
        this.container.stop();
    }

    private void onStreamClosed() {
      //TODO
        System.out.println("Stream closed");
    }

    @Override
    public void onEdgeAdded(String graphName, String edgeId, String fromTag,
            String toTag, boolean directed) {
      containerLoader.edgeAdded(edgeId, fromTag, toTag, directed);
    }

    @Override
    public void onEdgeAttributeAdded(String graphName, String tag,
            String attribute, Object value) {
        containerLoader.edgeAttributeAdded(tag, getEdgeAttributeColumn(attribute), value);
    }

    @Override
    public void onEdgeAttributeChanged(String graphName, String tag,
            String attribute, Object object, Object value) {
        containerLoader.edgeAttributeChanged(tag, getEdgeAttributeColumn(attribute), value);
    }

    @Override
    public void onEdgeAttributeRemoved(String sourceId, String edgeId,
            String attribute) {
        containerLoader.edgeAttributeRemoved(edgeId, getEdgeAttributeColumn(attribute));
    }

    @Override
    public void onEdgeRemoved(String sourceId, String edgeId) {
        containerLoader.edgeRemoved(edgeId);
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
        containerLoader.nodeAdded(nodeId);
    }

    @Override
    public void onNodeAttributeAdded(String sourceId, String nodeId,
            String attribute, Object value) {
        containerLoader.nodeAttributeAdded(nodeId, getNodeAttributeColumn(attribute), value);
    }

    @Override
    public void onNodeAttributeChanged(String sourceId, String nodeId,
            String attribute, Object oldValue, Object newValue) {
        containerLoader.nodeAttributeChanged(nodeId, getNodeAttributeColumn(attribute), newValue);
    }

    @Override
    public void onNodeAttributeRemoved(String sourceId, String nodeId,
            String attribute) {
        containerLoader.nodeAttributeRemoved(nodeId, getNodeAttributeColumn(attribute));
    }

    @Override
    public void onNodeRemoved(String sourceId, String nodeId) {
        containerLoader.nodeRemoved(nodeId);
    }

    @Override
    public void onStepBegins(String graphName, double time) {
      //TODO
        System.out.println("onStepBegins: Not implemented");
    }
    
    private AttributeColumn getEdgeAttributeColumn(String id) {
        AttributeTable edgeTable = attributeModel.getEdgeTable();
        AttributeColumn attributeColumn = edgeTable.getColumn(id);
        if (attributeColumn==null) {
            attributeColumn = edgeTable.addColumn(id, AttributeType.STRING);
        }
        return attributeColumn;
    }
    
    private AttributeColumn getNodeAttributeColumn(String id) {
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeColumn attributeColumn = nodeTable.getColumn(id);
        if (attributeColumn==null) {
            attributeColumn = nodeTable.addColumn(id, AttributeType.STRING);
        }
        return attributeColumn;
    }

    @Override
    public GraphEventContainer getContainer() {
        return container;
    }
    
    @Override
    public void setContainer(GraphEventContainer container) {
        this.container = container;
        this.containerLoader = container.getLoader();
    }

    @Override
    public String toString() {
        return "DGSStreamProcessor";
    }
}
