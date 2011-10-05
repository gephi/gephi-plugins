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
import org.openide.util.Exceptions;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class Main3 implements Container {

    private static final String DGS_RESOURCE = "amazon_0201485419_400.dgs";
    private static final String JSON_RESOURCE = "graph.json";

    public void handle(Request request, Response response) {

        try {
            String operation = request.getParameter("operation");
            if (operation!=null && operation.equalsIgnoreCase("updateGraph")) {
                response.close();
                return;
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

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

        URL url = this.getClass().getResource(JSON_RESOURCE);

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
                if (i % 250 == 0) {
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
