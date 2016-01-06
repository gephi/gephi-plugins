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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.gephi.streaming.server.AuthenticationFilter;

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
    
    public boolean authenticate(HttpServletRequest request, HttpServletResponse response) {
        if (enabled && !doAuthenticate(request, response)) {
            send401(request, response);
            return false;
        } else 
            return true;
    }
    
    private boolean doAuthenticate(HttpServletRequest request, HttpServletResponse response) {
        
        String encoded = request.getHeader("Authorization");
        
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
    
    private void send401(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.addHeader("WWW-Authenticate", "Basic realm=\""+REALM+"\"");
        
        try {
            response.getOutputStream().println("HTTP 401: Authorization Required");
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
