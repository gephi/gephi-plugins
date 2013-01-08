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

import java.util.Collection;
import org.neo4j.graphdb.GraphDatabaseService;

/**
 * Exports data to local or remote Neo4j database.
 *
 * @author Martin Škurla
 */
public interface Neo4jExporter {

    void exportDatabase(GraphDatabaseService graphDB, String fromColumn, String defaultValue,
            Collection<String> exportedEdgeColumnNames, Collection<String> exportedNodeColumnNames);
}
