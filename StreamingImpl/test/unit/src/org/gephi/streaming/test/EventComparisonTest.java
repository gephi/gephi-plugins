/**
 * 
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
