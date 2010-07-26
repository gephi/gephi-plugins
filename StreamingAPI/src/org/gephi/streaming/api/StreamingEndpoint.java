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

import java.io.Serializable;
import java.net.URL;

/**
 * A streaming endpoint, with the information required to connect to it
 * and process it.
 *
 * @author Andre' Panisson
 */
public class StreamingEndpoint implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private URL url;
    private StreamType streamType;

    public StreamingEndpoint() {}

    public StreamingEndpoint(URL url, StreamType streamType) {
        this.url = url;
        this.streamType = streamType;
    }

    /**
     * @return the URL to connect to
     */
    public URL getUrl() {
        return url;
    }

    /**
     * sets the URL to connect to
     * @param url the URL to connect to
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * @return the stream type
     */
    public StreamType getStreamType() {
        return streamType;
    }

    /**
     * Sets the stream type
     * 
     * @param streamType the stream type
     */
    public void setStreamType(StreamType streamType) {
        this.streamType = streamType;
    }

}
