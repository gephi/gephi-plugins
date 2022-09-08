package org.gephi.plugins.neo4j.importer.wizard;

import org.neo4j.driver.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {

    /**
     * Returns true if the value is null or empty, false otherwise.
     */
    public static Boolean isEmptyOrNull(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Create a neo4j driver.
     */
    public static Driver neo4jDriver(String url, String username, String password, Integer fetchSize) {
        if (fetchSize != null && fetchSize > 0)
            return GraphDatabase.driver(url, isEmptyOrNull(password) ? AuthTokens.none() : AuthTokens.basic(username, password), Config.builder().withFetchSize(fetchSize).build());
        else
            return GraphDatabase.driver(url, isEmptyOrNull(password) ? AuthTokens.none() : AuthTokens.basic(username, password));
    }

    /**
     * Check parameters by performing a call to the db.
     */
    public static void neo4jCheckConnection(String url, String username, String password, String dbName) throws Exception {
        // Do checks
        if (isEmptyOrNull(url)) throw new Exception("Url is mandatory");
        if ((!isEmptyOrNull(password) && isEmptyOrNull(username)) || (isEmptyOrNull(password) && !isEmptyOrNull(username)))
            throw new Exception("Username and password are mandatory");

        // Create driver and check connectivity
        try (Driver driver = GraphDatabase.driver(url, isEmptyOrNull(password) ? AuthTokens.none() : AuthTokens.basic(username, password))) {
            driver.verifyConnectivity();
            driver.session(isEmptyOrNull(dbName) ? SessionConfig.defaultConfig() : SessionConfig.forDatabase(dbName)).run("RETURN 1");
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Create a neo4j driver from the Neo4jImporterWizardData.
     */
    public static Driver neo4jWizardDriver() {
        return neo4jWizardDriver(null);
    }

    public static Driver neo4jWizardDriver(Integer fetchSize) {
        if (Neo4jImporterWizardData.dbAuthType == 0)
            return neo4jDriver(Neo4jImporterWizardData.dbUrl, Neo4jImporterWizardData.dbUsername, Neo4jImporterWizardData.dbPassword, fetchSize);
        else
            return neo4jDriver(Neo4jImporterWizardData.dbUrl, null, null, fetchSize);
    }

    /**
     * Check a query against server defined in Neo4jImporterWizardData
     */
    public static void neo4jWizardCheckQuery(String query, List<String> mandatoryFields) throws Exception {
        try (Driver driver = neo4jWizardDriver(10)) {
            Result result = driver.session(Neo4jImporterWizardData.dbName != null ? SessionConfig.forDatabase(Neo4jImporterWizardData.dbName) : SessionConfig.defaultConfig()).run(query);
            if (!result.keys().containsAll(mandatoryFields))
                throw new Exception("Query MUST returns the following field(s) " + mandatoryFields.stream().collect(Collectors.joining(", ")));
            if (result.list().size() == 0) throw new Exception("Query returns no data");
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Get server labels from server defined in Neo4jImporterWizardData
     */
    public static List<String> neo4jWizardGetLabels() {
        try (Driver driver = neo4jWizardDriver()) {
            Session session = driver.session(Neo4jImporterWizardData.dbName != null ? SessionConfig.forDatabase(Neo4jImporterWizardData.dbName) : SessionConfig.defaultConfig());
            return session.readTransaction(tx -> {
                List<String> labels = new ArrayList<>();
                Result rs = tx.run("CALL db.labels()");
                while (rs.hasNext()) {
                    labels.add(rs.next().get(0).asString());
                }
                return labels;
            });
        }
    }

    /**
     * Get server relationship types from server defined in Neo4jImporterWizardData
     */
    public static List<String> neo4jWizardGetRelationshipTypes() {
        try (Driver driver = neo4jWizardDriver()) {
            Session session = driver.session(Neo4jImporterWizardData.dbName != null ? SessionConfig.forDatabase(Neo4jImporterWizardData.dbName) : SessionConfig.defaultConfig());
            return session.readTransaction(tx -> {
                List<String> rels = new ArrayList<>();
                Result rs = tx.run("CALL db.relationshipTypes()");
                while (rs.hasNext()) {
                    rels.add(rs.next().get(0).asString());
                }
                return rels;
            });
        }
    }

}
