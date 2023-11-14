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
import java.net.URL;
import java.net.URLConnection;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.openide.util.Exceptions;

public class Main3 extends HttpServlet {

    private static final String DGS_RESOURCE = "amazon_0201485419_400.dgs";
    private static final String JSON_RESOURCE = "graph.json";

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) {

        String operation = request.getParameter("operation");
        if (operation!=null && operation.equalsIgnoreCase("updateGraph")) {
            return;
        }
        
        long time = System.currentTimeMillis();

        response.setHeader("Content-Type", "text/plain");
        response.setHeader("Server", "HelloWorld/1.0 (Simple 4.0)");
        response.setDateHeader("Date", time);
        response.setDateHeader("Last-Modified", time);

        URL url = this.getClass().getResource(JSON_RESOURCE);

        try {
            OutputStream out = response.getOutputStream();

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
            out.flush();
            inputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] list) throws Exception {
        Server server = new Server(8080);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new Main3()), "/*");
        server.setHandler(context);
        server.start();
        server.join();
    }
}
