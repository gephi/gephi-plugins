package org.openuniversityofisrael.katzcentrality;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.openide.util.Lookup;

public class GraphGenerator {
    public static GraphModel generateGraphWithoutEdges() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node n0 = graphModel.factory().newNode(((Integer) 0).toString());
        Node n1 = graphModel.factory().newNode(((Integer) 1).toString());
        Node n2 = graphModel.factory().newNode(((Integer) 2).toString());

        directedGraph.addNode(n0);
        directedGraph.addNode(n1);
        directedGraph.addNode(n2);

        return graphModel;
    }

    public static GraphModel generateUndirectedGraphWithSelfLoop() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        UndirectedGraph undirectedGraph = graphModel.getUndirectedGraph();
        Node n0 = graphModel.factory().newNode(((Integer) 0).toString());
        Node n1 = graphModel.factory().newNode(((Integer) 1).toString());
        Node n2 = graphModel.factory().newNode(((Integer) 2).toString());
        Node n3 = graphModel.factory().newNode(((Integer) 3).toString());

        undirectedGraph.addNode(n0);
        undirectedGraph.addNode(n1);
        undirectedGraph.addNode(n2);

        Edge e0 = graphModel.factory().newEdge(n0, n1, false);
        undirectedGraph.addEdge(e0);

        Edge e1 = graphModel.factory().newEdge(n0, n2, false);
        undirectedGraph.addEdge(e1);

        Edge e2 = graphModel.factory().newEdge(n1, n2, false);
        undirectedGraph.addEdge(e2);

        Edge e3 = graphModel.factory().newEdge(n2, n2, false);
        undirectedGraph.addEdge(e3);

        return graphModel;
    }

    public static GraphModel generateDirectedGraphWithoutWeights() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node n0 = graphModel.factory().newNode(((Integer) 0).toString());
        Node n1 = graphModel.factory().newNode(((Integer) 1).toString());
        Node n2 = graphModel.factory().newNode(((Integer) 2).toString());

        directedGraph.addNode(n0);
        directedGraph.addNode(n1);
        directedGraph.addNode(n2);

        Edge e0 = graphModel.factory().newEdge(n0, n1);
        directedGraph.addEdge(e0);

        Edge e1 = graphModel.factory().newEdge(n0, n2);
        directedGraph.addEdge(e1);

        Edge e2 = graphModel.factory().newEdge(n1, n2);
        directedGraph.addEdge(e2);

        return graphModel;
    }

    public static GraphModel generateGraphWithWeights(boolean isDirected, boolean withLabels) {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node n0 = graphModel.factory().newNode(((Integer) 0).toString());
        Node n1 = graphModel.factory().newNode(((Integer) 1).toString());
        Node n2 = graphModel.factory().newNode(((Integer) 2).toString());

        if (withLabels) {
            n0.setLabel("n0");
            n1.setLabel("n1");
            n2.setLabel("n2");
        }

        directedGraph.addNode(n0);
        directedGraph.addNode(n1);
        directedGraph.addNode(n2);

        Edge e0 = graphModel.factory().newEdge(n0, n1, isDirected);
        e0.setWeight(5);
        directedGraph.addEdge(e0);

        Edge e1 = graphModel.factory().newEdge(n0, n2, isDirected);
        e1.setWeight(7);
        directedGraph.addEdge(e1);

        Edge e2 = graphModel.factory().newEdge(n1, n2, isDirected);
        e2.setWeight(10);
        directedGraph.addEdge(e2);

        return graphModel;
    }

    public static GraphModel generateSingularMatrixGraph(boolean isDirected) {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node n0 = graphModel.factory().newNode(((Integer) 0).toString());
        Node n1 = graphModel.factory().newNode(((Integer) 1).toString());

        directedGraph.addNode(n0);
        directedGraph.addNode(n1);

        Edge e0 = graphModel.factory().newEdge(n0, n0, isDirected);
        e0.setWeight(1);
        directedGraph.addEdge(e0);

        Edge e1 = graphModel.factory().newEdge(n0, n1, isDirected);
        e1.setWeight(1);
        directedGraph.addEdge(e1);

        Edge e2 = graphModel.factory().newEdge(n1, n0, isDirected);
        e2.setWeight(0);
        directedGraph.addEdge(e2);

        Edge e3 = graphModel.factory().newEdge(n1, n1, isDirected);
        e3.setWeight(0);
        directedGraph.addEdge(e3);

        return graphModel;
    }

    public static GraphModel generateGraphResultingInNegativeEigenvalues(boolean isDirected) {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        DirectedGraph directedGraph = graphModel.getDirectedGraph();
        Node n1 = graphModel.factory().newNode(((Integer) 0).toString());
        Node n2 = graphModel.factory().newNode(((Integer) 1).toString());
        Node n3 = graphModel.factory().newNode(((Integer) 2).toString());
        Node n4 = graphModel.factory().newNode(((Integer) 3).toString());
        Node n5 = graphModel.factory().newNode(((Integer) 4).toString());

        directedGraph.addNode(n1);
        directedGraph.addNode(n2);
        directedGraph.addNode(n3);
        directedGraph.addNode(n4);
        directedGraph.addNode(n5);

        Edge e0 = graphModel.factory().newEdge(n1, n2, isDirected);
        e0.setWeight(1);
        directedGraph.addEdge(e0);

        Edge e1 = graphModel.factory().newEdge(n1, n3, isDirected);
        e1.setWeight(1);
        directedGraph.addEdge(e1);

        Edge e2 = graphModel.factory().newEdge(n1, n5, isDirected);
        e2.setWeight(1);
        directedGraph.addEdge(e2);

        Edge e3 = graphModel.factory().newEdge(n3, n4, isDirected);
        e3.setWeight(1);
        directedGraph.addEdge(e3);

        Edge e4 = graphModel.factory().newEdge(n3, n5, isDirected);
        e4.setWeight(1);
        directedGraph.addEdge(e4);

        Edge e5 = graphModel.factory().newEdge(n4, n5, isDirected);
        e5.setWeight(1);
        directedGraph.addEdge(e5);

        return graphModel;
    }
}
