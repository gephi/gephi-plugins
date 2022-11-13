package multiviz;

import algorithms.BasicLayout;
import algorithms.force.ForceAtlas2Layout;
import algorithms.force.ForceAtlasLayout;
import algorithms.force.FruchtermanReingoldLayout;
import helpers.CustomComboBoxEditor;
import helpers.LayoutDropDowns;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import helpers.VizUtils;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;

/**
 *
 * @author Jaymohan
 */
public class MultiLayerVisualization implements Layout {
    
    private final LayoutBuilder builder;
    private GraphModel graphModel;
    private boolean executing = false;
    
    private int layerDistance;
    private int noOfIterations;
    
    private String selectedColumn= "Node Label";
    private String layoutAlgorithm;
    
    private boolean horizontalLayout;
    private boolean is3DLayout;
    private boolean sortLayers = false;

    private int layerArea;
    private double gravity;
    private float speed;
    private boolean splitAsLevel;
    public static List<String> selectableColumns = new ArrayList<>();
    
    Graph graph;
    
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
        if (graphModel == null) return;
        
        graph = graphModel.getGraphVisible();
        graph.readLock();
        
        HashMap<String, List<Node>> layers = new HashMap<>();
        List<Node> nodes = graph.getNodes().toCollection().stream().collect(toList());
        List<Edge> edges = graph.getEdges().toCollection().stream().collect(toList());
        
        Table selectedTable;
        Column label;
        
        // case1 :layer disjoint
        // case2 :node aligned
        if(selectedColumn.startsWith("Node ")){
            
            selectedTable = graph.getModel().getNodeTable();
            
            //remove word 'Edge / Node ' from selected layer
            label = VizUtils.getLabel(selectedTable, selectedColumn.substring(5));
            
            for (Node node : nodes) {
                String key = VizUtils.getLabel(node, label);
                List<Node> values = layers.get(key);
                if (values == null) values = new ArrayList<>();
                values.add(node);
                layers.put(key, values);
            }
            
        } else if(selectedColumn.startsWith("Edge ")) {
            
            selectedTable = graph.getModel().getEdgeTable();
            label = VizUtils.getLabel(selectedTable, selectedColumn.substring(5));

            /**
             * get nodes without any edges/connections*
             */
            List<Node> edgeSourceNodes = edges.stream().map(v -> v.getTarget()).distinct().collect(Collectors.toList());
            List<Node> edgeTargetNodes = edges.stream().map(v -> v.getSource()).distinct().collect(Collectors.toList());
            List<Node> outliers = new ArrayList<>();

            nodes.forEach(node -> {
                if (edgeSourceNodes.contains(node) || edgeTargetNodes.contains(node)) {
                } else {
                    outliers.add(node);
                }
            });

            /**
             * If same label/nodes exists in multiple layers then node should
             * only be allowed to primary layer
             */
            Set<Node> valueList = new HashSet<>();

            edges.forEach(edge -> {
                String key = VizUtils.getLabel(edge, label);
                List<Node> value = layers.get(key);
                if (value == null) {
                    value = new ArrayList<>();
                }
                if (!value.contains(edge.getSource())) {
            
                    if(!valueList.contains(edge.getSource())){
                        value.add(edge.getSource());
                    }
                    valueList.add(edge.getSource());
                }
                if (!value.contains(edge.getTarget())) {
                    
                    if(!valueList.contains(edge.getTarget())){
                        value.add(edge.getTarget());
                    }
                    valueList.add(edge.getTarget());
                }
                layers.put(key, value);
            });

            if (!outliers.isEmpty()) {
                layers.put("outliers", outliers);
            }

            for (List<Node> layer : layers.values()) {
                Collections.sort(layer, (Node o1, Node o2) -> {
                    if (o1.getLabel() != null) {
                        try {
                            return (o1.getLabel()).compareTo(o2.getLabel());
                        } catch (NumberFormatException e) {
                            return Integer.compare(o1.getStoreId(), o2.getStoreId());
                        }
                    } else {
                        return Integer.compare(o1.getStoreId(), o2.getStoreId());
                    }
                });
            }
        } else return;
        
        String initialLayer = layers.keySet().stream().findFirst().get();
        Node initialNode = layers.get(initialLayer).get(0);
        
        boolean isDynamicWeight = graphModel.getEdgeTable().getColumn("weight").isDynamic();
        Interval interval = graph.getView().getTimeInterval();
        
        switch (layoutAlgorithm) {
            case "Linear Layout":
            case "Grid Layout":
            case "Circle Layout":
            case "Random Layout":
                new BasicLayout(getLayerDistance(), layers, initialLayer, initialNode, sortLayers, layoutAlgorithm).start();
                break;
            case "ForceAtlas":
                //AbstractLayout.ensureSafeLayoutNodePositions(graphModel);
                VizUtils.nodesRandom(nodes);
                new ForceAtlasLayout(getLayerDistance(), layers, initialLayer, initialNode, sortLayers, splitAsLevel, getGravity(), getSpeed(), isDynamicWeight, interval, nodes, edges, graph, getNoOfIterations()).start();
                break;
            case "ForceAtlas2":
                VizUtils.nodesRandom(nodes);
                //AbstractLayout.ensureSafeLayoutNodePositions(graphModel);
                new ForceAtlas2Layout(getLayerDistance(), layers, initialLayer, initialNode, splitAsLevel, sortLayers, getNoOfIterations(), getArea(), getSpeed(), getGravity(), isDynamicWeight, interval, nodes, edges, graph, graphModel).start();
                break;
            case "Fruchterman Reingold":
                VizUtils.nodesRandom(nodes);
                new FruchtermanReingoldLayout(getNoOfIterations(), getLayerDistance(), edges, getArea(), getGravity(), getSpeed(), layers, initialLayer, initialNode, sortLayers, splitAsLevel, nodes).start();
                break;
            default:
                endAlgo();
        }
        
        if(is3DLayout()){
            for (Node node : nodes) {
                double theta = 65 * Math.PI / 180;
                //if(isHorizontalLayout()){
                    node.setY((float) (node.y() * Math.cos(theta) - node.z() * Math.sin(theta)));
                //}
                node.setZ((float) (Math.random() * 0.01));
            }
        }
        
        if(isHorizontalLayout()){
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
        
        try{
            properties.add(LayoutProperty.createProperty(this, Integer.class, "Iterations", FORCED, "Number of iterations", "getNoOfIterations", "setNoOfIterations"));
            properties.add(LayoutProperty.createProperty(this, Integer.class, "Set Area", FORCED, "Set area for a layer", "getArea", "setArea"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class, "Split by level", FORCED, "If selected, Layout Algorithm will be applied on the whole network before splitting in to layers, If not Algorithm will be applied on each layer.", "isSplitAsLevel", "setSplitAsLevel"));
            properties.add(LayoutProperty.createProperty(this, Float.class, "Set Speed", FORCED, "Set Speed", "getSpeed", "setSpeed"));
            properties.add(LayoutProperty.createProperty(this, Double.class, "Set Gravity", FORCED, "Set gravity to prevent nodes going off the screen in force directed layouts", "getGravity", "setGravity"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class, "Horizontal Layout", BASIC, "If selected, layers will be placed next to each other, instad of stacking top of one another", "isHorizontalLayout", "setHorizontalLayout"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class, "Set as 3D", BASIC, "Set nodes in 3 Dimension", "is3DLayout", "set3DLayout"));
            properties.add(LayoutProperty.createProperty(this, Boolean.class, "Sort Layers", BASIC, "If selected, layers will sorted (layers with the least number of nodes will be placed at bottom/left)", "isSorted", "setSorted"));
            properties.add(LayoutProperty.createProperty(this, Integer.class, "Layer Distance", BASIC, "Distance between two layers", "getLayerDistance", "setLayerDistance"));
            properties.add(LayoutProperty.createProperty(this, String.class, "Select Layer", BASIC, "Select the feature which is to be considered as a layer", "getSelectedColumn", "setSelectedColumn", CustomComboBoxEditor.class));
            properties.add(LayoutProperty.createProperty(this, String.class, "Layout Algorithm", BASIC, "Select the layout algorithm which is to be applied to a layer", "getLayoutAlgorithm", "setLayoutAlgorithm", LayoutDropDowns.class));
        } catch(NoSuchMethodException e) {
            e.printStackTrace();
        }
        
        return properties.toArray(LayoutProperty[]::new);
    }

    @Override
    public void resetPropertiesValues() {
            layerDistance = 400;
            gravity = 10d;
            layerArea = 10000;
            noOfIterations = 100;
            horizontalLayout = false;
            is3DLayout = false;
            splitAsLevel = false;
            speed = 1f;
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

    public Integer getArea() {
        return layerArea;
    }

    public void setArea(Integer layerArea) {
        this.layerArea = layerArea;
    }

    public Double getGravity() {
        return gravity;
    }

    public void setGravity(Double gravity) {
        this.gravity = gravity;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
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
}
