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
package org.gephi.streaming.server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.gephi.streaming.server.Request;

/**
 * @author panisson
 *
 */
public class RequestWrapper implements Request {

    public final static String SOCKET_REFERENCE_KEY = "SOCKET_REFERENCE_KEY";
    
    private org.simpleframework.http.Request request;
    
    public RequestWrapper(org.simpleframework.http.Request request) {
        this.request = request;
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.Request#getInputStream()
     */
    public InputStream getInputStream() throws IOException {
        return request.getInputStream();
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.Request#getParameter(java.lang.String)
     */
    public String getParameter(String arg0) throws IOException {
        return request.getParameter(arg0);
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.Request#getValue(java.lang.String)
     */
    public String getValue(String arg0) {
        return request.getValue(arg0);
    }

    public String getClientAddress() {
        return request.getClientAddress().toString();
    }

    public Map getAttributes() {
        return request.getAttributes();
    }

}
