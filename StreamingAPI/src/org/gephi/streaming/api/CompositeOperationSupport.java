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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A simple OperationSupport implementation that is able to
 * loop through a list of OperationSupport and delegate the operations
 * to them.
 * 
 * @author Andre' Panisson
 *
 */
public class CompositeOperationSupport extends AbstractOperationSupport {
    
    private List<OperationSupport> operationSupports = new ArrayList<OperationSupport>();
    
    /**
     * Add an OperationSupport to the list of OperationSupports to delegate the operations.
     * 
     * @param operationSupport the OperationSupport to add
     */
    public void addOperationSupport(OperationSupport operationSupport) {
        operationSupports.add(operationSupport);
    }
    
    /**
     * Remove an OperationSupport from the list of OperationSupports
     * 
     * @param operationSupport the OperationSupport to remove
     */
    public void removeOperationSupport(OperationSupport operationSupport) {
        operationSupports.remove(operationSupport);
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAdded(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed) {
        for (OperationSupport writer: operationSupports) {
            writer.edgeAdded(edgeId, fromNodeId, toNodeId, directed);
        }

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAttributeAdded(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void edgeAttributeAdded(String edgeId, String attributeName,
            Object value) {
        for (OperationSupport writer: operationSupports) {
            writer.edgeAttributeAdded(edgeId, attributeName, value);
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAttributeChanged(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void edgeAttributeChanged(String edgeId, String attributeName,
            Object newValue) {
        for (OperationSupport writer: operationSupports) {
            writer.edgeAttributeChanged(edgeId, attributeName, newValue);
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAttributeRemoved(java.lang.String, java.lang.String)
     */
    @Override
    public void edgeAttributeRemoved(String edgeId, String attributeName) {
        for (OperationSupport writer: operationSupports) {
            writer.edgeAttributeRemoved(edgeId, attributeName);
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeRemoved(java.lang.String)
     */
    @Override
    public void edgeRemoved(String edgeId) {
        for (OperationSupport writer: operationSupports) {
            writer.edgeRemoved(edgeId);
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#graphAttributeAdded(java.lang.String, java.lang.Object)
     */
    @Override
    public void graphAttributeAdded(String attributeName, Object value) {
        for (OperationSupport writer: operationSupports) {
            writer.graphAttributeAdded(attributeName, value);
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#graphAttributeChanged(java.lang.String, java.lang.Object)
     */
    @Override
    public void graphAttributeChanged(String attributeName, Object newValue) {
        for (OperationSupport writer: operationSupports) {
            writer.graphAttributeChanged(attributeName, newValue);
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#graphAttributeRemoved(java.lang.String)
     */
    @Override
    public void graphAttributeRemoved(String attributeName) {
        for (OperationSupport writer: operationSupports) {
            writer.graphAttributeRemoved(attributeName);
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAdded(java.lang.String)
     */
    @Override
    public void nodeAdded(String nodeId, Map<String, Object> attributes) {
        for (OperationSupport writer: operationSupports) {
            writer.nodeAdded(nodeId, attributes);
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAttributeAdded(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void nodeAttributeAdded(String nodeId, String attributeName,
            Object value) {
        for (OperationSupport writer: operationSupports) {
            writer.nodeAttributeAdded(nodeId, attributeName, value);
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAttributeChanged(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void nodeAttributeChanged(String nodeId, String attributeName,
            Object newValue) {
        for (OperationSupport writer: operationSupports) {
            writer.nodeAttributeChanged(nodeId, attributeName, newValue);
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAttributeRemoved(java.lang.String, java.lang.String)
     */
    @Override
    public void nodeAttributeRemoved(String nodeId, String attributeName) {
        for (OperationSupport writer: operationSupports) {
            writer.nodeAttributeRemoved(nodeId, attributeName);
        }
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeRemoved(java.lang.String)
     */
    @Override
    public void nodeRemoved(String nodeId) {
        for (OperationSupport writer: operationSupports) {
            writer.nodeRemoved(nodeId);
        }
    }

}
