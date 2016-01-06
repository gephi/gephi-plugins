/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.statistics;

import fr.inria.edelweiss.semantic.statistics.gui.JTreeTable;
import fr.inria.edelweiss.semantic.statistics.gui.TypeTreeModel;
import fr.inria.edelweiss.semantic.statistics.gui.TypeTreeNode;
import fr.inria.edelweiss.semantic.statistics.gui.TreeTableModel;
import javax.swing.JFrame;

/**
 *
 * @author edemairy
 */
public class JTreeTableLauncher {

    static public void main(String[] args) {
        TypeTreeNode root = new TypeTreeNode("root node", 1, 100);
        TypeTreeModel model = new TypeTreeModel(root);

        model.getRoot().add(new TypeTreeNode("child1", 10, 50));
        model.findNode("child1").add(new TypeTreeNode("child1_1", 5, 10));
        model.findNode("child1").add(new TypeTreeNode("child1_2", 5, 10));
        model.getRoot().add(new TypeTreeNode("child2", 10, 50));
        model.findNode("child2").add(new TypeTreeNode("child2_1", 5, 10));
        model.findNode("child2").add(new TypeTreeNode("child2_2", 5, 10));
        model.findNode("child2").add(new TypeTreeNode("child2_3", 5, 10));

        assert (model.getRoot().getChildCount() == 2);
        assert (model.findNode("child1").getChildCount() == 2);
        assert (model.findNode("child1_1").getChildCount() == 0);
        assert (model.findNode("child1_2").getChildCount() == 0);
        assert (model.findNode("child2").getChildCount() == 3);
        assert (model.findNode("child2_1").getChildCount() == 0);
        assert (model.findNode("child2_2").getChildCount() == 0);
        assert (model.findNode("child2_3").getChildCount() == 0);

        JTreeTable table = new JTreeTable(model);
        table.setVisible(true);

        JFrame frame = new JFrame();
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(table);
        frame.setSize(frame.getPreferredSize());
        frame.setVisible(true);
    }
}
