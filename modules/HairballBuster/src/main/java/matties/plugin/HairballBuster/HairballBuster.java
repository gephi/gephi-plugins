package matties.plugin.HairballBuster;

/**
 * © 2020 The Johns Hopkins University Applied Physics Laboratory LLC. NO WARRANTY, NO LIABILITY. THIS MATERIAL IS PROVIDED “AS IS.” JHU/APL MAKES NO REPRESENTATION OR WARRANTY WITH RESPECT TO THE
 * PERFORMANCE OF THE MATERIALS, INCLUDING THEIR SAFETY, EFFECTIVENESS, OR COMMERCIAL VIABILITY, AND DISCLAIMS ALL WARRANTIES IN THE MATERIAL, WHETHER EXPRESS OR IMPLIED, INCLUDING (BUT NOT LIMITED
 * TO) ANY AND ALL IMPLIED WARRANTIES OF PERFORMANCE, MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND NON-INFRINGEMENT OF INTELLECTUAL PROPERTY OR OTHER THIRD PARTY RIGHTS. ANY USER OF THE
 * MATERIAL ASSUMES THE ENTIRE RISK AND LIABILITY FOR USING THE MATERIAL. IN NO EVENT SHALL JHU/APL BE LIABLE TO ANY USER OF THE MATERIAL FOR ANY ACTUAL, INDIRECT, CONSEQUENTIAL, SPECIAL OR OTHER
 * DAMAGES ARISING FROM THE USE OF, OR INABILITY TO USE, THE MATERIAL, INCLUDING, BUT NOT LIMITED TO, ANY DAMAGES FOR LOST PROFITS.
 */
import java.awt.Color;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.spi.LayoutData;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Exceptions;

/**
 * Implements <code>Layout</code> class to display Hairball Buster plots in Gephi.
 *
 */
public class HairballBuster implements Layout {

    private final LayoutBuilder builder;
    private GraphModel graphModel;
    private GraphFactory factory;
    private boolean executing = false;
    private boolean xLog = false;
    private boolean yLog = false;
    private boolean cliqueSort = false;
    private boolean plotNeighbors = true;
    private boolean preserveNodeColor = false;
    private boolean distinguishNeighbors = true;
    private boolean removeAxisLabel = false;
    private boolean offset = true;
    private int XScale = 5;
    private int YScale = 5;
    private int XSpacing = 20;
    private int YSpacing = 20;
    private int nodeSize = 5;
    private Color undirectedNeighborColor = new Color(92, 92, 92);
    private Color sourceNeighborColor = new Color(0, 255, 0);
    private Color targetNeighborColor = new Color(255, 0, 0);
    private Color nodeColor = new Color(0, 0, 255);

    private boolean restoreGraph = false;
    private boolean natural = true;
    private List<Float> nodeX;
    private List<Float> nodeY;
    private List<Float> nodeS;
    private List<Color> nodeC;
    private List<Object> nodeID;
    private List<Color> edgeColor;
    private final HBLayoutData LayoutData = new HBLayoutData();
    private Column column;
    private Table table;

    public HairballBuster(LayoutBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void initAlgo() {
        executing = true;
    }

    @Override
    public void setGraphModel(GraphModel gm) {
        graphModel = gm;
        factory = gm.factory();
        table = gm.getNodeTable();
    }

    // Displays Hairball Buster plot using user-set parameters
    @Override
    public void goAlgo() {
        Graph graph = graphModel.getGraphVisible();
        deleteNodesAndEdges(graph);

        List<Node> nodeList = new ArrayList<>(0);
        nodeList.addAll(Arrays.asList(graph.getNodes().toArray()));

        if (natural) {
            saveGraphLayout(nodeList, graph);
        }
        resetGraph(nodeList, graph);

        if (!restoreGraph && !cliqueSort) { //Creates a plot using degree centrality
            if (!table.hasColumn("axisLabel")) {
                column = table.addColumn("axisLabel", Integer.class);
            }
            nodeList = degreeSort(nodeList, graph);
            natural = false;
            plotNodesDegree(nodeList, graph);

            if (offset) {
                offsetNodes(nodeList, graph);
            }

            if (plotNeighbors) {
                plotGraphNeighbors(nodeList, graph);
            }

            drawPlot(nodeList, graph);
        }

        if (!restoreGraph && cliqueSort) {
            if (!table.hasColumn("axisLabel")) {
                column = table.addColumn("axisLabel", Integer.class);
            }
            nodeList = cliqueSizeSort(nodeList, graph);
            natural = false;

            plotNodesClique(nodeList, graph);

            if (offset) {
                offsetNodes(nodeList, graph);
            }

            if (plotNeighbors) {
                plotGraphNeighbors(nodeList, graph);
            }

            drawPlot(nodeList, graph);
        }

        endAlgo();
    }

    private void plotNodesDegree(List<Node> nodeList, Graph graph) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.get(i).setSize(nodeSize);
            if (xLog) {
                nodeList.get(i).setX((float) (Math.log10((i + 1) * 10) * XSpacing));
            } else {
                nodeList.get(i).setX((i + 1) * XSpacing);
            }
            if (yLog) {
                nodeList.get(i).setY((float) Math.log10(graph.getDegree(nodeList.get(i)) * 10) * YSpacing);
            } else {
                nodeList.get(i).setY(graph.getDegree(nodeList.get(i)) * YSpacing);
            }

            if (!preserveNodeColor) {
                nodeList.get(i).setColor(nodeColor);
            }
        }
    }

    private void plotNodesClique(List<Node> nodeList, Graph graph) {
        int index = 0;
        for (Node node : nodeList) { //draw all nodes to plot
            node.setSize(nodeSize);
            if (xLog) {
                node.setX((float) (Math.log10((index + 1) * 10) * XSpacing));
            } else {
                node.setX((index + 1) * XSpacing);
            }
            if (yLog) {
                node.setY((float) Math.log10(maximalCliqueSize(node, graph) * 10) * YSpacing);
            } else {
                node.setY(maximalCliqueSize(node, graph) * YSpacing);
            }

            if (!preserveNodeColor) {
                node.setColor(nodeColor);
            }

            index++;
        }
    }

    private Node createAndAddNode(Graph graph) {
        Node newNode = factory.newNode();
        newNode.setLayoutData(LayoutData);
        graph.addNode(newNode);
        return newNode;
    }

    private void drawPlot(List<Node> nodeList, Graph graph) {
        curveToFront(graph);
        whiteoutEdges(graph);
        drawAxes(graph, nodeList);
        drawScale(graph, nodeList);
    }

    private void whiteoutEdges(Graph graph) {
        for (Edge edge : graph.getEdges().toArray()) {
            edge.setColor(Color.WHITE);
        }
    }

    private void plotGraphNeighbors(List<Node> nodeList, Graph graph) {
        for (Node node : nodeList) { //plots node neighbors
            for (Node neighbor : graph.getNeighbors(node).toArray()) {
                int rank = getRank(neighbor, nodeList);
                Node newNode = createAndAddNode(graph);
                if (xLog) {
                    newNode.setX((float) (Math.log10((rank + 1) * 10) * XSpacing));
                } else {
                    newNode.setX(((rank + 1) * XSpacing));
                }
                newNode.setY(node.y());

                Edge edge = graph.getEdge(node, neighbor);

                if (distinguishNeighbors) {
                    if (edge != null) {
                        if (edge.isDirected()) {
                            newNode.setColor(sourceNeighborColor);
                        } else {
                            newNode.setColor(undirectedNeighborColor);
                        }
                    } else {
                        newNode.setColor(targetNeighborColor);
                    }
                } else {
                    newNode.setColor(undirectedNeighborColor);
                }
                newNode.setSize((float) (nodeSize));
            }
        }
    }

    private int heightNeighbors(int index, List<Node> nodeList) {
        int neighbors = 1;
        while (index < nodeList.size() - 2 && nodeList.get(index).y() == nodeList.get(index + 1).y()) {
            neighbors++;
            index++;
        }
        return neighbors;
    }

    private void curveToFront(Graph graph) {
        for (Node node : graph.getNodes().toArray()) {
            for (int i = 0; i < nodeID.size(); i++) {
                if (node.getId().equals(nodeID.get(i))) {
                    Node newNode = createAndAddNode(graph);
                    newNode.setPosition(node.x(), node.y());
                    if (preserveNodeColor) {
                        newNode.setColor(nodeC.get(i));
                    } else {
                        newNode.setColor(nodeColor);
                    }
                    newNode.setLabel(node.getLabel());
                    newNode.setSize(nodeSize);
                }
            }
        }
    }

    private void offsetNodes(List<Node> nodeList, Graph graph) {
        int j = 0;
        while (j < nodeList.size()) {
            int neighbors = heightNeighbors(j, nodeList);
            if (neighbors > 1) {
                float yOffset = YSpacing / neighbors;
                for (int k = 0; k < neighbors; k++) {
                    if (neighbors < 5) {
                        yOffset = YSpacing / 8;
                        offsetNodesHelper(yOffset, neighbors, j, k, nodeList);
                    } else {
                        offsetNodesHelper(yOffset, neighbors, j, k, nodeList);
                    }
                    if (j + k == nodeList.size() - 2) {
                        if (neighbors < 5) {
                            yOffset = YSpacing / 8;
                            float thisOne;
                            if (!(YSpacing / neighbors == 0)) {
                                if (neighbors % 2 == 1) {
                                    thisOne = (float) neighbors - 1 - (k + 1) - (int) (neighbors / 2);
                                } else {
                                    thisOne = (float) neighbors - 1 - (k + 1) - ((neighbors / 2) - 1);
                                }
                            } else {
                                thisOne = 0;
                            }
                            offsetNodesLogHelper(neighbors, yOffset, thisOne, j, k, nodeList);
                        } else {
                            float thisOne;
                            if (neighbors % 2 == 1) {
                                thisOne = (float) neighbors - 1 - (k + 1) - (int) (neighbors / 2);
                            } else {
                                thisOne = (float) neighbors - 1 - (k + 1) - ((neighbors / 2) - 1);
                            }
                            offsetNodesLogHelper(neighbors, yOffset, thisOne, j, k, nodeList);
                        }
                    }
                }
                j += neighbors;
            } else {
                j++;
            }
        }

    }

    private void offsetNodesHelper(float yOffset, int neighbors, int j, int k, List<Node> nodeList) {
        float thisOne;
        if (neighbors % 2 == 1) {
            thisOne = (float) neighbors - 1 - k - (int) (neighbors / 2);
        } else {
            thisOne = (float) neighbors - 1 - k - ((neighbors / 2) - 1);
        }
        if (yLog) {
            if (j == 0) {
                yOffset = (nodeList.get(j).y() - nodeList.get(j + neighbors).y()) / 2;
            } else {
                yOffset = (nodeList.get(j - 1).y() - nodeList.get(j + neighbors).y()) / 2;
            }
            Node jk = nodeList.get(j + k);
            jk.setY(jk.y() + thisOne / neighbors * yOffset);
        } else {
            nodeList.get(j + k).setY(nodeList.get(j + k).y() + (thisOne * yOffset));
        }
    }

    private void offsetNodesLogHelper(int neighbors, float yOffset, float thisOne, int j, int k, List<Node> nodeList) {
        if (yLog) {
            yOffset = (nodeList.get(j - 1).y() + 1) / 2;
            Node jk1 = nodeList.get(j + k + 1);
            jk1.setY(jk1.y() + thisOne / neighbors * yOffset);
        } else {
            Node jk1 = nodeList.get(j + k + 1);
            jk1.setY(jk1.y() + (thisOne * yOffset));
        }
    }

    private void drawAxes(Graph graph, List<Node> nodeList) {
        float maxX = nodeList.get(nodeList.size() - 1).x();
        float maxY = nodeList.get(0).y();

        List<Node> xAxes = new LinkedList<>();
        List<Node> yAxes = new LinkedList<>();

        Node origin = factory.newNode();
        origin.setLayoutData(LayoutData);
        graph.addNode(origin);
        origin.setSize(0);
        origin.setPosition(0, 0);

        drawXAxis(maxX, origin, xAxes, graph);
        drawYAxis(maxY, origin, yAxes, graph);

        graph.addNode(origin);
        origin.setSize(0);
        origin.setPosition(0, 0);
    }

    private void drawXAxis(float maxX, Node origin, List<Node> xAxes, Graph graph) {
        for (int i = XSpacing * 5; i < maxX + XSpacing * 5; i += XSpacing * 5) {
            Node newNode = createAndAddNode(graph);
            xAxes.add(newNode);
            newNode.setSize(0);
            newNode.setPosition(i, 0);
            if (i == XSpacing * 5) {
                Edge xAxis = factory.newEdge(origin, newNode, false);
                graph.addEdge(xAxis);
                xAxis.setWeight(5);
            } else {
                Edge xAxis = factory.newEdge(xAxes.get(xAxes.size() - 2), newNode, false);
                graph.addEdge(xAxis);
                xAxis.setWeight(5);
            }
        }
    }

    private void drawYAxis(float maxY, Node origin, List<Node> yAxes, Graph graph) {
        for (int i = YSpacing * 5; i < maxY + YSpacing * 5; i += YSpacing * 5) {
            Node newNode = createAndAddNode(graph);
            yAxes.add(newNode);
            newNode.setSize(0);
            newNode.setPosition(0, i);
            if (i == YSpacing * 5) {
                Edge yAxis = factory.newEdge(origin, newNode, false);
                graph.addEdge(yAxis);
                yAxis.setWeight(5);
            } else {
                Edge yAxis = factory.newEdge(yAxes.get(yAxes.size() - 2), newNode, false);
                graph.addEdge(yAxis);
                yAxis.setWeight(5);
            }
        }
    }

    private void drawScale(Graph graph, List<Node> nodeList) {
        float maxX = nodeList.get(nodeList.size() - 1).x();
        float maxY = nodeList.get(0).y();

        if (xLog) {
            for (int i = 0; i < maxX / XSpacing; i++) {
                drawScaleNode((i + 1) * XSpacing, -2 * nodeSize, (int) Math.pow(10, i), graph);
            }
        } else {
            for (int i = XScale; i <= maxX / XSpacing; i += XScale) {
                drawScaleNode(i * XSpacing, -2 * nodeSize, i, graph);
            }
        }

        if (yLog) {
            for (int i = 0; i < maxY / YSpacing; i++) {
                drawScaleNode(-2 * nodeSize, (i + 1) * YSpacing, (int) Math.pow(10, i), graph);
            }
        } else {
            for (int i = YScale; i <= maxY / YSpacing; i += YScale) {
                drawScaleNode(-2 * nodeSize, i * YSpacing, i, graph);
            }
        }
    }

    private void drawScaleNode(int x, int y, int label, Graph graph) {
        Node newNode = createAndAddNode(graph);
        newNode.setPosition(x, y);
        newNode.setSize(nodeSize);
        newNode.setAttribute(column, label);
        newNode.setColor(Color.WHITE);
    }

    private List<Node> degreeSort(List<Node> nodeList, Graph graph) { //quicksorts nodes by degree
        List<Node> listA = new ArrayList(0);
        List<Node> listB = new ArrayList(0);
        if (nodeList.size() > 1) {
            Node pivot = nodeList.get(0);
            int pivotDegree = graph.getDegree(pivot);
            int index = 0;
            for (Node node : nodeList) {
                if (index > 0) {
                    if (graph.getDegree(node) > pivotDegree) {
                        listA.add(node);
                    } else {
                        listB.add(node);
                    }
                }
                index++;
            }
            listA = degreeSort(listA, graph);
            listB = degreeSort(listB, graph);
            return append(listA, listB, nodeList.get(0));
        } else {
            return nodeList;
        }
    }

    private void saveGraphLayout(List<Node> nodeList, Graph graph) {
        nodeX = new ArrayList<>();
        nodeY = new ArrayList<>();
        nodeS = new ArrayList<>();
        nodeC = new ArrayList<>();
        nodeID = new ArrayList<>();
        for (Node node : nodeList) {
            nodeX.add(node.x());
            nodeY.add(node.y());
            nodeS.add(node.size());
            nodeC.add(node.getColor());
            nodeID.add(node.getId());
        }

        edgeColor = new ArrayList<>();
        for (Edge edge : graph.getEdges().toArray()) {
            edgeColor.add(edge.getColor());
        }
    }

    private void resetGraph(List<Node> nodeList, Graph graph) {
        deleteNodesAndEdges(graph);

        Edge[] edges = graph.getEdges().toArray();
        for (int i = 0; i < edges.length; i++) {
            edges[i].setColor(edgeColor.get(i));
        }

        for (int i = 0; i < nodeID.size(); i++) {
            for (Node node : nodeList) {
                if (node.getId() == nodeID.get(i)) {
                    node.setX(nodeX.get(i));
                    node.setY(nodeY.get(i));
                    node.setColor(nodeC.get(i));
                    node.setSize(nodeS.get(i));
                }
            }
        }

        if (table.hasColumn("axisLabel") && removeAxisLabel) {
            table.removeColumn(column);
        }

        natural = true;
    }

    private void deleteNodesAndEdges(Graph graph) {
        Node[] nodes = graph.getNodes().toArray();
        Edge[] edgesRemovable;
        for (Node node : nodes) {
            if (node.getLayoutData() == LayoutData) {
                edgesRemovable = graph.getEdges(node).toArray();
                for (Edge edge : edgesRemovable) {
                    graph.removeEdge(edge);
                }
                graph.removeNode(node);
            }
        }
    }

    private List<Node> cliqueSizeSort(List<Node> nodeList, Graph graph) { //quicksorts nodes by degree
        List<Node> listA = new ArrayList(0);
        List<Node> listB = new ArrayList(0);
        if (nodeList.size() > 1) {
            Node pivot = nodeList.get(0);
            int pivotSize = maximalCliqueSize(pivot, graph);
            int index = 0;
            for (Node node : nodeList) {
                if (index > 0) {
                    if (maximalCliqueSize(node, graph) > pivotSize) {
                        listA.add(node);
                    } else {
                        listB.add(node);
                    }
                }
                index++;
            }
            listA = cliqueSizeSort(listA, graph);
            listB = cliqueSizeSort(listB, graph);
            return append(listA, listB, nodeList.get(0));
        } else {
            return nodeList;
        }
    }

    private List<Node> append(List<Node> small, List<Node> big, Node pivot) {
        small.add(pivot);
        for (Node node : big) {
            small.add(node);
        }
        return small;
    }

    private List<List<Node>> generateNeighborsList(Graph graph, List<Node> nodes) {
        List<List<Node>> nodeNeighbors = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            nodeNeighbors.add(new ArrayList<Node>());
        }
        for (int i = 0; i < nodes.size(); i++) {
            Node[] neighbors = graph.getNeighbors(nodes.get(i)).toArray();
            nodeNeighbors.get(i).add(nodes.get(i));
            nodeNeighbors.get(i).addAll(Arrays.asList(neighbors));
        }
        return nodeNeighbors;
    }

    private int getRank(Node node, List<Node> list) {
        for (int i = 0; i < list.size(); i++) {
            if (node.getId() == list.get(i).getId()) {
                return i;
            }
        }
        return 0;
    }

    private int maximalCliqueSize(Node node, Graph graph) {
        int max = 0;
        if (graph.getDegree(node) >= max) {
            List<Node> clique = new LinkedList<>();
            for (Node neighbor : graph.getNeighbors(node)) {
                if (graph.getDegree(neighbor) >= max) {
                    clique.add(neighbor);
                }
            }
            max = clique(graph, clique, 1, max);
        }

        return max;
    }

    private int clique(Graph graph, List<Node> clique, int size, int max) {
        if (clique.isEmpty()) {
            if (size > max) {
                max = size;
            }
            return max;
        }
        while (clique.size() > 0) {
            if (size + clique.size() <= max) {
                return max;
            }
            int index = clique.size() / 2;
            List<Node> nextdoorNeighbors = new LinkedList<>();
            for (Node node : graph.getNeighbors(clique.get(index))) {
                if (graph.getDegree(node) >= max) {
                    nextdoorNeighbors.add(node);
                }
            }
            clique.remove(index);
            return clique(graph, intersect(clique, nextdoorNeighbors), size + 1, max);
        }
        return max;
    }

    private List<Node> intersect(List<Node> listA, List<Node> listB) {
        List<Node> toReturn = new LinkedList<>();
        for (Node nodeA : listA) {
            for (Node nodeB : listB) {
                if (nodeA.equals(nodeB)) {
                    toReturn.add(nodeA);
                }
            }
        }
        return toReturn;
    }

    @Override
    public boolean canAlgo() {
        return executing;
    }

    @Override
    public void endAlgo() {
        executing = false;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<>();
        final String SCALE = "Plot scale";
        final String COLOR = "Plot colors";
        final String RESET = "Reset graph";
        final String OTHER = "Other";

        try {
            properties.add(LayoutProperty.createProperty(this, Integer.class,
                    "X spacing",
                    SCALE,
                    "Spacing of nodes along the x-axis",
                    "getXSpacing", "setXSpacing"));
            properties.add(LayoutProperty.createProperty(this, Integer.class,
                    "Y spacing",
                    SCALE,
                    "Spacing of nodes along the y-axis",
                    "getYSpacing", "setYSpacing"));
            properties.add(LayoutProperty.createProperty(this, Integer.class,
                    "X scale",
                    SCALE,
                    "Plot x-axis scale",
                    "getXScale", "setXScale"));
            properties.add(LayoutProperty.createProperty(this, Integer.class,
                    "Y scale",
                    SCALE,
                    "Plot y-axis scale",
                    "getYScale", "setYScale"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class,
                    "X-Log", SCALE,
                    "Log scale for X-axis",
                    "getXLog", "setXLog"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class,
                    "Y-Log", SCALE,
                    "Log scale for Y-axis",
                    "getYLog", "setYLog"));
            properties.add(LayoutProperty.createProperty(this, Integer.class,
                    "NodeSize", SCALE,
                    "Node Size (in pixels)",
                    "getNodeSize", "setNodeSize"));
            properties.add(LayoutProperty.createProperty(
                    this, Color.class,
                    "Undirected neighbor color", COLOR,
                    "Undirected node neighbor color",
                    "getUndirectedNeighborsColor", "setUndirectedNeighborsColor"));
            properties.add(LayoutProperty.createProperty(
                    this, Color.class,
                    "Source neighbor color", COLOR,
                    "Directed source node neighbor color",
                    "getSourceNeighborColor", "setSourceNeighborColor"));
            properties.add(LayoutProperty.createProperty(
                    this, Color.class,
                    "Target neighbor color", COLOR,
                    "Directed target node neighbor color",
                    "getTargetNeighborColor", "setTargetNeighborColor"));
            properties.add(LayoutProperty.createProperty(
                    this, Color.class,
                    "Node color", COLOR,
                    "Node color",
                    "getNodesColor", "setNodesColor"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    "Preserve original node color", COLOR,
                    "",
                    "getPreserveNodeColor", "setPreserveNodeColor"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    "Color node neighbors", COLOR,
                    "Use undirected neighbor color for all neighbors",
                    "getColorNeighbors", "setColorNeighbors"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class,
                    "Clique sort", OTHER,
                    "Plot nodes by maximal clique size (defaults to degree centrality when unchecked)",
                    "getCliqueSort", "setCliqueSort"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class,
                    "Plot neighbors", OTHER,
                    "Plot node neighbors",
                    "getPlotNeighbors", "setPlotNeighbors"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class,
                    "Offset", OTHER,
                    "Offset curve",
                    "getOffset", "setOffset"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    "Reset graph", RESET,
                    "Reset graph to previous layout",
                    "getRestoreGraph", "setRestoreGraph"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    "Remove axis labels", RESET,
                    "Remove axis labels when resetting graph. \n(ONLY ENABLE IF AXIS LABELS ARE HIDDEN)",
                    "getRemoveLabels", "setRemoveLabels"));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    public Integer getXSpacing() {
        return XSpacing;
    }

    public void setXSpacing(Integer scale) {
        XSpacing = scale;
    }

    public Integer getYSpacing() {
        return YSpacing;
    }

    public void setYSpacing(Integer scale) {
        YSpacing = scale;
    }

    public Integer getXScale() {
        return XScale;
    }

    public void setXScale(Integer scale) {
        XScale = scale;
    }

    public Integer getYScale() {
        return YScale;
    }

    public void setYScale(Integer scale) {
        YScale = scale;
    }

    public Boolean getXLog() {
        return xLog;
    }

    public void setXLog(Boolean state) {
        xLog = state;
    }

    public Boolean getYLog() {
        return yLog;
    }

    public void setYLog(Boolean state) {
        yLog = state;
    }

    public Integer getNodeSize() {
        return nodeSize;
    }

    public void setNodeSize(Integer size) {
        nodeSize = size;
    }

    public Color getUndirectedNeighborsColor() {
        return undirectedNeighborColor;
    }

    public void setUndirectedNeighborsColor(Color color) {
        undirectedNeighborColor = color;
    }

    public Color getSourceNeighborColor() {
        return sourceNeighborColor;
    }

    public void setSourceNeighborColor(Color color) {
        sourceNeighborColor = color;
    }

    public Color getTargetNeighborColor() {
        return targetNeighborColor;
    }

    public void setTargetNeighborColor(Color color) {
        targetNeighborColor = color;
    }

    public Color getNodesColor() {
        return nodeColor;
    }

    public void setNodesColor(Color color) {
        nodeColor = color;
    }

    public Boolean getPreserveNodeColor() {
        return preserveNodeColor;
    }

    public void setPreserveNodeColor(Boolean state) {
        preserveNodeColor = state;
    }

    public Boolean getColorNeighbors() {
        return distinguishNeighbors;
    }

    public void setColorNeighbors(Boolean state) {
        distinguishNeighbors = state;
    }

    public Boolean getCliqueSort() {
        return cliqueSort;
    }

    public void setCliqueSort(Boolean state) {
        cliqueSort = state;
    }

    public Boolean getPlotNeighbors() {
        return plotNeighbors;
    }

    public void setPlotNeighbors(Boolean state) {
        plotNeighbors = state;
    }

    public Boolean getOffset() {
        return offset;
    }

    public void setOffset(Boolean state) {
        offset = state;
    }

    public Boolean getRestoreGraph() {
        return restoreGraph;
    }

    public void setRestoreGraph(Boolean state) {
        restoreGraph = state;
    }

    public Boolean getRemoveLabels() {
        return removeAxisLabel;
    }

    public void setRemoveLabels(Boolean state) {
        removeAxisLabel = state;
    }

    @Override
    public void resetPropertiesValues() {
        xLog = false;
        yLog = false;
        cliqueSort = false;
        plotNeighbors = true;
        preserveNodeColor = false;
        distinguishNeighbors = true;
        removeAxisLabel = false;
        offset = true;
        XScale = 5;
        YScale = 5;
        XSpacing = 20;
        YSpacing = 20;
        nodeSize = 5;
        undirectedNeighborColor = new Color(92, 92, 92);
        sourceNeighborColor = new Color(0, 255, 0);
        targetNeighborColor = new Color(255, 0, 0);
        nodeColor = new Color(0, 0, 255);
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    /**
     * Simplifies removal of nodes created to plot neighbors and draw axes in Hairball Buster
     */
    private class HBLayoutData implements LayoutData {

        private boolean temp = true;

        public void HBLayoutData(boolean bool) {
            temp = bool;
        }

        public boolean getTemp() {
            return temp;
        }
    }

}
