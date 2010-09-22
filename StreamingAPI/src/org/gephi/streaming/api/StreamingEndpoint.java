/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.gephi.streaming.api;

import java.io.Serializable;
import java.net.URL;

/**
 * A streaming endpoint, with the information required to connect to a stream
 * and process it.
 *
 * @author Andre' Panisson
 */
public class StreamingEndpoint implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private URL url;
    private StreamType streamType;
    private String user;
    private String password;

    /**
     * Create a new StreamingEndpoint with no information on it
     */
    public StreamingEndpoint() {}

    /**
     * Create a new StreamingEndpoint setting its URL and streamType
     * @param url - the URL to set
     * @param streamType - the streamType to set
     */
    public StreamingEndpoint(URL url, StreamType streamType) {
        this.url = url;
        this.streamType = streamType;
    }

    /**
     * Get the URL to connect to
     * @return the URL to connect to
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets the URL to connect to
     * @param url the URL to connect to
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Return the stream type
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

    /**
     * Return the user to be used in case of authenticated connection
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the user to be used in case of authenticated connection
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Return the password to be used in case of authenticated connection
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password to be used in case of authenticated connection
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
