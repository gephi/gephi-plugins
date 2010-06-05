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

import java.io.IOException;
import java.io.InputStream;

/**
 * Read events from a stream.
 * An implementation of this class should read information
 * from the InputStream and call the appropriate
 * OperationSupport operations.
 *
 * @author Andre' Panisson
 */
public abstract class StreamReader {
    
    protected final OperationSupport operator;
    
    /**
     * @param operator the OperationSupport to which the operations will be delegated
     */
    public StreamReader(OperationSupport operator) {
        this.operator = operator;
    }

    /**
     * Read from the InputStream and call the appropriate operations
     * in the OperationSupport
     * 
     * @param inputStream the InputStream to read from.
     * @throws IOException when unable to connect to the InputStream
     */
    public abstract void processStream(InputStream inputStream) throws IOException;

}
