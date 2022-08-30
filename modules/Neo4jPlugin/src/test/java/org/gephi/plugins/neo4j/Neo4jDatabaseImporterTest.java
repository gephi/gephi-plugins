package org.gephi.plugins.neo4j;

import org.gephi.io.importer.impl.ImportContainerImpl;
import org.gephi.plugins.neo4j.test.Neo4jIntegrationTest;
import org.junit.Test;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import static org.junit.Assert.assertEquals;

public class Neo4jDatabaseImporterTest extends Neo4jIntegrationTest {

    public Neo4jDatabaseImporterTest() {
        super("/cypher/movie.cyp");
    }

    @Test
    public void import_full_database_should_work (){
        ImportContainerImpl container = new ImportContainerImpl();

        Neo4jDatabaseImporter.setUrl(Neo4jIntegrationTest.neo4j.getBoltUrl());
        Neo4jDatabaseImporter.setPasswd(Neo4jIntegrationTest.neo4j.getAdminPassword());
        Neo4jDatabaseImporter importer = new Neo4jDatabaseImporter();
        importer.execute(container.getLoader());

        try (Session session  = this.getNeo4jDriver().session()) {
            long nbNodes = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH (n) RETURN count(n)");
                return rs.next().get(0).asLong();
            });
            long nbEdges = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH ()-[r]->() RETURN count(r)");
                return rs.next().get(0).asLong();
            });

            assertEquals("Bad node size", nbNodes, container.getNodeCount());
            assertEquals("Bad edge size", nbEdges, container.getEdgeCount());
        }

    }
}
