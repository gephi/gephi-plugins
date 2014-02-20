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
package org.gephi.desktop.neo4j.ui.util;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.TransactionFailureException;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 *
 * @author Martin Škurla
 */
public class Neo4jUtils {

    private Neo4jUtils() {
    }

    public static GraphDatabaseService localDatabase(File neo4jDirectory) throws Neo4jStoreAlreadyInUseException {

        try {
            return new GraphDatabaseFactory().newEmbeddedDatabase(neo4jDirectory.getAbsolutePath());
        }
        catch (TransactionFailureException e) {
            if (e.getCause() instanceof IllegalStateException)
                throw new Neo4jStoreAlreadyInUseException(e);

            throw e;
        }
    }

    public static String[] relationshipTypeNames(GraphDatabaseService graphDB) {
        List<String> relationshipTypeNames = new LinkedList<String>();

        for (RelationshipType relationshipType : graphDB.getRelationshipTypes()) {
            relationshipTypeNames.add(relationshipType.name());
        }

        return relationshipTypeNames.toArray(new String[0]);
    }
}
