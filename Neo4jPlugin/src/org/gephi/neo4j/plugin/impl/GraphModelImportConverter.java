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

import gnu.trove.TIntLongHashMap;
import gnu.trove.TLongIntHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.project.api.WorkspaceProvider;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.management.Primitives;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Škurla
 */
public class GraphModelImportConverter {

    private static GraphModelImportConverter singleton;
    private static GraphModel graphModel;
    private static Graph graph;
    private static AttributeModel attributeModel;
    private static Neo4jGraphModel currentNeo4jModel;

    private GraphModelImportConverter() {
    }

    public synchronized static GraphModelImportConverter getInstance(GraphDatabaseService graphDB) {
        if (singleton == null) {
            singleton = new GraphModelImportConverter();
        }

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);

        Workspace currentWorkspace = projectController.newWorkspace(projectController.getCurrentProject());
        projectController.openWorkspace(currentWorkspace);

        currentNeo4jModel = currentWorkspace.getLookup().lookup(Neo4jGraphModel.class);
        if (currentNeo4jModel == null) {
            currentNeo4jModel = new Neo4jGraphModel(graphDB);
            currentWorkspace.add(currentNeo4jModel);
        }

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        graphModel = graphController.getModel();
        graph = graphModel.getGraph();

        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        attributeModel = attributeController.getModel();

        projectController.addWorkspaceListener(new WorkspaceListener() {

            @Override
            public void initialize(Workspace workspace) {
            }

            @Override
            public void select(Workspace workspace) {
            }

            @Override
            public void unselect(Workspace workspace) {
            }

            @Override
            public void close(Workspace workspace) {
                Neo4jGraphModel model = workspace.getLookup().lookup(Neo4jGraphModel.class);
                if (model != null) {
                    model.graphDb.shutdown();
                }
                if (graphModel != null && graphModel.getWorkspace() == workspace) {
                    graphModel = null;
                    graph = null;
                    attributeModel = null;
                    currentNeo4jModel = null;
                }
            }

            @Override
            public void disable() {
            }
        });

        return singleton;
    }

    /**
     * Creates Gephi node representation from Neo4j node and all its property data. If Gephi node
     * doesn't already exist, it will be created with all attached data from Neo4j node, otherwise
     * it will not be created again.
     *
     * @param neoNode Neo4j node
     * @return Gephi node
     */
    public void createGephiNodeFromNeoNode(org.neo4j.graphdb.Node neoNode) {
        org.gephi.graph.api.Node gephiNode = graphModel.factory().newNode();
        graph.addNode(gephiNode);

        fillGephiNodeDataWithNeoNodeData(gephiNode, neoNode);
        currentNeo4jModel.neo4jToGephiNodeMap.put(neoNode.getId(), gephiNode.getId());
        currentNeo4jModel.gephiToNeo4jNodeMap.put(gephiNode.getId(), neoNode.getId());
    }

    private void fillGephiNodeDataWithNeoNodeData(org.gephi.graph.api.Node gephiNode, org.neo4j.graphdb.Node neoNode) {
        AttributeTable nodeTable = attributeModel.getNodeTable();
        Attributes attributes = gephiNode.getNodeData().getAttributes();

        Long neoNodeId = neoNode.getId();
        for (String neoPropertyKey : neoNode.getPropertyKeys()) {
            Object neoPropertyValue = neoNode.getProperty(neoPropertyKey);

            if (!nodeTable.hasColumn(neoPropertyKey)) {
                if (!neoPropertyValue.getClass().isArray()) {
                    nodeTable.addColumn(neoPropertyKey, neoPropertyKey, AttributeType.parse(neoPropertyValue), Neo4jDelegateProviderImpl.getInstance(), null);
                } else {
                    AttributeType attributeType =
                            Neo4jArrayToGephiAttributeTypeMapper.map(neoPropertyValue);

                    nodeTable.addColumn(neoPropertyKey, neoPropertyKey, attributeType, Neo4jDelegateProviderImpl.getInstance(), null);
                }
            }

            if (nodeTable.getColumn(neoPropertyKey).getOrigin() == AttributeOrigin.DELEGATE) {
                attributes.setValue(neoPropertyKey, neoNodeId);
            } else {
                attributes.setValue(neoPropertyKey, neoPropertyValue);
            }
        }
    }

    /**
     * Creates Gephi edge betweeen two Gephi nodes. Graph is traversing through all relationships
     * (edges), so for every Neo4j relationship a Gephi edge will be created.
     *
     * @param startGephiNode  start Gephi node
     * @param endGephiNode    end Gephi node
     * @param neoRelationship Neo4j relationship
     */
    public void createGephiEdge(Relationship neoRelationship) {
        int start = currentNeo4jModel.neo4jToGephiNodeMap.get(neoRelationship.getStartNode().getId());
        int end = currentNeo4jModel.neo4jToGephiNodeMap.get(neoRelationship.getEndNode().getId());
        org.gephi.graph.api.Node startGephiNode = graph.getNode(start);
        org.gephi.graph.api.Node endGephiNode = graph.getNode(end);

        if (startGephiNode != null && endGephiNode != null) {
            Edge gephiEdge = graphModel.factory().newEdge(startGephiNode, endGephiNode);
            graph.addEdge(gephiEdge);

            fillGephiEdgeDataWithNeoRelationshipData(gephiEdge, neoRelationship);
        }
    }

    private void fillGephiEdgeDataWithNeoRelationshipData(Edge gephiEdge, Relationship neoRelationship) {
        AttributeTable edgeTable = attributeModel.getEdgeTable();
        Attributes attributes = gephiEdge.getEdgeData().getAttributes();

        Object neoRelationshipId = neoRelationship.getId();
        for (String neoPropertyKey : neoRelationship.getPropertyKeys()) {
            Object neoPropertyValue = neoRelationship.getProperty(neoPropertyKey);
            if (neoPropertyKey.equalsIgnoreCase("weight")) {
                if (neoPropertyValue instanceof Integer) {
                    neoPropertyValue = ((Integer) neoPropertyValue).floatValue();
                }
            }

        }
        if (!edgeTable.hasColumn(neoPropertyKey)) {
            if (!neoPropertyValue.getClass().isArray()) {
                edgeTable.addColumn(neoPropertyKey, neoPropertyKey, AttributeType.parse(neoPropertyValue), Neo4jDelegateProviderImpl.getInstance(), null);
            } else {
                AttributeType attributeType =
                        Neo4jArrayToGephiAttributeTypeMapper.map(neoPropertyValue);

                edgeTable.addColumn(neoPropertyKey, neoPropertyKey, attributeType, Neo4jDelegateProviderImpl.getInstance(), null);
            }
        }
        if (edgeTable.getColumn(neoPropertyKey).getOrigin() == AttributeOrigin.DELEGATE) {
            attributes.setValue(neoPropertyKey, neoRelationshipId);
        } else {
            attributes.setValue(neoPropertyKey, neoPropertyValue);
        }
    }

    attributes.setValue (PropertiesColumn.NEO4J_RELATIONSHIP_TYPE.getId





(), neoRelationshipId);


}

    public void

createNeo4jRelationshipTypeGephiColumn() {
        PropertiesColumn propertiesColumn = PropertiesColumn.NEO4J_RELATIONSHIP_TYPE;

        attributeModel.

getEdgeTable().addColumn(propertiesColumn.getId(),
                propertiesColumn.getTitle(),
                AttributeType.STRING,
                Neo4jDelegateProviderImpl.getInstance(),
                null);






}

    public static GraphDatabaseService





getGraphDBForCurrentWorkspace() {
        Neo4jGraphModel neo4jmodel = getNeo4jModelForCurrentWorkspace();







return neo4jmodel != null ? neo4jmodel.graphDb
                                  : null;






}

    public static Neo4jGraphModel











    getNeo4jModelForCurrentWorkspace() {
        Workspace currentWorkspace = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();




































               return currentWorkspace.getLookup().lookup(Neo4jGraphModel.class);
    }

    static Collection<GraphDatabaseService> getAllGraphDBs() {
        List<GraphDatabaseService> dbs = new ArrayList<GraphDatabaseService>();
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        for (Workspace w : pc.getCurrentProject().getLookup().lookup(WorkspaceProvider.class).getWorkspaces()) {
            //TODO vypis + debug
            Neo4jGraphModel neo4jmodel = w.getLookup().lookup(Neo4jGraphModel.class);
            if (neo4jmodel != null) {
                dbs.add(neo4jmodel.graphDb);
            }
        }
        return dbs;
    }

    public static class Neo4jGraphModel {

        private final GraphDatabaseService graphDb;
        private final TLongIntHashMap neo4jToGephiNodeMap;
        private final TIntLongHashMap gephiToNeo4jNodeMap;

        public Neo4jGraphModel(GraphDatabaseService graphDb) {
            this.graphDb = graphDb;
            int numberOfNeo4jNodeIds = (int) ((EmbeddedGraphDatabase) graphDb).getManagementBean(Primitives.class).getNumberOfNodeIdsInUse();
            this.neo4jToGephiNodeMap = new TLongIntHashMap(numberOfNeo4jNodeIds, 1f);
            this.gephiToNeo4jNodeMap = new TIntLongHashMap(numberOfNeo4jNodeIds, 1f);
        }

        public TLongIntHashMap getNeo4jToGephiNodeMap() {
            return neo4jToGephiNodeMap;
        }

        public TIntLongHashMap getGephiToNeo4jNodeMap() {
            return gephiToNeo4jNodeMap;
        }

        public GraphDatabaseService getGraphDB() {
            return graphDb;
        }
    }
}
