package cz.cvut.fit.gephi.multimode;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
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

    /**
     * Opens another workspace in the current project, as the user does, and returns its graph model. It holds a node
     * and the columns of the model given, as when a file with the same attributes is opened: they are equal to the
     * columns of that model, but they are other columns and can't be read on the nodes of the other graph.
     */
    static GraphModel openOtherWorkspace(GraphModel first) {
        Lookup.getDefault().lookup(ProjectController.class).openNewWorkspace();
        GraphModel model = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        for (Column column : first.getNodeTable().toArray()) {
            if (!model.getNodeTable().hasColumn(column.getId())) {
                model.getNodeTable().addColumn(column.getId(), column.getTypeClass());
            }
        }
        model.getUndirectedGraph().addNode(model.factory().newNode("other"));
        return model;
    }

    static Node addNode(GraphModel model, String id, String kind) {
        Node node = model.factory().newNode(id);
        node.setAttribute(KIND, kind);
        model.getUndirectedGraph().addNode(node);
        return node;
    }

    static Edge addEdge(GraphModel model, Node a, Node b, double weight) {
        return addEdge(model, a, b, weight, false);
    }

    static Edge addEdge(GraphModel model, Node source, Node target, double weight, boolean directed) {
        Edge edge = model.factory().newEdge(source, target, 0, weight, directed);
        model.getGraph().addEdge(edge);
        return edge;
    }
}
