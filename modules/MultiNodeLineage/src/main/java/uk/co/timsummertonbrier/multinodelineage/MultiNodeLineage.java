package uk.co.timsummertonbrier.multinodelineage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.statistics.spi.Statistics;
import static uk.co.timsummertonbrier.multinodelineage.GetNodeById.getNodeById;

public class MultiNodeLineage implements Statistics {
    
    private List<String> originNodeIds = new ArrayList<>();
    private Map<String, Lineage> lineages;
    private GraphModel graphModel;
    private DirectedGraph graph;
    private List<String> errors;
    
    @Override
    public void execute(GraphModel graphModel) {
        init(graphModel);
        if (!validate()) {
            return;
        }
        originNodeIds.forEach(originNodeId -> calculateLineage(originNodeId));
        recordResultsAsAttributes();
    }
    
    @Override
    public String getReport() {
        if (errors.isEmpty()) {
            return getSuccessfulReport();
        }
        return getErrorReport();
    }
    
    public void setOriginNodeIds(List<String> originNodeIds) {
        this.originNodeIds = originNodeIds;
    }
    
    private void init(GraphModel graphModel) {
        this.graphModel = graphModel;
        graph = graphModel.getDirectedGraphVisible();
        lineages = new HashMap<>();
        errors = new ArrayList<>();
    }
    
    private boolean validate() {
        errors.addAll(new MultiNodeLineageValidator(graphModel, graph, originNodeIds).validate());
        return errors.isEmpty();
    }
    
    private void calculateLineage(String originNodeId) {
        Set<String> ancestorIds = BreadthFirstSearch.run(
            graph, 
            originNodeId, 
            (DirectedGraph g, Node n) -> g.getPredecessors(n)
        );
        ancestorIds.remove(originNodeId);
        
        Set<String> descendantIds = BreadthFirstSearch.run(
            graph, 
            originNodeId, 
            (DirectedGraph g, Node n) -> g.getSuccessors(n)
        );
        descendantIds.remove(originNodeId);
        
        lineages.put(originNodeId, new Lineage(ancestorIds, descendantIds));
    }

    private void recordResultsAsAttributes() {
        setupColumns();
                
        lineages.forEach((originNodeId, lineage) -> {
            getNodeById(graph, originNodeId).setAttribute("IsOrigin", true);
            
            lineage.getAncestorIds().forEach(ancestorId -> {
                Node ancestorNode = getNodeById(graph, ancestorId);
                ancestorNode.setAttribute("IsAncestor", true);
                addNodeIdToList(ancestorNode, "AncestorOf", originNodeId);
            });
            
            lineage.getDescendantIds().forEach(descendantId -> {
                Node descendantNode = getNodeById(graph, descendantId);
                descendantNode.setAttribute("IsDescendant", true);
                addNodeIdToList(descendantNode, "DescendantOf", originNodeId);
            });
        });
    }

    private void setupColumns() {
        Table table = graphModel.getNodeTable();
        recreateColumn(table, "IsOrigin", Boolean.class, false);
        recreateColumn(table, "IsAncestor", Boolean.class, false);
        recreateColumn(table, "IsDescendant", Boolean.class, false);
        recreateColumn(table, "AncestorOf", String.class, "");
        recreateColumn(table, "DescendantOf", String.class, "");
    }

    private <T> Column recreateColumn(Table table, String name, Class<T> type, Object defaultValue) {
        // Remove existing column to delete the results from previous runs
        if (table.hasColumn(name)) {
            table.removeColumn(name);
        }
        return table.addColumn(name, name, type, defaultValue);
    }

    private void addNodeIdToList(Node node, String listColumnName, String idToAdd) {
        String existingList = (String) node.getAttribute(listColumnName);
        
        if (existingList.isEmpty()) {
            node.setAttribute(listColumnName, idToAdd);
        } else {
            node.setAttribute(listColumnName, existingList + "," + idToAdd);
        }
    }

    private String getSuccessfulReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("<p>Multi Node Lineage has run successfully.</p>\n");
        report.append("<p>The results have been recorded as new attributes on each Node (see Data Laboratory).</p>\n");
        
        lineages.forEach((originNodeId, lineage) -> 
            report.append(reportSectionForOneOrigin(originNodeId, lineage))
        );
        
        return report.toString();
    }

    private String getErrorReport() {
        StringBuilder report = new StringBuilder();
        report.append("<p>Error running Multi Node Lineage.</p>\n");
        errors.forEach(error -> report.append("<p>").append(error).append("</p>\n"));
        return report.toString();
    }
    
    private String reportSectionForOneOrigin(String originNodeId, Lineage lineage) {
        return String.format(
            "<p>Node %s has %d ancestors and %d descendants.</p>\n",
            originNodeId,
            lineage.getAncestorIds().size(),
            lineage.getDescendantIds().size()
        );
    }

}