/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.gephi.streaming.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author panisson
 */
public class ClientManager {

    private List<Response> registeredClients = new ArrayList<Response>();
    private List<ClientManagerListener> listeners = new ArrayList<ClientManagerListener>();

    public void add(Request request, Response response) {
        registeredClients.add(response);
        String clientId = request.getClientAddress();
        request.getAttributes().put("CLIENT_IDENTIFIER", clientId);
        for (ClientManagerListener listener: listeners) {
            listener.clientConnected(clientId);
        }
    }

    public void stopAll() {
        Iterator<Response> clients = registeredClients.iterator();
        while (clients.hasNext()) {
            Response response = clients.next();
            try {
                response.close();
                response.getOutputStream().close();
                clients.remove();
            } catch (IOException e) { }
        }
    }

    public void remove(Request request, Response response) {
        registeredClients.remove(response);
        String clientId = (String)request.getAttributes().get("CLIENT_IDENTIFIER");
        for (ClientManagerListener listener: listeners) {
            listener.clientDisconnected(clientId);
        }
    }

    public interface ClientManagerListener {
        public void clientConnected(String client);
        public void clientDisconnected(String client);
    }

    public void addClientManagerListener(ClientManagerListener listener) {
        this.listeners.add(listener);
    }

}
