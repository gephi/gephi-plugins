/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.statistics;

import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author edemairy
 */
public class SuccessorComparatorTest {

    public SuccessorComparatorTest() {
    }

    @Test
    public void testComparator() {
        HashMap<String, Set<String>> successorMap = new HashMap<String, Set<String>>();
        TreeSet<String> setA = new TreeSet<String>();
        setA.add("A1");
        setA.add("A2");
        successorMap.put("A", setA);
        TreeSet<String> setB = new TreeSet<String>();
        setA.add("B1");
        successorMap.put("B", setB);
        SuccessorComparator comparator = new SuccessorComparator(successorMap);
        assertEquals(1,comparator.compare("A", "B"));
    }
}
