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

import org.gephi.data.attributes.api.AttributeColumn;

/**
 * 
 * @author Andre Panisson
 */
public interface ContainerLoader {

    /**
     * @param nodeId
     */
    void nodeAdded(String nodeId);

    /**
     * @param nodeId
     */
    void nodeRemoved(String nodeId);

    /**
     * @param edgeId
     * @param fromNodeId
     * @param toNodeId
     * @param directed
     */
    void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed);

    /**
     * @param edgeId
     */
    void edgeRemoved(String edgeId);

    /**
     * @param edgeId
     * @param attributeColumn
     * @param value
     */
    void edgeAttributeAdded(String edgeId, AttributeColumn attributeColumn, Object value);

    /**
     * @param edgeId
     * @param attributeColumn
     * @param newValue
     */
    void edgeAttributeChanged(String edgeId, AttributeColumn attributeColumn,
            Object newValue);

    /**
     * @param edgeId
     * @param attributeColumn
     */
    void edgeAttributeRemoved(String edgeId, AttributeColumn attributeColumn);

    /**
     * @param attributeColumn
     * @param value
     */
    void graphAttributeAdded(AttributeColumn attributeColumn, Object value);

    /**
     * @param attributeColumn
     * @param newValue
     */
    void graphAttributeChanged(AttributeColumn attributeColumn,
            Object newValue);

    /**
     * @param attributeColumn
     */
    void graphAttributeRemoved(AttributeColumn attributeColumn);

    /**
     * @param nodeId
     * @param attributeColumn
     * @param value
     */
    void nodeAttributeAdded(String nodeId, AttributeColumn attributeColumn,
            Object value);

    /**
     * @param nodeId
     * @param attributeColumn
     * @param newValue
     */
    void nodeAttributeChanged(String nodeId, AttributeColumn attributeColumn,
            Object newValue);

    /**
     * @param nodeId
     * @param attributeColumn
     */
    void nodeAttributeRemoved(String nodeId, AttributeColumn attributeColumn);

}
