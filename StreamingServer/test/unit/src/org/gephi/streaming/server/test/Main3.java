package org.gephi.streaming.server.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class Main3 implements Container {

    private static final String DGS_RESOURCE = "amazon_0201485419_400.dgs";
    private static final String JSON_RESOURCE = "amazon.json";

    public void handle(Request request, Response response) {
        PrintStream body;
        try {
            body = response.getPrintStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        long time = System.currentTimeMillis();

        response.set("Content-Type", "text/plain");
        response.set("Server", "HelloWorld/1.0 (Simple 4.0)");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);

        URL url = this.getClass().getResource(DGS_RESOURCE);

        try {
            OutputStream out = response.getOutputStream();
            response.getPrintStream();

            URLConnection connection = url.openConnection();
            connection.connect();

            InputStream inputStream = connection.getInputStream();

            int i = 0;
            int data;
            while ((data = inputStream.read()) != -1) {

                out.write(data);

                i++;
                if (i % 1000 == 0) {
                    System.out.println("Sending data");
                    out.flush();
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                    }
                }

            }
            body.flush();
            inputStream.close();
            out.close();
            response.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] list) throws Exception {
        Container container = new Main3();
        Connection connection = new SocketConnection(container);
        SocketAddress address = new InetSocketAddress(8080);

        connection.connect(address);
    }
}
