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
package org.gephi.streaming.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import org.gephi.streaming.api.GraphEventHandler;
import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamReaderFactory;
import org.gephi.streaming.api.StreamType;
import org.gephi.streaming.api.event.GraphEventBuilder;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author panisson
 *
 */
@ServiceProvider(service = StreamReaderFactory.class)
public class StreamReaderFactoryImpl implements StreamReaderFactory {

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.StreamProcessorFactory#createStreamProcessor(java.lang.String)
     */
    @Override
    public StreamReader createStreamReader(String streamType, GraphEventHandler handler,
            GraphEventBuilder eventBuilder) {
        Collection<? extends StreamType> streamTypes = Lookup.getDefault().lookupAll(StreamType.class);
        for (StreamType type: streamTypes) {
            if(type.getType().equalsIgnoreCase(streamType)) {
                return createStreamReader(type, handler, eventBuilder);
            }
        }
        throw new IllegalArgumentException("Type " + streamType + " not registered as a valid stream type.");
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.StreamProcessorFactory#createStreamProcessor(java.lang.String)
     */
    @Override
    public StreamReader createStreamReader(StreamType streamType, GraphEventHandler handler,
            GraphEventBuilder eventBuilder) {
        try {
            Constructor<? extends StreamReader> constructor =
                    streamType.getStreamReaderClass().getConstructor(
                    GraphEventHandler.class, GraphEventBuilder.class);
            return constructor.newInstance(handler, eventBuilder);

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
