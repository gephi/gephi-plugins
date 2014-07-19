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

import static org.gephi.neo4j.plugin.impl.GraphModelImportConverter.Neo4jGraphModel;

import org.gephi.neo4j.plugin.api.GephiToNeo4jMapper;
import org.neo4j.graphdb.GraphDatabaseService;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin Škurla
 */
@ServiceProvider(service=GephiToNeo4jMapper.class)
public class GephiToNeo4jMapperImpl implements GephiToNeo4jMapper {

    @Override
    public long getNeo4jNodeIdFromGephiNodeId(int gephiNodeId) {
        Neo4jGraphModel graphModel = GraphModelImportConverter.getNeo4jModelForCurrentWorkspace();

        return graphModel.getGephiToNeo4jNodeMap().get(gephiNodeId);
    }

    @Override
    public void clearMappers() {
        Neo4jGraphModel graphModel = GraphModelImportConverter.getNeo4jModelForCurrentWorkspace();

        graphModel.getGephiToNeo4jNodeMap().clear();
        graphModel.getNeo4jToGephiNodeMap().clear();
    }

    @Override
    public boolean isNeo4jDatabaseInCurrentWorkspace() {
        Neo4jGraphModel graphModel = GraphModelImportConverter.getNeo4jModelForCurrentWorkspace();

        return graphModel != null;
    }

    @Override
    public GraphDatabaseService getGraphDBFromCurrentWorkspace() {
        Neo4jGraphModel graphModel = GraphModelImportConverter.getNeo4jModelForCurrentWorkspace();

        return graphModel.getGraphDB();
    }
}
