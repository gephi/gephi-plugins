/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.streaming.server;


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
    public boolean authenticate(Request request, Response response);

}
