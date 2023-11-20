/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.statistics.gui;

import java.util.ArrayList;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

/**
 *
 * This class is a data container for the Tree.
 */
public class TypeTreeNode extends DefaultMutableTreeNode implements Comparable<TypeTreeNode> {

    private static final long serialVersionUID = 5927002411753001709L;
    private String name;
    private int number;
    private double percentage;
    private ArrayList<TypeTreeNode> fathers = new ArrayList<TypeTreeNode>();

    public TypeTreeNode(final String name, final int number, final double percentage) {
        this.name = name;
        this.number = number;
        this.percentage = percentage;
    }

    @Override
    public void add(MutableTreeNode node) {
        if (children == null) {
            children = new Vector<>();
        }
        children.insertElementAt(node, getChildCount());
        TypeTreeNode typeTreeNode = (TypeTreeNode) node;
        typeTreeNode.addFather(this);
    }

    /**
     * Returns the the string to be used to display this leaf in the JTree.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Loads the children, caching the results in the children var.
     */
    protected TypeTreeNode[] getChildren() {
        ArrayList<TypeTreeNode> result = new ArrayList<TypeTreeNode>();
        for (int i = 0; i < getChildCount(); ++i) {
            result.add((TypeTreeNode) getChildAt(i));
        }
        return result.toArray(new TypeTreeNode[0]);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @return the percentage
     */
    public double getPercentage() {
        return percentage;
    }

    @Override
    public int compareTo(TypeTreeNode o) {
        return getName().compareTo(o.getName());
    }

    public boolean hasFather() {
        return (!fathers.isEmpty());
    }

    private void addFather(TypeTreeNode fatherNode) {
        fathers.add(fatherNode);
    }

    @Override
    public void remove(MutableTreeNode aChild) {
        TypeTreeNode node = (TypeTreeNode) aChild;
        int index = children.indexOf(aChild);
        if (index != -1) {
            node.removeFather(this);
            children.remove(index);
        } else {

        }
    }

    private void removeFather(TypeTreeNode aThis) {
        fathers.remove(aThis);
    }

    /**
     * @param number the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @param percentage the percentage to set
     */
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
}