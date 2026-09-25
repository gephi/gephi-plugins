package uk.co.timsummertonbrier.multinodelineage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphModel;
import static uk.co.timsummertonbrier.multinodelineage.GetNodeById.getNodeById;

public class MultiNodeLineageValidator {
    
    private final GraphModel graphModel;
    private final DirectedGraph graph;
    private final List<String> originNodeIds;
    private final List<String> errors;
    private final List<String> notIntNodeIds;
    private final List<String> notFoundNodeIds;
    
    public MultiNodeLineageValidator(GraphModel graphModel, DirectedGraph graph, List<String> originNodeIds) {
        this.graphModel = graphModel;
        this.graph = graph;
        this.originNodeIds = originNodeIds;
        errors = new ArrayList<>();
        notIntNodeIds = new ArrayList<>();
        notFoundNodeIds = new ArrayList<>();
    }
    
    public List<String> validate() {
        validateOriginNodeIdsIsNotEmpty();
        validateGraphIsDirected();
        validateEachNodeId();
        return errors;
    }
    
    private void validateOriginNodeIdsIsNotEmpty() {
        if (originNodeIds.isEmpty()) {
            errors.add("No origin node ID(s) were supplied");
        }
    }
    
    private void validateGraphIsDirected() {
        // In testing calculating lineage on undirected graphs does "work", but
        // it produces confusing results where some nodes are ancestors and some
        // are descendants. In reality on an undirected graph all connected
        // nodes should be ancestors and descendants.
        if (!graphModel.isDirected()) {
            errors.add("This graph is not directed. Calculating lineage only makes sense on directed graphs.");
        }
    }
    
    private void validateEachNodeId() {
        originNodeIds.forEach(id -> checkNodeId(id));
        if (!notIntNodeIds.isEmpty()) {
            errors.add("This graph uses integer node IDs, and the following requested origin node IDs are not integers: " + listToString(notIntNodeIds));
        }
        if (!notFoundNodeIds.isEmpty()) {
            errors.add("The following requested origin node IDs could not be found in the graph: " + listToString(notFoundNodeIds));
        }
    }
     
     private boolean usingIntegerNodeIds() {
         return graphModel.getConfiguration().getNodeIdType() == Integer.class;
     }
     
     private void checkNodeId(String id) {
         if (usingIntegerNodeIds() && !isInteger(id)) {
            notIntNodeIds.add(id);
            // Exit as there is no point in checking if this node id exists
            return;
        }
        if (getNodeById(graph, id) == null) {
            notFoundNodeIds.add(id);
        }
     }
    
    private boolean isInteger(String str) {
        try {
            Integer.valueOf(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String listToString(List<String> list) {
        return list.stream().map(id -> "'" + id + "'").collect(Collectors.joining(", "));
    }

}