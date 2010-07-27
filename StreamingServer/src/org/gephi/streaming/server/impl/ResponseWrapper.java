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
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import org.gephi.streaming.server.Response;


/**
 * A simple wrapper for a org.simpleframework.http.Response object.
 * 
 * @author panisson
 *
 */
public class ResponseWrapper implements Response {
    
    private final org.simpleframework.http.Response response;
    private SocketChannel channel;
    
    /**
     * Creates a wrapper using this response
     * 
     * @param response - the response to be used as delegate
     */
    public ResponseWrapper(org.simpleframework.http.Response response, SocketChannel channel) {
        this.response = response;
        this.channel = channel;
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.Response#close()
     */
    public void close() throws IOException {
        response.close();
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.Response#getOutputStream()
     */
    public OutputStream getOutputStream() throws IOException {
        return new OutputStreamWrapper(response.getOutputStream());
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.Response#getPrintStream()
     */
    public PrintStream getPrintStream() throws IOException {
        return new PrintStream(getOutputStream());
//        return response.getPrintStream();
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.Response#set(java.lang.String, java.lang.String)
     */
    public void set(String arg0, String arg1) {
        response.set(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.Response#setCode(int)
     */
    public void setCode(int arg0) {
        response.setCode(arg0);
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.server.impl.Response#setText(java.lang.String)
     */
    public void setText(String arg0) {
        response.setText(arg0);
    }

    @Override
    public void add(String name,
            String value) {
        response.add(name, value);
    }

    @Override
    public void setDate(String name, long date) {
        response.setDate(name, date);
    }

    @Override
    public void commit() throws IOException {
        response.commit();
    }

    /**
     * OutputStream wrapper that is able to detect when the socket
     * was closed.
     * Workaround to the fact that the OutputStream in the simpleframework
     * implementation is buffered and does not throws an exception in the
     * calling thread when the socket was closed by the client.
     */
    private class OutputStreamWrapper extends OutputStream {

        private final OutputStream out;

        public OutputStreamWrapper(OutputStream out) {
            this.out = out;
        }

        @Override
        public void write(int b) throws IOException {
            if (!channel.isConnected()) {
                throw new SocketException("Socket closed");
            }
            out.write(b);
        }

        @Override
        public void close() throws IOException {
            out.close();
        }

        @Override
        public void flush() throws IOException {
            if (!channel.isConnected()) {
                throw new SocketException("Socket closed");
            }
            out.flush();
        }

    }

}
