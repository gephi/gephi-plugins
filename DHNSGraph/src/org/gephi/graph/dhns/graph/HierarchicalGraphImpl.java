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
package org.gephi.graph.dhns.graph;

import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.ImmutableTreeNode;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeIterable;
import org.gephi.graph.dhns.core.Dhns;
import org.gephi.graph.dhns.core.GraphViewImpl;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.iterators.ChildrenIterator;
import org.gephi.graph.dhns.node.iterators.DescendantIterator;
import org.gephi.graph.dhns.node.iterators.LevelIterator;
import org.gephi.graph.dhns.node.iterators.TreeIterator;
import org.gephi.graph.dhns.predicate.Predicate;
import org.gephi.graph.dhns.predicate.Tautology;
import org.gephi.graph.dhns.utils.TreeNodeWrapper;

/**
 *
 * @author Mathieu Bastian
 */
public abstract class HierarchicalGraphImpl extends AbstractGraphImpl implements HierarchicalGraph {

    protected final Predicate<AbstractNode> enabledNodePredicate;
    
    public HierarchicalGraphImpl(Dhns dhns, GraphViewImpl view) {
        super(dhns, view);
        enabledNodePredicate = new Predicate<AbstractNode>() {

            public boolean evaluate(AbstractNode element) {
                return element.isEnabled();
            }
        };
    }

    public abstract HierarchicalGraphImpl copy(Dhns dhns, GraphViewImpl view);

    public boolean addNode(Node node, Node parent) {
        if (node == null) {
            throw new IllegalArgumentException("Node can't be null");
        }
        AbstractNode absNode = (AbstractNode) node;
        AbstractNode absParent = null;
        if (parent != null) {
            absParent = checkNode(parent);
        }
        if (absNode.isValid(view.getViewId())) {
            return false;
        }
        if (absNode.avlNode != null) { //exist in another view
            if (absNode.getInView(view.getViewId()) != null) {
                return false;
            }
            absNode = new AbstractNode(absNode.getNodeData(), view.getViewId());
        }
        if (!absNode.getNodeData().hasAttributes()) {
            absNode.getNodeData().setAttributes(dhns.factory().newNodeAttributes(absNode.getNodeData()));
        }
        view.getStructureModifier().addNode(absNode, absParent);
        return true;
    }

    public boolean addNode(Node node) {
        return addNode(node, null);
    }

    public boolean contains(Node node) {
        if (node == null) {
            throw new NullPointerException();
        }

        AbstractNode absNode = (AbstractNode) node;
        boolean res = false;
        if (absNode.isValid(view.getViewId())) {
            res = structure.getTree().contains(absNode);
        } else if ((absNode = absNode.getInView(view.getViewId())) != null) {
            res = true;
        }
        return res;
    }

    public Node getNode(int id) {
        return dhns.getGraphStructure().getNodeFromDictionnary(id, view.getViewId());
    }

    public Edge getEdge(int id) {
        return dhns.getGraphStructure().getEdgeFromDictionnary(id);
    }

    public Node getNode(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        return dhns.getGraphStructure().getNodeFromDictionnary(id, view.getViewId());
    }

    public Edge getEdge(String id) {
        if (id == null) {
            throw new NullPointerException();
        }
        return dhns.getGraphStructure().getEdgeFromDictionnary(id);
    }

    public NodeIterable getNodes() {
        readLock();
        return dhns.newNodeIterable(new TreeIterator(structure, true, Tautology.instance));
    }

    public NodeIterable getNodesTree() {
        readLock();
        return dhns.newNodeIterable(new TreeIterator(structure, false, Tautology.instance));
    }

    public int getNodeCount() {
        //int count = structure.getTreeSize() - 1;// -1 Exclude virtual root
        int count = view.getNodesEnabled();
        return count;
    }

    public NodeIterable getNodes(int level) {
        level += 1;     //Because we ignore the virtual root
        readLock();
        int height = structure.getTreeHeight();
        if (level > height) {
            readUnlock();
            throw new IllegalArgumentException("Level must be between 0 and the height of the tree, currently height=" + (height - 1));
        }
        return dhns.newNodeIterable(new LevelIterator(structure, level, Tautology.instance));

    }

    public int getLevelSize(int level) {
        level += 1;     //Because we ignore the virtual root
        int height = structure.getTreeHeight();
        if (level > height) {
            throw new IllegalArgumentException("Level must be between 0 and the height of the tree, currently height=" + (height - 1));
        }
        int res = structure.getLevelSize(level);
        return res;
    }

    public boolean isSelfLoop(Edge edge) {
        AbstractEdge absEdge = checkEdge(edge);
        return absEdge.getSource(view.getViewId()) == absEdge.getTarget(view.getViewId());
    }

    public boolean isAdjacent(Edge edge1, Edge edge2) {
        if (edge1 == edge2) {
            throw new IllegalArgumentException("Edges can't be the same");
        }
        AbstractEdge absEdge1 = checkEdge(edge1);
        AbstractEdge absEdge2 = checkEdge(edge2);
        return absEdge1.getSource(view.getViewId()) == absEdge2.getSource(view.getViewId())
                || absEdge1.getSource(view.getViewId()) == absEdge2.getTarget(view.getViewId())
                || absEdge1.getTarget(view.getViewId()) == absEdge2.getSource(view.getViewId())
                || absEdge1.getTarget(view.getViewId()) == absEdge2.getTarget(view.getViewId());
    }

    public Node getOpposite(Node node, Edge edge) {
        checkNode(node);
        AbstractEdge absEdge = checkEdgeOrMetaEdge(edge);
        if (absEdge.getSource(view.getViewId()) == node) {
            return absEdge.getTarget(view.getViewId());
        } else if (absEdge.getTarget(view.getViewId()) == node) {
            return absEdge.getSource(view.getViewId());
        }
        throw new IllegalArgumentException("Node must be either source or target of the edge.");
    }

    public boolean removeNode(Node node) {
        AbstractNode absNode = checkNode(node);
        view.getStructureModifier().deleteNode(absNode);
        return true;
    }

    public void clear() {
        view.getStructureModifier().clear();
    }

    public void clearEdges() {
        view.getStructureModifier().clearEdges();
    }

    public void clearEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        view.getStructureModifier().clearEdges(absNode);
    }

    public void clearMetaEdges(Node node) {
        AbstractNode absNode = checkNode(node);
        view.getStructureModifier().clearMetaEdges(absNode);
    }

    public void setId(Node node, String id) {
        if (node == null) {
            throw new NullPointerException("node can't be null");
        }
        dhns.getGraphStructure().setNodeId(((AbstractNode) node).getNodeData(), id);
    }

    public void setId(Edge edge, String id) {
        if (edge == null) {
            throw new NullPointerException("edge can't be null");
        }
        dhns.getGraphStructure().setEdgeId((AbstractEdge) edge, id);
    }

    public ImmutableTreeNode wrapToTreeNode() {
        TreeNodeWrapper wrapper = new TreeNodeWrapper(structure);
        ImmutableTreeNode treeNode;
        readLock();
        treeNode = wrapper.wrap(new TreeIterator(structure, false, Tautology.instance));
        readUnlock();
        return treeNode;
    }

    public int getChildrenCount(Node node) {
        AbstractNode absNode = checkNode(node);
        int count = 0;
        ChildrenIterator itr = new ChildrenIterator(structure, absNode, Tautology.instance);
        for (; itr.hasNext();) {
            itr.next();
            count++;
        }
        return count;
    }

    public int getDescendantCount(Node node) {
        AbstractNode absNode = checkNode(node);
        return absNode.size;
    }

    public Node getParent(Node node) {
        AbstractNode absNode = checkNode(node);
        Node parent = null;
        if (absNode.parent != structure.getRoot()) {
            parent = absNode.parent;
        }
        return parent;
    }

    public NodeIterable getChildren(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new ChildrenIterator(structure, absNode, Tautology.instance));
    }

    public NodeIterable getDescendant(Node node) {
        readLock();
        AbstractNode absNode = checkNode(node);
        return dhns.newNodeIterable(new DescendantIterator(structure, absNode, Tautology.instance));
    }

    public NodeIterable getTopNodes() {
        readLock();
        return dhns.newNodeIterable(new ChildrenIterator(structure, Tautology.instance));
    }

    public boolean isDescendant(Node node, Node descendant) {
        AbstractNode absNode = checkNode(node);
        AbstractNode absDesc = checkNode(descendant);
        boolean res = false;
        res = absDesc.getPre() > absNode.getPre() && absDesc.getPost() < absNode.getPost();
        return res;
    }

    public boolean isAncestor(Node node, Node ancestor) {
        return isDescendant(ancestor, node);
    }

    public boolean isFollowing(Node node, Node following) {
        AbstractNode absNode = checkNode(node);
        AbstractNode absFoll = checkNode(following);
        boolean res = absFoll.getPre() > absNode.getPre() && absFoll.getPost() > absNode.getPost();
        return res;
    }

    public boolean isPreceding(Node node, Node preceding) {
        return isFollowing(preceding, node);
    }

    public boolean isParent(Node node, Node parent) {
        AbstractNode absNode = checkNode(node);
        AbstractNode absParent = checkNode(parent);
        boolean res = absNode.parent == absParent;
        return res;
    }

    public int getHeight() {
        int res = structure.getTreeHeight() - 1;
        return res;
    }

    public int getLevel(Node node) {
        AbstractNode absNode = checkNode(node);
        int res = absNode.level - 1;
        return res;
    }

    public void moveToGroup(Node node, Node nodeGroup) {
        AbstractNode absNode = checkNode(node);
        AbstractNode absGroup = checkNode(nodeGroup);
        if (isDescendant(absNode, absGroup)) {
            throw new IllegalArgumentException("nodeGroup can't be a descendant of node");
        }
        view.getStructureModifier().moveToGroup(absNode, absGroup);
    }

    public void removeFromGroup(Node node) {
        AbstractNode absNode = checkNode(node);
        if (absNode.parent.parent == null) {   //Equal root
            throw new IllegalArgumentException("Node parent can't be the root of the tree");
        }
        view.getStructureModifier().moveToGroup(absNode, absNode.parent.parent);
    }

    public Node groupNodes(Node[] nodes) {
        if (nodes == null || nodes.length == 0) {
            throw new IllegalArgumentException("nodes can't be null or empty");
        }
        AbstractNode[] absNodes = new AbstractNode[nodes.length];
        AbstractNode parent = null;
        for (int i = 0; i < nodes.length; i++) {
            AbstractNode node = checkNode(nodes[i]);
            absNodes[i] = node;
            if (parent == null) {
                parent = node.parent;
            } else if (parent != node.parent) {
                throw new IllegalArgumentException("All nodes must have the same parent");
            }
        }

        Node group = view.getStructureModifier().group(absNodes);
        return group;
    }

    public void ungroupNodes(Node nodeGroup) {
        AbstractNode absNode = checkNode(nodeGroup);
        if (absNode.size == 0) {
            throw new IllegalArgumentException("nodeGroup can't be empty");
        }

        view.getStructureModifier().ungroup(absNode);
    }

    public boolean expand(Node node) {
        AbstractNode absNode = checkNode(node);
        if (absNode.size == 0 || !absNode.isEnabled()) {
            return false;
        }
        view.getStructureModifier().expand(absNode);
        return true;
    }

    public boolean retract(Node node) {
        AbstractNode absNode = checkNode(node);
        if (absNode.size == 0 || absNode.isEnabled()) {
            return false;
        }
        view.getStructureModifier().retract(absNode);
        return true;
    }

    public boolean isInView(Node node) {
        AbstractNode absNode = checkNode(node);
        boolean res = absNode.isEnabled();
        return res;
    }

    public void resetViewToLeaves() {
        view.getStructureModifier().resetViewToLeaves();
    }

    public void resetViewToLevel(int level) {
        readLock();
        level += 1;     //Because we ignore the virtual root
        int height = structure.getTreeHeight();
        if (level > height) {
            readUnlock();
            throw new IllegalArgumentException("Level must be between 0 and the height of the tree, currently height=" + (height - 1));
        }
        readUnlock();
        view.getStructureModifier().resetViewToLevel(level);
    }

    public void resetViewToTopNodes() {
        view.getStructureModifier().resetViewToTopNodes();
    }

    public void flatten() {
        view.getStructureModifier().flatten();
    }

    public Attributes getAttributes() {
        return view.getAttributes();
    }
}
