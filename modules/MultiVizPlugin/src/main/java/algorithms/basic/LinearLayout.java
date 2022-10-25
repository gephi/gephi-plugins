
package algorithms.basic;

import java.util.HashMap;
import java.util.List;
import helpers.VizUtils;
import org.gephi.graph.api.Node;

/**
 *
 * @author J
 */
public class LinearLayout {

    private HashMap<String, List<Node>> layers;
    private String initialLayer;
    private Node initialNode;
    private boolean sortLayers;
    private int layerDistance;

    public LinearLayout(int layerDistance, HashMap<String, List<Node>> layers, String initialLayer, Node initialNode, boolean sortLayers) {
        this.initialLayer = initialLayer ;
        this.initialNode = initialNode ;
        this.sortLayers = sortLayers;
        this.layers = layers ;
        if(this.sortLayers){
            this.layers = VizUtils.sortedLayers(layers);
        }
    }

    public void start() {
        String previousLayer = initialLayer;
        for (String currentLayer : layers.keySet()) {
            Node biggestNode = VizUtils.getBiggestNode(layers, currentLayer);

            float y = initialNode.y();            

            if(!previousLayer.equals(currentLayer)){
                y =  VizUtils.getLayerPlacement(layers, previousLayer, layerDistance);
            }
            
            double distance = 0;
            int index = 0;
            for (Node node : layers.get(currentLayer)) {
                if(index == 0) {
                    node.setX(initialNode.x());
                    node.setY(y);
                } else {
                    distance += (node.size() * 2)  + (node.getTextProperties().getWidth() + 20);
                    double randomY = Math.random() * ((biggestNode.size() + biggestNode.getTextProperties().getSize()) - 1 + 1) + 1;
                    
                    node.setX(initialNode.x() + (float)distance);
                    node.setY(y + (float)randomY);
                }
                node.setZ(initialNode.z());
                index++;
            }
            previousLayer = currentLayer;
        }
    }
}