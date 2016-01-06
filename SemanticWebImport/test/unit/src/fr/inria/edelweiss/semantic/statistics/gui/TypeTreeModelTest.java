/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.edelweiss.semantic.statistics.gui;

import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author edemairy
 */
public class TypeTreeModelTest {

    public TypeTreeModelTest() {
    }
    final int NB_NODES = 10;
    private TypeTreeNode[] nodes = new TypeTreeNode[NB_NODES];

    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < NB_NODES; ++i) {
            nodes[i] = new TypeTreeNode("node" + i, i, i);
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testBuildTree() {
        //
        //     0
        //     |- 1 - 2 - 8
        //     |   \    \ 9
        //     |    \     
        //     |     \3
        //     |
        //     |- 4 - 5
        //        | - 6
        //        l - 7
        //

        TypeTreeModel model = new TypeTreeModel(nodes[0]);
        nodes[0].add(nodes[1]);
        nodes[1].add(nodes[2]);
        nodes[2].add(nodes[8]);
        nodes[2].add(nodes[9]);
        nodes[1].add(nodes[3]);
        nodes[0].add(nodes[4]);
        nodes[4].add(nodes[5]);
        nodes[4].add(nodes[6]);
        nodes[4].add(nodes[7]);
        assertEquals("node5", model.findNode("node5").getName());
    }

    @Test
    public void testRemoveLeaves() throws CloneNotSupportedException {
        //
        //     0
        //     |- 1 - 2 - 8
        //     |   \    \ 9
        //     |    \     
        //     |     \3
        //     |
        //     |- 4 - 5
        //        | - 6
        //        l - 7
        //

        TypeTreeModel model = new TypeTreeModel(nodes[0]);
        nodes[0].add(nodes[1]);
        nodes[1].add(nodes[2]);
        nodes[2].add(nodes[8]);
        nodes[2].add(nodes[9]);
        nodes[1].add(nodes[3]);
        nodes[0].add(nodes[4]);
        nodes[4].add(nodes[5]);
        nodes[4].add(nodes[6]);
        nodes[4].add(nodes[7]);

        model.removeLeaves();
        assertEquals(2, model.findNode("node0").getChildCount());
        assertEquals(0, model.findNode("node2").getChildCount());
        assertEquals(0, model.findNode("node4").getChildCount());
        assertEquals(1, model.findNode("node1").getChildCount());
        assertEquals(null, model.findNode("node3"));
        assertEquals(null, model.findNode("node8"));
        assertEquals(null, model.findNode("node9"));
        assertEquals(null, model.findNode("node5"));
        assertEquals(null, model.findNode("node6"));
        assertEquals(null, model.findNode("node7"));

    }
}
