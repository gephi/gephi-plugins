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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * The Request is used to provide an interface to the 
 * HTTP entity body and message header. 
 * This provides methods that allow the entity body 
 * to be acquired as a stream.
 * 
 * @author panisson
 *
 */
public interface Request {

    /**
     * This is used to read the content body.
     * 
     * @return this returns an input stream containing the message body
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException;

    /**
     * This is used to provide quick access to the parameters.
     * 
     * @param name - this is the name of the parameter value
     * @return the parameter value for the given name
     * @throws IOException
     */
    public String getParameter(String name) throws IOException;

    /**
     * This can be used to get the value of the 
     * first message header that has the specified name.
     * 
     * @param name - the HTTP message header to get the value from
     * @return this returns the value that the HTTP message header
     */
    public String getValue(String name);

    /**
     *
     * @return the String representation of the client address
     */
    public String getClientAddress();

    public Map getAttributes();

}