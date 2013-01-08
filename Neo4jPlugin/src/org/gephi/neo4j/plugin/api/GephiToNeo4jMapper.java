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
package org.gephi.neo4j.plugin.api;

import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Mapper class useful for Tool implementation.
 *
 * @author Martin Škurla
 */
public interface GephiToNeo4jMapper {

    /**
     * Returns Neo4j node id from given Gephi node id. Tool implementation can get selected Gephi nodes and with this
     * method it is possible to map selected Gephi nodes to appropriate Neo4j nodes for further processing.
     *
     * @param gephiNodeId gephi node id
     * @return neo4j node id
     */
    long getNeo4jNodeIdFromGephiNodeId(int gephiNodeId);

    /**
     * Clear mappers. During Neo4j database import, mappers from Neo4j node ids to Gephi node ids are created. This method
     * will clear both mappers resulting in cleaning memory.
     */
    void clearMappers();

    /**
     * Determines if Neo4j database was imported into current workspace.
     * 
     * @return <b>true</b> if Neo4j database was imported into current workspace<br />
     *         <b>false</b> otherwise
     */
    boolean isNeo4jDatabaseInCurrentWorkspace();

    /**
     * Returns Neo4j GraphDB from current workspace.
     *
     * @return Neo4j GraphDB
     */
    GraphDatabaseService getGraphDBFromCurrentWorkspace();
}
