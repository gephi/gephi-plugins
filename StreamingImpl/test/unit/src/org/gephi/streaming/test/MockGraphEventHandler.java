package org.gephi.streaming.test;

import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.event.GraphEvent;

class MockGraphEventHandler implements GraphEventHandler {

    private int eventCount = 0;

    @Override
    public void handleGraphEvent(GraphEvent event) {
        eventCount++;
    }

    public int getEventCount() {
        return eventCount;
    }
}
