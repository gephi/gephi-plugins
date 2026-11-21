package cz.cvut.fit.gephi.multimode;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;

/**
 * Builds the small graphs used by the tests. The nodes carry the value that tells which mode they
 * belong to in a "kind" column.
 */
final class TestGraphs {

    static final String KIND = "kind";

    private TestGraphs() {
    }

    /**
     * Opens a new project, as the user does, and returns the graph model of its workspace. The plugin
     * looks the model up the same way.
     */
    static GraphModel newProjectModel() {
        Lookup.getDefault().lookup(ProjectController.class).newProject();
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        model.getNodeTable().addColumn(KIND, String.class);
        return model;
    }

    static Node addNode(GraphModel model, String id, String kind) {
        Node node = model.factory().newNode(id);
        node.setAttribute(KIND, kind);
        model.getUndirectedGraph().addNode(node);
        return node;
    }

    static Edge addEdge(GraphModel model, Node a, Node b, double weight) {
        UndirectedGraph graph = model.getUndirectedGraph();
        Edge edge = model.factory().newEdge(a, b, 0, weight, false);
        graph.addEdge(edge);
        return edge;
    }
}
