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
package org.gephi.streaming.server;

/**
 * Controller used by the server to handle requests and responses.
 * 
 * @author panisson
 */
public interface ServerController {

    /**
     * Used to manage the client connections.
     *
     * @return the ClientManager to manage client connections.
     */
    public ClientManager getClientManager();

    /**
     * Used by the server to handle requests and responses.
     * @param request
     * @param response
     */
    public void handle(Request request, Response response);

    /**
     * Stop this controller.
     */
    public void stop();

}