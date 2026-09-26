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

package org.gephi.streaming.api;

import java.io.Serializable;
import java.net.URL;

/**
 * A streaming endpoint, with the information required to connect to a stream
 * and process it.
 *
 * @author Andre' Panisson
 */
public class StreamingEndpoint implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private URL url;
    private StreamType streamType;
    private String user;
    private String password;

    /**
     * Create a new StreamingEndpoint with no information on it
     */
    public StreamingEndpoint() {}

    /**
     * Create a new StreamingEndpoint setting its URL and streamType
     * @param url - the URL to set
     * @param streamType - the streamType to set
     */
    public StreamingEndpoint(URL url, StreamType streamType) {
        this.url = url;
        this.streamType = streamType;
    }

    /**
     * Get the URL to connect to
     * @return the URL to connect to
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Sets the URL to connect to
     * @param url the URL to connect to
     */
    public void setUrl(URL url) {
        this.url = url;
    }

    /**
     * Return the stream type
     * @return the stream type
     */
    public StreamType getStreamType() {
        return streamType;
    }

    /**
     * Sets the stream type
     * 
     * @param streamType the stream type
     */
    public void setStreamType(StreamType streamType) {
        this.streamType = streamType;
    }

    /**
     * Return the user to be used in case of authenticated connection
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Sets the user to be used in case of authenticated connection
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Return the password to be used in case of authenticated connection
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password to be used in case of authenticated connection
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
