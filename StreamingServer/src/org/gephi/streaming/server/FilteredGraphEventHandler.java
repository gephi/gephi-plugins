/**
 * 
 */
package org.gephi.streaming.server;

import java.util.Set;

import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.event.GraphEvent;

/**
 * @author panisson
 *
 */
public class FilteredGraphEventHandler implements GraphEventHandler {

    private Set<GraphEvent> filteredEvents;
    private GraphEventHandler operationSupport;
    private Object source = this;

    public FilteredGraphEventHandler(GraphEventHandler operationSupport, Set<GraphEvent> filteredEvents) {
        this.operationSupport = operationSupport;
        this.filteredEvents = filteredEvents;
    }

    @Override
    public void handleGraphEvent(GraphEvent event) {
        if(!filteredEvents.contains(event))
            operationSupport.handleGraphEvent(event);
    }

}
