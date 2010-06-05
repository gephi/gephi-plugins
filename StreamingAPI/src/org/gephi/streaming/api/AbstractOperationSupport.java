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

import java.util.logging.Logger;

/**
 * <p>Base class for implementations of OperationSupport.
 * <p>Classes implementing OperationSupport should subclass this
 * in place of implementing OperationSupport:
 * If you extend this class the implementation of all operations
 * is not mandatory, but if you implement OperationSupport
 * the future addition of new operations could break compatibility.
 * 
 * @author Andre' Panisson
 *
 */
public class AbstractOperationSupport implements OperationSupport {
    
    private static Logger logger =  Logger.getLogger(AbstractOperationSupport.class.getName());

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAdded(java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    @Override
    public void edgeAdded(String edgeId, String fromNodeId, String toNodeId,
            boolean directed) {
        logger.warning("Operation edgeAdded not supported in the class " + this.getClass().getSimpleName());
    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAttributeAdded(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void edgeAttributeAdded(String edgeId, String attributeName,
            Object value) {
        logger.warning("Operation edgeAttributeAdded not supported in the class " + this.getClass().getSimpleName());

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAttributeChanged(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void edgeAttributeChanged(String edgeId, String attributeName,
            Object newValue) {
        logger.warning("Operation edgeAttributeChanged not supported in the class " + this.getClass().getSimpleName());

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeAttributeRemoved(java.lang.String, java.lang.String)
     */
    @Override
    public void edgeAttributeRemoved(String edgeId, String attributeName) {
        logger.warning("Operation edgeAttributeRemoved not supported in the class " + this.getClass().getSimpleName());

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#edgeRemoved(java.lang.String)
     */
    @Override
    public void edgeRemoved(String edgeId) {
        logger.warning("Operation edgeRemoved not supported in the class " + this.getClass().getSimpleName());

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#graphAttributeAdded(java.lang.String, java.lang.Object)
     */
    @Override
    public void graphAttributeAdded(String attributeName, Object value) {
        logger.warning("Operation graphAttributeAdded not supported in the class " + this.getClass().getSimpleName());

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#graphAttributeChanged(java.lang.String, java.lang.Object)
     */
    @Override
    public void graphAttributeChanged(String attributeName, Object newValue) {
        logger.warning("Operation graphAttributeChanged not supported in the class " + this.getClass().getSimpleName());

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#graphAttributeRemoved(java.lang.String)
     */
    @Override
    public void graphAttributeRemoved(String attributeName) {
        logger.warning("Operation graphAttributeRemoved not supported in the class " + this.getClass().getSimpleName());

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAdded(java.lang.String)
     */
    @Override
    public void nodeAdded(String nodeId) {
        logger.warning("Operation nodeAdded not supported in the class " + this.getClass().getSimpleName());

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAttributeAdded(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void nodeAttributeAdded(String nodeId, String attributeName,
            Object value) {
        logger.warning("Operation nodeAttributeAdded not supported in the class " + this.getClass().getSimpleName());

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAttributeChanged(java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void nodeAttributeChanged(String nodeId, String attributeName,
            Object newValue) {
        logger.warning("Operation nodeAttributeChanged not supported in the class " + this.getClass().getSimpleName());

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeAttributeRemoved(java.lang.String, java.lang.String)
     */
    @Override
    public void nodeAttributeRemoved(String nodeId, String attributeName) {
        logger.warning("Operation nodeAttributeRemoved not supported in the class " + this.getClass().getSimpleName());

    }

    /* (non-Javadoc)
     * @see org.gephi.streaming.api.OperationSupport#nodeRemoved(java.lang.String)
     */
    @Override
    public void nodeRemoved(String nodeId) {
        logger.warning("Operation nodeRemoved not supported in the class " + this.getClass().getSimpleName());

    }

}
