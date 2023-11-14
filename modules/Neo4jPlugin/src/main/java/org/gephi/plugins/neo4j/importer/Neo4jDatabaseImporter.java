package org.gephi.plugins.neo4j.importer;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import org.gephi.io.importer.api.*;
import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.neo4j.driver.*;
import org.neo4j.driver.reactive.RxResult;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.util.Pair;
import org.openide.util.NotImplementedException;

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

    public ContainerLoader container;
    private Report report;
    private boolean cancel = false;
    private ProgressTicket progressTicket;


    /**
     * Url of Neo4j database.
     * Default is `neo4j://localhost`
     */
    private String url = "neo4j://localhost";

    /**
     * Username for the Neo4j database.
     * Default is `neo4j`
     */
    private String username = "neo4j";

    /**
     * Password for the Neo4j database.
     * If not specified, we do not do auth
     */
    private String passwd;

    /**
     * Database name
     * If not specified, we use the default neo4j database
     */
    private String DBName;

    /**
     * List of labels that we want to import
     */
    private List<String> labels;

    /**
     * List of relationship that we want to import
     */
    private List<String> relationshipTypes;

    /**
     * Cypher query to retrieve nodes
     */
    private String nodeQuery;

    /**
     * Cypher query to retrieve edges
     */
    private String edgeQuery;

    /**
     * Neo4j driver instance.
     */
    private Driver driver;

    @Override
    public boolean execute(ContainerLoader containerLoader) {
        this.container = containerLoader;
        this.report = new Report();
        if (this.progressTicket != null) {
            this.progressTicket.start();
            this.progressTicket.setDisplayName("Neo4j import");
            this.progressTicket.switchToIndeterminate();
        }

        try {
            // Create the neo4j driver
            if (this.progressTicket != null) this.progressTicket.progress("Connecting to neo4j...");
            if (this.driver != null) this.driver.close();
            this.driver = GraphDatabase.driver(
                    url != null ? url : "neo4j://localhost",
                    this.passwd != null ? AuthTokens.basic(this.username, this.passwd) : AuthTokens.none()
            );
            this.driver.verifyConnectivity();

            // Creating default columns for nodes/edges
            this.getContainer().addNodeColumn("labels", String[].class);

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
            if (this.progressTicket != null) this.progressTicket.finish();
        }

        return !cancel;
    }

    /**
     * Do import by specifying node & edge types.
     */
    private void doImportByNodeAndEdgeTypes(List<String> labels, List<String> relationshipTypes) {
        Value parameters = parameters("labels", labels, "relationshipTypes", relationshipTypes);

        // Import nodes
        if (this.progressTicket != null) this.progressTicket.progress("Importing nodes...");
        long nbNodesImported = Flowable.using(
                () -> this.driver.rxSession(this.DBName != null ? SessionConfig.forDatabase(this.DBName) : SessionConfig.defaultConfig()),
                session -> session.readTransaction(tx -> {
                    RxResult result = tx.run(QUERY_BY_LABELS, parameters);
                    return Flowable.fromPublisher(result.records())
                            .map(record -> record.get("n").asNode())
                            .map(this::mergeNodeInGephi)
                            .filter(Boolean::booleanValue)
                            .count()
                            .toFlowable();
                }),
                session -> Observable.fromPublisher(session.close()).subscribe()
        ).blockingSingle();
        this.getReport().log(String.format("%s nodes imported", nbNodesImported));

        // Import edges
        if (this.progressTicket != null) this.progressTicket.progress("Importing edges...");
        long nbEdgesImported = Flowable.using(
                () -> this.driver.rxSession(this.DBName != null ? SessionConfig.forDatabase(this.DBName) : SessionConfig.defaultConfig()),
                session -> session.readTransaction(tx -> {
                    RxResult result = tx.run(QUERY_BY_RELS, parameters);
                    return Flowable.fromPublisher(result.records())
                            .map(record -> record.get("r").asRelationship())
                            .map(this::mergeEdgeInGephi)
                            .filter(Boolean::booleanValue)
                            .count()
                            .toFlowable();
                }),
                session -> Observable.fromPublisher(session.close()).subscribe()
        ).blockingSingle();
        this.getReport().log(String.format("%s edges imported", nbEdgesImported));

    }

    /**
     * Do import by specifying node & edge query.
     */
    private void doImportByNodeAndEdgeQueries() {
        // Import nodes
        if (this.progressTicket != null) this.progressTicket.progress("Importing nodes...");
        long nbNodesImported = Flowable.using(
                () -> this.driver.rxSession(this.DBName != null ? SessionConfig.forDatabase(this.DBName) : SessionConfig.defaultConfig()),
                session -> session.readTransaction(tx -> {
                    RxResult result = tx.run(this.nodeQuery);
                    return Flowable.fromPublisher(result.records())
                            .map(record -> {
                                if (record.get("id").isNull()) {
                                    this.getReport().logIssue(new Issue(new Exception(String.format("Node result %s has no ID", record.toString())), Issue.Level.SEVERE));
                                    return false;
                                }
                                return this.mergeNodeInGephi(
                                        record.get("id").toString(),
                                        record.containsKey("labels") ? record.get("labels").asList(Value::toString).toArray(new String[0]) : null,
                                        record.fields().stream().filter(t -> !Arrays.asList("id", "labels").contains(t.key())).collect(Collectors.toMap(Pair::key, Pair::value))
                                );
                            })
                            .filter(Boolean::booleanValue)
                            .count()
                            .toFlowable();
                }),
                session -> Observable.fromPublisher(session.close()).subscribe()
        ).blockingSingle();
        this.getReport().log(String.format("%s nodes imported", nbNodesImported));

        // Import edges
        if (this.progressTicket != null) this.progressTicket.progress("Importing edges...");
        long nbEdgesImported = Flowable.using(
                () -> this.driver.rxSession(this.DBName != null ? SessionConfig.forDatabase(this.DBName) : SessionConfig.defaultConfig()),
                session -> session.readTransaction(tx -> {
                    RxResult result = tx.run(this.edgeQuery);
                    return Flowable.fromPublisher(result.records())
                            .map(record -> {
                                if (record.get("id").isNull()) {
                                    this.getReport().logIssue(new Issue(new Exception(String.format("Edge result %s has no ID", record.toString())), Issue.Level.SEVERE));
                                    return false;
                                }
                                if (record.get("sourceId").isNull()) {
                                    this.getReport().logIssue(new Issue(new Exception(String.format("Edge %s has no sourceId", record.get("id").asString())), Issue.Level.SEVERE));
                                    return false;
                                }
                                if (record.get("targetId").isNull()) {
                                    this.getReport().logIssue(new Issue(new Exception(String.format("Edge %s has no targetId", record.get("id").asString())), Issue.Level.SEVERE));
                                    return false;
                                }
                                return this.mergeEdgeInGephi(
                                        record.get("id").toString(),
                                        record.containsKey("type") ? record.get("type").asString() : null,
                                        record.get("sourceId").toString(),
                                        record.get("targetId").toString(),
                                        record.fields().stream().filter(t -> !Arrays.asList("id", "type", "sourceId", "targetId").contains(t.key())).collect(Collectors.toMap(Pair::key, Pair::value))
                                );
                            })
                            .filter(Boolean::booleanValue)
                            .count()
                            .toFlowable();
                }),
                session -> Observable.fromPublisher(session.close()).subscribe()
        ).blockingSingle();
        this.getReport().log(String.format("%s edges imported", nbEdgesImported));
    }

    /**
     * Get the list of labels from Neo4j.
     */
    private List<String> getDbLabels() {
        try (Session session = this.driver.session(this.DBName != null ? SessionConfig.forDatabase(this.DBName) : SessionConfig.defaultConfig())) {
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
     * Get the list of relationship types from Neo4j.
     */
    private List<String> getDbRelationshipTypes() {
        try (Session session = this.driver.session(this.DBName != null ? SessionConfig.forDatabase(this.DBName) : SessionConfig.defaultConfig())) {
            return session.readTransaction(tx -> {
                List<String> labels = new ArrayList<>();
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
    private Boolean mergeNodeInGephi(Node neo4jNode) {
        return this.mergeNodeInGephi(
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
    private Boolean mergeNodeInGephi(String id, String[] labels, Map<String, Value> attributes) {
        if (this.getContainer().nodeExists(id)) return false;

        NodeDraft draft = this.getContainer().factory().newNodeDraft(id);
        String mainLabel = (labels != null && labels.length > 0) ? labels[0] : "";

        // Setting gephi label
        if (attributes.containsKey("name")) {
            draft.setLabel(attributes.get("name").toString());
        } else if (attributes.containsKey("id")) {
            draft.setLabel(attributes.get("id").toString());
        } else if (attributes.containsKey("title")) {
            draft.setLabel(attributes.get("title").toString());
        } else {
            draft.setLabel(id);
        }
        // Setting node label
        if (labels != null) draft.setValue("labels", labels);
        // Setting attributes
        this.addNeo4jAttributes(draft, attributes, mainLabel);

        // Add node to gephi
        this.getContainer().addNode(draft);
        return true;
    }

    /**
     * Merge a Neo4j relationship in Gephi container.
     */
    private Boolean mergeEdgeInGephi(Relationship neo4jRel) {
        return this.mergeEdgeInGephi(
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
    private Boolean mergeEdgeInGephi(String id, String type, String sourceId, String
            targetId, Map<String, Value> attributes) {

        // Do some check to validate that the edge can be imported
        if (this.getContainer().edgeExists(id)) return false;
        if (!this.getContainer().nodeExists(sourceId) || !this.getContainer().nodeExists(targetId)) {
            this.report.log(String.format("Edge %s has been skipped due to missing extremity", id));
            return false;
        }

        EdgeDraft draft = this.getContainer().factory().newEdgeDraft(id);
        draft.setLabel(type);
        draft.setType(type);
        draft.setSource(this.getContainer().getNode(sourceId));
        draft.setTarget(this.getContainer().getNode(targetId));

        // Setting gephi Weight if possible
        try {
            if (attributes.get("Weight") != null) {
                draft.setWeight(attributes.get("Weight").asDouble());
            } else if (attributes.get("weight") != null) {
                draft.setWeight(attributes.get("weight").asDouble());
            } else if (attributes.get("score") != null) {
                draft.setWeight(attributes.get("score").asDouble());
            }
        } catch (Exception e) {
            // nothing to do
        }
        this.addNeo4jAttributes(draft, attributes, type);

        // Add edge to Gephi
        this.getContainer().addEdge(draft);
        return true;
    }

    private void addNeo4jAttributes(ElementDraft draft, Map<String, Value> attributes, String attributePrefix) {
        attributes.keySet().forEach(key -> {
            Value value = attributes.get(key);
            String gephiColKey = attributePrefix != null ? attributePrefix + "_" + key : key;
            try {
                Object gephiValue = this.neo4jValueToGephi(value);
                if (gephiValue != null) {
                    draft.setValue(gephiColKey, gephiValue);
                }
            } catch (Exception e) {
                this.getReport().log(String.format("Property %s on %s %s has been skipped: %s", key, draft instanceof NodeDraft ? "node" : "edge", draft.getId(), e.getMessage()));
            }
        });
    }

    private Object neo4jValueToGephi(Value value) {
        Object result = null;
        switch (value.type().name()) {
            case "ANY":
                throw new NotImplementedException("Any value is not implemented");
            case "BOOLEAN":
                result = value.asBoolean();
                break;
            case "BYTES":
                throw new NotImplementedException("Bytes value is not implemented");
            case "DATE_TIME":
                throw new NotImplementedException("DateTime value is not implemented");
            case "DATE":
                throw new NotImplementedException("Date value is not implemented");
            case "DURATION":
                throw new NotImplementedException("Duration value is not implemented");
            case "FLOAT":
                result = value.asFloat();
                break;
            case "INTEGER":
                result = value.asInt();
                break;
            case "LIST":
            case "LIST OF ANY?":
                if (value.asList().size() > 0) {
                    result = toArray(value.asList(this::neo4jValueToGephi));
                }

                break;
            case "LOCAL_DATE_TIME":
                throw new NotImplementedException("LocalDateTime value is not implemented");
            case "LOCAL_TIME":
                throw new NotImplementedException("LocalTime value is not implemented");
            case "MAP":
                throw new NotImplementedException("Map value is not implemented");
            case "NODE":
                throw new NotImplementedException("Node value is not implemented");
            case "NULL":
                result = null;
                break;
            case "NUMBER":
                result = value.asNumber();
                break;
            case "ObjectValueAdapter":
                throw new NotImplementedException("Object value is not implemented");
            case "PATH":
                throw new NotImplementedException("Path value is not implemented");
            case "POINT":
                throw new NotImplementedException("Point  value is not implemented");
            case "RELATIONSHIP":
                throw new NotImplementedException("Relationship value is not implemented");
            case "STRING":
                result = value.asString();
                break;
            case "TIME":
                throw new NotImplementedException("Time value is not implemented");
        }
        return result;
    }

    /**
     * Create an
     *
     * @param list
     * @param <T>
     * @return
     */
    private <T> T[] toArray(List<T> list) {
        Class clazz = list.get(0).getClass(); // check for size and null before
        T[] array = (T[]) java.lang.reflect.Array.newInstance(clazz, list.size());
        return list.toArray(array);
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
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

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public void setDBName(String DBName) {
        this.DBName = DBName;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void setRelationshipTypes(List<String> relationshipTypes) {
        this.relationshipTypes = relationshipTypes;
    }

    public void setNodeQuery(String nodeQuery) {
        this.nodeQuery = nodeQuery;
    }

    public void setEdgeQuery(String edgeQuery) {
        this.edgeQuery = edgeQuery;
    }

}
