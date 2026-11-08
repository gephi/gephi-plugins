package org.bitnine.importer;

import org.gephi.io.importer.api.AbstractDatabase;
import org.gephi.io.importer.api.PropertiesAssociations.EdgeProperties;
import org.gephi.io.importer.api.PropertiesAssociations.NodeProperties;

/**
 *
 * @author dehowefeng
 */
public class AgensGraphDatabaseImpl extends AbstractDatabase{

    private String nodeQuery;
    private String edgeQuery;
    private String nodeAttributesQuery;
    private String edgeAttributesQuery;
    private String graphPath;

    public AgensGraphDatabaseImpl() {

        //Default node associations
        properties.addNodePropertyAssociation(NodeProperties.ID, "id");
        properties.addNodePropertyAssociation(NodeProperties.LABEL, "label");
        properties.addNodePropertyAssociation(NodeProperties.X, "x");
        properties.addNodePropertyAssociation(NodeProperties.Y, "y");
        properties.addNodePropertyAssociation(NodeProperties.SIZE, "size");
        properties.addNodePropertyAssociation(NodeProperties.COLOR, "color");
        properties.addNodePropertyAssociation(NodeProperties.START, "start");
        properties.addNodePropertyAssociation(NodeProperties.END, "end");
        properties.addNodePropertyAssociation(NodeProperties.START, "start_open");
        properties.addNodePropertyAssociation(NodeProperties.END_OPEN, "end_open");

        //Default edge associations
        properties.addEdgePropertyAssociation(EdgeProperties.ID, "id");
        properties.addEdgePropertyAssociation(EdgeProperties.SOURCE, "source");
        properties.addEdgePropertyAssociation(EdgeProperties.TARGET, "target");
        properties.addEdgePropertyAssociation(EdgeProperties.LABEL, "label");
        properties.addEdgePropertyAssociation(EdgeProperties.WEIGHT, "weight");
        properties.addNodePropertyAssociation(NodeProperties.COLOR, "color");
        properties.addEdgePropertyAssociation(EdgeProperties.START, "start");
        properties.addEdgePropertyAssociation(EdgeProperties.END, "end");
        properties.addEdgePropertyAssociation(EdgeProperties.START, "start_open");
        properties.addEdgePropertyAssociation(EdgeProperties.END_OPEN, "end_open");
    }
    
    public String getGraphPath() {
        return graphPath;
    }

    public void setGraphPath(String graphPath) {
        this.graphPath = graphPath;
    }

    public String getEdgeAttributesQuery() {
        return edgeAttributesQuery;
    }

    public void setEdgeAttributesQuery(String edgeAttributesQuery) {
        this.edgeAttributesQuery = edgeAttributesQuery;
    }

    public String getEdgeQuery() {
        return edgeQuery;
    }

    public void setEdgeQuery(String edgeQuery) {
        this.edgeQuery = edgeQuery;
    }

    public String getNodeAttributesQuery() {
        return nodeAttributesQuery;
    }

    public void setNodeAttributesQuery(String nodeAttributesQuery) {
        this.nodeAttributesQuery = nodeAttributesQuery;
    }

    public String getNodeQuery() {
        return nodeQuery;
    }

    public void setNodeQuery(String nodeQuery) {
        this.nodeQuery = nodeQuery;
    }
}
