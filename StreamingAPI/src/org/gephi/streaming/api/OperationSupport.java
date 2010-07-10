/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.streaming.api;

import java.util.Map;

/**
 * Defines the operations of the Graph Streaming API.<br>
 * Implementations of this interface can be used to implement 
 * what should be done when an event occurred (update the workspace,
 * generate an event, export to a specific format, etc.)
 * 
 * @author Andre' Panisson
 * @see GraphEventOperationSupport
 *
 */
public interface OperationSupport {
    /**
     * A node is added
     * @param nodeId the node identifier
     * @param attributes the node attributes
     */
    void nodeAdded(String nodeId, Map<String, Object> attributes);
    
    /**
     * A node is changed
     * @param nodeId the node identifier
     * @param attributes the node attributes
     */
    void nodeChanged(String nodeId, Map<String, Object> attributes);

    /**
     * A node is removed
     * @param nodeId the node identifier to remove
     */
    void nodeRemoved(String nodeId);

    /**
     * An edge is added
     * 
     * @param edgeId the edge identifier
     * @param fromNodeId the source node identifier
     * @param toNodeId the target node identifier
     * @param directed if the edge is directed
     * @param attributes the edge attributes
     */
    void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed, Map<String, Object> attributes);

    /**
     * An edge is changed
     * @param edgeId the edge identifier
     * @param attributes the edge attributes
     */
    void edgeChanged(String edgeId, Map<String, Object> attributes);
    
    /**
     * An edge is removed
     * 
     * @param edgeId the edge identifier to remove
     */
    void edgeRemoved(String edgeId);

    /**
     * A graph attribute is added
     * 
     * @param attributeName the name of the attribute to add
     * @param value the value of the attribute to add
     */
    void graphAttributeAdded(String attributeName, Object value);

    /**
     * A graph attribute is changed
     * 
     * @param attributeName the name of the attribute to change
     * @param newValue the new value of the attribute
     */
    void graphAttributeChanged(String attributeName,
            Object newValue);

    /**
     * A graph attribute is removed
     * 
     * @param attributeName the name of the attribute to remove
     */
    void graphAttributeRemoved(String attributeName);

}
