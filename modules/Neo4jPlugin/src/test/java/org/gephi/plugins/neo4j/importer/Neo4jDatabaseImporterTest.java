package org.gephi.plugins.neo4j.importer;

import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.impl.ImportContainerImpl;
import org.gephi.plugins.neo4j.AbstractNeo4jIntegrationTest;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class Neo4jDatabaseImporterTest extends AbstractNeo4jIntegrationTest {

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

            this.logReport(importer.getReport());
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

            this.logReport(importer.getReport());
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

            this.logReport(importer.getReport());
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

            this.logReport(importer.getReport());
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
        importer.setEdgeQuery("MATCH (p1:Person)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(p2:Person) WHERE id(p1) < id(p2) RETURN  id(p1) + '-' + id(p2) AS id, id(p1) AS sourceId, id(p2) AS targetId, count(m) AS weight");
        importer.execute(container.getLoader());

        try (Session session = Neo4jDatabaseImporterTest.getNeo4jDriver().session()) {
            long nbNodes = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH (n:Person) RETURN count(n)");
                return rs.next().get(0).asLong();
            });
            long nbEdges = session.readTransaction(tx -> {
                Result rs = tx.run("MATCH (p1:Person)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(p2:Person) WHERE id(p1) < id(p2) WITH DISTINCT p1, p2 RETURN count(*)");
                return rs.next().get(0).asLong();
            });

            assertEquals("Bad node size", nbNodes, container.getNodeCount());
            assertEquals("Bad edge size", nbEdges, container.getEdgeCount());

            this.logReport(importer.getReport());
        }
    }

    private void logReport(Report report) {
        System.out.println(report.getText());
        report.getIssues(100).forEachRemaining(i -> System.out.println(i.getMessage()));
    }

}
