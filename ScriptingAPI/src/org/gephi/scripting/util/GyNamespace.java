/*
Copyright 2008-2011 Gephi
Authors : Luiz Ribeiro <luizribeiro@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.scripting.util;

import java.util.regex.Pattern;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.filters.api.FilterController;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
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
    private Workspace workspace;
    /** Workspace's graph model */
    private GraphModel graphModel;
    /** Workspace's attribute model */
    private AttributeModel attributeModel;

    public GyNamespace(Workspace workspace) {
        this.workspace = workspace;
        this.graphModel = workspace.getLookup().lookup(GraphModel.class);
        this.attributeModel = workspace.getLookup().lookup(AttributeModel.class);
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
    public GyNode getGyNode(int id) {
        return (GyNode) __finditem__(NODE_PREFIX + id);
    }

    /**
     * Returns a GyEdge wrapper for the edge given by the id.
     * @param id    the edge's internal id
     * @return      GyEdge wrapper for the given edge
     */
    public GyEdge getGyEdge(int id) {
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
                // TODO: throw an exception
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
        } else if (key.startsWith(NODE_PREFIX)) {
            // Checks if key matches a node reserved variable name
            String id = key.substring(NODE_PREFIX.length());
            if (Pattern.compile("[0-9]+").matcher(id).matches()) {
                throw Py.NameError(key + " is a reserved variable name.");
            }
        } else if (key.startsWith(EDGE_PREFIX)) {
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
            String strId = key.substring(EDGE_PREFIX.length());
            if (Pattern.compile("[1-9][0-9]*").matcher(strId).matches()) {
                int id = Integer.parseInt(strId);
                Graph graph = graphModel.getGraph();
                Node node = graph.getNode(id);
                if (node != null) {
                    ret = new GyNode(this, node);
                }
            }
        } else if (key.startsWith(EDGE_PREFIX)) {
            // Check if it is an edge
            String strId = key.substring(EDGE_PREFIX.length());
            if (Pattern.compile("[1-9][0-9]*").matcher(strId).matches()) {
                int id = Integer.parseInt(strId);
                Graph graph = graphModel.getGraph();
                Edge edge = graph.getEdge(id);
                if (edge != null) {
                    ret = new GyEdge(this, edge);
                }
            }
        }

        if (ret != null) {
            // Update the namespace binding, in case something was found
            super.__setitem__(key, ret);
        } else {
            // Check if it is an attribute column
            // Note that attribute columns are not stored in the namespace binding
            // and are always looked up by the namespace
            AttributeColumn nodeColumn = attributeModel.getNodeTable().getColumn(key);
            AttributeColumn edgeColumn = attributeModel.getEdgeTable().getColumn(key);

            if (nodeColumn != null && edgeColumn != null) {
                // Found a node attribute column and also an edge attribute column
                // with the same name, so we throw an error
                throw Py.NameError("name '" + key + "' is an ambiguous column name");
            } else if (nodeColumn != null) {
                // Found a node attribute column
                ret = new GyAttributeColumn(this, nodeColumn);
            } else if (edgeColumn != null) {
                // Found an edge attribute column
                ret = new GyAttributeColumn(this, edgeColumn);
            }
        }

        return ret;
    }
}
