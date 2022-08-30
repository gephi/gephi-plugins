package org.gephi.plugins.neo4j.test;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.neo4j.driver.*;
import org.testcontainers.containers.Neo4jContainer;

import java.util.Scanner;

public class Neo4jIntegrationTest {

    @ClassRule
    public static Neo4jContainer neo4j = new Neo4jContainer("neo4j:4.4");

    /**
     * Default constructor.
     *
     * @param file
     */
    public Neo4jIntegrationTest(String file) {
        // init db if needed
        if (file != null) {
            String query = "";
            try (Scanner s = new Scanner(Neo4jIntegrationTest.class.getResourceAsStream(file)).useDelimiter("\\n")) {
                while (s.hasNext()) {
                    query += s.next() + "\n";
                }
            }
            Driver driver = getNeo4jDriver();
            try (Session session = driver.session()) {
                session.run(query);
            }
        }
    }

    /**
     * Retrieve the Neo4j driver instance for the running container.
     */
    public static  Driver getNeo4jDriver() {
        return GraphDatabase.driver(neo4j.getBoltUrl(), AuthTokens.basic("neo4j", neo4j.getAdminPassword()));
    }

    @BeforeClass
    public static void beforeClass() {
        neo4j.start();
    }

    @AfterClass
    public static void  afterClass() {
        neo4j.stop();
    }
}
