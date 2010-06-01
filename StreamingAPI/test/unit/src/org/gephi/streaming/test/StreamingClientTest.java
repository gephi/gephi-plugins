package org.gephi.streaming.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.gephi.streaming.api.GraphEventContainer;
import org.gephi.streaming.api.StreamProcessor;
import org.gephi.streaming.impl.StreamingClient;
import org.junit.Test;


public class StreamingClientTest {
    
    private static final String DGS_RESOURCE = "amazon_0201485419_400.dgs";

    @Test
    public void testClientConnection() throws MalformedURLException {
        final StringBuilder buffer = new StringBuilder();
        StreamProcessor dataProcessor = new StreamProcessor() {
            @Override
            public void processStream(InputStream inputStream) throws IOException {
                int data;
                while ((data = inputStream.read()) != -1) {
                    buffer.append((char)data);
                }
            }

            @Override
            public GraphEventContainer getContainer() {
                return null;
            }

            @Override
            public void stop() { }

            @Override
            public void setContainer(GraphEventContainer container) { }
        };
        URL url = this.getClass().getResource(DGS_RESOURCE);
        
        StreamingClient client = new StreamingClient();
        client.connectToEndpoint(url, dataProcessor);
        
        try {
            Thread.sleep(1000);
        }catch(InterruptedException e) {};
        
//        StreamingClient client = new StreamingClient(url, dataProcessor);
//        client.run();
        
        assertTrue(buffer.length()>0);
    }
    
}
