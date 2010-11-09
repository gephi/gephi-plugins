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
import java.util.Collection;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.properties.PropertiesColumn;
import org.gephi.graph.api.Attributes;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Škurla
 */
public class GraphModelExportConverter {

    private static TIntLongHashMap gephiNodeIdToNeoNodeIdMapper;
    private static GraphModelExportConverter singleton;
    private static GraphModel graphModel;
    private static AttributeModel attributeModel;
    private static GraphDatabaseService graphDB;

    private GraphModelExportConverter() {
    }

    public void reset() {
        graphDB = null;
        graphModel = null;
        attributeModel = null;
        gephiNodeIdToNeoNodeIdMapper = null;
    }

    public synchronized static GraphModelExportConverter getInstance(GraphDatabaseService graphDB) {
        if (singleton == null) {
            singleton = new GraphModelExportConverter();
        }

        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        graphModel = graphController.getModel();

        AttributeController attributeController = Lookup.getDefault().lookup(AttributeController.class);
        attributeModel = attributeController.getModel();

        GraphModelExportConverter.graphDB = graphDB;

        int numberOfGephiNodes = graphModel.getGraph().getNodeCount();
        gephiNodeIdToNeoNodeIdMapper = new TIntLongHashMap(numberOfGephiNodes);

        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
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
                if (graphModel != null && graphModel.getWorkspace() == workspace) {
                    graphModel = null;
                    attributeModel = null;
                    gephiNodeIdToNeoNodeIdMapper = null;
                }
            }

            @Override
            public void disable() {
            }
        });

        return singleton;
    }

    public void createNeoNodeFromGephiNode(org.gephi.graph.api.Node gephiNode,
            Collection<String> exportedNodeColumnNames) {

        org.neo4j.graphdb.Node neoNode = graphDB.createNode();
        fillNeoNodeDataFromGephiNodeData(neoNode, gephiNode, exportedNodeColumnNames);

        gephiNodeIdToNeoNodeIdMapper.put(gephiNode.getId(), neoNode.getId());
    }

    private void fillNeoNodeDataFromGephiNodeData(org.neo4j.graphdb.Node neoNode, org.gephi.graph.api.Node gephiNode,
            Collection<String> exportedNodeColumnNames) {
        Attributes attributes = gephiNode.getNodeData().getAttributes();

        for (AttributeColumn attributeColumn : attributeModel.getNodeTable().getColumns()) {
            if (exportedNodeColumnNames.contains(attributeColumn.getTitle())) {
                Object attributeValue = ((AttributeRow) attributes).getValue(attributeColumn);

                if (attributeValue != null) {
                    neoNode.setProperty(attributeColumn.getId(), attributeValue);
                }
            }
        }
    }

    public void createNeoRelationship(Edge gephiEdge, Collection<String> exportedEdgeColumnNames,
            String fromColumn, String defaultValue) {
        Long startNeoNodeId = gephiNodeIdToNeoNodeIdMapper.get(gephiEdge.getSource().getId());
        Long endNeoNodeId = gephiNodeIdToNeoNodeIdMapper.get(gephiEdge.getTarget().getId());

        if (startNeoNodeId != null && endNeoNodeId != null) {
            org.neo4j.graphdb.Node startNeoNode = graphDB.getNodeById(startNeoNodeId);
            org.neo4j.graphdb.Node endNeoNode = graphDB.getNodeById(endNeoNodeId);

            Relationship neoRelationship =
                    startNeoNode.createRelationshipTo(endNeoNode,
                    DynamicRelationshipType.withName(getRelationshipTypeName(gephiEdge,
                    fromColumn,
                    defaultValue)));
            fillNeoRelationshipDataFromGephiEdgeData(neoRelationship, gephiEdge, exportedEdgeColumnNames);
        }
    }

    private void fillNeoRelationshipDataFromGephiEdgeData(Relationship neoRelationship, Edge gephiEdge,
            Collection<String> exportedEdgeColumnNames) {
        Attributes attributes = gephiEdge.getEdgeData().getAttributes();

        for (AttributeColumn attributeColumn : attributeModel.getEdgeTable().getColumns()) {
            if (exportedEdgeColumnNames.contains(attributeColumn.getId())) {
                Object attributeValue = ((AttributeRow) attributes).getValue(attributeColumn);

                if (attributeValue != null
                        && !attributeColumn.getId().equals(PropertiesColumn.NEO4J_RELATIONSHIP_TYPE.getId())) {
                    neoRelationship.setProperty(attributeColumn.getId(), attributeValue);
                }
            }
        }
    }

    private String getRelationshipTypeName(Edge gephiEdge, String fromColumn, String defaultValue) {
        AttributeColumn attributeColumn = attributeModel.getEdgeTable().getColumn(fromColumn);
        Object relationshipTypeName =
                ((AttributeRow) gephiEdge.getEdgeData().getAttributes()).getValue(attributeColumn);

        return (relationshipTypeName != null) ? relationshipTypeName.toString()
                : defaultValue;
    }
}
