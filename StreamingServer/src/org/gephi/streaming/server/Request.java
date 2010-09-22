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
package org.gephi.streaming.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * The Request is used to provide an interface to the 
 * HTTP entity body and message header. 
 * This provides methods that allow the entity body 
 * to be acquired as a stream.
 * 
 * @author panisson
 *
 */
public interface Request {

    /**
     * This is used to read the content body.
     * 
     * @return this returns an input stream containing the message body
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException;

    /**
     * This is used to provide quick access to the parameters.
     * 
     * @param name - this is the name of the parameter value
     * @return the parameter value for the given name
     * @throws IOException
     */
    public String getParameter(String name) throws IOException;

    /**
     * This can be used to get the value of the 
     * first message header that has the specified name.
     * 
     * @param name - the HTTP message header to get the value from
     * @return this returns the value that the HTTP message header
     */
    public String getValue(String name);

    /**
     *
     * @return the String representation of the client address
     */
    public String getClientAddress();

    public Map getAttributes();

}