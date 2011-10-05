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

package org.gephi.streaming.server.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.gephi.streaming.server.ClientManager;
import org.gephi.streaming.server.Request;
import org.gephi.streaming.server.Response;

/**
 *
 * @author panisson
 */
public class ClientManagerImpl implements ClientManager {

    private List<ClientData> registeredClients = new ArrayList<ClientData>();
    private List<ClientManagerListener> listeners = new ArrayList<ClientManagerListener>();

    public void add(Request request, Response response) {
        registeredClients.add(new ClientData(request,response));
        String clientId = request.getClientAddress();
        request.getAttributes().put("CLIENT_IDENTIFIER", clientId);
        for (ClientManagerListener listener: listeners) {
            listener.clientConnected(clientId);
        }
    }

    public void stopAll() {
        Iterator<ClientData> clients = registeredClients.iterator();
        while (clients.hasNext()) {
            ClientData client = clients.next();
            try {
                client.response.close();
                client.response.getOutputStream().close();
                clients.remove();
                String clientId = (String)client.request.getAttributes().get("CLIENT_IDENTIFIER");
                for (ClientManagerListener listener: listeners) {
                    listener.clientDisconnected(clientId);
                }
            } catch (IOException e) { }
        }
    }

    public void remove(Request request, Response response) {
        registeredClients.remove(new ClientData(request,response));
        String clientId = (String)request.getAttributes().get("CLIENT_IDENTIFIER");
        for (ClientManagerListener listener: listeners) {
            listener.clientDisconnected(clientId);
        }
    }

    public void addClientManagerListener(ClientManagerListener listener) {
        this.listeners.add(listener);
    }

    private class ClientData {
        private final Request request;
        private final Response response;
        public ClientData(Request request, Response response) {
            this.request = request;
            this.response = response;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 47 * hash + (this.request != null ? this.request.hashCode() : 0);
            hash = 47 * hash + (this.response != null ? this.response.hashCode() : 0);
            return hash;
        }
        
        @Override
        public boolean equals(Object obj) {
            if ( this == obj ) return true;
            if ( obj == null || obj.getClass() != this.getClass() ) return false;

            ClientData client = (ClientData)obj;
            return client.request == this.request &&
                    client.response == this.response;
        }
    }

}
