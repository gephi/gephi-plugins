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
package org.gephi.graph.api;

/**
 * Implementation of graphs within graphs concept, by proposing methods to manipulate
 * the hierarchy of nodes.
 * <p>
 * The hierarchical graph maintains a tree of all nodes, it's the hierarchy and a
 * <b>in-view</b> flag for each. Note that 'view' means something else than for
 * <code>GraphView</code>.
 * <p>
 * A node in the hierarchy view means it is visible and none of its ancestors or
 * descendants are. If this node is expanded, it's children become 'in-view'. If it is
 * retracted, its parent becomes 'in-view'. The hierarchy view can be modified with
 * <code>expand()</code>, <code>retract()</code>, <code>resetViewToLeaves()</code>,
 * <code>resetViewToTopNodes()</code> and <code>resetViewToLevel()</code>. Note that
 * the nodes and edges returns by <code>getNodes()</code> or <code>getEdges()</code>
 * use only nodes in the current view. To get all nodes in the hierarchy, see
 * <code>getNodesTree()</code>.
 *
 * @author Mathieu Bastian
 * @see GraphModel
 */
public interface HierarchicalGraph extends Graph {

    /**
     * Add <code>node</code> as a child of <code>parent</code> in the graph. If <code>parent</code> is
     * <code>null</code>, <code>node</code> is added as a child of the (virtual) root node.
     * Fails if the node already exists.
     * @param node the node to add
     * @param parent the existing node whose a child is to be added or <code>null</code>
     * @return <code>true</code> if add is successful, false otherwise
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>,
     * or if <code>parent</code> is not legal in the graph
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean addNode(Node node, Node parent);

    /**
     * Remove <code>metaEdge</code> from the graph. Fails if the edge doesn't exist.
     * @param metaEdge  the meta edge that is to be removed
     * @return  <code>true</code> if remove is successful, false otherwise
     * @throws IllegalArgumentException if <code>edge</code> is <code>null</code> or nodes not legal in
     * the graph
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean removeMetaEdge(Edge metaEdge);

    /**
     * Returns the number of children of <code>node</code>. Returns <code>zero</code> if <code>node</code> is a leaf.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @return  the number of <code>node</code>'s children
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public int getChildrenCount(Node node);

    /**
     * Returns the number of descendant of <code>node</code>. Returns <code>zero</code> if <code>node</code> is a leaf.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @return  the number of <code>node</code>'s descendant
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public int getDescendantCount(Node node);

    /**
     * Returns the parent of <code>node</code> or <code>null</code> if <code>node</code>'s parent is (virtual) root.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose parent is to be returned
     * @return <code>node</code>'s parent
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public Node getParent(Node node);

    /**
     * Returns children of <code>node</code>.
     * @param node the node whose children are to be returned
     * @return a node iterable of <code>node</code>'s children
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public NodeIterable getChildren(Node node);

    /**
     * Returns descendants of <code>node</code>. Descendants are nodes which <code>node</code> is an ancestor.
     * @param node the node whose descendant are to be returned
     * @return a node iterable of <code>node</code>'s descendant
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public NodeIterable getDescendant(Node node);

    /**
     * Returns edges incident to <code>nodeGroup</code> and <code>nodeGroup</code>'s descendants. Edges connected
     * to nodes which are not descendant of <code>nodeGroup</code> are excluded.
     * @param nodeGroup the node whose inner edges are to be returned
     * @return an edge iterable of edges inner <code>nodeGroup</code>
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in the graph
     */
    public EdgeIterable getInnerEdges(Node nodeGroup);

    /**
     * Returns edges <b>not</b> incident to <code>nodeGroup</code> or <code>nodeGroup</code>'s descendants.
     * Edges connected to nodes which are descendant of <code>nodeGroup</code> are excluded.
     * @param nodeGroup the node whose outer edges are to be returned
     * @return an edge iterable of edges outer <code>nodeGroup</code>
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in the graph
     */
    public EdgeIterable getOuterEdges(Node nodeGroup);

    /**
     * Returns roots of the hierarchy forest. They are children of the tree's (virtual) root an have
     * the level equal <code>zero</code>. If all nodes have the same level (i.e. no hierarchy) this
     * method is similar as <code>getNodes()</code>.
     * @return a node iterable of nodes at the top of the tree
     */
    public NodeIterable getTopNodes();

    /**
     * Similar as {@link #getNodes()} but all nodes are visited, not only those in the current view.
     * @return  a node iterable of all nodes in the hierarchy
     */
    public NodeIterable getNodesTree();

    /**
     * Similar as {@link #getEdges()} but all nodes are visited in the hierarchy, so
     * it returns edges for all possible nodes.
     * @return  an edge iterable of all edges
     */
    public EdgeIterable getEdgesTree();

    /**
     * Returns the number of edges and meta edges in the graph
     * <p>
     * Special case of interest:
     * <ul><li>Count self-loops once only.</li>
     * <li>For <b>hierarchical</b> graph, count edges incident only to nodes
     * in the current view.</li></ul>
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @return the number of edges in the graph.
     */
    public int getTotalEdgeCount();

    /**
     * Returns nodes at the given <code>level</code> in the hierarchy. Top nodes
     * have the level <code>zero</code> and leaves' level is the height of the tree.
     * @param level the level whose nodes are to be returned
     * @return a node iterable of nodes located at <code>level</code> in the tree
     * @throws IllegalArgumentException if <code>level</code> is not between 0 and the height of the tree
     */
    public NodeIterable getNodes(int level);

    /**
     * The number of nodes located at the given <code>level</code> int the hierarchy. Similar as
     * <code>getNodes(level).toArray().length</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param level the level whose nodes are to be returned
     * @return the number of nodes at <code>level</code>
     * @throws IllegalArgumentException if <code>level</code> is not between 0 and the height of the tree
     */
    public int getLevelSize(int level);

    /**
     * Returns <code>true</code> if <code>descendant</code> is a descendant of <code>node</code>. True if <code>node</code> is an ancestor
     * of <code>descendant</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @param descendant the descendant node to be queried
     * @return <code>true</code> if <code>descendant</code> is a descendant of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>descendant</code> is <code>null</code> or not legal in the graph
     */
    public boolean isDescendant(Node node, Node descendant);

    /**
     * Returns <code>true</code> if <code>ancestor</code> is an ancestor of <code>node</code>. True if <code>node</code> is a descendant of
     * <code>ancestor</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @param ancestor the ancestor to be queried
     * @return <code>true</code> if <code>ancestor</code> is an ancestor of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>ancestor</code> is <code>null</code> or not legal in the graph
     */
    public boolean isAncestor(Node node, Node ancestor);

    /**
     * Returns <code>true</code> if <code>following</code> is after <code>node</code>. The definition is similar to <code>XML</code> following
     * axis. Is true when <code>following</code> has a greater <b>pre</b> and <b>post</b> order than <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @param following the following to be queried
     * @return <code>true</code> if <code>following</code> is following <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>following</code> is <code>null</code> or not legal in the graph
     */
    public boolean isFollowing(Node node, Node following);

    /**
     * Returns <code>true</code> if <code>preceding</code> is before <code>node</code>. The definition is similar to <code>XML</code> preceding
     * axis. Is true when <code>preceding</code> has a lower <b>pre</b> and <b>post</b> order than <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @param preceding the preceding to be queried
     * @return <code>true</code> if <code>preceding</code> is preceding <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>preceding</code> is <code>null</code> or not legal in the graph
     */
    public boolean isPreceding(Node node, Node preceding);

    /**
     * Returns <code>true</code> if <code>parent</code> is the parent of <code>node</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @param parent the parent to be queried
     * @return <code>true</code> if <code>parent</code> is the parent of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> or <code>parent</code> is <code>null</code> or not legal in the graph
     */
    public boolean isParent(Node node, Node parent);

    /**
     * Returns the height of the tree. The height is <code>zero</code> when all nodes have the same level.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @return the height of the tree
     */
    public int getHeight();

    /**
     * Returns the level of <code>node</code> in the hierarchy. Roots have the level <code>zero</code> and it inscreases when going down
     * in the tree.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @return the level value of <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     */
    public int getLevel(Node node);

    /**
     * Move <code>node</code> and descendants of <code>node</code> to <code>nodeGroup</code>, as <code>node</code> will be a child of
     * <code>nodeGroup</code>. Be aware <code>nodeGroup</code> can't be a descendant of <code>node</code>.
     * @param node the node to be appened to <code>nodeGroup</code> children
     * @param nodeGroup the node to receive <code>node</code> as a child
     * @throws IllegalArgumentException if <code>node</code> or <code>nodeGroup</code> is <code>null</code> or not legal in the graph,
     * or if <code>nodeGroup</code> is a descendant of node
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void moveToGroup(Node node, Node nodeGroup);

    /**
     * Remove <code>node</code> from its parent group and append it to <code>node</code>'s parent. In other words <code>node</code> rise
     * one level in the tree and is no more a child of its parent.
     * @param node the node to be removed from it's group
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph,
     * or if <code>node</code> is already at the top of the tree
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void removeFromGroup(Node node);

    /**
     * Group <code>nodes</code> into a new node group (i.e. cluster). Creates an upper node in the tree and appends <code>nodes</code> to it.
     * Content of <code>nodes</code> can be existing groups. In that case, <code>nodes</code> must only contains roots of groups.
     * Therefore all nodes in <code>nodes</code> must have the same <b>parent</b>. The method returns the newly
     * created group of nodes.
     * @param nodes the nodes to be grouped in a new group
     * @return the newly created group of nodes which contains <code>nodes</code> and descendants of <code>nodes</code>
     * @throws IllegalArgumentException if <code>nodes</code> is <code>null</code>,
     * or if <code>nodes</code> is empty,
     * or if content nodes are not legal in the graph,
     * or if <code>nodes</code>' parent is not similar between elements
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public Node groupNodes(Node[] nodes);

    /**
     * Ungroup nodes in <code>nodeGroup</code> and destroy <code>nodeGroup</code>. Descendants of <code>nodeGroup</code> are appened to
     * <code>nodeGroup</code>'s parent node. This method is the opposite of <code>groupNodes()</code>. If called with the result of
     * <code>groupNodes()</code> the state will be equal to the state before calling <code>groupNodes()</code>.
     * @param nodeGroup the parent node of nodes to be ungrouped
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>, empty or not legal in the graph
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void ungroupNodes(Node nodeGroup);

    /**
     * Flatten the hierarchy by keeping only nodes in the view and by transforming meta edges into edges. All nodes not in the
     * view are removed from the graph. New edges are created from meta edges, with same attributes and weight.
     */
    public void flatten();

    public EdgeIterable getHierarchyEdges();

    /**
     * Returns the hierarchy tree of all nodes in the form of a <code>TreeNode</code>.
     * @return a Java <code>TreeNode</code> wrapper of all nodes in the hierarchy tree
     */
    public ImmutableTreeNode wrapToTreeNode();

    /**
     * Expands the graph view from <code>node</code> to its children. The children of <code>node</code> are put in the view and
     * <code>node</code> is pulled off. Fails if <code>node</code> is not currently in the view or if <code>node</code> don't
     * have any children.
     * <p>
     * Meta edges are automatically updated.
     * @param node the node to be expanded
     * @return <code>true</code> if the expand succeed or <code>false</code> if not
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean expand(Node node);

    /**
     * Retracts the graph view from <code>node</code>'s children to <code>node</code>. The children of <code>node</code> are pulled
     * off the view and <code>node</code> is added to the view. Fails if <code>node</code> is already in the view of if <code>node</code>
     * don't have any children.
     * <p>
     * Meta edges are automatically updated.
     * @param node the nodes' parent to be retracted
     * @return <code>true</code> if the expand succeed or <code>false</code> if not
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> or not legal in the graph
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public boolean retract(Node node);

    /**
     * Returns true if <code>node</code> is currently in the graph view.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node to be queried
     * @return <code>true</code> if <code>node</code> is in the view, <code>false</code> otherwise
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in
     * the graph
     */
    public boolean isInView(Node node);

    /**
     * Reset the current view to leaves of the clustered graph tree. Therefore the
     * <code>getNodesInView()</code> method returns only these leaves.
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void resetViewToLeaves();

    /**
     * Reset the current view to top nodes of the clustered graph tree. Therefore the
     * <code>getNodesInView()</code> method returns only these nodes.
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void resetViewToTopNodes();

    /**
     * Reset the current view to nodes located at the specified <code>level</code> in the
     * clustered graph hierarchy. Therefore the <code>getNodesInView()</code> method returns
     * only these nodes.
     * @throws IllegalArgumentException if <code>level</code> is not between 0 and the height of the tree
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void resetViewToLevel(int level);

    /**
     * Returns meta edges for the whole graph. Meta edges are edges between a group and a leaf
     * or between two groups. They represents proper edges between descendants of groups. Meta
     * edges are always located only on nodes which are in the current view.
     * <p>
     * <b>Example:</b>
     * In a clustered graph, let's define <code>group1</code> and <code>group2</code>, both with
     * two leaves as children. Leaves are named <code>l11</code>, <code>l12</code>, <code>l21</code>
     * and <code>l22</code>. Then we add an edge between <code>l11</code> and <code>l22</code>.
     * Then we look at the view of the graph. Let's say the view is set for groups only, that means
     * only groups are visible and leaves are not. At this point we can say a meta edge exist between
     * <code>group1</code> and <code>group2</code> and it represents the edge <code>l11-l22</code>.
     * <p>
     * Therefore meta edges are useful when a graph is retracted/collapsed into clusters. Relations
     * between clusters can be get with meta edges directly. Note that a meta edge knows which edges
     * it represents and its weight is the sum of content edges' weight.
     * @return an edge iterable of all meta edges in the current graph view
     */
    public EdgeIterable getMetaEdges();

    /**
     * Return a unique <code>EdgeIterable</code> for edges and meta edges. The content is the
     * union of <code>getEdges()</code> and <code>getMetaEdges()</code>.
     * @return an edge iterable of all edges and meta edges in the current graph view
     */
    public EdgeIterable getEdgesAndMetaEdges();

    /**
     * Returns meta edges for <code>nodeGroup</code>.
     * @param nodeGroup the node whose meta edges are queried
     * @return an edge iterable of meta edges incident to nodeGroup
     * @throws IllegalArgumentException if <code>nodeGroup</code> is <code>null</code> or not legal in
     * the graph
     */
    public EdgeIterable getMetaEdges(Node nodeGroup);

    /**
     * Returns edges and meta edges incident to <code>node</code>.
     * <p>
     * For <b>directed</b> graph, note that self-loops are repeated only once. <b>Undirected</b>
     * graphs repeats edges once by default.
     * @param node the node whose incident edges and meta edges are to be returned
     * @return an edge iterable of edges and meta edges incident to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code>
     * or not legal in the graph.
     */
    public EdgeIterable getEdgesAndMetaEdges(Node node);

    /**
     * Finds and returns a <b>directed</b> or <b>undirected</b> meta edge that connects <code>node1</code> and
     * <code>node2</code>. Returns <code>null</code> if no such edge is found.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node1 the first incident node of the queried meta edge
     * @param node2 thge second incident node of the queried meta edge
     * @return a meta edge that connects <code>node1</code> and <code>node2</code>
     * or <code>null</code> if no such edge exists
     * @throws IllegalArgumentException if <code>node1</code> or <code>node2</code>
     * are <code>null</code> or not legal nodes in the graph
     */
    public MetaEdge getMetaEdge(Node node1, Node node2);

    /**
     * Returns the degree for node's meta edges.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose meta degree is queried
     * @return the number of meta edges connected to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph.
     */
    public int getMetaDegree(Node node);

    /**
     * Returns the sum of the degree and the meta-edge degree. Equavalent to
     * <code>getDegree(Node) + getMetaDegree(Node)</code>.
     * <p><b>Warning:</b> This method is not thread safe, be sure to call it in a locked
     * statement.
     * @param node the node whose total degree is queried
     * @return the number of meta edges connected to <code>node</code>
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph.
     */
    public int getTotalDegree(Node node);

    /**
     * Clears all meta edges for <code>node</code>.
     * @param node the node whose meta edges will be deleted
     * @throws IllegalArgumentException if <code>node</code> is <code>null</code> of not legal in
     * the graph.
     * @throws IllegalMonitorStateException if the current thread is holding a read lock
     */
    public void clearMetaEdges(Node node);
}
