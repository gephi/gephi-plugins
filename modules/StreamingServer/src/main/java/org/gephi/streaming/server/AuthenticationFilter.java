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
package org.gephi.streaming.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Filter for server authentication.
 * 
 * @author panisson
 *
 */
public interface AuthenticationFilter {
    
    /**
     * This is used to enable or disable the authentication filter. 
     * 
     * @param enabled set it to true if the filter should be enabled, 
     * false to disable it
     */
    public void setAuthenticationEnabled(boolean enabled);

    /**
     * This is used to verify if the filter is enabled
     *
     * @return true if the filter is enabled, false otherwise
     */
    public boolean isAuthenticationEnabled();
    
    /**
     * This is used to get the user currently configured in this 
     * AuthenticationFilter
     * 
     * @return the configured user
     */
    public String getUser();
    
    /**
    * This is used to set the user currently configured in this 
     * AuthenticationFilter
     * 
     * @param user - the user to be used by this filter
     */
    public void setUser(String user);
    
    /**
     * This is used to get the password currently configured in this 
     * AuthenticationFilter
     * 
     * @return the configured password
     */
    public String getPassword();
    
    /**
     * This is used to set the password currently configured in this 
      * AuthenticationFilter
      * 
      * @param password - the password to be used by this filter
      */
    public void setPassword(String password);
    
    /**
     * This is used to verify the user and password provided by the client
     * against the currently configured user and password. 
     * 
     * @param request - the request to be used to check
     * @param response - the response to send errors
     * @return true if the user and password are valid, false otherwise
     */
    public boolean authenticate(HttpServletRequest request, HttpServletResponse response);

}
