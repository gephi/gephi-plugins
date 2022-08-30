package org.gephi.plugins.neo4j;

import org.gephi.io.importer.impl.ImportContainerImpl;
import org.gephi.plugins.neo4j.test.Neo4jIntegrationTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class Neo4jDatabaseImporterTest extends Neo4jIntegrationTest {

    @BeforeClass
    public static void before() {
        Neo4jDatabaseImporterTest.loadFile("/cypher/movie.cyp");
    }

    @Test
    public void import_full_database_should_work() {
        ImportContainerImpl container = new ImportContainerImpl();

        Neo4jDatabaseImporter importer = new Neo4jDatabaseImporter();
        importer.setUrl(Neo4jDatabaseImporterTest.neo4j.getBoltUrl());
        importer.setPasswd(Neo4jDatabaseImporterTest.neo4j.getAdminPassword());
        importer.execute(container.getLoader());

        try (Session session = Neo4jDatabaseImporterTest.getNeo4jDriver().session()) {
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

    @Test
    public void import_by_node_and_edge_types_should_work() {
        ImportContainerImpl container = new ImportContainerImpl();

        Neo4jDatabaseImporter importer = new Neo4jDatabaseImporter();
        importer.setUrl(Neo4jDatabaseImporterTest.neo4j.getBoltUrl());
        importer.setPasswd(Neo4jDatabaseImporterTest.neo4j.getAdminPassword());
        importer.setLabels(Arrays.asList("Person"));
        importer.setRelationshipTypes(Arrays.asList("FOLLOWS"));
        importer.execute(container.getLoader());

        try (Session session = Neo4jDatabaseImporterTest.getNeo4jDriver().session()) {
            long nbNodes = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH (n:Person) RETURN count(n)");
                return rs.next().get(0).asLong();
            });
            long nbEdges = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH (:Person)-[r:FOLLOWS]->(:Person) RETURN count(r)");
                return rs.next().get(0).asLong();
            });

            assertEquals("Bad node size", nbNodes, container.getNodeCount());
            assertEquals("Bad edge size", nbEdges, container.getEdgeCount());
        }
    }

    @Test
    public void import_by_node_types_should_work() {
        ImportContainerImpl container = new ImportContainerImpl();

        Neo4jDatabaseImporter importer = new Neo4jDatabaseImporter();
        importer.setUrl(Neo4jDatabaseImporterTest.neo4j.getBoltUrl());
        importer.setPasswd(Neo4jDatabaseImporterTest.neo4j.getAdminPassword());
        importer.setLabels(Arrays.asList("Person"));
        importer.execute(container.getLoader());

        try (Session session = Neo4jDatabaseImporterTest.getNeo4jDriver().session()) {
            long nbNodes = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH (n:Person) RETURN count(n)");
                return rs.next().get(0).asLong();
            });
            long nbEdges = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH (:Person)-[r]->(:Person) RETURN count(r)");
                return rs.next().get(0).asLong();
            });

            assertEquals("Bad node size", nbNodes, container.getNodeCount());
            assertEquals("Bad edge size", nbEdges, container.getEdgeCount());
        }
    }

    @Test
    public void import_by_edge_types_should_work() {
        ImportContainerImpl container = new ImportContainerImpl();

        Neo4jDatabaseImporter importer = new Neo4jDatabaseImporter();
        importer.setUrl(Neo4jDatabaseImporterTest.neo4j.getBoltUrl());
        importer.setPasswd(Neo4jDatabaseImporterTest.neo4j.getAdminPassword());
        importer.setRelationshipTypes(Arrays.asList("FOLLOWS"));
        importer.execute(container.getLoader());

        try (Session session = Neo4jDatabaseImporterTest.getNeo4jDriver().session()) {
            long nbNodes = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH (n) RETURN count(n)");
                return rs.next().get(0).asLong();
            });
            long nbEdges = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH ()-[r:FOLLOWS]->() RETURN count(r)");
                return rs.next().get(0).asLong();
            });

            assertEquals("Bad node size", nbNodes, container.getNodeCount());
            assertEquals("Bad edge size", nbEdges, container.getEdgeCount());
        }
    }

    @Test
    public void import_by_nodes_and_edge_queries_should_work() {
        ImportContainerImpl container = new ImportContainerImpl();
        System.out.println(container.getNodeCount());

        Neo4jDatabaseImporter importer = new Neo4jDatabaseImporter();
        importer.setUrl(Neo4jDatabaseImporterTest.neo4j.getBoltUrl());
        importer.setPasswd(Neo4jDatabaseImporterTest.neo4j.getAdminPassword());
        importer.setNodeQuery("MATCH (n:Person) RETURN id(n) AS id, n.name AS label");
        importer.setEdgeQuery("MATCH (n:Person)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(m:Person) WHERE id(n) < id(m) RETURN  id(n) + '-'+ id(m) AS id, id(n) AS sourceId, id(m) AS targetId, count(m) AS weight");
        importer.execute(container.getLoader());

        try (Session session = Neo4jDatabaseImporterTest.getNeo4jDriver().session()) {
            long nbNodes = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH (n:Person) RETURN count(n)");
                return rs.next().get(0).asLong();
            });
            long nbEdges = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH (n:Person)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(m:Person) WHERE id(n) < id(m) WITH DISTINCT n, m RETURN count(*)");
                return rs.next().get(0).asLong();
            });

            assertEquals("Bad node size", nbNodes, container.getNodeCount());
            assertEquals("Bad edge size", nbEdges, container.getEdgeCount());
        }
    }

}
