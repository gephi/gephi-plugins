package multiviz;

import helpers.CustomComboBoxEditor;
import helpers.LayoutDropDowns;
import helpers.VizUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;

import org.gephi.filters.api.FilterController;
import org.gephi.filters.plugin.partition.PartitionBuilder.NodePartitionFilter;
import org.openide.util.Lookup;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.filters.plugin.partition.PartitionBuilder.PartitionFilter;
import org.gephi.graph.api.Edge;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.fruchterman.FruchtermanReingold;
import org.gephi.layout.plugin.random.RandomLayout;

/**
 *
 * @author J
 */

public class MultiLayerVisualization implements Layout {

    private final LayoutBuilder builder;
    private GraphModel graphModel;
    private boolean executing = false;

    private int layerDistance;
    private int noOfIterations;

    private String selectedColumn = "Node Label";
    private String layoutAlgorithm;

    private boolean horizontalLayout;
    private boolean is3DLayout;
    private boolean sortLayers = false;

    private float layerArea;
    private double gravity;
    private double speed;
    private boolean splitAsLevel;
    public static List<String> selectableColumns = new ArrayList<>();

    private Column label;
    Graph graph;
    HashMap<Object, Integer> classes;
    private String algorithmType = "BasicL";

    MultiLayerVisualization(MLVBuilder mBuilder) {
        this.builder = mBuilder;
    }

    @Override
    public void initAlgo() {
        executing = true;
    }

    @Override
    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
        graph = this.graphModel.getGraphVisible();
        selectableColumns = new ArrayList<>();
        for (int i = 0; i < graphModel.getNodeTable().countColumns(); i++) {
            selectableColumns.add("Node " + graphModel.getNodeTable().getColumn(i).getTitle());
        }
        for (int i = 0; i < graphModel.getEdgeTable().countColumns(); i++) {
            selectableColumns.add("Edge " + graphModel.getEdgeTable().getColumn(i).getTitle());
        }
    }

    @Override
    public void goAlgo() {

        if (graphModel == null) {
            return;
        }
        graph = graphModel.getGraphVisible();

        Node[] nodes = graph.getNodes().toArray();
        Edge[] edges = graph.getEdges().toArray();

        label = graphModel.getNodeTable().getColumn(0);

        FilterController filterController = Lookup.getDefault().lookup(FilterController.class);
        AppearanceModel appearanceModel = Lookup.getDefault().lookup(AppearanceController.class).getModel();
        NodePartitionFilter partitionFilter = null;

        classes = new HashMap<>();
        if (selectedColumn.startsWith("Node ")) {
            label = VizUtils.getLabel(graph.getModel().getNodeTable(), selectedColumn.substring(5));
            if(label != null) {
                for (Node node : nodes) {
                    Object key = node.getAttribute(label);
                    if (classes.containsKey(key)) {
                        Integer value = classes.get(key);
                        classes.replace(key, value, 1 + value);
                    } else {
                        classes.put(key, 1);
                    }
                }
                partitionFilter = new NodePartitionFilter(appearanceModel.getNodePartition(label));
            }
        } else if (selectedColumn.startsWith("Edge ")) {

            Column newColumn = VizUtils.getLabel(graph.getModel().getNodeTable(), "mviz_edge_" + selectedColumn.substring(5));
            label = VizUtils.getLabel(graph.getModel().getEdgeTable(), selectedColumn.substring(5));

            if (label != null) {

                if (newColumn == null) {
                    newColumn = graphModel.getNodeTable().addColumn("mviz_edge_" + label.getTitle(), String.class);
                }

                List<Node> edgeNodes = new ArrayList<>();
                for (Edge edge : edges) {

                    Object key = edge.getAttribute(label);

                    if (edgeNodes.contains(edge.getSource())) {
                    } else {
                        edgeNodes.add(edge.getSource());
                        edge.getSource().setAttribute(newColumn, String.valueOf(key));
                    }
                    if (edgeNodes.contains(edge.getTarget())) {
                    } else {
                        edgeNodes.add(edge.getTarget());
                        edge.getTarget().setAttribute(newColumn, String.valueOf(key));
                    }
                }

                /**
                 * If same label/nodes exists in multiple layers then node should
                 * only be allowed to primary layer
                 */
                for (Node node : nodes) {
                    if (edgeNodes.contains(node)) {
                    } else {
                        node.setAttribute(newColumn, "mviz_outlier_nodes");
                    }

                    Object key = node.getAttribute(newColumn);
                    if (classes.containsKey(key)) {
                        Integer value = classes.get(key);
                        classes.replace(key, value, 1 + value);
                    } else {
                        classes.put(key, 1);
                    }
                }
                partitionFilter = new NodePartitionFilter(appearanceModel.getNodePartition(newColumn));
            }
        }

        if (partitionFilter != null) {

            if (sortLayers) {
                classes = VizUtils.sortByValue(classes);
            }

            VizUtils.nodesRandom(nodes);
            
            if ("ForceD".equals(getAlgorithmType())) {
                if (!isSplitAsLevel()) {
                    Node farthestNode = null;
                    for (Object layer : classes.keySet()) {
                        Pair pair = getSubset(partitionFilter, filterController, layer);
                        Node[] subset = (Node[]) pair.getNodes();
                        if (subset != null && subset.length > 0) {
                            Node biggestNode = (Node) pair.getBiggestNode();
                            drawForceDirectedLayouts(graphModel);
                            splitLayer(subset, farthestNode, biggestNode);
                            farthestNode = Arrays.stream(subset).max(Comparator.comparing(v -> v.y())).get();
                        }
                    }
                    graphModel.setVisibleView(null);
                } else {
                    drawForceDirectedLayouts(graphModel);
                    Node farthestNode = null;
                    for (Object layer : classes.keySet()) {
                        Pair pair = getSubset(partitionFilter, filterController, layer);
                        Node[] subset = (Node[]) pair.getNodes();
                        if(subset != null) {
                            splitLayer(subset, farthestNode, (Node) pair.getBiggestNode());
                            farthestNode = Arrays.stream(subset).max(Comparator.comparing(v -> v.y())).get();
                            graphModel.setVisibleView(null);
                        }
                    }
                }
            } else {
                Node initialNode = nodes[0];
                Node farthestNode = null;
                int iter = 0;
                for (Object layer : classes.keySet()) {
                    Pair pair = getSubset(partitionFilter, filterController, layer);
                    Node[] subset = (Node[]) pair.getNodes();
                    if (subset != null && subset.length > 0) {
                        Node biggestNode = (Node) pair.getBiggestNode();
                        if (iter == 0) {
                            initialNode = subset[0];
                        }
                        if ("Linear Layout".equals(getLayoutAlgorithm())) {
                            linearLayout(subset, initialNode);
                        } else if ("Grid Layout".equals(getLayoutAlgorithm())) {
                            gridLayout(subset, biggestNode, initialNode);
                        } else if ("Circle Layout".equals(getLayoutAlgorithm())) {
                            circleLayout(subset, initialNode);
                        } else if ("Random Layout".equals(getLayoutAlgorithm())) {
                            randomLayout(graphModel, subset, biggestNode);
                        }
                        splitLayer(subset, farthestNode, biggestNode);
                        farthestNode = Arrays.stream(subset).max(Comparator.comparing(v -> v.y())).get();
                    }
                    iter += 1;
                }
                graphModel.setVisibleView(null);
            }
        } else {
        }

        graph.readLock();
        if (is3DLayout()) {
            for (Node node : nodes) {
                if (!node.isFixed()) {
                    double theta = 65 * Math.PI / 180;
                    node.setY((float) (node.y() * Math.cos(theta) - node.z() * Math.sin(theta)));
                    node.setZ((float) (Math.random() * 0.01));
                }
            }
        }

        if (isHorizontalLayout()) {
            double theta = 270 * Math.PI / 180;
            float px = 0;
            float py = 0;
            for (Node u : nodes) {
                if (!u.isFixed()) {
                    float dx = u.x() - px;
                    float dy = u.y() - py;
                    u.setX((float) (px + dx * Math.cos(theta) - dy * Math.sin(theta)));
                    u.setY((float) (py + dy * Math.cos(theta) + dx * Math.sin(theta)));
                }
            }
        }
        graph.readUnlock();
        endAlgo();
    }

    @Override
    public boolean canAlgo() {
        return executing;
    }

    @Override
    public void endAlgo() {
        executing = false;
        try {
            graph.readLock();
            for (Node node : graph.getNodes()) {
                node.setLayoutData(null);
            }
        } finally {
            graph.readUnlockAll();
        }
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<>();
        final String BASIC = "Basic Features";
        final String FORCED = "Force Directed Features";
        try {
            properties.add(LayoutProperty.createProperty(this, Integer.class, "Iterations", FORCED, "Number of iterations", "getNoOfIterations", "setNoOfIterations"));
            properties.add(LayoutProperty.createProperty(this, Float.class, "Set Area", FORCED, "Set area for a layer", "getArea", "setArea"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class, "Split by level", FORCED, "If selected, Layout Algorithm will be applied on the whole network before splitting in to layers, If not Algorithm will be applied on each layer.", "isSplitAsLevel", "setSplitAsLevel"));
            properties.add(LayoutProperty.createProperty(this, Double.class, "Set Speed", FORCED, "Set Speed", "getSpeed", "setSpeed"));
            properties.add(LayoutProperty.createProperty(this, Double.class, "Set Gravity", FORCED, "Set gravity to prevent nodes going off the screen in force directed layouts", "getGravity", "setGravity"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class, "Horizontal Layout", BASIC, "If selected, layers will be placed next to each other, instad of stacking top of one another", "isHorizontalLayout", "setHorizontalLayout"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class, "Set as 3D", BASIC, "Set nodes in 3 Dimension", "is3DLayout", "set3DLayout"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class, "Sort Layers", BASIC, "If selected, layers will sorted (layers with the least number of nodes will be placed at bottom/left)", "isSorted", "setSorted"));
            properties.add(LayoutProperty.createProperty(this, Integer.class, "Layer Distance", BASIC, "Distance between two layers", "getLayerDistance", "setLayerDistance"));
            properties.add(LayoutProperty.createProperty(this, String.class, "Select Layer", BASIC, "Select the feature which is to be considered as a layer", "getSelectedColumn", "setSelectedColumn", CustomComboBoxEditor.class));
            properties.add(LayoutProperty.createProperty(this, String.class, "Layout Algorithm", BASIC, "Select the layout algorithm which is to be applied to a layer", "getLayoutAlgorithm", "setLayoutAlgorithm", LayoutDropDowns.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return properties.toArray(LayoutProperty[]::new);
    }

    @Override
    public void resetPropertiesValues() {
        layerDistance = 400;
        gravity = 10d;
        layerArea = 10000f;
        noOfIterations = 100;
        horizontalLayout = false;
        is3DLayout = false;
        splitAsLevel = false;
        speed = 1.0;
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    public Integer getLayerDistance() {
        return layerDistance;
    }

    public void setLayerDistance(Integer layerDistance) {
        this.layerDistance = layerDistance;
    }

    public Integer getNoOfIterations() {
        return noOfIterations;
    }

    public void setNoOfIterations(Integer noOfIterations) {
        this.noOfIterations = noOfIterations;
    }

    public String getSelectedColumn() {
        return this.selectedColumn;
    }

    public void setSelectedColumn(String selectedColumn) {
        this.selectedColumn = selectedColumn;
    }

    public String getLayoutAlgorithm() {
        return layoutAlgorithm;
    }

    public void setLayoutAlgorithm(String layoutAlgorithm) {
        this.layoutAlgorithm = layoutAlgorithm;
        if (!"ForceAtlas2".equals(layoutAlgorithm) && !"Fruchterman Reingold".equals(layoutAlgorithm) && !"Yifan Hu".equals(layoutAlgorithm)) {
            setAlgorithmType("BasicL");
        } else {
            setAlgorithmType("ForceD");
        }
    }

    public Boolean isHorizontalLayout() {
        return horizontalLayout;
    }

    public void setHorizontalLayout(Boolean horizontalLayout) {
        this.horizontalLayout = horizontalLayout;
    }

    public Boolean is3DLayout() {
        return is3DLayout;
    }

    public void set3DLayout(Boolean is3DLayout) {
        this.is3DLayout = is3DLayout;
    }

    public Float getArea() {
        return layerArea;
    }

    public void setArea(Float layerArea) {
        this.layerArea = layerArea;
    }

    public Double getGravity() {
        return gravity;
    }

    public void setGravity(Double gravity) {
        this.gravity = gravity;
    }

    public Double getSpeed() {
        return speed;
    }

    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    public Boolean isSplitAsLevel() {
        return splitAsLevel;
    }

    public void setSplitAsLevel(Boolean splitAsLevel) {
        this.splitAsLevel = splitAsLevel;
    }

    public Boolean isSorted() {
        return sortLayers;
    }

    public void setSorted(Boolean sortLayers) {
        this.sortLayers = sortLayers;
    }

    public String getAlgorithmType() {
        return algorithmType;
    }

    public void setAlgorithmType(String algorithmType) {
        this.algorithmType = algorithmType;
    }

    private void linearLayout(Node[] subset, Node initialNode) {
        double distancex = 0;
        int index = 0;
        for (Node node : subset) {
            if (index > 0) {
                distancex += subset[index - 1].size() + (node.size() * 2) + (node.getTextProperties().getWidth() + 20);
                double ry = Math.random() * ((subset[index - 1].size() + subset[index - 1].getTextProperties().getHeight() + node.size() + node.getTextProperties().getSize()) - 1 + 1) + 1;
                node.setX(initialNode.x() + (float) distancex);
                node.setY(node.y() + (float) ry);
            }
            index++;
        }
    }

    private void gridLayout(Node[] subset, Node biggestNode, Node initialNode) {
        int rows = (int) Math.round(Math.sqrt(subset.length)) + 1;
        int cols = (int) Math.round(Math.sqrt(subset.length)) + 1;
        double layerSize = ((biggestNode.size() + biggestNode.getTextProperties().getSize()) * subset.length) * 1.2f;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols && (i * rows + j) < subset.length; j++) {
                Node node = subset[i * rows + j];
                double nx = (-layerSize / 2f) + ((float) j / cols) * layerSize;
                double ny = (layerSize / 2f) - ((float) i / rows) * layerSize;
                double tx = nx;
                double ty = (initialNode.y() + (initialNode.y() + (ny - initialNode.y())));
                if (i == 0 && j == 0) {
                    tx = ((initialNode.x() + (nx - initialNode.x())) + (0));
                } else {
                    tx = ((initialNode.x() + (nx - initialNode.x())) + (10 * j));
                }
                node.setX((float) tx);
                node.setY((float) ty);
                node.setZ(initialNode.z());
            }
        }
    }

    private void circleLayout(Node[] subset, Node initialNode) {
        if (subset.length == 1) {
            subset[0].setX(initialNode.x());
            subset[0].setY(initialNode.y());
            subset[0].setZ(initialNode.z());
        } else {
            double circumference = 0;
            for (Node node : subset) {
                circumference += (node.size() * 2) + node.getTextProperties().getWidth();
            }
            circumference = circumference * 1.2f;

            double diameter = circumference / Math.PI;
            double theta = (2 * Math.PI) / circumference;

            double tempTheta = 0;
            double nodeSize = 0;

            for (Node node : subset) {
                if (!node.isFixed()) {
                    nodeSize = node.size() + node.getTextProperties().getWidth() / 2;
                    double arc = nodeSize * 1.2f * theta;
                    float dx = (float) (diameter * (Math.cos((tempTheta + arc) + (Math.PI / 2))));
                    float dy = (float) (diameter * (Math.sin((tempTheta + arc) + (Math.PI / 2))));
                    tempTheta += nodeSize * 2 * theta * 1.2f;
                    node.setX(initialNode.x() + dx);
                    node.setY(initialNode.y() + dy);
                    node.setZ(initialNode.z());
                }
            }
        }
    }

    private void randomLayout(GraphModel graphModel, Node[] subset, Node biggestNode) {
        float layerSpace = (biggestNode.size() + biggestNode.getTextProperties().getSize()) * subset.length;
        var randomLayout = new RandomLayout(builder, layerSpace);
        randomLayout.setGraphModel(graphModel);
        randomLayout.initAlgo();
        randomLayout.goAlgo();
        randomLayout.endAlgo();
    }

    private void forceAtlas2(GraphModel graphModel) {
        var forceAtlas2 = new ForceAtlas2(null);
        forceAtlas2.setGraphModel(graphModel);
        forceAtlas2.setJitterTolerance(1.0);
        forceAtlas2.setBarnesHutOptimize(true);
        forceAtlas2.setBarnesHutTheta(1.2);
        forceAtlas2.setOutboundAttractionDistribution(true);
        //forceAtlas2.setThreadsCount(1); //1 to 7
        forceAtlas2.setGravity(gravity);
        forceAtlas2.setAdjustSizes(true);
        forceAtlas2.initAlgo();
        for (int i = 0; i < getNoOfIterations(); i++) {
            forceAtlas2.goAlgo();
        }
    }

    private void fruchterman(GraphModel graphModel) {
        var fruchtermanReingold = new FruchtermanReingold(getBuilder());
        fruchtermanReingold.setGraphModel(graphModel);
        fruchtermanReingold.setArea(getArea());
        fruchtermanReingold.setGravity(getGravity());
        fruchtermanReingold.setSpeed(getSpeed());
        fruchtermanReingold.initAlgo();
        for (int i = 0; i < getNoOfIterations(); i++) {
            fruchtermanReingold.goAlgo();
        }
    }

    private void yifanHuNormal(GraphModel graphModel) {
        var yifanHu = new YifanHuLayout(getBuilder(), new StepDisplacement(1f));
        yifanHu.setGraphModel(graphModel);
        yifanHu.initAlgo();
        for (int i = 0; i < getNoOfIterations(); i++) {
            yifanHu.goAlgo();
        }
    }

    private void drawForceDirectedLayouts(GraphModel graphModel) {
        if ("ForceAtlas2".equals(getLayoutAlgorithm())) {
            forceAtlas2(graphModel);
        } else if ("Fruchterman Reingold".equals(getLayoutAlgorithm())) {
            fruchterman(graphModel);
        } else if ("Yifan Hu".equals(getLayoutAlgorithm())) {
            yifanHuNormal(graphModel);
        }
    }

    class Pair<U, V, W> {

        private U mGraphModel;
        private V mNodes;
        private W mBiggestNode;

        public Pair(U mGraphModel, V mNodes, W mBiggestNode) {
            this.mGraphModel = mGraphModel;
            this.mNodes = mNodes;
            this.mBiggestNode = mBiggestNode;
        }

        public W getBiggestNode() {
            return mBiggestNode;
        }

        public U getGraphModel() {
            return mGraphModel;
        }

        public V getNodes() {
            return mNodes;
        }
    }

    private Pair<GraphModel, Node[], Node> getSubset(PartitionFilter partitionFilter, FilterController filterController, Object value) {
        try {
            partitionFilter.unselectAll();
            partitionFilter.addPart(value);
            graphModel.setVisibleView(filterController.filter(filterController.createQuery(partitionFilter)));
            Node[] subset = graphModel.getGraphVisible().getNodes().toArray();
            Node biggestNode = null;
            if (subset.length > 0) {
                biggestNode = Arrays.stream(subset).max(Comparator.comparing(node -> node.size())).orElse(subset[0]);
            }
            return new Pair(graphModel, subset, biggestNode);
        } catch(Exception e) {
            return new Pair(graphModel, null, null);        
        }
    }

    private void splitLayer(Node[] subset, Node farthestNode, Node biggestNode) {
        for (Node node : subset) {
            float y = node.y();
            if (farthestNode != null) {
                y = y + (farthestNode.y() + farthestNode.size() + farthestNode.getTextProperties().getHeight()) + getLayerDistance() + biggestNode.size();
            }
            node.setY(y);
        }
    }
}
