package org.gephi.plugins.neo4j.importer.wizard;

import java.util.List;

public class Neo4jImporterWizardData {
    /**
     * Neo4j DB Url (ex: neo4j://localhost)
     */
    public static String dbUrl = null;

    /**
     * Neo4j database name.
     * If empty, we use the default one.
     */
    public static String dbName = null;

    /**
     * Neo4j authentication mechanism
     * - 0 -> login/password
     * - 1 -> no auth
     */
    public static Integer dbAuthType = null;

    /**
     * Neo4j username
     */
    public static String dbUsername = null;

    /**
     * Neo4j password
     */
    public static String dbPassword = null;

    /**
     * Mode for the import
     * - 0 -> by types
     * - 1 -> by queries
     */
    public static Integer importMode = null;

    /**
     * Neo4j labels to import
     * If null we import every labels
     */
    public static List<String> labels = null;

    /**
     * Neo4j types to import
     * If null we import every types
     */
    public static List<String> relationshipTypes = null;

    /**
     * Cypher query for nodes to import
     */
    public static String nodeQuery = null;

    /**
     * Cypher query for edges to import
     */
    public static String edgeQuery = null;
}
