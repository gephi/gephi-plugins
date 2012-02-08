/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.graph.dhns.core;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;

/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * This class is a modification of the <b><code>TreeList</code></b> from Apache Commons Collections 3.1.
 * <p>Basically
 * the <code>TreeList</code> is a <code>List</code> implementation that is optimised for fast insertions and
 * removals at any index in the list.
 * <p>
 * This list implementation uses a tree structure internally to ensure that
 * all insertions and removals are O(ln(n)). This provides much faster performance
 * than both an <code>ArrayList</code> and a <code>LinkedList</code> where elements
 * are inserted and removed repeatedly from anywhere in the list.
 * <p>
 * The class has been modified in the way any modification avoid renumbering of the <b>pre</b> order.
 * <ul><li>Tuned for only store {@link AbstractNode}. And <code>AbstractNode</code> knows his {@link DurableAVLNode}.</li>
 * <li>The class know if the <b>pre</b> number of items is synchronized with indexes or not. See
 * <code>preConsistent</code> integer.</li>
 * <li>When index are not synchronized the real index of <code>DurableAVLNode</code> has to be retrieved.</li>
 * <li>That's why the parent node has been added to <code>DurableAVLNode</code>. In that way retrieving a
 *  node index can be performed in O(H) where H is the height of the tree.</li></ul>
 * @author Joerg Schmuecker
 * @author Stephen Colebourne
 * @author Mathieu Bastian
 */
public class DurableTreeList extends AbstractList<AbstractNode> implements Iterable<AbstractNode> {
//    add; toArray; iterator; insert; get; indexOf; remove
//    TreeList = 1260;7360;3080;  160;   170;3400;  170;
//   ArrayList =  220;1480;1760; 6870;    50;1540; 7200;
//  LinkedList =  270;7360;3350;55860;290720;2910;55200;

    /** The root node in the AVL tree */
    DurableAVLNode root;
    /** The current size of the list */
    int size = 0;
    private int preConsistent = 0;
    protected int[] levelsSize;
    private final GraphViewImpl view;

    //-----------------------------------------------------------------------
    /**
     * Constructs a new empty list.
     */
    public DurableTreeList(GraphViewImpl view) {
        super();
        this.view = view;
        levelsSize = new int[1];
    }

    public GraphViewImpl getView() {
        return view;
    }

    /**
     * Constructs a new empty list that copies the specified list.
     * 
     * @param coll  the collection to copy
     * @throws NullPointerException if the collection is null
     */
    /*public DurableTreeList(Collection<AbstractNode> coll) {
    super();
    addAll(coll);
    }*/
    public void incPreConsistent() {
        preConsistent++;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the element at the specified index.
     * 
     * @param index  the index to retrieve
     * @return the element at the specified index
     */
    public AbstractNode get(int index) {
        checkInterval(index, 0, size() - 1);
        return root.get(index).getValue();
    }

    public DurableAVLNode getNode(int index) {
        checkInterval(index, 0, size() - 1);
        return root.get(index);
    }

    /**
     * Gets the current size of the list.
     * 
     * @return the current size
     */
    public int size() {
        return size;
    }

    /**
     * Gets an iterator over the list.
     * 
     * @return an iterator over the list
     */
    @Override
    public Iterator<AbstractNode> iterator() {
        // override to go 75% faster
        return new TreeListIterator(this);
    }

    public Iterator<AbstractNode> iterator(int fromIndex) {
        // override to go 75% faster
        return new TreeListIterator(this, fromIndex);
    }

    /**
     * Searches for the index of an object in the list.
     * 
     * @return the index of the object, -1 if not found
     */
    public int indexOf(AbstractNode object) {
        // override to go 75% faster
        if (root == null) {
            return -1;
        }

        return root.indexOf(object, root.relativePosition);
    }

    /**
     * Searches for the presence of an object in the list.
     * 
     * @return true if the object is found
     */
    public boolean contains(AbstractNode object) {
        return (indexOf(object) >= 0);
    }

    /**
     * Converts the list into an array.
     * 
     * @return the list as an array
     */
    @Override
    public AbstractNode[] toArray() {
        // override to go 20% faster
        AbstractNode[] array = new AbstractNode[size()];
        if (root != null) {
            root.toArray(array, root.relativePosition);
        }
        return array;
    }

    //-----------------------------------------------------------------------
    /**
     * Adds a new element to the list.
     * 
     * @param index  the index to add before
     * @param obj  the element to add
     */
    @Override
    public void add(int index, AbstractNode obj) {
        modCount++;
        checkInterval(index, 0, size());
        incPreConsistent();
        if (root == null) {
            root = new DurableAVLNode(this, index, obj, null, null, null);
        } else {
            root = root.insert(index, obj);
            root.parent = null;
        }
        if (obj.level >= levelsSize.length) {
            levelsSize = Arrays.copyOf(levelsSize, levelsSize.length + 1);
        }
        levelsSize[obj.level]++;
        size++;
    }

    @Override
    public boolean add(AbstractNode e) {
        add(size, e);
        return true;
    }

    /**
     * Sets the element at the specified index.
     * 
     * @param index  the index to set
     * @param obj  the object to store at the specified index
     * @return the previous object at that index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    @Override
    public AbstractNode set(int index, AbstractNode obj) {
        checkInterval(index, 0, size() - 1);
        DurableAVLNode node = root.get(index);
        AbstractNode result = node.value;
        node.setValue(obj);
        return result;
    }

    /**
     * Removes the element at the specified index.
     * 
     * @param index  the index to remove
     * @return the previous object at that index
     */
    @Override
    public AbstractNode remove(int index) {
        modCount++;
        checkInterval(index, 0, size() - 1);
        AbstractNode result = get(index);
        levelsSize[result.level]--;
        result.avlNode.setIndex(index);
        root = root.remove(index);
        result.avlNode = null;
        result.parent = null;
        size--;
        incPreConsistent();
        return result;
    }

    public AbstractNode removeAndKeepParent(int index) {
        checkInterval(index, 0, size() - 1);

        //Remove without setting null parent
        AbstractNode result = get(index);
        levelsSize[result.level]--;
        root = root.remove(index);
        result.avlNode = null;
        result.size = 0;
        size--;
        incPreConsistent();
        return result;
    }

    public void move(int index, int destination) {
        checkInterval(index, 0, size() - 1);

        AbstractNode node = get(index);
        AbstractNode parent = get(destination);
        int destinationPre = parent.pre + parent.size + 1;
        int nodeLimit = node.pre + node.size;
        boolean forward = destinationPre > node.pre;
        int difflevel = 0;

        //Move descendant & self
        int count = 0;
        for (int i = node.pre; i <= nodeLimit; i++) {
            int sourcePre = i;
            int destPre = destinationPre + count;
            if (forward) {
                sourcePre -= count;
                destPre -= count + 1;
            }

            AbstractNode sourceNode = get(sourcePre);
            levelsSize[sourceNode.level]--;
            root = root.remove(sourcePre);      //Remove
            sourceNode.avlNode = null;          //Remove
            size--;                             //Remove
            //System.out.println("add "+(destPre)+"   remove "+sourceNode.getId());

            if (count == 0) {
                sourceNode.parent = parent;
                difflevel = node.parent.level - node.level + 1;
            }
            sourceNode.level += difflevel;
            add(destPre, sourceNode);
            count++;
        }
        incPreConsistent();
    }

    /**
     * Clears the list, removing all entries.
     */
    @Override
    public void clear() {
        modCount++;
        root = null;
        size = 0;
        levelsSize = new int[1];
    }

    //-----------------------------------------------------------------------
    /**
     * Checks whether the index is valid.
     * 
     * @param index  the index to check
     * @param startIndex  the first allowed index
     * @param endIndex  the last allowed index
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    private void checkInterval(int index, int startIndex, int endIndex) {
        if (index < startIndex || index > endIndex) {
            throw new IndexOutOfBoundsException("Invalid index:" + index + ", size=" + size());
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Implements an DurableAVLNode which keeps the offset updated.
     * <p>
     * This node contains the real work.
     * TreeList is just there to implement {@link java.util.List}.
     * The nodes don't know the index of the object they are holding.  They
     * do know however their position relative to their parent node.
     * This allows to calculate the index of a node while traversing the tree.
     * <p>
     * The Faedelung calculation stores a flag for both the left and right child
     * to indicate if they are a child (false) or a link as in linked list (true).
     */
    public static class DurableAVLNode {

        /** The left child node or the predecessor if {@link #leftIsPrevious}.*/
        private DurableAVLNode left;
        /** Flag indicating that left reference is not a subtree but the predecessor. */
        private boolean leftIsPrevious;
        /** The right child node or the successor if {@link #rightIsNext}. */
        private DurableAVLNode right;
        /** Flag indicating that right reference is not a subtree but the successor. */
        private boolean rightIsNext;
        /** How many levels of left/right are below this one. */
        private int height;
        /** The relative position, root holds absolute position. */
        private int relativePosition;
        /** The stored element. */
        AbstractNode value;
        private DurableAVLNode parent;
        private int preConsistent;
        private DurableTreeList tree;

        /**
         * Constructs a new node with a relative position.
         * 
         * @param relativePosition  the relative position of the node
         * @param obj  the value for the node
         * @param rightFollower the node with the value following this one
         * @param leftFollower the node with the value leading this one
         */
        private DurableAVLNode(DurableTreeList treeParent, int relativePosition, AbstractNode obj, DurableAVLNode rightFollower, DurableAVLNode leftFollower, DurableAVLNode parentNode) {
            this.relativePosition = relativePosition;
            value = obj;
            obj.avlNode = this;
            tree = treeParent;
            rightIsNext = true;
            leftIsPrevious = true;
            right = rightFollower;
            left = leftFollower;
            parent = parentNode;
            preConsistent = tree.preConsistent;
        }

        public DurableTreeList getList() {
            return tree;
        }

        public int getIndex() {
            if (preConsistent != tree.preConsistent) {
                //The Pre is not consistent
                DurableAVLNode currentParent = parent;
                int index = relativePosition;
                while (currentParent != null) {
                    index += currentParent.relativePosition;
                    currentParent = currentParent.parent;
                }
                value.pre = index;
                value.getPost();
                preConsistent = tree.preConsistent;
            }
            return value.pre;
        }

        public void setIndex(int index) {
            value.pre = index;
            value.getPost();
            preConsistent = tree.preConsistent;
        }

        public boolean isConsistent() {
            return preConsistent == tree.preConsistent;
        }

        /**
         * Gets the value.
         * 
         * @return the value of this node
         */
        public AbstractNode getValue() {
            return value;
        }

        /**
         * Sets the value.
         * 
         * @param obj  the value to store
         */
        void setValue(AbstractNode obj) {
            this.value = obj;
            obj.avlNode = this;
        }

        /**
         * Locate the element with the given index relative to the
         * offset of the parent of this node.
         */
        DurableAVLNode get(int index) {
            int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe == 0) {
                //value.setPre(index);
                return this;
            }

            DurableAVLNode nextNode = ((indexRelativeToMe < 0) ? getLeftSubTree() : getRightSubTree());
            if (nextNode == null) {
                return null;
            }
            return nextNode.get(indexRelativeToMe);
        }

        /**
         * Locate the index that contains the specified object.
         */
        int indexOf(AbstractNode object, int index) {
            //value.setPre(index);
            if (getLeftSubTree() != null) {
                int result = left.indexOf(object, index + left.relativePosition);
                if (result != -1) {
                    return result;
                }
            }
            if (value == null ? value == object : value.equals(object)) {
                return index;
            }
            if (getRightSubTree() != null) {
                return right.indexOf(object, index + right.relativePosition);
            }
            return -1;
        }

        /**
         * Stores the node and its children into the array specified.
         * 
         * @param array the array to be filled
         * @param index the index of this node
         */
        void toArray(AbstractNode[] array, int index) {
            array[index] = value;
            if (getLeftSubTree() != null) {
                left.toArray(array, index + left.relativePosition);
            }
            if (getRightSubTree() != null) {
                right.toArray(array, index + right.relativePosition);
            }
        }

        /**
         * Gets the next node in the list after this one.
         * 
         * @return the next node
         */
        public DurableAVLNode next() {
            if (rightIsNext || right == null) {
                return right;
            }
            return right.min();
        }

        /**
         * Gets the node in the list before this one.
         * 
         * @return the previous node
         */
        DurableAVLNode previous() {
            if (leftIsPrevious || left == null) {
                return left;
            }
            return left.max();
        }

        /**
         * Inserts a node at the position index.  
         * 
         * @param index is the index of the position relative to the position of 
         * the parent node.
         * @param obj is the object to be stored in the position.
         */
        DurableAVLNode insert(int index, AbstractNode obj) {
            int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe <= 0) {
                return insertOnLeft(indexRelativeToMe, obj);
            } else {
                return insertOnRight(indexRelativeToMe, obj);
            }
        }

        private DurableAVLNode insertOnLeft(int indexRelativeToMe, AbstractNode obj) {
            DurableAVLNode ret = this;

            if (getLeftSubTree() == null) {
                setLeft(new DurableAVLNode(tree, -1, obj, this, left, this), null);
            } else {
                setLeft(left.insert(indexRelativeToMe, obj), null);
            }

            if (relativePosition >= 0) {
                relativePosition++;
            }
            ret = balance();
            recalcHeight();
            return ret;
        }

        private DurableAVLNode insertOnRight(int indexRelativeToMe, AbstractNode obj) {
            DurableAVLNode ret = this;

            if (getRightSubTree() == null) {
                setRight(new DurableAVLNode(tree, +1, obj, right, this, this), null);
            } else {
                setRight(right.insert(indexRelativeToMe, obj), null);
            }
            if (relativePosition < 0) {
                relativePosition--;
            }
            ret = balance();
            recalcHeight();
            return ret;
        }

        //-----------------------------------------------------------------------
        /**
         * Gets the left node, returning null if its a faedelung.
         */
        private DurableAVLNode getLeftSubTree() {
            return (leftIsPrevious ? null : left);
        }

        /**
         * Gets the right node, returning null if its a faedelung.
         */
        private DurableAVLNode getRightSubTree() {
            return (rightIsNext ? null : right);
        }

        /**
         * Gets the rightmost child of this node.
         * 
         * @return the rightmost child (greatest index)
         */
        private DurableAVLNode max() {
            return (getRightSubTree() == null) ? this : right.max();
        }

        /**
         * Gets the leftmost child of this node.
         * 
         * @return the leftmost child (smallest index)
         */
        private DurableAVLNode min() {
            return (getLeftSubTree() == null) ? this : left.min();
        }

        /**
         * Removes the node at a given position.
         * 
         * @param index is the index of the element to be removed relative to the position of 
         * the parent node of the current node.
         */
        DurableAVLNode remove(int index) {
            int indexRelativeToMe = index - relativePosition;

            if (indexRelativeToMe == 0) {
                return removeSelf();
            }
            if (indexRelativeToMe > 0) {
                setRight(right.remove(indexRelativeToMe), right.right);
                if (relativePosition < 0) {
                    relativePosition++;
                }
            } else {
                setLeft(left.remove(indexRelativeToMe), left.left);
                if (relativePosition > 0) {
                    relativePosition--;
                }
            }

            recalcHeight();
            return balance();
        }

        private DurableAVLNode removeMax() {
            if (getRightSubTree() == null) {
                return removeSelf();
            }
            setRight(right.removeMax(), right.right);
            if (relativePosition < 0) {
                relativePosition++;
            }
            recalcHeight();
            return balance();
        }

        private DurableAVLNode removeMin() {
            if (getLeftSubTree() == null) {
                return removeSelf();
            }
            setLeft(left.removeMin(), left.left);
            if (relativePosition > 0) {
                relativePosition--;
            }
            recalcHeight();
            return balance();
        }

        /**
         * Removes this node from the tree.
         *
         * @return the node that replaces this one in the parent
         */
        private DurableAVLNode removeSelf() {
            if (getRightSubTree() == null && getLeftSubTree() == null) {
                return null;
            }
            if (getRightSubTree() == null) {
                if (relativePosition > 0) {
                    left.relativePosition += relativePosition + (relativePosition > 0 ? 0 : 1);
                }
                left.max().setRight(null, right);
                return left;
            }
            if (getLeftSubTree() == null) {
                right.relativePosition += relativePosition - (relativePosition < 0 ? 0 : 1);
                right.min().setLeft(null, left);
                return right;
            }

            if (heightRightMinusLeft() > 0) {
                // more on the right, so delete from the right
                DurableAVLNode rightMin = right.min();
                value = rightMin.value;
                value.avlNode = this;
                if (leftIsPrevious) {
                    left = rightMin.left;
                }
                right = right.removeMin();
                right.parent = this;
                if (relativePosition < 0) {
                    relativePosition++;
                }
            } else {
                // more on the left or equal, so delete from the left
                DurableAVLNode leftMax = left.max();
                value = leftMax.value;
                value.avlNode = this;
                if (rightIsNext) {
                    right = leftMax.right;
                }
                DurableAVLNode leftPrevious = left.left;
                left = left.removeMax();

                if (left == null) {
                    // special case where left that was deleted was a double link
                    // only occurs when height difference is equal
                    left = leftPrevious;
                    leftIsPrevious = true;
                } else {
                    left.parent = this;
                }

                if (relativePosition > 0) {
                    relativePosition--;
                }
            }

            recalcHeight();
            return this;
        }

        //-----------------------------------------------------------------------
        /**
         * Balances according to the AVL algorithm.
         */
        private DurableAVLNode balance() {
            switch (heightRightMinusLeft()) {
                case 1:
                case 0:
                case -1:
                    return this;
                case -2:
                    if (left.heightRightMinusLeft() > 0) {
                        setLeft(left.rotateLeft(), null);
                    }
                    return rotateRight();
                case 2:
                    if (right.heightRightMinusLeft() < 0) {
                        setRight(right.rotateRight(), null);
                    }
                    return rotateLeft();
                default:
                    throw new RuntimeException("tree inconsistent!");
            }
        }

        /**
         * Gets the relative position.
         */
        private int getOffset(DurableAVLNode node) {
            if (node == null) {
                return 0;
            }
            return node.relativePosition;
        }

        /**
         * Sets the relative position.
         */
        private int setOffset(DurableAVLNode node, int newOffest) {
            if (node == null) {
                return 0;
            }
            int oldOffset = getOffset(node);
            node.relativePosition = newOffest;
            return oldOffset;
        }

        /**
         * Sets the height by calculation.
         */
        private void recalcHeight() {
            height = Math.max(
                    getLeftSubTree() == null ? -1 : getLeftSubTree().height,
                    getRightSubTree() == null ? -1 : getRightSubTree().height) + 1;
        }

        /**
         * Returns the height of the node or -1 if the node is null.
         */
        private int getHeight(DurableAVLNode node) {
            return (node == null ? -1 : node.height);
        }

        /**
         * Returns the height difference right - left
         */
        private int heightRightMinusLeft() {
            return getHeight(getRightSubTree()) - getHeight(getLeftSubTree());
        }

        private DurableAVLNode rotateLeft() {
            DurableAVLNode newTop = right; // can't be faedelung!
            DurableAVLNode movedNode = getRightSubTree().getLeftSubTree();

            int newTopPosition = relativePosition + getOffset(newTop);
            int myNewPosition = -newTop.relativePosition;
            int movedPosition = getOffset(newTop) + getOffset(movedNode);

            setRight(movedNode, newTop);
            newTop.parent = parent;
            newTop.setLeft(this, null);

            setOffset(newTop, newTopPosition);
            setOffset(this, myNewPosition);
            setOffset(movedNode, movedPosition);
            return newTop;
        }

        private DurableAVLNode rotateRight() {
            DurableAVLNode newTop = left; // can't be faedelung
            DurableAVLNode movedNode = getLeftSubTree().getRightSubTree();

            int newTopPosition = relativePosition + getOffset(newTop);
            int myNewPosition = -newTop.relativePosition;
            int movedPosition = getOffset(newTop) + getOffset(movedNode);

            setLeft(movedNode, newTop);
            newTop.parent = parent;
            newTop.setRight(this, null);

            setOffset(newTop, newTopPosition);
            setOffset(this, myNewPosition);
            setOffset(movedNode, movedPosition);
            return newTop;
        }

        /**
         * Sets the left field to the node, or the previous node if that is null
         *
         * @param node  the new left subtree node
         * @param previous  the previous node in the linked list
         */
        private void setLeft(DurableAVLNode node, DurableAVLNode previous) {
            leftIsPrevious = (node == null);
            if (leftIsPrevious) {
                left = previous;
            } else {
                left = node;
                left.parent = this;
            }
            //left = (leftIsPrevious ? previous : node);

            recalcHeight();
        }

        /**
         * Sets the right field to the node, or the next node if that is null
         *
         * @param node  the new left subtree node
         * @param next  the next node in the linked list
         */
        private void setRight(DurableAVLNode node, DurableAVLNode next) {
            rightIsNext = (node == null);
            if (rightIsNext) {
                right = next;
            } else {
                right = node;
                right.parent = this;
            }
            //right = (rightIsNext ? next : node);
            recalcHeight();
        }

//      private void checkFaedelung() {
//          DurableAVLNode maxNode = left.max();
//          if (!maxNode.rightIsFaedelung || maxNode.right != this) {
//              throw new RuntimeException(maxNode + " should right-faedel to " + this);
//          }
//          DurableAVLNode minNode = right.min();
//          if (!minNode.leftIsFaedelung || minNode.left != this) {
//              throw new RuntimeException(maxNode + " should left-faedel to " + this);
//          }
//      }
//
//        private int checkTreeDepth() {
//            int hright = (getRightSubTree() == null ? -1 : getRightSubTree().checkTreeDepth());
//            //          System.out.print("checkTreeDepth");
//            //          System.out.print(this);
//            //          System.out.print(" left: ");
//            //          System.out.print(_left);
//            //          System.out.print(" right: ");
//            //          System.out.println(_right);
//
//            int hleft = (left == null ? -1 : left.checkTreeDepth());
//            if (height != Math.max(hright, hleft) + 1) {
//                throw new RuntimeException(
//                    "height should be max" + hleft + "," + hright + " but is " + height);
//            }
//            return height;
//        }
//
//        private int checkLeftSubNode() {
//            if (getLeftSubTree() == null) {
//                return 0;
//            }
//            int count = 1 + left.checkRightSubNode();
//            if (left.relativePosition != -count) {
//                throw new RuntimeException();
//            }
//            return count + left.checkLeftSubNode();
//        }
//        
//        private int checkRightSubNode() {
//            DurableAVLNode right = getRightSubTree();
//            if (right == null) {
//                return 0;
//            }
//            int count = 1;
//            count += right.checkLeftSubNode();
//            if (right.relativePosition != count) {
//                throw new RuntimeException();
//            }
//            return count + right.checkRightSubNode();
//        }
        /**
         * Used for debugging.
         */
        public String toString() {
            return "AVLNode(" + relativePosition + "," + (left != null) + "," + value
                    + "," + (getRightSubTree() != null) + ", faedelung " + rightIsNext + " )";
        }
    }

    public DurableAVLNode getRoot() {
        return root;
    }
}
