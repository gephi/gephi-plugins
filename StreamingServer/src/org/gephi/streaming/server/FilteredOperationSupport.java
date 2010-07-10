/**
 * 
 */
package org.gephi.streaming.server;

import java.util.Map;
import java.util.Set;

import org.gephi.streaming.api.AbstractOperationSupport;
import org.gephi.streaming.api.OperationSupport;
import org.gephi.streaming.api.event.EdgeAddedEvent;
import org.gephi.streaming.api.event.ElementAttributeEvent;
import org.gephi.streaming.api.event.ElementEvent;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEvent;

/**
 * @author panisson
 *
 */
public class FilteredOperationSupport extends AbstractOperationSupport {

    private Set<GraphEvent> filteredEvents;
    private OperationSupport operationSupport;
    private Object source = this;

    public FilteredOperationSupport(OperationSupport operationSupport, Set<GraphEvent> filteredEvents) {
        this.operationSupport = operationSupport;
        this.filteredEvents = filteredEvents;
    }

    @Override
    public void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed, Map<String, Object> attributes) {
        EdgeAddedEvent event = new EdgeAddedEvent(this, edgeId, fromNodeId, toNodeId, directed, attributes);
        if(!filteredEvents.contains(event))
            operationSupport.edgeAdded(edgeId, fromNodeId, toNodeId, directed, attributes);
    }
    
    @Override
    public void edgeChanged(String edgeId, Map<String, Object> attributes) {
        ElementEvent event = new ElementEvent(source, EventType.CHANGE, ElementType.EDGE, edgeId, attributes);
        if(!filteredEvents.contains(event))
            operationSupport.nodeAdded(edgeId, attributes);
    }

    @Override
    public void edgeAttributeAdded(String edgeId, String attributeName,
            Object value) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.ADD, ElementType.EDGE, edgeId, attributeName, value);
        if(!filteredEvents.contains(event))
            operationSupport.edgeAttributeAdded(edgeId, attributeName, value);
    }

    @Override
    public void edgeAttributeChanged(String edgeId, String attributeName,
            Object newValue) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.CHANGE, ElementType.EDGE, edgeId, attributeName, newValue);
        if(!filteredEvents.contains(event))
            operationSupport.edgeAttributeChanged(edgeId, attributeName, newValue);
    }

    @Override
    public void edgeAttributeRemoved(String edgeId, String attributeName) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.REMOVE, ElementType.EDGE, edgeId, attributeName, null);
        if(!filteredEvents.contains(event))
            operationSupport.edgeAttributeRemoved(edgeId, attributeName);
    }

    @Override
    public void edgeRemoved(String edgeId) {
        ElementEvent event = new ElementEvent(source, EventType.REMOVE, ElementType.EDGE, edgeId, null);
        if(!filteredEvents.contains(event))
            operationSupport.edgeRemoved(edgeId);
    }

    @Override
    public void graphAttributeAdded(String attributeName, Object value) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.ADD, ElementType.GRAPH, null, attributeName, value);
        if(!filteredEvents.contains(event))
            operationSupport.graphAttributeAdded(attributeName, value);
    }

    @Override
    public void graphAttributeChanged(String attributeName, Object newValue) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.CHANGE, ElementType.GRAPH, null, attributeName, newValue);
        if(!filteredEvents.contains(event))
            operationSupport.graphAttributeChanged(attributeName, newValue);
    }

    @Override
    public void graphAttributeRemoved(String attributeName) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.CHANGE, ElementType.GRAPH, null, attributeName, null);
        if(!filteredEvents.contains(event))
            operationSupport.graphAttributeRemoved(attributeName);
    }

    @Override
    public void nodeAdded(String nodeId, Map<String, Object> attributes) {
        ElementEvent event = new ElementEvent(source, EventType.ADD, ElementType.NODE, nodeId, attributes);
        if(!filteredEvents.contains(event))
            operationSupport.nodeAdded(nodeId, attributes);
    }
    
    @Override
    public void nodeChanged(String nodeId, Map<String, Object> attributes) {
        ElementEvent event = new ElementEvent(source, EventType.CHANGE, ElementType.NODE, nodeId, attributes);
        if(!filteredEvents.contains(event))
            operationSupport.nodeAdded(nodeId, attributes);
    }

    @Override
    public void nodeAttributeAdded(String nodeId, String attributeName,
            Object value) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.ADD, ElementType.NODE, nodeId, attributeName, value);
        if(!filteredEvents.contains(event))
            operationSupport.nodeAttributeAdded(nodeId, attributeName, value);
    }

    @Override
    public void nodeAttributeChanged(String nodeId, String attributeName,
            Object newValue) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.CHANGE, ElementType.NODE, nodeId, attributeName, newValue);
        if(!filteredEvents.contains(event))
            operationSupport.nodeAttributeChanged(nodeId, attributeName, newValue);
    }

    @Override
    public void nodeAttributeRemoved(String nodeId, String attributeName) {
        ElementAttributeEvent event = new ElementAttributeEvent(source, EventType.REMOVE, ElementType.NODE, nodeId, attributeName, null);
        if(!filteredEvents.contains(event))
            operationSupport.nodeAttributeRemoved(nodeId, attributeName);
    }

    @Override
    public void nodeRemoved(String nodeId) {
        ElementEvent event = new ElementEvent(source, EventType.REMOVE, ElementType.NODE, nodeId, null);
        if(!filteredEvents.contains(event))
            operationSupport.nodeRemoved(nodeId);
    }

}
