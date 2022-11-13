/*
 * Algorithm : Circle Layout
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

/**
 * Algorithm : Grid Layout
 * Copyright 2008-2011 Gephi
 * Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 * 
 * Algorithm : Random Layout
 * Copyright 2008-2010 Gephi
 * Authors : Helder Suzuki <heldersuzuki@gephi.org>
 * 
 * This file is part of Gephi.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright 2011 Gephi Consortium. All rights reserved.
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 3 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://gephi.org/about/legal/license-notice/
 * or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License files at
 * /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 3, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 3] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 3 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 3 code and therefore, elected the GPL
 * Version 3 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * Contributor(s):
 * Portions Copyrighted 2011 Gephi Consortium.
 */

package algorithms;

import java.util.HashMap;
import java.util.List;
import helpers.VizUtils;
import java.awt.GridLayout;
import java.util.Random;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.random.RandomLayout;

/**
 *
 * @author J
 */
public class BasicLayout {

    private final HashMap<String, List<Node>> layers;
    private final String initialLayer;
    private final Node initialNode;
    private final String layoutAlgorithm;
    private final int layerDistance;

    public BasicLayout(int layerDistance, HashMap<String, List<Node>> layers, String initialLayer, Node initialNode, boolean sortLayers, String layoutAlgorithm) {
        this.initialLayer = initialLayer;
        this.initialNode = initialNode;
        if(sortLayers){
            this.layers = VizUtils.sortedLayers(layers);
        } else this.layers = layers;
        this.layerDistance = layerDistance;
        this.layoutAlgorithm = layoutAlgorithm;
    }

    public void start() {
        String previousLayer = initialLayer;
        for (String currentLayer : layers.keySet()) {
            
            float y = initialNode.y();
            if (!previousLayer.equals(currentLayer)) {
                y = VizUtils.getLayerPlacement(layers, previousLayer, layerDistance);
            }
            
            Node biggestNode = VizUtils.getBiggestNode(layers, currentLayer);
            
            if (null != layoutAlgorithm) switch (layoutAlgorithm) {
                case "Random Layout":
                    float layerSpace = VizUtils.getLayerSize(layers, biggestNode, currentLayer);
                    for (Node node : layers.get(currentLayer)) {
                        double dx = initialNode.x() + node.size() + (float) (-layerSpace / 2 + layerSpace * new Random().nextDouble());
                        double ly = (y + layerSpace / 2) + (float) (-layerSpace / 2 + layerSpace * new Random().nextDouble()) + node.size();
                        node.setX((float) dx);
                        node.setY((float) ly);
                        node.setZ(initialNode.y());
                    }       break;
                case "Linear Layout":
                    double distance = 0;
                    int index = 0;
                    for (Node node : layers.get(currentLayer)) {
                        if(index == 0) {
                            node.setX(initialNode.x());
                            node.setY(y);
                        } else {
                            distance += layers.get(currentLayer).get(index-1).size() + (node.size() * 2)  + (node.getTextProperties().getWidth() + 20);
                            double randomY = Math.random() * ((biggestNode.size() + biggestNode.getTextProperties().getSize()) - 1 + 1) + 1;
                            
                            node.setX(initialNode.x() + (float)distance);
                            node.setY(y + (float)randomY);
                        }
                        node.setZ(initialNode.z());
                        index++;
                    }   break;
                case "Grid Layout":
                    int rows = (int) Math.round(Math.sqrt(layers.get(currentLayer).size())) + 1;
                    int cols = (int) Math.round(Math.sqrt(layers.get(currentLayer).size())) + 1;
                    double layerSize = VizUtils.getLayerSize(layers, biggestNode, currentLayer) * 1.2f;
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < cols && (i * rows + j) < layers.get(currentLayer).size(); j++) {
                            
                            Node node = layers.get(currentLayer).get(i * rows + j);
                            
                            double nx = (-layerSize / 2f) + ((float) j / cols) * layerSize;
                            double ny = (layerSize / 2f) - ((float) i / rows) * layerSize;
                            
                            double tx = nx;
                            double ty = (y + (y + (ny - y)));
                            
                            if (i == 0 && j == 0) {
                                tx = ((initialNode.x() + (nx - initialNode.x())) + (0));
                            } else {
                                tx = ((initialNode.x() + (nx - initialNode.x())) + (10 * j));
                            }
                            node.setX((float) tx);
                            node.setY((float) ty);
                            node.setZ(initialNode.z());
                        }
                    }       break;
                case "Circle Layout":
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
                    }   break;
                default:
                    break;
            }
            
            previousLayer = currentLayer;
        }
    }
}