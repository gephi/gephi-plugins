package cz.cvut.fit.gephi.edgebetweenness;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

public class EdgeBetweenness implements Statistics, LongTask {

  public static final String EDGE_BETWEENNESS = "edgebetweenness";
  private double[] edgeBetweenness;
  private double edgeBetwNum;
  private boolean isCancelled;
  private boolean isDirected;
  private boolean isNormalized;
  private ProgressTicket progressTicket;
  private String report = "";
  /**
   * Nodes count
   */
  private int N;
  /**
   * Edges count
   */
  private int E;

  public EdgeBetweenness() {

    GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
    if (graphController != null && graphController.getGraphModel() != null) {
      isDirected = graphController.getGraphModel().isDirected();
    }
  }

  @Override
  public void execute(GraphModel graphModel) {
    isCancelled = false;

    Graph graph;
    if (isDirected) {
      graph = graphModel.getDirectedGraphVisible();
    } else {
      graph = graphModel.getUndirectedGraphVisible();
    }
    execute(graph);
  }

  private Column initializeAttributeColumn(GraphModel graphModel) {
    Table edgeTable = graphModel.getEdgeTable();

    Column eigenCol = edgeTable.getColumn(EDGE_BETWEENNESS);
    if (eigenCol == null) {
      eigenCol = edgeTable.addColumn(EDGE_BETWEENNESS, "Edge Betweenness", Double.class, 0.0);
    }
    return eigenCol;
  }

  public void execute(Graph graph) {
    graph.readLock();

    N = graph.getNodeCount();
    E = graph.getEdgeCount();

    long startTime = System.currentTimeMillis();
    report += "Algorithm started \n";
    Progress.start(progressTicket, N);

    // Get column for EDGE_BETWEENNESS (create if needed)
    Column edgeBetweennessCol = initializeAttributeColumn(graph.getModel());

    // Reset
    for (Edge edge : graph.getEdges()) {
      edge.setAttribute(edgeBetweennessCol, 0.0);
    }

    // Allocate new array for betweenness of each edge
    // Inicialize to 1
    edgeBetweenness = new double[E];
    for (int i = 0; i < E; i++) {
      edgeBetweenness[i] = 1;
    }

    String l = "Creating list of nodes (time: " + (System.currentTimeMillis() - startTime) + ")\n";
    report += l;

    // Enumerate every node
    int nodeIndex = 0;
    int edgeIndex = 0;
    HashMap<Node, Integer> nodesIndex = new HashMap<Node, Integer>();
    HashMap<Edge, Integer> edgesIndex = new HashMap<Edge, Integer>();

    // Remember level in tree for every node
    for (Node n : graph.getNodes()) {
      nodesIndex.put(n, nodeIndex);
      nodeIndex++;
    }
    for (Edge e : graph.getEdges()) {
      edgesIndex.put(e, edgeIndex);
      edgeIndex++;
    }

    l = "List of nodes and indexes created (time: " + (System.currentTimeMillis() - startTime) + ")\n";
    report += l;


    l = "Creating queue for BFS (time: " + (System.currentTimeMillis() - startTime) + ")\n";
    report += l;

    int count = 0;
    for (Node rootNode : graph.getNodes()) {
      count++;
      Progress.progress(progressTicket, count);

      if (isCancelled) {
        graph.readUnlockAll();
        return;
      }

      Stack<Node> stack = new Stack<Node>();

      // Create new array for distance and inicialize to -1 values
      LinkedList<Node>[] predecessorList = new LinkedList[N];
      LinkedList<Edge>[] edgesList = new LinkedList[N];

      int[] distance = new int[N];
      for (int j = 0; j < N; j++) {
        predecessorList[j] = new LinkedList<Node>();
        edgesList[j] = new LinkedList<Edge>();
        distance[j] = -1;
      }

      int srcIndex = nodesIndex.get(rootNode);
      distance[srcIndex] = 0;

      // BFS
      LinkedList<Node> queue = new LinkedList<Node>();
      queue.addLast(rootNode);

      while (!queue.isEmpty()) {
        Node v = queue.removeFirst();
        stack.push(v);
        int vIndex = nodesIndex.get(v);

        for (Edge edge : graph.getEdges(v)) {
          Node neighNode = graph.getOpposite(v, edge);

          int neighIndex = nodesIndex.get(neighNode);

          // if 'neigh node' was found for the first time
          if (distance[neighIndex] < 0) {
            queue.addLast(neighNode);
            // distance from 'source node n' is '(distance to v) + 1'
            distance[neighIndex] = distance[vIndex] + 1;
          }

          // shortest path to 'neigh node' via 'v node'?
          if (distance[neighIndex] == (distance[vIndex] + 1)) {
            // Copy path from previous level
            predecessorList[neighIndex] = (LinkedList<Node>) ((predecessorList[vIndex]).clone());
            // Add new node into path
            predecessorList[neighIndex].addLast(v);

            // Copy path from previous level
            edgesList[neighIndex] = (LinkedList<Edge>) edgesList[vIndex].clone();
            // Add new edge into path
            edgesList[neighIndex].addLast(edge);

            // Increment betweenness value for each edge
            for (Edge e : edgesList[neighIndex]) {
              e.setAttribute(edgeBetweennessCol, ((Double)e.getAttribute(edgeBetweennessCol)) + 1);
            }
          }
        }
      } ///BFS
    }

    double sumVal = 0;
    double maxBetweenness = Double.NEGATIVE_INFINITY;

    if (isNormalized) {
      maxBetweenness = findMaxBetweenness(graph, edgeBetweennessCol);
      if (!isDirected) {
        maxBetweenness /= 2;
      }
    }

    // For undirected graph divide edge betweenness to half
    for (Edge e : graph.getEdges()) {
      Double val = (Double) e.getAttribute(edgeBetweennessCol);
      if (!isDirected) {
        val /= 2;
      }

      sumVal += val;

      if (isNormalized) {
        val /= maxBetweenness;
      }
      e.setAttribute(edgeBetweennessCol, val);
    }

    // sum of edge betweenness
    this.edgeBetwNum = sumVal;

    graph.readUnlockAll();
    Progress.finish(progressTicket);
    report += "Algorithm finished (time: " + (System.currentTimeMillis() - startTime) + ")\n";
  }

  @Override
  public boolean cancel() {
    return isCancelled = true;
  }

  @Override
  public void setProgressTicket(ProgressTicket pt) {
    this.progressTicket = pt;
  }

  @Override
  public String getReport() {
    return report;
  }

  public void setDirected(boolean isDirected) {
    this.isDirected = isDirected;
  }

  public boolean getDirected() {
    return isDirected;
  }

  public boolean isNormalized() {
    return isNormalized;
  }

  public void doNormalize(boolean isNormalized) {
    this.isNormalized = isNormalized;
  }

  double getEdgeBetweenness() {
    return edgeBetwNum;
  }

  private String printArr(LinkedList<Node> linkedList, int size) {
    if (size == 1) {
      return "[straight]";
    }

    String result = "[through " + linkedList.size() + " nodes: ";
    for (Node l : linkedList) {
      result += l.getId() + " (" + l.getLabel() + ")";
      result += ", ";
    }
    result += "]";
    return result;
  }

  private String printEdges(LinkedList<Edge> linkedList) {
    String result = "[through " + linkedList.size() + " edges: ";
    for (Edge e : linkedList) {
      result += " " + e.getId() + "(" + e.getSource().getLabel() + " to " + e.getTarget().getLabel() + "), ";
    }
    return result;
  }

  private double findMaxBetweenness(Graph graph, Column column) {
    double max = Double.NEGATIVE_INFINITY;

    for (Edge e : graph.getEdges()) {
      final Double val = (Double) e.getAttribute(column);

      if (val > max) {
        max = val;
      }
    }
    
    return max;
  }
}
