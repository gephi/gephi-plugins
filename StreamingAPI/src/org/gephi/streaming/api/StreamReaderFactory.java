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

/**
 * A factory for StreamReaders
 * 
 * @author Andre' Panisson
 *
 */
public interface StreamReaderFactory {
    
    /**
     * Create a StreamReader based on the specified stream type.
     * The read events will be sent to the specified OperationSupport.
     * 
     * @param streamType
     * @param operationSupport 
     * @return the StreamReader able to process the specified stream type
     */
    public StreamReader createStreamReader(String streamType, OperationSupport operationSupport);

    /**
     * Create a StreamReader based on the specified stream type.
     * The read events will be sent to the specified OperationSupport.
     * 
     * @param streamType
     * @param operationSupport 
     * @return the StreamReader able to process the specified stream type
     */
    public StreamReader createStreamReader(StreamType streamType, OperationSupport operationSupport);
}
