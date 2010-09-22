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

import java.io.OutputStream;

/**
 * A factory for StreamWriters
 * 
 * @author Andre' Panisson
 *
 */
public interface StreamWriterFactory {
    
    /**
     * Create a StreamWriter based on the specified stream type
     * 
     * @param streamType
     * @param outputStream 
     * @return the StreamWriter able to write in the specified stream type
     */
    public StreamWriter createStreamWriter(String streamType, OutputStream outputStream);

    /**
     * Create a StreamWriter based on the specified stream type
     * 
     * @param streamType
     * @param outputStream 
     * @return the StreamWriter able to write in the specified stream type
     */
    public StreamWriter createStreamWriter(StreamType streamType, OutputStream outputStream);
}
