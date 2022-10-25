/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;

/**
 *
 * @author J
 */
public class VizUtils {

    public static String getLabel(Node node, Column column) {
        return node.getAttribute(column).toString();
    }

    public static Column getLabel(Table table, String column) {
        Column label = table.getColumn(column);
        if (label == null) {
            label = table.getColumn(column.replace(" ", "_"));
        }
        return label;
    }

    public static String getLabel(Edge edge, Column column) {
        return edge.getAttribute(column).toString();
    }

    public static Float getLayerPlacement(HashMap<String, List<Node>> layers, String layer, int layerDistance) {
        Node farthestNode = layers.get(layer).stream().max(Comparator.comparing(node -> node.y())).get();
        return farthestNode.y() + farthestNode.size() + farthestNode.getTextProperties().getSize() + layerDistance;
    }

    public static Node getBiggestNode(HashMap<String, List<Node>> layers, String currentLayer) {
        return layers.get(currentLayer).stream().max(Comparator.comparing(node -> node.size())).get();
    }

    public static Float getLayerSize(HashMap<String, List<Node>> layers, Node biggestNode, String currentLayer) {
        return (biggestNode.size() + biggestNode.getTextProperties().getSize()) * layers.get(currentLayer).size();
    }

    public static HashMap<String, List<Node>> sortedLayers(HashMap<String, List<Node>> layers) {
        List<Map.Entry<String, List<Node>>> list = new ArrayList<>(layers.entrySet());
        Collections.sort(list, (Map.Entry<String, List<Node>> o1, Map.Entry<String, List<Node>> o2) -> Integer.compare(o2.getValue().size(), o1.getValue().size()));
        HashMap<String, List<Node>> temp = new LinkedHashMap<>();
        for (Map.Entry<String, List<Node>> item : list) {
            temp.put(item.getKey(), item.getValue());
        }
        return temp;
    }

    public static void nodesRandom(List<Node> nodes) {
        for (Node node : nodes) {
            float random = (float) ((0.01 + Math.random()) * 1000) - 500;
            node.setX(random);
            node.setY((float) ((0.01 + Math.random()) * 1000) - 500);
        }
    }

    public static List<Edge> getLayerEdges(HashMap<String, List<Node>> layers, List<Edge> allEdges, List<Node> nodes) {
        List<Edge> edges = new ArrayList<>();
        for (Edge edge : allEdges) {
            if (nodes.contains(edge.getSource()) && nodes.contains(edge.getTarget())) {
                if (!edges.contains(edge)) {
                    edges.add(edge);
                }
            }
        }
        return edges;
    }

    public static void initiateLayerSplitter(HashMap<String, List<Node>> layers, String initialLayer, Node initialNode, boolean splitAsLevel, int layerDistance) {
        String previousLayer = initialLayer;
        for (String currentLayer : layers.keySet()) {
            if(splitAsLevel){
                for (Node node : layers.get(currentLayer)) {
                    float y = node.y();
                    if(currentLayer == null ? initialLayer != null : !currentLayer.equals(initialLayer)){
                        y = y + getLayerPlacement(layers, previousLayer, layerDistance);
                    }
                    node.setY(y);
                }
            } else {
                float y = initialNode.y();
                if(currentLayer == null ? initialLayer != null : !currentLayer.equals(initialLayer)){
                    y = getLayerPlacement(layers, previousLayer, layerDistance);
                }
                for (Node node : layers.get(currentLayer)) {
                    node.setY(node.y() + y);
                }
            }
            previousLayer = currentLayer;
        }
    }

    public static List<Edge> getLayerEdges(List<Edge> allEdges, List<Node> nodes) {
        List<Edge> temp_edges = new ArrayList<>();
        allEdges.forEach(e -> {
            if (nodes.contains(e.getSource()) && nodes.contains(e.getTarget())) {
                if (!temp_edges.contains(e)) {
                    temp_edges.add(e);
                }
            }
        });
        return temp_edges;
    }

    public static Double getEdgeWeight(Edge edge, boolean dynamicWeight, Interval timeInterval) {
        if (dynamicWeight) {
            return edge.getWeight(timeInterval);
        } else {
            return edge.getWeight();
        }
    }
}
