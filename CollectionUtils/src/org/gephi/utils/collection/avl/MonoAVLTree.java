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
package org.gephi.utils.collection.avl;

import java.util.Iterator;
import org.gephi.utils.collection.avl.ResetableIterator;

/**
 * Simple AVL Tree storing items of {@link AVLItem} class. It uses the <code>getNumber()</code> method to
 * get item's key. With the iterator, nodes in the tree will be returned in a ascending order.
 * <p>
 * The AVL tree implementaion is based on a iterative method and guarantee O(ln(n)) access.
 * <p>
 * This tree has a single iterator, it can be accessed from a single thread.
 * @author Mathieu Bastian
 */
public class MonoAVLTree implements Iterable<AVLItem> {

    protected MonoAVLNode root;
    protected int count;
    protected MonoAVLIterator iterator;

    public MonoAVLTree() {
        iterator = new MonoAVLIterator();
    }

    public boolean add(AVLItem item) {
        MonoAVLNode p = this.root;

        if (p == null) {
            this.root = new MonoAVLNode(item);
        } else {
            while (true) {
                int c = item.getNumber() - p.item.getNumber();

                if (c < 0) {
                    if (p.left != null) {
                        p = p.left;
                    } else {
                        p.left = new MonoAVLNode(item, p);
                        p.balance--;

                        break;
                    }
                } else if (c > 0) {
                    if (p.right != null) {
                        p = p.right;
                    } else {
                        p.right = new MonoAVLNode(item, p);
                        p.balance++;

                        break;
                    }
                } else {
                    return false;
                }
            }

            while ((p.balance != 0) && (p.parent != null)) {
                if (p.parent.left == p) {
                    p.parent.balance--;
                } else {
                    p.parent.balance++;
                }

                p = p.parent;

                if (p.balance == -2) {
                    MonoAVLNode x = p.left;

                    if (x.balance == -1) {
                        x.parent = p.parent;

                        if (p.parent == null) {
                            this.root = x;
                        } else {
                            if (p.parent.left == p) {
                                p.parent.left = x;
                            } else {
                                p.parent.right = x;
                            }
                        }

                        p.left = x.right;

                        if (p.left != null) {
                            p.left.parent = p;
                        }

                        x.right = p;
                        p.parent = x;

                        x.balance = 0;
                        p.balance = 0;
                    } else {
                        MonoAVLNode w = x.right;

                        w.parent = p.parent;

                        if (p.parent == null) {
                            this.root = w;
                        } else {
                            if (p.parent.left == p) {
                                p.parent.left = w;
                            } else {
                                p.parent.right = w;
                            }
                        }

                        x.right = w.left;

                        if (x.right != null) {
                            x.right.parent = x;
                        }

                        p.left = w.right;

                        if (p.left != null) {
                            p.left.parent = p;
                        }

                        w.left = x;
                        w.right = p;

                        x.parent = w;
                        p.parent = w;

                        if (w.balance == -1) {
                            x.balance = 0;
                            p.balance = 1;
                        } else if (w.balance == 0) {
                            x.balance = 0;
                            p.balance = 0;
                        } else // w.balance == 1
                        {
                            x.balance = -1;
                            p.balance = 0;
                        }

                        w.balance = 0;
                    }

                    break;
                } else if (p.balance == 2) {
                    MonoAVLNode x = p.right;

                    if (x.balance == 1) {
                        x.parent = p.parent;

                        if (p.parent == null) {
                            this.root = x;
                        } else {
                            if (p.parent.left == p) {
                                p.parent.left = x;
                            } else {
                                p.parent.right = x;
                            }
                        }

                        p.right = x.left;

                        if (p.right != null) {
                            p.right.parent = p;
                        }

                        x.left = p;
                        p.parent = x;

                        x.balance = 0;
                        p.balance = 0;
                    } else {
                        MonoAVLNode w = x.left;

                        w.parent = p.parent;

                        if (p.parent == null) {
                            this.root = w;
                        } else {
                            if (p.parent.left == p) {
                                p.parent.left = w;
                            } else {
                                p.parent.right = w;
                            }
                        }

                        x.left = w.right;

                        if (x.left != null) {
                            x.left.parent = x;
                        }

                        p.right = w.left;

                        if (p.right != null) {
                            p.right.parent = p;
                        }

                        w.right = x;
                        w.left = p;

                        x.parent = w;
                        p.parent = w;

                        if (w.balance == 1) {
                            x.balance = 0;
                            p.balance = -1;
                        } else if (w.balance == 0) {
                            x.balance = 0;
                            p.balance = 0;
                        } else // w.balance == -1
                        {
                            x.balance = 1;
                            p.balance = 0;
                        }

                        w.balance = 0;
                    }

                    break;
                }
            }
        }

        this.count++;
        return true;
    }

    public boolean remove(AVLItem item) {
        return this.remove(item.getNumber());
    }

    public boolean remove(int number) {
        MonoAVLNode p = this.root;

        while (p != null) {
            int c = number - p.item.getNumber();

            if (c < 0) {
                p = p.left;
            } else if (c > 0) {
                p = p.right;
            } else {
                MonoAVLNode y; // node from which rebalancing begins

                int choice = 0; 		//0:Done  1:Left  2:Right

                if (p.right == null) // Case 1: p has no right child
                {
                    if (p.left != null) {
                        p.left.parent = p.parent;
                    }

                    if (p.parent == null) {
                        this.root = p.left;

                        count--;
                        return true;
                    }

                    if (p == p.parent.left) {
                        p.parent.left = p.left;

                        y = p.parent;

                        choice = 1;
                    // goto LeftDelete;
                    } else {
                        p.parent.right = p.left;

                        y = p.parent;

                        choice = 2;
                    //goto RightDelete;
                    }
                } else if (p.right.left == null) // Case 2: p's right child has no left child
                {
                    if (p.left != null) {
                        p.left.parent = p.right;
                        p.right.left = p.left;
                    }

                    p.right.balance = p.balance;
                    p.right.parent = p.parent;

                    if (p.parent == null) {
                        this.root = p.right;
                    } else {
                        if (p == p.parent.left) {
                            p.parent.left = p.right;
                        } else {
                            p.parent.right = p.right;
                        }
                    }

                    y = p.right;

                    choice = 2;
                //goto RightDelete;
                } else // Case 3: p's right child has a left child
                {
                    MonoAVLNode s = p.right.left;

                    while (s.left != null) {
                        s = s.left;
                    }

                    if (p.left != null) {
                        p.left.parent = s;
                        s.left = p.left;
                    }

                    s.parent.left = s.right;

                    if (s.right != null) {
                        s.right.parent = s.parent;
                    }

                    p.right.parent = s;
                    s.right = p.right;

                    y = s.parent; // for rebalacing, must be set before we change s.parent

                    s.balance = p.balance;
                    s.parent = p.parent;

                    if (p.parent == null) {
                        this.root = s;
                    } else {
                        if (p == p.parent.left) {
                            p.parent.left = s;
                        } else {
                            p.parent.right = s;
                        }
                    }

                    choice = 1;
                // goto LeftDelete;
                }

                // rebalancing begins
                while (choice != 0) {
                    if (choice == 1) {
                        //LeftDelete:

                        y.balance++;

                        if (y.balance == 1) {
                            //goto Done;
                            choice = 0;
                        } else if (y.balance == 2) {
                            MonoAVLNode x = y.right;

                            if (x.balance == -1) {
                                MonoAVLNode w = x.left;

                                w.parent = y.parent;

                                if (y.parent == null) {
                                    this.root = w;
                                } else {
                                    if (y.parent.left == y) {
                                        y.parent.left = w;
                                    } else {
                                        y.parent.right = w;
                                    }
                                }

                                x.left = w.right;

                                if (x.left != null) {
                                    x.left.parent = x;
                                }

                                y.right = w.left;

                                if (y.right != null) {
                                    y.right.parent = y;
                                }

                                w.right = x;
                                w.left = y;

                                x.parent = w;
                                y.parent = w;

                                if (w.balance == 1) {
                                    x.balance = 0;
                                    y.balance = -1;
                                } else if (w.balance == 0) {
                                    x.balance = 0;
                                    y.balance = 0;
                                } else // w.balance == -1
                                {
                                    x.balance = 1;
                                    y.balance = 0;
                                }

                                w.balance = 0;

                                y = w; // for next iteration
                            } else {
                                x.parent = y.parent;

                                if (y.parent != null) {
                                    if (y.parent.left == y) {
                                        y.parent.left = x;
                                    } else {
                                        y.parent.right = x;
                                    }
                                } else {
                                    this.root = x;
                                }

                                y.right = x.left;

                                if (y.right != null) {
                                    y.right.parent = y;
                                }

                                x.left = y;
                                y.parent = x;

                                if (x.balance == 0) {
                                    x.balance = -1;
                                    y.balance = 1;

                                    //goto Done
                                    choice = 0;
                                } else {
                                    x.balance = 0;
                                    y.balance = 0;

                                    y = x; // for next iteration
                                }
                            }
                        }
                    } else if (choice == 2) {
                        //goto LoopTest;


                        //RightDelete:

                        y.balance--;

                        if (y.balance == -1) {
                            choice = 0;
                        //goto Done;
                        } else if (y.balance == -2) {
                            MonoAVLNode x = y.left;

                            if (x.balance == 1) {
                                MonoAVLNode w = x.right;

                                w.parent = y.parent;

                                if (y.parent == null) {
                                    this.root = w;
                                } else {
                                    if (y.parent.left == y) {
                                        y.parent.left = w;
                                    } else {
                                        y.parent.right = w;
                                    }
                                }

                                x.right = w.left;

                                if (x.right != null) {
                                    x.right.parent = x;
                                }

                                y.left = w.right;

                                if (y.left != null) {
                                    y.left.parent = y;
                                }

                                w.left = x;
                                w.right = y;

                                x.parent = w;
                                y.parent = w;

                                if (w.balance == -1) {
                                    x.balance = 0;
                                    y.balance = 1;
                                } else if (w.balance == 0) {
                                    x.balance = 0;
                                    y.balance = 0;
                                } else // w.balance == 1
                                {
                                    x.balance = -1;
                                    y.balance = 0;
                                }

                                w.balance = 0;

                                y = w; // for next iteration
                            } else {
                                x.parent = y.parent;

                                if (y.parent != null) {
                                    if (y.parent.left == y) {
                                        y.parent.left = x;
                                    } else {
                                        y.parent.right = x;
                                    }
                                } else {
                                    this.root = x;
                                }

                                y.left = x.right;

                                if (y.left != null) {
                                    y.left.parent = y;
                                }

                                x.right = y;
                                y.parent = x;

                                if (x.balance == 0) {
                                    x.balance = 1;
                                    y.balance = -1;

                                    choice = 0;
                                //goto Done;
                                } else {
                                    x.balance = 0;
                                    y.balance = 0;

                                    y = x; // for next iteration
                                }
                            }
                        }
                    }


                    if (choice == 0) {
                        this.count--;
                        return true;
                    }

                    //LoopTest: {

                    if (y.parent != null) {
                        if (y == y.parent.left) {
                            y = y.parent;
                            choice = 1;
                        // goto LeftDelete;
                        } else {
                            y = y.parent;
                            choice = 2;
                        //goto RightDelete;
                        }
                    } else {
                        //Done
                        this.count--;
                        return true;
                    }
                }

            }
        }

        return false;
    }

    public boolean contains(AVLItem item) {
        MonoAVLNode p = this.root;

        while (p != null) {
            int c = item.getNumber() - p.item.getNumber();

            if (c < 0) {
                p = p.left;
            } else if (c > 0) {
                p = p.right;
            } else {
                return true;
            }
        }

        return false;
    }

    public AVLItem get(int number) {
        MonoAVLNode p = this.root;

        while (p != null) {
            int c = number - p.item.getNumber();

            if (c < 0) {
                p = p.left;
            } else if (c > 0) {
                p = p.right;
            } else {
                return p.item;
            }
        }

        return null;
    }

    public void clear() {
        this.root = null;
        this.count = 0;
    }

    public Iterator<AVLItem> iterator() {
        iterator.setNode(this);
        return iterator;
    }

    public int getCount() {
        return count;
    }

    private class MonoAVLNode {

        MonoAVLNode parent;
        MonoAVLNode left;
        MonoAVLNode right;
        int balance;
        AVLItem item;

        public MonoAVLNode(AVLItem item) {
            this.item = item;
        }

        public MonoAVLNode(AVLItem item, MonoAVLNode parent) {
            this.item = item;
            this.parent = parent;
        }
    }

    private static class MonoAVLIterator implements Iterator<AVLItem>, ResetableIterator {

        private MonoAVLNode next;
        private AVLItem current;

        public MonoAVLIterator() {
        }

        public MonoAVLIterator(MonoAVLNode node) {
            this.next = node;
            goToDownLeft();
        }

        public MonoAVLIterator(MonoAVLTree tree) {
            this(tree.root);
        }

        public void setNode(MonoAVLTree tree) {
            this.next = tree.root;
            goToDownLeft();
        }

        private void goToDownLeft() {
            if (next != null) {
                while (next.left != null) {
                    next = next.left;
                }
            }
        }

        public boolean hasNext() {
            if (next == null) {
                return false;
            }

            current = this.next.item;

            if (next.right == null) {
                while ((next.parent != null) && (next == next.parent.right)) {
                    this.next = this.next.parent;
                }

                this.next = this.next.parent;
            } else {
                this.next = this.next.right;

                while (this.next.left != null) {
                    this.next = this.next.left;
                }
            }

            return true;
        }

        public AVLItem next() {
            return current;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
