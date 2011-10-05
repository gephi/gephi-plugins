/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.streaming.impl.dgs;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Date;
import java.util.Map;

import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEventBuilder;

/**
 * A stream reader for the GraphStream DSG file format.
 * 
 * @author panisson
 *
 */
public class DGSStreamReader extends StreamReader implements DGSParserListener {
    
    /**
     * @param handler the GraphEventHandler to which the events will be delegated
     */
    public DGSStreamReader(GraphEventHandler handler,
            GraphEventBuilder eventBuilder) {
        super(handler, eventBuilder);
    }

    @Override
    public void processStream(InputStream inputStream) {
        
        DGSParser parser = new DGSParser(inputStream, this, report, listener);
        try {
            parser.parse();
        } catch (IOException e) {
        } finally {
            if (report!=null)
                report.log("Stream closed at "+new Date());
        }

        if (listener!=null) {
            listener.onStreamClosed();
        }
    }

    @Override
    public void onEdgeAdded(String graphName, String edgeId, String fromTag,
            String toTag, boolean directed, Map<String, Object>  attributes) {
        handler.handleGraphEvent(eventBuilder.edgeAddedEvent(edgeId, fromTag, toTag, directed, attributes));
        if (report!=null)
            report.incrementEventCounter();
    }
    
    @Override
    public void onEdgeChanged(String sourceId, String edgeId, Map<String, Object> attributes) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.EDGE, EventType.CHANGE, edgeId, attributes));
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public void onEdgeRemoved(String sourceId, String edgeId) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.EDGE, EventType.REMOVE, edgeId, null));
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public void onGraphChanged(Map<String, Object> attributes) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.GRAPH, EventType.CHANGE, null, attributes));
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public void onNodeAdded(String sourceId, String nodeId, Map<String, Object> attributes) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.ADD, nodeId, attributes));
        if (report!=null)
            report.incrementEventCounter();
    }
    
    @Override
    public void onNodeChanged(String sourceId, String nodeId, Map<String, Object> attributes) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.CHANGE, nodeId, attributes));
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public void onNodeRemoved(String sourceId, String nodeId) {
        handler.handleGraphEvent(eventBuilder.graphEvent(ElementType.NODE, EventType.REMOVE, nodeId, null));
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public void onStepBegins(String graphName, double time) {
      //TODO
        System.out.println("onStepBegins: Not implemented");
        if (report!=null)
            report.incrementEventCounter();
    }

    @Override
    public String toString() {
        return "DGSStreamProcessor";
    }

    @Override
    public void processStream(ReadableByteChannel channel) throws IOException {
        this.processStream(Channels.newInputStream(channel));
    }
}
