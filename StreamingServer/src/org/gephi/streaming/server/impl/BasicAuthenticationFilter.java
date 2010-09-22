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

import org.gephi.streaming.server.AuthenticationFilter;
import org.gephi.streaming.server.Request;
import org.gephi.streaming.server.Response;

/**
 * This is the AuthenticationFilter implementation for the
 * HTTP Basic Authentication.
 * 
 * @author panisson
 *
 */
public class BasicAuthenticationFilter implements AuthenticationFilter {
    
    private static final String REALM = "Gephi GraphStreaming";
    
    private String user;
    private String password;
    private boolean enabled = false;
    
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    
    public boolean authenticate(Request request, Response response) {
        if (enabled && !doAuthenticate(request, response)) {
            send401(request, response);
            return false;
        } else 
            return true;
    }
    
    private boolean doAuthenticate(Request request, Response response) {
        
        String encoded = request.getValue("Authorization");
        
        if (encoded==null) {
            return false;
        }
        
        encoded = encoded.replace("Basic ", "");
        
        String decoded = null;
        try {
            decoded = new String(Base64.decode(encoded));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        String[] userpass = decoded.split(":");
        if(userpass.length < 2) {
            return false;
        }
        
        if(userpass[0].equals(user) && userpass[1].equals(password))
            return true;
        else
            return false;
    }
    
    private void send401(Request request, Response response) {
        response.setCode(401);
        response.setText("Unauthorized");
        response.add("WWW-Authenticate", "Basic realm=\""+REALM+"\"");
        
        try {
            response.getPrintStream().println("HTTP 401: Authorization Required");
            response.close();
        } catch (IOException e) {}
    }
    
    @Override
    public void setAuthenticationEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isAuthenticationEnabled() {
        return enabled;
    }

}
