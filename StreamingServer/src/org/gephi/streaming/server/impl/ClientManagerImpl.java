/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
