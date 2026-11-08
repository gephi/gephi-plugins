/*
Copyright 2008-2010 Gephi
Author : Andre Panisson <panisson@gmail.com>
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
package org.gephi.streaming.server.impl.jetty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.http.HttpServletRequest;
import org.eclipse.jetty.websocket.WebSocket;
import org.gephi.streaming.server.ClientManager;
import org.gephi.streaming.server.impl.ServerOperationExecutor;
import org.openide.util.Exceptions;

/**
 *
 * @author panisson
 */
public class GephiWebSocket implements WebSocket.OnTextMessage {
    
    private Connection connection;
    private final ServerOperationExecutor executor;
    private final ClientManager clientManager;
    private String clientId;
    private String format;
    
    public GephiWebSocket(HttpServletRequest request, ServerOperationExecutor executor, ClientManager clientManager) {
        this.executor = executor;
        this.clientManager = clientManager;
        
        format = request.getParameter("format");
        if(format==null) {
            // Default format is JSON
            format = "JSON";
        }
        clientId = clientManager.add(request);
    }

    public void onMessage(String m) {
        try {
            executor.executeUpdateGraph(format, new ByteArrayInputStream(m.getBytes()), new ByteArrayOutputStream());
            connection.sendMessage(m);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void onOpen(Connection cnctn) {
        this.connection = cnctn;
        try {
            executor.executeGetGraph(format, clientId, new WSOutputStream());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void onClose(int i, String string) {
        clientManager.remove(clientId);
    }
    
    private class WSOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            this.write(new byte[]{(byte)b}, 0, 1);
        }
        
        @Override
        public void write(byte b[], int off, int len) throws IOException {
            connection.sendMessage(new String(b, off, len));
        }
        
        @Override
        public void write(byte b[]) throws IOException {
            connection.sendMessage(new String(b));
        }
        
    }

}
