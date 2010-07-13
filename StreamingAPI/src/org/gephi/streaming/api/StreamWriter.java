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
package org.gephi.streaming.api;

import java.io.OutputStream;

/**
 * A sub-type of GraphEventHandler that writes to an OutputStream
 * when an event arrives. Can be used to implement 
 * stream exporters or to expose the events to a socket
 * in a specific format.
 * 
 * @author Andre' Panisson
 *
 */
public abstract class StreamWriter implements GraphEventHandler {
    
    protected final OutputStream outputStream;
    
    /**
     * Sets the OutputStream to write to
     * 
     * @param outputStream
     */
    public StreamWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
    
    /**
     * @return the OutputStream to write to
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Called before start processing the events (to write 
     * headers, etc.)
     */
    public abstract void startStream();

    /**
     * Called after end processing the events (to write 
     * footers, etc.)
     */
    public abstract void endStream();
}
