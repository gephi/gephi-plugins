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
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This is used to represent the HTTP response. 
 * This provides methods that can be used to set various characteristics 
 * of the response. An OutputStream can be acquired via this interface 
 * which can be used to write the response body.
 * 
 * @author panisson
 *
 */
public interface Response {
    
    /**
     * Used to write a message body with the Response. 
     * The semantics of this OutputStream will be determined 
     * by the HTTP version of the client, and whether or not 
     * the content length has been set, through the setContentLength method. 
     * If the length of the output is not known then the output 
     * is chunked for HTTP/1.1 clients and closed for HTTP/1.0 clients.
     * 
     * @return an output stream object with the specified semantics
     * @throws IOException
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * This method is provided for convenience so that the HTTP content 
     * can be written using the print methods provided by the PrintStream. 
     * 
     * @return a print stream that provides convenience writing
     * @throws IOException
     */
    public PrintStream getPrintStream() throws IOException;

    /**
     * This is used to close the connection and commit the request. 
     * This provides the same semantics as closing the output stream 
     * and ensures that the HTTP response is committed. 
     * This will throw an exception if the response can not be committed.
     * 
     * @throws IOException thrown if there is a problem writing
     */
    public void close() throws IOException;

    /**
     * This method allows the status for the response to be changed. 
     * This MUST be reflected the the response content given to the client. 
     * For a description of the codes see RFC 2616 section 10, 
     * Status Code Definitions.
     * 
     * @param code - the new status code for the HTTP response
     */
    public void setCode(int code);

    /**
     * This is used to set the text of the HTTP status line.
     * This should match the status code specified by the RFC.
     * 
     * @param text - the descriptive text message of the status
     */
    public void setText(String text);

    /**
     * This can be used to add a HTTP message header to this object. 
     * The name and value of the HTTP message header will be used 
     * to create a HTTP message header object which can be retrieved 
     * using the getValue in combination with the get methods.
     * 
     * @param name - the name of the HTTP message header to be added
     * @param value - the value the HTTP message header will have
     */
    public void add(String name, String value);

    /**
     * This is used as a convenience method for adding a header 
     * that needs to be parsed into a HTTP date string. 
     * This will convert the date given into a date string 
     * defined in RFC 2616 sec 3.3.1. This will perform a remove 
     * using the issued header name before the header value is set.
     * 
     * @param name - the name of the HTTP message header to be added
     * @param date - the value constructed as an RFC 1123 date string
     */
    public void setDate(String name, long date);

    /**
     * This is used to write the headers that where given to the Response.
     * Any further attempts to give headers to the Response will be
     * futile as only the headers that were given at the time of the
     * first commit will be used in the message header.
     *
     * @throws IOException
     */
    public void commit() throws IOException;

}