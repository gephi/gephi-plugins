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
package org.gephi.streaming.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.gephi.streaming.api.StreamReader;
import org.gephi.streaming.api.StreamReaderFactory;
import org.gephi.streaming.api.StreamWriter;
import org.gephi.streaming.api.StreamWriterFactory;
import org.junit.Test;
import org.openide.util.Lookup;

/**
 * Test cases for the GraphStream DSG file format.
 * 
 * @author panisson
 *
 */
public class DGSStreamProcessorTest extends AbstractStreamProcessorTest {
    
    @Test
    public void testDGS2JSON() throws IOException {

        OutputStream out = new FileOutputStream("/tmp/a.json");

        StreamWriterFactory writerFactory = Lookup.getDefault().lookup(StreamWriterFactory.class);
//       ByteArrayOutputStream out = new ByteArrayOutputStream();
        StreamWriter streamWriter = writerFactory.createStreamWriter("JSON", out);

        StreamReaderFactory factory = Lookup.getDefault().lookup(StreamReaderFactory.class);
        StreamReader processor = factory.createStreamReader(streamType, streamWriter);
        
        InputStream inputStream = this.getClass().getResourceAsStream(resource);
        processor.processStream(inputStream);
        
//        System.out.println(new String(out.toByteArray()));
        
    }
    
}
