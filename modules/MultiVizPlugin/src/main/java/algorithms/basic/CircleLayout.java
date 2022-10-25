/*
 * Copyright (c) 2010, Matt Groeninger
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package algorithms.basic;

import java.util.HashMap;
import java.util.List;
import helpers.VizUtils;
import org.gephi.graph.api.Node;

/**
 *
 * @author J
 */
public class CircleLayout {

    private HashMap<String, List<Node>> layers;
    private String initialLayer;
    private Node initialNode;
    private int layerDistance;

    public CircleLayout(int layerDistance, HashMap<String, List<Node>> layers, String initialLayer, Node initialNode, boolean sortLayers) {
        this.initialLayer = initialLayer;
        this.initialNode = initialNode;
        if(sortLayers){
            this.layers = VizUtils.sortedLayers(layers);
        } else this.layers = layers;
        this.layerDistance = layerDistance;
    }

    public void start() {
        String previousLayer = initialLayer;
        for (String currentLayer : layers.keySet()) {
            float y = initialNode.y();
            if (!previousLayer.equals(currentLayer)) {
                y = VizUtils.getLayerPlacement(layers, previousLayer, layerDistance);
            }
            List<Node> nodes = layers.get(currentLayer);
            if (nodes.size() == 1) {
                nodes.get(0).setX(initialNode.x());
                nodes.get(0).setY(y);
                nodes.get(0).setZ(initialNode.z());
            } else {
                double circumference = 0;
                for (Node node : nodes) {
                    circumference += (node.size() * 2) + node.getTextProperties().getWidth();
                }
                circumference = circumference * 1.2f;

                double diameter = circumference / Math.PI;
                double theta = (2 * Math.PI) / circumference;

                double tempTheta = 0;
                double nodeSize = 0;

                for (Node node : nodes) {
                    if (!node.isFixed()) {
                        nodeSize = node.size() + node.getTextProperties().getWidth() / 2;
                        double arc = nodeSize * 1.2f * theta;
                        float dx = (float) (diameter * (Math.cos((tempTheta + arc) + (Math.PI / 2))));
                        float dy = (float) (diameter * (Math.sin((tempTheta + arc) + (Math.PI / 2))));
                        tempTheta += nodeSize * 2 * theta * 1.2f;
                        node.setX(initialNode.x() + dx);
                        node.setY(y + dy);
                        node.setZ(initialNode.z());
                    }
                }
            }
            previousLayer = currentLayer;
        }
    }
}