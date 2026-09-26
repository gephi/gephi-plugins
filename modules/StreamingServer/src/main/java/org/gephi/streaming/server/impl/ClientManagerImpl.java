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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gephi.streaming.server.ClientManager;
import org.openide.util.Exceptions;

/**
 *
 * @author panisson
 */
public class ClientManagerImpl implements ClientManager {
    
    private static final Logger logger =  Logger.getLogger(ClientManagerImpl.class.getName());

    private Map<String, ClientData> registeredClients = new HashMap<String, ClientData>();
    private List<ClientManagerListener> listeners = new ArrayList<ClientManagerListener>();
    private int idCount = 0;

    public String add(HttpServletRequest request) {
        String clientId = request.getRemoteAddr();
        while (registeredClients.containsKey(clientId)) {
            clientId = clientId + (idCount++);
        }
        registeredClients.put(clientId, new ClientData(clientId, request));
        request.setAttribute("CLIENT_IDENTIFIER", clientId);
        for (ClientManagerListener listener: listeners) {
            listener.clientConnected(clientId);
        }
        return clientId;
    }

    public void stopAll() {
        Iterator<ClientData> clients = registeredClients.values().iterator();
        while (clients.hasNext()) {
            ClientData client = clients.next();
            // TODO: verify request status
            try {
                client.request.getAsyncContext().complete();
            } catch (IllegalStateException e) {
                logger.warning(e.getMessage());
            }
            clients.remove();
            for (ClientManagerListener listener: listeners) {
                listener.clientDisconnected(client.clientId);
            }
        }
    }
    
    public void remove(HttpServletRequest request) {
        String clientId = (String)request.getAttribute("CLIENT_IDENTIFIER");
        remove(clientId);
    }

    public void remove(String clientId) {
        
        ClientData client = registeredClients.get(clientId);
        registeredClients.remove(clientId);
        // TODO: verify request status
        try {
            client.request.getAsyncContext().complete();
        } catch (IllegalStateException e) {
            logger.warning(e.getMessage());
        }
        for (ClientManagerListener listener: listeners) {
            listener.clientDisconnected(clientId);
        }
    }

    public void addClientManagerListener(ClientManagerListener listener) {
        this.listeners.add(listener);
    }

    private class ClientData {
        private final HttpServletRequest request;
        private final String clientId;
        public ClientData(String clientId, HttpServletRequest request) {
            this.request = request;
            this.clientId = clientId;
        }
    }

}
