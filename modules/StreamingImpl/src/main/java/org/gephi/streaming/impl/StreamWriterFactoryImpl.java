/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Andre Panisson <panisson@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
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
