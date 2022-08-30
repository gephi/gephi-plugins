package org.gephi.plugins.neo4j;

import org.gephi.io.importer.api.*;
import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.neo4j.driver.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.neo4j.driver.Values.parameters;

public class Neo4jDatabaseImporter implements WizardImporter, LongTask {

    private final static String QUERY_BY_LABELS = "" +
            "MATCH (n) " +
            "WHERE any(label IN labels(n) WHERE label IN $labels) " +
            "RETURN n";

    private final static String QUERY_BY_RELS = "" +
            "MATCH (n)-[r]->(m) " +
            "WHERE type(r) IN $relationshipTypes AND " +
            "      any(label IN labels(n) WHERE label IN $labels) AND " +
            "      any(label IN labels(m) WHERE label IN $labels) " +
            "RETURN r";

    public static ContainerLoader container;
    private static Report report;
    private boolean cancel = false;


    /**
     * Url of Neo4j database.
     * Default is `neo4j://localhost`
     */
    private static String url = "neo4j://localhost";

    /**
     * Username for the Neo4j database.
     * Default is `neo4j`
     */
    private static String username = "neo4j";

    /**
     * Password for the Neo4j database.
     * If not specified, we do not do auth
     */
    private static String passwd;

    /**
     * Database name
     * If not specified, we use the default neo4j database
     */
    private static String DBName;

    /**
     * List of labels that we want to import
     */
    private static List<String> labels;

    /**
     * List of relationship that we want to import
     */
    private static List<String> relationshipTypes;

    /**
     * Cypher query to retrieve nodes
     */
    private static String nodeQuery;

    /**
     * Cypher query to retrieve edges
     */
    private static String edgeQuery;

    /**
     * Neo4J driver instance.
     */
    private Driver driver;

    @Override
    public boolean execute(ContainerLoader containerLoader) {
        this.container = containerLoader;
        this.report = new Report();


        // Create the neo4j driver
        if (this.driver != null) this.driver.close();
        this.driver = GraphDatabase.driver(
                url != null ? url : "neo4j://localhost",
                this.passwd != null ? AuthTokens.basic(this.username, this.passwd) : AuthTokens.none()
        );

        try {
            this.driver.verifyConnectivity();
            // Creating default columns for nodes/edges
            this.getContainer().addNodeColumn("labels", String[].class);
            this.getContainer().addEdgeColumn("type", String.class);

            // Do the related import
            if (this.nodeQuery != null && this.edgeQuery != null) {
                this.doImportByNodeAndEdgeQueries();
            } else {
                this.doImportByNodeAndEdgeTypes(
                        this.labels != null ? this.labels : this.getDbLabels(),
                        this.relationshipTypes != null ? this.relationshipTypes : this.getDbRelationshipTypes()
                );
            }
        } catch (Exception e) {
            this.getReport().logIssue(new Issue(e, Issue.Level.CRITICAL));
        } finally {
            if (this.driver != null) this.driver.close();
        }

        return !cancel;
    }

    /**
     * Do import by specifying node & edge types.
     */
    private void doImportByNodeAndEdgeTypes(List<String> labels, List<String> relationshipTypes) {
        try (Session session = this.driver.session(this.DBName != null ? SessionConfig.forDatabase(this.DBName) : SessionConfig.defaultConfig())) {
            session.readTransaction(tx -> {

                // Query and import nodes
                long nbNodesImported = 0;
                Value params = parameters("labels", labels, "relationshipTypes", relationshipTypes);
                Result nodesResult = tx.run(QUERY_BY_LABELS, params);
                while (nodesResult.hasNext()) {
                    this.mergeNodeInGephi(nodesResult.next().get("n").asNode());
                    nbNodesImported++;
                }
                this.getReport().log(String.format("%s nodes imported", nbNodesImported));

                // Query and import edges
                long nbEdgeImported = 0;
                Result edgesResult = tx.run(QUERY_BY_RELS, params);
                while (edgesResult.hasNext()) {
                    this.mergeEdgeInGephi(edgesResult.next().get("r").asRelationship());
                    nbEdgeImported++;
                }
                this.getReport().log(String.format("%s edges imported", nbEdgeImported));

                return 1;
            });
        }
    }

    /**
     * Do import by specifying node & edge query.
     */
    private void doImportByNodeAndEdgeQueries() {
        try (Session session = this.driver.session(this.DBName != null ? SessionConfig.forDatabase(this.DBName) : SessionConfig.defaultConfig())) {
            session.readTransaction(tx -> {

                // Query nodes
                Result nodesResult = tx.run(this.nodeQuery);
                // Checking for mandatory keys
                if (nodesResult.keys().containsAll(Arrays.asList("id"))) {
                    this.getReport().logIssue(new Issue(new Exception("Node query returns no `id` column"), Issue.Level.CRITICAL));
                    return 1;
                }
                // Import nodes
                long nbNodesImported = 0;
                while (nodesResult.hasNext()) {
                    Record record = nodesResult.next();
                    this.mergeNodeInGephi(
                            record.get("id").asString(),
                            record.containsKey("labels") ? record.get("labels").asList(t -> t.toString()).toArray(new String[0]) : null,
                            record.fields().stream().filter(t -> !Arrays.asList("id", "labels").contains(t.key())).collect(Collectors.toMap(Pair::key, Pair::value))
                    );
                    nbNodesImported++;
                }
                this.getReport().log(String.format("%s nodes imported", nbNodesImported));

                // Query edges
                Result edgesResult = tx.run(this.edgeQuery);
                // Checking for mandatory keys
                if (nodesResult.keys().containsAll(Arrays.asList("id", "sourceId", "targetId"))) {
                    this.getReport().logIssue(new Issue(new Exception("Edge query returns no `id` column"), Issue.Level.CRITICAL));
                    return 1;
                }
                // Query and import edges
                long nbEdgeImported = 0;
                while (edgesResult.hasNext()) {
                    Record record = edgesResult.next();
                    this.mergeEdgeInGephi(
                            record.get("id").asString(),
                            record.containsKey("type") ? record.get("type").asString() : null,
                            record.get("sourceId").asString(),
                            record.get("targetId").asString(),
                            record.fields().stream().filter(t -> !Arrays.asList("id", "type", "sourceId", "targetId").contains(t.key())).collect(Collectors.toMap(Pair::key, Pair::value))
                    );
                    nbEdgeImported++;
                }
                this.getReport().log(String.format("%s edges imported", nbEdgeImported));

                return 1;
            });
        }
    }

    /**
     * Get the list of labels from Neo4j.
     */
    private List<String> getDbLabels() {
        try (Session session = this.driver.session(this.DBName != null ? SessionConfig.forDatabase(this.DBName) : SessionConfig.defaultConfig())) {
            return session.readTransaction(tx -> {
                List<String> labels = new ArrayList();
                Result rs = tx.run("CALL db.labels()");
                while (rs.hasNext()) {
                    labels.add(rs.next().get(0).asString());
                }
                return labels;
            });
        }
    }

    /**
     * Get the list of relationship types from Neo4j.
     */
    private List<String> getDbRelationshipTypes() {
        try (Session session = this.driver.session(this.DBName != null ? SessionConfig.forDatabase(this.DBName) : SessionConfig.defaultConfig())) {
            return session.readTransaction(tx -> {
                List<String> labels = new ArrayList();
                Result rs = tx.run("CALL db.relationshipTypes()");
                while (rs.hasNext()) {
                    labels.add(rs.next().get(0).asString());
                }
                return labels;
            });
        }
    }


    /**
     * Merge a Neo4j node in Gephi container.
     */
    private void mergeNodeInGephi(Node neo4jNode) {
        this.mergeNodeInGephi(
                String.valueOf(neo4jNode.id()),
                StreamSupport.stream(neo4jNode.labels().spliterator(), false).toArray(String[]::new),
                neo4jNode.asMap(value -> value));
    }

    /**
     * Merge a Node in Gephi container.
     *
     * @param id         Neo4j node's ID
     * @param labels     Neo4j node's labels
     * @param attributes Neo4j node's attributes
     */
    private void mergeNodeInGephi(String id, String[] labels, Map<String, Value> attributes) {
        if (!this.getContainer().nodeExists(id)) {
            NodeDraft draft = this.getContainer().factory().newNodeDraft();
            String mainLabel = (labels != null && labels.length > 0) ? labels[0] : "";

            // Setting gephi label
            if (attributes.containsKey("name")) {
                draft.setLabel(attributes.get("name").asString());
            } else if (attributes.containsKey("id")) {
                draft.setLabel(attributes.get("id").asString());
            } else {
                draft.setLabel(id);
            }

            // Setting node label
            draft.setValue("labels", labels);

            this.addNeo4jAttributes(draft, attributes, mainLabel);

            this.getContainer().addNode(draft);
        }
    }

    /**
     * Merge a Neo4j relationship in Gephi container.
     */
    private void mergeEdgeInGephi(Relationship neo4jRel) {
        this.mergeEdgeInGephi(
                String.valueOf(neo4jRel.id()),
                neo4jRel.type(),
                String.valueOf(neo4jRel.startNodeId()),
                String.valueOf(neo4jRel.endNodeId()),
                neo4jRel.asMap(value -> value));
    }

    /**
     * Merge a relationship in Gephi container.
     *
     * @param id         Neo4j rel's ID
     * @param type       Neo4j rel's type
     * @param attributes Neo4j rel's attributes
     */
    private void mergeEdgeInGephi(String id, String type, String sourceId, String targetId, Map<String, Value> attributes) {
        if (!this.getContainer().edgeExists(id) &&
                this.getContainer().nodeExists(sourceId) &&
                this.getContainer().nodeExists(targetId)
        ) {
            EdgeDraft draft = this.getContainer().factory().newEdgeDraft();

            draft.setLabel(type);
            draft.setType(type);
            draft.setSource(this.getContainer().getNode(sourceId));
            draft.setTarget(this.getContainer().getNode(targetId));
            this.addNeo4jAttributes(draft, attributes, type);

            this.getContainer().addEdge(draft);
        }
    }

    private void addNeo4jAttributes(ElementDraft draft, Map<String, Value> attributes, String attributePrefix) {
        attributes.keySet().forEach(key -> {
            Value value = attributes.get(key);
            String gephiColKey = attributePrefix != null ? attributePrefix + "_" + key : key;

            try {
                switch (value.getClass().getSimpleName()) {
                    case "IntegerValue":
                        draft.setValue(gephiColKey, value.asInt());
                        break;
                    case "BooleanValue":
                        draft.setValue(gephiColKey, value.asBoolean());
                        break;
                    case "FloatValue":
                        draft.setValue(gephiColKey, value.asFloat());
                        break;
                    case "StringValue":
                        draft.setValue(gephiColKey, value.toString());
                        break;
                }
            } catch (Exception e) {
                this.getReport().log(String.format("Property %s on node %s has been skipped due to bad type", key, draft.getId()));
            }
        });
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
    }

    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return cancel;
    }

    //
    // Generated setter for import parameters
    //

    public static void setUrl(String url) {
        Neo4jDatabaseImporter.url = url;
    }

    public static void setUsername(String username) {
        Neo4jDatabaseImporter.username = username;
    }

    public static void setPasswd(String passwd) {
        Neo4jDatabaseImporter.passwd = passwd;
    }

    public static void setDBName(String DBName) {
        Neo4jDatabaseImporter.DBName = DBName;
    }

    public static void setLabels(List<String> labels) {
        Neo4jDatabaseImporter.labels = labels;
    }

    public static void setRelationshipTypes(List<String> relationshipTypes) {
        Neo4jDatabaseImporter.relationshipTypes = relationshipTypes;
    }

    public static void setNodeQuery(String nodeQuery) {
        Neo4jDatabaseImporter.nodeQuery = nodeQuery;
    }

    public static void setEdgeQuery(String edgeQuery) {
        Neo4jDatabaseImporter.edgeQuery = edgeQuery;
    }

}
