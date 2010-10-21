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
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.remote.RemoteGraphDatabase;

/**
 *
 * @author Martin Škurla
 */
public class Neo4jUtils {

    private Neo4jUtils() {
    }

    public static GraphDatabaseService localDatabase(File neo4jDirectory) {
        return new EmbeddedGraphDatabase(neo4jDirectory.getAbsolutePath());
    }

    public static GraphDatabaseService remoteDatabase(String resourceURI, String login, String password) {
        try {
            if (login == null && password == null) {
                return new RemoteGraphDatabase(resourceURI);
            } else {
                return new RemoteGraphDatabase(resourceURI, login, password);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
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
