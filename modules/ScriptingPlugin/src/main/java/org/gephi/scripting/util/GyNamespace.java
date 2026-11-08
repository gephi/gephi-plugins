/*
Copyright 2008-2012 Gephi
Authors : Luiz Ribeiro <luizribeiro@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2012 Gephi Consortium. All rights reserved.

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
package org.gephi.scripting.util;
import java.util.regex.Pattern;
import org.gephi.graph.api.Column; // look up dependencies!
import org.gephi.filters.api.FilterController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;  // set dependency to Gephi 0.9!
import org.gephi.graph.api.Node;
import org.gephi.project.api.Workspace;
import org.gephi.scripting.wrappers.GyAttributeColumn;
import org.gephi.scripting.wrappers.GyAttributeTopology;
import org.gephi.scripting.wrappers.GyEdge;
import org.gephi.scripting.wrappers.GyGraph;
import org.python.core.PyObject;
import org.gephi.scripting.wrappers.GyNode;
import org.gephi.scripting.wrappers.GySubGraph;
import org.openide.util.Lookup;
import org.python.core.Py;
import org.python.core.PyStringMap;


/**
 * Class that implements a namespace in Gython.
 * 
 * Namespaces are what Python uses to keep track of variables. Hence, a
 * namespace is just like a dictionary where the keys are the names of variables
 * and the dictionary values are the values of those variables.
 * 
 * Since namespaces are just dictionaries, this class extends PyStringMap, which
 * is used as the default namespace on Jython. This class is extended on Gython
 * to perform extra functionality related to Gephi and Gython itself (e.g.
 * lookups for nodes, edges, etc).
 *
 * @author Luiz Ribeiro
 */
public final class GyNamespace extends PyStringMap {

    /** String constant that prefixes nodes' variables names */
    public static final String NODE_PREFIX = "v";
    /** String constant that prefixes edges' variables names */
    public static final String EDGE_PREFIX = "e";
    /** Main graph's variable name */
    public static final String GRAPH_NAME = "g";
    /** Visible subgraph's variable name */
    public static final String VISIBLE_NAME = "visible";
    /** Workspace related to this namespace */
    private final Workspace workspace;
    /** Workspace's graph model */
    private final GraphModel graphModel; // use public Table getNodeTable(); and  public Table getEdgeTable(); instead of AttributeModel
    /** Workspace's attribute model */
    //private AttributeModel attributeModel;  // AttributeModel has been Removed in Gephi 0.9. Get Attributes directly from GraphModel!
                            

    public GyNamespace(Workspace workspace) {
        this.workspace = workspace;
        this.graphModel = workspace.getLookup().lookup(GraphModel.class);
        //this.attributeModel = workspace.getLookup().lookup(AttributeModel.class);
    }

    /**
     * Returns the workspace's graph model.
     * @return      the workspace's graph model
     */
    public GraphModel getGraphModel() {
        return graphModel;
    }

    /**
     * Returns the workspace related to this namespace.
     * @return      the workspace related to this namespace
     */
    public Workspace getWorkspace() {
        return workspace;
    }

    /**
     * Returns a GyNode wrapper for the node given by the id.
     * @param id    the node's internal id
     * @return      GyNode wrapper for the given node
     */
    public GyNode getGyNode(String id) {
        return (GyNode) __finditem__(NODE_PREFIX + id);
    }

    /**
     * Returns a GyEdge wrapper for the edge given by the id.
     * @param id    the edge's internal id
     * @return      GyEdge wrapper for the given edge
     */
    public GyEdge getGyEdge(String id) {
        return (GyEdge) __finditem__(EDGE_PREFIX + id);
    }

    /**
     * Assigns an object to the given key.
     * 
     * Before assigning the object to the given key, this method checks if the
     * key matches any of the reserved variable names. In a positive case, the
     * method will act accordingly. If the key equals to VISIBLE_NAME, for
     * example, this method will update the current visible view to the given
     * subgraph.
     * 
     * Also, if the user tries to assign something to the main graph's variable
     * (or any other reserved variable name), this method will throw an
     * exception to warn the user.
     * 
     * @param key   the key to which the object will be assigned to
     * @param value the object that will be assigned
     */
    @Override
    public void __setitem__(String key, PyObject value) {
        // The user shouldn't be able to set any of the variables that match
        // the regular expressions NODE_PREFIX[0-9]+ or EDGE_PREFIX[0-9]+

        if (key.equals(VISIBLE_NAME)) {
            // Checks if the key is the variable name of the visible subgraph
            if (value instanceof GySubGraph) {
                GySubGraph subGraph = (GySubGraph) value;
                FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
                filterController.setCurrentQuery(subGraph.getFilter().getUnderlyingQuery());
                getGraphModel().setVisibleView(subGraph.getUnderlyingGraphView());
                return;
            } else {
                throw Py.TypeError("Passed object is not a valid subgraph.");
            }
        } else if (key.equals(GRAPH_NAME)) {
            // Checks if the key is the variable name of the main graph
            throw Py.NameError(key + " is a reserved variable name.");
        } else if (key.equals("degree")) {
            // Checks if the key is the degree topology attribute
            throw Py.NameError(key + " is a reserved variable name.");
        } else if (key.equals("indegree")) {
            // Checks if the key is the in degree topology attribute
            throw Py.NameError(key + " is a reserved variable name.");
        } else if (key.equals("outdegree")) {
            // Checks if the key is the out degree topology attribute
            throw Py.NameError(key + " is a reserved variable name.");
        } else if (key.startsWith(NODE_PREFIX) && key.length() > 1) {
            // Checks if key matches a node reserved variable name
            String id = key.substring(NODE_PREFIX.length());
            if (Pattern.compile("[0-9]+").matcher(id).matches()) {
                throw Py.NameError(key + " is a reserved variable name.");
            }
        } else if (key.startsWith(EDGE_PREFIX) && key.length() > 1) {
            // Checks if key matches an edge reserved variable name
            String id = key.substring(EDGE_PREFIX.length());
            if (Pattern.compile("[0-9]+").matcher(id).matches()) {
                throw Py.NameError(key + " is a reserved variable name.");
            }
        }

        // If everything ok, set the binding on the namespace
        super.__setitem__(key, value);
    }

    /**
     * Deletes from the namespace the object assigned to the given key.
     * 
     * Before effectively deleting the object from the namespace, this method
     * checks if the key matches one of the reserved variable names. This is
     * done for preventing that an unaware user deletes a readonly variable
     * like, for instance, the main graph.
     * 
     * In case the given object is a node or an edge, the reference is removed
     * from the namespace and the entity is also removed from the graph.
     * 
     * Note that deleting an object from the namespace does not guarantee that
     * the object will be removed from memory. This is the case when there are
     * multiple references to the object on the local namespace (or anywhere
     * else). Remember that the object is only removed from the memory when
     * there are no more references to it when the JVM's garbage collector runs.
     * 
     * @param key   the key of the object to be deleted from the namespace
     */
    @Override
    public void __delitem__(String key) {
        // If the user tries to delete a node's binding from the namespace, it
        // will delete the node from the graph accordingly

        PyObject object = __finditem__(key);
        if (object instanceof GyNode) {
            GyNode node = (GyNode) object;
            graphModel.getGraph().removeNode(node.getNode());
        } else if (object instanceof GyEdge) {
            GyEdge node = (GyEdge) object;
            graphModel.getGraph().removeEdge(node.getEdge());
        } else if (object instanceof GyGraph) {
            throw Py.NameError(key + " is a readonly variable.");
        } else if (key.equals(VISIBLE_NAME)) {
            throw Py.NameError(key + " cannot be deleted.");
        } else if (key.equals("degree")) {
            // Checks if the key is the degree topology attribute
            throw Py.NameError(key + " is a reserved variable name.");
        } else if (key.equals("indegree")) {
            // Checks if the key is the in degree topology attribute
            throw Py.NameError(key + " is a reserved variable name.");
        } else if (key.equals("outdegree")) {
            // Checks if the key is the out degree topology attribute
            throw Py.NameError(key + " is a reserved variable name.");
        }

        // Effectively delete the binding from the namespace
        super.__delitem__(key);
    }

    /**
     * Performs a lookup for the given key in the namespace.
     * 
     * Note that before effectively looking in the namespace's string map, this
     * method checks if the key matches one of the reserved variable names
     * (e.g. a node, an edge or the main graph). If the key matches one of the
     * reserved variable names, a wrapper for the entity is instantiated and
     * added to the namespace's string map before returning it.
     * 
     * In case nothing matches the given key on the namespace, null is returned.
     * 
     * @param key   lookup key
     * @return      the object assigned to the key or null if nothing was found
     */
    @Override
    public PyObject __finditem__(String key) {
        PyObject ret = super.__finditem__(key);

        if (ret != null) {
            // Object is already on the namespace
            return ret;
        }

        // Got a namespace lookup failure

        if (key.matches(VISIBLE_NAME)) {
            // Check if it is the visible subgraph
            FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
            return new GySubGraph(this, filterController.getModel().getCurrentQuery());
        } else if (key.matches(GRAPH_NAME)) {
            // Check if it is the main graph
            ret = new GyGraph(this);
        } else if (key.equals("degree")) {
            // Checks if the key is the degree topology attribute
            ret = new GyAttributeTopology(this, GyAttributeTopology.Type.DEGREE);
        } else if (key.equals("indegree")) {
            // Checks if the key is the in degree topology attribute
            ret = new GyAttributeTopology(this, GyAttributeTopology.Type.IN_DEGREE);
        } else if (key.equals("outdegree")) {
            // Checks if the key is the out degree topology attribute
            ret = new GyAttributeTopology(this, GyAttributeTopology.Type.OUT_DEGREE);
        } else if (key.startsWith(NODE_PREFIX)) {
            // Check if it is a node
            String strId = key.substring(NODE_PREFIX.length());
            Graph graph = graphModel.getGraph();
            Node node = graph.getNode(strId);
            if (node != null) {
                ret = new GyNode(this, node);
            }
        } else if (key.startsWith(EDGE_PREFIX)) {
            // Check if it is an edge
            String strId = key.substring(EDGE_PREFIX.length());
            Graph graph = graphModel.getGraph();
            Edge edge = graph.getEdge(strId);
            if (edge != null) {
                ret = new GyEdge(this, edge);
            }
        }
        
        if(ret == null){
            // Check if it is an attribute column
            // Note that attribute columns are not stored in the namespace binding
            // and are always looked up by the namespace

            // get attribute tables from GraphModel!
            //AttributeColumn nodeColumn = attributeModel.getNodeTable().getColumn(key);
            //AttributeColumn edgeColumn = attributeModel.getEdgeTable().getColumn(key);
            Column edgeColumn;
            edgeColumn = graphModel.getEdgeTable().getColumn(key);
            Column nodeColumn;
            nodeColumn = graphModel.getNodeTable().getColumn(key);
            if (nodeColumn != null && edgeColumn != null) {
                // Found a node attribute column and also an edge attribute column
                // with the same name, so we throw an error
                throw Py.NameError("name '" + key + "' is an ambiguous column name");
            } else if (nodeColumn != null) {
                // Found a node attribute column
                ret = new GyAttributeColumn(this, (Column) nodeColumn);
            } else if (edgeColumn != null) {
                // Found an edge attribute column
                ret = new GyAttributeColumn(this, (Column) edgeColumn);
            }
        }

        return ret;
    }
}
