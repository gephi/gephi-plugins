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
import org.junit.Ignore;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

@Ignore
public class StreamingServerTest implements Container {
	
    private static final String DGS_RESOURCE = "alt_add_remove.dgs";

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
                if (i % 10 == 0) {
                    System.out.println("Sending data");
                    out.flush();
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                    }
                }

            }
            body.flush();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] list) throws Exception {
        Container container = new StreamingServerTest();
        Connection connection = new SocketConnection(container);
        SocketAddress address = new InetSocketAddress(8080);

        connection.connect(address);
    }
}