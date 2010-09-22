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
