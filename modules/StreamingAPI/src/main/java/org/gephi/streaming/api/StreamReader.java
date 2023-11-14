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
package org.gephi.streaming.api;

import org.gephi.streaming.api.event.GraphEventBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

/**
 * Read events from a stream.
 * An implementation of this class should read information
 * from the InputStream, create the appropriate GraphEvent
 * and send it to the GraphEventHandler handler.
 *
 * @author Andre' Panisson
 */
public abstract class StreamReader {

    /**
     * The handler that will handle events
     */
    protected final GraphEventHandler handler;
    /**
     * The event builder to create graph events
     */
    protected final GraphEventBuilder eventBuilder;
    /**
     * The report to add useful information
     */
    protected Report report;
    /**
     * A listener to receive status notifications
     */
    protected StreamReaderStatusListener listener;
    
    /**
     * Create a new StreamReader that will send to the handler
     * the events created using the event builder.
     *
     * @param handler the GraphEventHandler to which the events will be delegated
     */
    public StreamReader(GraphEventHandler handler,
            GraphEventBuilder eventBuilder) {
        this.handler = handler;
        this.eventBuilder = eventBuilder;
    }

    /**
     * Used to get the Report where information is being added.
     *
     * @return the report
     */
    public Report getReport() {
        return report;
    }

    /**
     * Used to set the Report where information will be added.
     *
     * @param report the report to set
     */
    public void setReport(Report report) {
        this.report = report;
    }

    /**
     * Read from the InputStream and send the appropriate event
     * to the GraphEventHandler
     * 
     * @param inputStream the InputStream to read from.
     * @throws IOException when unable to connect to the InputStream
     */
    public abstract void processStream(InputStream inputStream) throws IOException;

    /**
     * Read from the channel and send the appropriate event
     * to the GraphEventHandler
     *
     * @param channel the ReadableByteChannel to read from.
     * @throws IOException when unable to connect to the InputStream
     */
    public abstract void processStream(ReadableByteChannel channel) throws IOException;

    /**
     * Set a listener to asynchronously receive status notifications.
     * @param listener the listener to be notifiedConnection
     */
    public void setStatusListener(StreamReaderStatusListener listener) {
        this.listener = listener;
    }

    /**
     * This is the listener interface to asynchronously receive status notifications.
     * It should be registered using setStatusListener().
     */
    public interface StreamReaderStatusListener {
        /**
         * Called when the stream is closed
         */
        public void onStreamClosed();
        /**
         * Called when data is received
         */
        public void onDataReceived();
        /**
         * Called when error occurs
         */
        public void onError();
    }

}
