/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.statistics.gui;

import java.util.*;

/**
 *
 * @author edemairy
 */
public class TypeTreeModel extends AbstractTreeTableModel implements TreeTableModel {
    // Names of the columns.

    static protected String[] columnNames = {"Name", "Number", "Percentage"};
    // Types of the columns.
    static protected Class[] cTypes = {TreeTableModel.class, Integer.class, Float.class};
    private TypeTreeNode rootNode;
//    private Map<String, TypeTreeNode> nodeTable;

    public TypeTreeModel(TypeTreeNode root) {
        super(root);
//        nodeTable = new TreeMap<String, TypeTreeNode>();
        this.rootNode = root;
    }

    //
    // Some convenience methods.
    //
    protected TypeTreeNode[] getChildren(TypeTreeNode node) {
        return node.getChildren();
    }

    //
    // The TreeModel interface
    //
    @Override
    public int getChildCount(Object node) {
        Object[] children = getChildren((TypeTreeNode) node);
        return (children == null) ? 0 : children.length;
    }

    @Override
    public Object getChild(Object node, int i) {
        return getChildren((TypeTreeNode) node)[i];
    }

    @Override
    public TypeTreeNode getRoot() {
        return rootNode;
    }

    // The superclass's implementation would work, but this is more efficient.
    @Override
    public boolean isLeaf(Object node) {
        return (getChildCount(node) == 0);
    }

    //
    //  The TreeTableNode interface.
    //
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return cTypes[column];
    }

    @Override
    public Object getValueAt(Object objectNode, int column) {
        TypeTreeNode node = (TypeTreeNode) objectNode;
        try {
            switch (column) {
                case 0:
                    return node.getName();
                case 1:
                    return node.getNumber();
                case 2:
                    return node.getPercentage();
            }
        } catch (SecurityException se) {
        }

        return null;
    }

    public TypeTreeNode findNode(final String name) {
        LinkedList<TypeTreeNode> queueNodes = new LinkedList<TypeTreeNode>();
        queueNodes.add(rootNode);
        while (!queueNodes.isEmpty()) {
            TypeTreeNode currentNode = queueNodes.removeLast();
            if (currentNode.getName().equals(name)) {
                return currentNode;
            } else {
                for (TypeTreeNode node : currentNode.getChildren()) {
                    queueNodes.add(node);
                }
            }
        }
        return null;
    }

    public TypeTreeNode findCreateNode(final String name) {
        TypeTreeNode result = findNode(name);
        if (result == null) {
            result = new TypeTreeNode(name, 0, 0);
        }
        return result;
    }

    public void removeLeaves() {
        LinkedList<TypeTreeNode> queueNodes = new LinkedList<TypeTreeNode>();
        queueNodes.add(getRoot());
        while (!queueNodes.isEmpty()) {
            TypeTreeNode currentNode = queueNodes.removeLast();
            TypeTreeNode[] children = currentNode.getChildren();
            for (TypeTreeNode child : children) {
                if (child.isLeaf()) {
                    currentNode.remove(child);
                } else {
                    queueNodes.add(child);
                }
            }
        }
    }

}
