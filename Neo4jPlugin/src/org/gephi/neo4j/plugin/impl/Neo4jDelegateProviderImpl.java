/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
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
package org.gephi.neo4j.plugin.impl;


import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.spi.AttributeValueDelegateProvider;
import org.gephi.data.attributes.spi.GraphItemDelegateFactoryProvider;
import org.gephi.data.attributes.type.AbstractList;
import org.gephi.data.properties.PropertiesColumn;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;


/**
 *
 * @author Martin Škurla
 */
class Neo4jDelegateProviderImpl extends AttributeValueDelegateProvider<Long> {
    private static final Neo4jDelegateProviderImpl instance;

    
    static {
        instance = new Neo4jDelegateProviderImpl();
    }

    public static Neo4jDelegateProviderImpl getInstance() {
        return instance;
    }

    private Neo4jDelegateProviderImpl() {}


    @Override
    public String storageEngineName() {
        return "Neo4j";
    }

    @Override
    public Object getNodeAttributeValue(Long delegateId, AttributeColumn attributeColumn) {
        GraphDatabaseService graphDB = GraphModelImportConverter.getGraphDBForCurrentWorkspace();
        Transaction tx = graphDB.beginTx();
        try {
            final Object value = graphDB.getNodeById(delegateId).getProperty(attributeColumn.getId());
            tx.success();
            return value;
        } finally {
            tx.close();
        }
    }

    @Override
    public void setNodeAttributeValue(Long delegateId, AttributeColumn attributeColumn, Object nodeValue) {
        // will work for both adding new and overriding previous value
        if (nodeValue instanceof AbstractList)
            nodeValue = ListTypeToPrimitiveArrayConvertor.convert(nodeValue);

        GraphDatabaseService graphDB = GraphModelImportConverter.getGraphDBForCurrentWorkspace();
        
        Transaction tx = graphDB.beginTx();
        try {
            graphDB.getNodeById(delegateId).setProperty(attributeColumn.getId(), nodeValue);
            tx.success();
        } finally {
            tx.close();
        }
    }

    @Override
    public void deleteNodeAttributeValue(Long delegateId, AttributeColumn attributeColumn) {
        GraphDatabaseService graphDB = GraphModelImportConverter.getGraphDBForCurrentWorkspace();

        Transaction tx = graphDB.beginTx();
        try {
            graphDB.getNodeById(delegateId).removeProperty(attributeColumn.getId());
            tx.success();
        } finally {
            tx.close();
        }
    }


    @Override
    public Object getEdgeAttributeValue(Long delegateId, AttributeColumn attributeColumn) {
        GraphDatabaseService graphDB = GraphModelImportConverter.getGraphDBForCurrentWorkspace();

        Transaction tx = graphDB.beginTx();
        try {
            Object value;
            if (attributeColumn.getId().equals(PropertiesColumn.NEO4J_RELATIONSHIP_TYPE.getId()))
                value = graphDB.getRelationshipById(delegateId).getType().name();
            else
                value = graphDB.getRelationshipById(delegateId).getProperty(attributeColumn.getId());
            tx.success();
            return value;
        } finally {
            tx.close();
        }
    }

    @Override
    public void setEdgeAttributeValue(Long delegateId, AttributeColumn attributeColumn, Object edgeValue) {
        // will work for both adding new and overriding previous value
        if (edgeValue instanceof AbstractList)
            edgeValue = ListTypeToPrimitiveArrayConvertor.convert(edgeValue);

        GraphDatabaseService graphDB = GraphModelImportConverter.getGraphDBForCurrentWorkspace();

        Transaction tx = graphDB.beginTx();
        try {
            graphDB.getRelationshipById(delegateId).setProperty(attributeColumn.getId(), edgeValue);
            tx.success();
        } finally {
            tx.close();
        }
    }

    @Override
    public void deleteEdgeAttributeValue(Long delegateId, AttributeColumn attributeColumn) {
        GraphDatabaseService graphDB = GraphModelImportConverter.getGraphDBForCurrentWorkspace();

        Transaction tx = graphDB.beginTx();
        try {
            graphDB.getRelationshipById(delegateId).removeProperty(attributeColumn.getId());
            tx.success();
        } finally {
            tx.close();
        }
    }

    public GraphItemDelegateFactoryProvider<Long> graphItemDelegateFactoryProvider() {
        return Neo4jGraphItemDelegateFactoryProviderImpl.getInstance();
    }

    private static class ListTypeToPrimitiveArrayConvertor {
        private ListTypeToPrimitiveArrayConvertor() {}

        static Object convert(Object listTypeObject) {
            return null;//TODO >>> finish conversion
        }
    }

    private static class Neo4jGraphItemDelegateFactoryProviderImpl implements GraphItemDelegateFactoryProvider<Long> {
        private static final Neo4jGraphItemDelegateFactoryProviderImpl instance;
        private static final String DEFAULT_RELATIONSHIP_TYPE_NAME = "";


        static {
            instance = new Neo4jGraphItemDelegateFactoryProviderImpl();
        }

        public static Neo4jGraphItemDelegateFactoryProviderImpl getInstance() {
            return instance;
        }
        

        @Override
        public Long createNode() {
            GraphDatabaseService graphDB = GraphModelImportConverter.getGraphDBForCurrentWorkspace();

            Transaction tx = graphDB.beginTx();
            try {
                long id = graphDB.createNode().getId();
                tx.success();
                return id;
            } finally {
                tx.close();
            }
        }

        @Override
        public void deleteNode(Long nodeId) {
            GraphDatabaseService graphDB = GraphModelImportConverter.getGraphDBForCurrentWorkspace();

            Transaction tx = graphDB.beginTx();
            try {
                graphDB.getNodeById(nodeId).delete();
                tx.success();
            } finally {
                tx.close();
            }
        }

        @Override
        public Long createEdge(Long startNodeId, Long endNodeId) {
            GraphDatabaseService graphDB = GraphModelImportConverter.getGraphDBForCurrentWorkspace();

            Transaction tx = graphDB.beginTx();
            try {
                org.neo4j.graphdb.Node startNode = graphDB.getNodeById(startNodeId);
                org.neo4j.graphdb.Node endNode   = graphDB.getNodeById(endNodeId);

                long id = startNode.createRelationshipTo(endNode,
                                                      DynamicRelationshipType.withName(DEFAULT_RELATIONSHIP_TYPE_NAME)).getId();
                tx.success();
                return id;
            } finally {
                tx.close();
            }
        }

        @Override
        public void deleteEdge(Long edgeId) {
            GraphDatabaseService graphDB = GraphModelImportConverter.getGraphDBForCurrentWorkspace();

            Transaction tx = graphDB.beginTx();
            try {
                graphDB.getRelationshipById(edgeId).delete();
                tx.success();
            } finally {
                tx.close();
            }
        }
    }
}
