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
