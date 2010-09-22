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
package org.gephi.streaming.impl;

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamType;
import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.StreamWriterFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author panisson
 *
 */
@ServiceProvider(service = StreamWriterFactory.class)
public class StreamWriterFactoryImpl implements StreamWriterFactory {

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.StreamProcessorFactory#createStreamProcessor(java.lang.String)
     */
    @Override
    public StreamWriter createStreamWriter(String streamType, OutputStream outputStream) {
        Collection<? extends StreamType> streamTypes = Lookup.getDefault().lookupAll(StreamType.class);
        for (StreamType type: streamTypes) {
            if(type.getType().equalsIgnoreCase(streamType)) {
                return createStreamWriter(type, outputStream);
            }
        }
        throw new IllegalArgumentException("Type " + streamType + " not registered as a valid stream type.");
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.StreamProcessorFactory#createStreamProcessor(java.lang.String)
     */
    @Override
    public StreamWriter createStreamWriter(StreamType streamType, OutputStream outputStream) {
        try {
            Constructor<? extends StreamWriter> constructor = streamType.getStreamWriterClass().getConstructor(OutputStream.class);
            return constructor.newInstance(outputStream);

        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Error loading stream processor for type " + streamType, e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Error loading stream processor for type " + streamType, e);
        } catch (SecurityException e) {
            throw new IllegalArgumentException("Error loading stream processor for type " + streamType, e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Error loading stream processor for type " + streamType, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Error loading stream processor for type " + streamType, e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Error loading stream processor for type " + streamType, e);
        }
    }

}
