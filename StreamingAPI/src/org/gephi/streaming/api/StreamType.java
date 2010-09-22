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

/**
 * Defines a Stream Type
 * 
 * @author Andre' Panisson
 *
 */
public interface StreamType {

    /**
     * Used to get the String representing this stream type
     * @return the String representing this stream type
     */
    public String getType();
    /**
     * Used to get the StreamReader class implementation for this stream type
     * @return the StreamReader class implementation for this stream type
     */
    public Class<? extends StreamReader> getStreamReaderClass();
    /**
     * Used to get the StreamWriter class implementation for this stream type
     * @return the StreamWriter class implementation for this stream type
     */
    public Class<? extends StreamWriter> getStreamWriterClass();
}
