/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.semantic.statistics.gui;

import java.util.Arrays;
import java.util.Collections;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author edemairy
 */
public class TypeTreeNodeTest {

    final static int NB_NODES = 5;
    static TypeTreeNode[] nodes;

    @Before
    public void setUp() throws Exception {
        nodes = new TypeTreeNode[NB_NODES];
        for (int i = 0; i < NB_NODES; ++i) {
            nodes[i] = new TypeTreeNode("node" + i, i, i);
        }
    }

    @Test
    public void testConstructorSettersGetters() {
        for (int i = 0; i < NB_NODES; ++i) {
            assertEquals(nodes[i].getName(), "node" + i);
            assertEquals(nodes[i].toString(), "node" + i);
            assertEquals(nodes[i].getNumber(), i);
            assertEquals(nodes[i].getPercentage(), i, 1e-6);
            assertEquals(nodes[i].children().hasMoreElements(), false);
        }
    }

    @Test
    public void testGetChildren() {
        for (int i = 1; i < NB_NODES; ++i) {
            nodes[0].add(nodes[i]);
        }
        assertEquals(nodes[0].getChildCount(), NB_NODES - 1);
        for (int i = 1; i < NB_NODES; ++i) {
            assertEquals(nodes[i].getChildCount(), 0);
        }
        TypeTreeNode[] children = nodes[0].getChildren();
        Arrays.sort(children);
        for (int i = 1; i < NB_NODES; ++i) {
            assertEquals("node"+i,children[i-1].getName());
        }
    }

    @Test
    public void testCompare() {
        assertEquals(-1, nodes[0].compareTo(nodes[1]));
        assertEquals( 1, nodes[2].compareTo(nodes[1]));
        assertEquals( 0, nodes[2].compareTo(nodes[2]));
    }
}
