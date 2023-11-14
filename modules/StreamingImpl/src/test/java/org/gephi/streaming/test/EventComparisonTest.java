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
package org.gephi.streaming.test;

import java.util.HashSet;
import java.util.Set;
import org.gephi.streaming.api.event.ElementEvent;

import org.gephi.streaming.api.event.ElementType;
import org.gephi.streaming.api.event.EventType;
import org.gephi.streaming.api.event.GraphEvent;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author panisson
 *
 */
public class EventComparisonTest {

    @Test
    public void compareEventTest() {
        ElementEvent event = new ElementEvent(this, EventType.ADD, ElementType.NODE, "0321356985", null);
        assertTrue(event.equals(event));
        assertTrue(event.hashCode() == event.hashCode());


//        cn "0321356985"  "full-label":"Practical Business Intelligence with SQL Server 2005 (Microsoft Windows Server System Series)"
//        cn "0521780195"  "label":"An Introduction..."
//        cn "0521780195"  "full-label":"An Introduction to Support Vector Machines and Other Kernel-based Learning Methods"
//        cn "0521813972"  "label":"Kernel Methods ..."

    }

    @Test
    public void hashSetTest() {
        Set<GraphEvent> set = new HashSet<GraphEvent>();
        ElementEvent event = new ElementEvent(this, EventType.ADD, ElementType.NODE, "0321356985", null);
        set.add(event);
        assertTrue(set.contains(event));
    }

}
