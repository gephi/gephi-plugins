/**
 * 
 */
package org.gephi.streaming.server;

import java.util.Map;
import java.util.Set;

import org.gephi.streaming.api.AbstractOperationSupport;
import org.gephi.streaming.api.OperationSupport;
import org.gephi.streaming.api.event.EdgeAddedEvent;
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
    public void edgeRemoved(String edgeId) {
        ElementEvent event = new ElementEvent(source, EventType.REMOVE, ElementType.EDGE, edgeId, null);
        if(!filteredEvents.contains(event))
            operationSupport.edgeRemoved(edgeId);
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
    public void nodeRemoved(String nodeId) {
        ElementEvent event = new ElementEvent(source, EventType.REMOVE, ElementType.NODE, nodeId, null);
        if(!filteredEvents.contains(event))
            operationSupport.nodeRemoved(nodeId);
    }

}
