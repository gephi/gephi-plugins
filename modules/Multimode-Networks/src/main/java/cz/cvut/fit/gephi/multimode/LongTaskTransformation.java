package cz.cvut.fit.gephi.multimode;

import java.util.*;
import org.gephi.graph.api.Column;

import org.gephi.graph.api.*;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Kuchar
 */
public class LongTaskTransformation implements LongTask, Runnable {

    private ProgressTicket progressTicket;
    private boolean cancelled = false;
    private Column attributeColumn = null;
    private final String inDimension;
    private final String commonDimension;
    private final String outDimension;
    private boolean removeEdges = true;
    private boolean removeNodes = true;
    private boolean proportional = true;
    private boolean considerDirected = false;

    private double threshold = 0.0;

    public LongTaskTransformation(Column attributeColumn,
            String inDimension,
            String commonDimension,
            String outDimension,
            double threshold,
            boolean removeEdges,
            boolean removeNodes,
            boolean proportional,
            boolean considerDirected) {

        this.attributeColumn = attributeColumn;
        this.inDimension = inDimension;
        this.commonDimension = commonDimension;
        this.outDimension = outDimension;
        this.removeEdges = removeEdges;
        this.removeNodes = removeNodes;
        this.proportional = proportional;
        this.threshold = threshold;
        this.considerDirected = considerDirected;
    }

    public void run_offline(GraphModel graphModel) {
        execute(graphModel, false);
    }

    @Override
    public void run() {
        // graph
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();
        execute(graphModel, true);
    }

    private void execute(GraphModel graphModel, Boolean online) {
        // number of tickets
        if (online) {
            Progress.start(progressTicket, 5);
        }

        try {
            final GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
            final Graph graph;
            if (considerDirected) {
                graph = graphModel.getDirectedGraphVisible();
            } else {
                graph = graphModel.getUndirectedGraphVisible();
            }
            // Need to do that now because removing all edges later will creates error
            // on undirected graph
            final boolean isDirectedGraph = graph.isDirected();

            final Node[] nodes = graph.getNodes().toArray();

            final int nullEdgeType = graph.getModel().getEdgeType(null);

            // matrix axis
            final List<Node> firstHorizontal = new ArrayList<Node>();
            final List<Node> firstVertical = new ArrayList<Node>();
            final List<Node> secondHorizontal = new ArrayList<Node>();
            final List<Node> secondVertical = new ArrayList<Node>();
            for (Node n : nodes) {
                String nodeValue;
                Object val = n.getAttribute(attributeColumn);
                if (val != null) {
                    nodeValue = val.toString();
                } else {
                    nodeValue = "null";
                }
                // matrix axis
                if (nodeValue.equals(inDimension)) {
                    firstVertical.add(n);
                }
                if (nodeValue.equals(commonDimension)) {
                    firstHorizontal.add(n);
                    secondVertical.add(n);
                }
                if (nodeValue.equals(outDimension)) {
                    secondHorizontal.add(n);
                }
            }

            if (cancelled) {
                return;
            }

            if (online) {
                Progress.progress(progressTicket, "Matrix generation");
            }

            // first matrix
            final Matrix firstMatrix = new Matrix(firstVertical.size(), firstHorizontal.size());
            final Matrix firstUnweightMatrix = new Matrix(firstVertical.size(), firstHorizontal.size());
            final float[] firstWeights = new float[firstVertical.size()];
            final float[] firstUnweightWeights = new float[firstVertical.size()];

            for (int i = 0; i < firstVertical.size(); i++) {
                final Node node = firstVertical.get(i);
                final Set<Node> intersection = new HashSet<Node>(Arrays.asList(graph.getNeighbors(node).toArray()));
                intersection.retainAll(firstHorizontal);
                if (!intersection.isEmpty()) {
                    for (Node neighbour : intersection) {
                        int j = firstHorizontal.indexOf(neighbour);
                        double w = getAllEdgesWeightSum(graph, node, neighbour);
                        if (w > 0) {
                            firstWeights[i] += w * w;
                            firstUnweightWeights[i] += 1;
                            firstMatrix.set(i, j, w);
                            firstUnweightMatrix.set(i, j, 1);
                        }
                    }
                }
            }

            // second matrix
            final Matrix secondMatrix = new Matrix(secondVertical.size(), secondHorizontal.size());
            final Matrix secondUnweightMatrix = new Matrix(secondVertical.size(), secondHorizontal.size());
            final float[] secondWeights = new float[secondHorizontal.size()];
            for (int i = 0; i < secondVertical.size(); i++) {
                final Node node = secondVertical.get(i);
                final Set<Node> intersection = new HashSet<Node>(Arrays.asList(graph.getNeighbors(node).toArray()));
                intersection.retainAll(secondHorizontal);
                if (!intersection.isEmpty()) {
                    for (Node neighbour : intersection) {
                        int j = secondHorizontal.indexOf(neighbour);
                        double w = getAllEdgesWeightSum(graph, node, neighbour);
                        if (w > 0) {
                            secondWeights[j] += w * w;
                            secondMatrix.set(i, j, w);
                            secondUnweightMatrix.set(i, j, 1);
                        }
                    }
                }
            }

            if (cancelled) {
                return;
            }

            if (online) {
                Progress.progress(progressTicket, "Multiplication");
            }

            final Matrix result = firstMatrix.timesParallelIndexed(secondMatrix);
            if (cancelled) {
                return;
            }

            if (online) {
                Progress.progress(progressTicket, "Unweighted Multiplication");
            }

            final Matrix resultUnweighted;
            if (proportional) {
                resultUnweighted = firstUnweightMatrix.timesParallelIndexed(secondUnweightMatrix);
            } else {
                resultUnweighted = null;
            }

            if (cancelled) {
                return;
            }

            if (online) {
                Progress.progress(progressTicket, "Removing nodes/edges");
            }

            final float minDim = (float) secondVertical.size();

            if (removeNodes) {
                graph.removeAllNodes(firstHorizontal);

                firstHorizontal.clear();
                secondVertical.clear();
            } else {
                if (removeEdges) {
                    for (int i = 0; i < firstMatrix.getM(); i++) {
                        for (int j = 0; j < firstMatrix.getN(); j++) {
                            final Node node1 = firstVertical.get(i);
                            final Node node2 = firstHorizontal.get(j);
                            if (graph.contains(node1) && graph.contains(node2) && firstMatrix.get(i, j) > 0) {
                                removeAllEdges(graph, node1, node2);
                            }
                        }
                    }

                    for (int i = 0; i < secondMatrix.getM(); i++) {
                        for (int j = 0; j < secondMatrix.getN(); j++) {
                            final Node node1 = secondVertical.get(i);
                            final Node node2 = secondHorizontal.get(j);

                            if (graph.contains(node1) && graph.contains(node2) && secondMatrix.get(i, j) > 0) {
                                removeAllEdges(graph, node1, node2);
                            }
                        }
                    }
                }
            }

            if (cancelled) {
                return;
            }

            if (online) {
                Progress.progress(progressTicket, "Creating new edges");
            }

            final Table edgeTable = graphController.getGraphModel().getEdgeTable();
            final Column MMNT;
            if (!edgeTable.hasColumn("MMNT-EdgeType")) {
                MMNT = edgeTable.addColumn("MMNT-EdgeType", String.class);
            } else {
                MMNT = edgeTable.getColumn("MMNT-EdgeType");
            }

            if (!this.proportional) {
                for (int i = 0; i < result.getM(); i++) {
                    for (int j = 0; j < result.getN(); j++) {
                        final Node node1 = firstVertical.get(i);
                        final Node node2 = secondHorizontal.get(j);
                        final Edge ee = graph.getEdge(node1, node2);
                        final double pairResult = result.get(i, j);

                        if (node1 != node2
                                && graph.contains(node1) && graph.contains(node2)
                                && ee == null//Add if not already existing
                                && pairResult > threshold) {

                            final Edge newEdge = graphModel.factory().newEdge(node1, node2, nullEdgeType, pairResult, isDirectedGraph);
                            graph.addEdge(newEdge);
                            newEdge.setWeight(pairResult);
                            newEdge.setAttribute(MMNT, inDimension + "<--->" + outDimension);
                            newEdge.setLabel(inDimension + "-" + outDimension);
                        }

                        if (cancelled) {
                            return;
                        }
                    }
                }
            } else {
                final Table nodeTable = graphController.getGraphModel().getNodeTable();
                final Column edgeStrengthCol;
                if (!edgeTable.hasColumn("MM-Strength")) {
                    edgeStrengthCol = edgeTable.addColumn("MM-Strength", Float.class);
                } else {
                    edgeStrengthCol = edgeTable.getColumn("MM-Strength");
                }

                final Column nodeStrengthCol;
                if (!nodeTable.hasColumn("MM-Connections")) {
                    nodeStrengthCol = nodeTable.addColumn("MM-Connections", Float.class);
                } else {
                    nodeStrengthCol = nodeTable.getColumn("MM-Connections");
                }

                final Column nodeVolCol;
                if (!nodeTable.hasColumn("MM-vol")) {
                    nodeVolCol = nodeTable.addColumn("MM-vol", Float.class);
                } else {
                    nodeVolCol = nodeTable.getColumn("MM-vol");
                }

                for (int i = 0; i < result.getM(); i++) {
                    // for each node include the number of connections it has
                    // and the accumulated quadratic weights of them
                    final Node node1 = firstVertical.get(i);
                    node1.setAttribute(nodeStrengthCol, new Float(100.0 * firstUnweightWeights[i] / minDim));
                    node1.setAttribute(nodeVolCol, new Float(Math.sqrt(firstWeights[i])));
                    for (int j = 0; j < result.getN(); j++) {
                        final Node node2 = secondHorizontal.get(j);
                        final Edge ee = graph.getEdge(node1, node2);

                        if (graph.contains(node1) && graph.contains(node2)
                                && ee == null) {
                            float iniWeight = (float) result.get(i, j);
                            iniWeight = (float) (100.0 - 200.0 * Math.acos(iniWeight / (Math.sqrt(secondWeights[j]) * Math.sqrt(firstWeights[i]))) / Math.PI); //that is the cosine distance
                            // now we weight it by the number of components
                            iniWeight = Math.round(iniWeight * 100) / 100;
                            float finalWeight = (float) 100.0 * (float) Math.sqrt(resultUnweighted.get(i, j) / minDim); // how many components in common
                            if (iniWeight > threshold) {
                                final Edge newEdge = graphModel.factory().newEdge(node1, node2, nullEdgeType, iniWeight, isDirectedGraph);
                                if (!newEdge.isSelfLoop()) {
                                    newEdge.setAttribute(edgeStrengthCol, finalWeight);
                                    newEdge.setAttribute(MMNT, inDimension + "<--->" + outDimension);
                                    newEdge.setLabel(inDimension + "-" + outDimension);
                                    graph.addEdge(newEdge);
                                }
                            }
                        }
                    }
                    if (cancelled) {
                        return;
                    }
                }
            }
        } finally {
            if (online) {
                Progress.finish(progressTicket);
            }
        }
    }

    private static double getAllEdgesWeightSum(Graph graph, Node n1, Node n2) {
        double sum = 0;

        for (int edgeType : graph.getModel().getEdgeTypes()) {
            for (Edge e : graph.getEdges(n1, n2, edgeType)) {
                sum += e.getWeight();
            }
        }

        return sum;
    }

    private static void removeAllEdges(Graph graph, Node n1, Node n2) {
        for (int edgeType : graph.getModel().getEdgeTypes()) {
            graph.removeAllEdges(graph.getEdges(n1, n2, edgeType).toCollection());
        }
    }

    @Override
    public boolean cancel() {
        cancelled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this.progressTicket = pt;
    }
}
