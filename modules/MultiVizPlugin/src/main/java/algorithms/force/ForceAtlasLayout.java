/*
 Copyright 2008-2010 Gephi
 Authors : Helder Suzuki <heldersuzuki@gephi.org>
 Website : http://www.gephi.org
 This file is part of Gephi.
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 Copyright 2011 Gephi Consortium. All rights reserved.
 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"
 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.
 Contributor(s):
 Portions Copyrighted 2011 Gephi Consortium.
 */

package algorithms.force;

import helpers.Point;
import java.util.HashMap;
import java.util.List;
import helpers.VizUtils;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.ForceVectorNodeLayoutData;
import org.gephi.layout.plugin.ForceVectorUtils;

/**
 *
 * @author J
 */
public class ForceAtlasLayout {

    private int layerDistance;
    private HashMap<String, List<Node>> layers;
    private String initialLayer;
    private Node initialNode;
    private boolean sortLayers;
    private boolean splitAsLevel;

    public double inertia;
    private double repulsionStrength;
    private double attractionStrength;
    private double maxDisplacement;
    private boolean freezeBalance;
    private double freezeStrength;
    private double freezeInertia;
    private double gravity;
    private double speed;
    private double cooling;
    private boolean outboundAttractionDistribution;
    private boolean isDynamicWeight;
    private Interval timeInterval;
    private List<Node> allNodes;
    private List<Edge> allEdges;
    private Graph graph;
    private int iterations;

    public ForceAtlasLayout(int layerDistance, HashMap<String, List<Node>> layers, String initialLayer, Node initialNode, boolean sortLayers, boolean splitAsLevel, double gravity, float speed, boolean isDynamicWeight, Interval interval, List<Node> nodes, List<Edge> edges, Graph graph, int iterationnos) {
        this.layerDistance = layerDistance;
        this.initialLayer = initialLayer;
        this.initialNode = initialNode;
        this.sortLayers = sortLayers;
        this.splitAsLevel = splitAsLevel;
        if (this.sortLayers) {
            this.layers = VizUtils.sortedLayers(layers);
        } else {
            this.layers = layers;
        }
        this.isDynamicWeight = isDynamicWeight;
        this.timeInterval = interval;
        this.iterations = iterationnos;
        this.gravity = gravity;
        this.speed = speed;

        this.inertia = 0.1;
        this.repulsionStrength = 200d;
        this.attractionStrength = 10d;
        this.maxDisplacement = 10d;

        this.freezeBalance = true;
        this.freezeStrength = 80d;

        //gravity = 30d;
        //speed = 1d;
        this.cooling = 1d;

        this.graph = graph;
        this.outboundAttractionDistribution = false;
        this.allNodes = nodes;
        this.allEdges = edges;
    }

    public void start() {
        try {
            for (int i = 0; i < iterations; i++) {
                if (splitAsLevel) {
                    runAlgorithm(allNodes, allEdges, null);
                } else {
                    int counter = 0;
                    for (String currentLayer : layers.keySet()) {
                        Point ghostPoint = new Point(initialNode.x(), initialNode.y() + (layerDistance * counter * 2));
                        List<Node> nodes = layers.get(currentLayer);
                        List<Edge> edges = VizUtils.getLayerEdges(layers, allEdges, nodes);
                        runAlgorithm(nodes, edges, ghostPoint);
                        counter++;
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        VizUtils.initiateLayerSplitter(layers, initialLayer, initialNode, splitAsLevel, layerDistance);
    }

    private void runAlgorithm(List<Node> nodes, List<Edge> edges, Point ghostPoint) {
        for (Node n : nodes) {
            if (n.getLayoutData() == null || !(n.getLayoutData() instanceof ForceVectorNodeLayoutData)) {
                n.setLayoutData(new ForceVectorNodeLayoutData());
            }

            ForceVectorNodeLayoutData layoutData = n.getLayoutData();
            layoutData.old_dx = layoutData.dx;
            layoutData.old_dy = layoutData.dy;
            layoutData.dx *= inertia;
            layoutData.dy *= inertia;
        }
        // repulsion
        for (Node n1 : nodes) {
            for (Node n2 : nodes) {
                if (n1 != n2) {
                    ForceVectorUtils.fcBiRepulsor_noCollide(n1, n2, repulsionStrength * (1 + graph.getDegree(n1)) * (1 + graph.getDegree(n2)));
                }
            }
        }
        // attraction
        if (outboundAttractionDistribution) {
            for (Edge e : edges) {
                Node nf = e.getSource();
                Node nt = e.getTarget();
                double bonus = (nf.isFixed() || nt.isFixed()) ? (100) : (1);
                bonus *= VizUtils.getEdgeWeight(e, isDynamicWeight, timeInterval);
                ForceVectorUtils.fcBiAttractor_noCollide(nf, nt,
                        bonus * attractionStrength / (1 + graph.getDegree(nf)));
            }
        } else {
            for (Edge e : edges) {
                Node nf = e.getSource();
                Node nt = e.getTarget();
                double bonus = (nf.isFixed() || nt.isFixed()) ? (100) : (1);
                bonus *= VizUtils.getEdgeWeight(e, isDynamicWeight, timeInterval);
                ForceVectorUtils.fcBiAttractor_noCollide(nf, nt, bonus * attractionStrength);
            }
        }

        // gravity
        for (Node n : nodes) {
            float nx = n.x();
            float ny = n.y();
            double d = 0.0001 + Math.sqrt(nx * nx + ny * ny);
            double gf = 0.0001 * gravity * d;
            ForceVectorNodeLayoutData layoutData = n.getLayoutData();
            layoutData.dx -= gf * nx / d;
            layoutData.dy -= gf * ny / d;
        }

        // speed
        if (freezeBalance) {
            for (Node n : nodes) {
                ForceVectorNodeLayoutData layoutData = n.getLayoutData();
                layoutData.dx *= speed * 10f;
                layoutData.dy *= speed * 10f;
            }
        } else {
            for (Node n : nodes) {
                ForceVectorNodeLayoutData layoutData = n.getLayoutData();
                layoutData.dx *= speed;
                layoutData.dy *= speed;
            }
        }

        // apply forces
        for (Node n : nodes) {
            ForceVectorNodeLayoutData nLayout = n.getLayoutData();
            if (!n.isFixed()) {
                double d = 0.0001 + Math.sqrt(nLayout.dx * nLayout.dx + nLayout.dy * nLayout.dy);
                float ratio;
                if (freezeBalance) {
                    nLayout.freeze = (float) (freezeInertia * nLayout.freeze
                            + (1 - freezeInertia) * 0.1 * freezeStrength * (Math.sqrt(Math.sqrt(
                                    (nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx)
                                    + (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy)))));
                    ratio = (float) Math.min((d / (d * (1f + nLayout.freeze))), maxDisplacement / d);
                } else {
                    ratio = (float) Math.min(1, maxDisplacement / d);
                }
                nLayout.dx *= ratio / cooling;
                nLayout.dy *= ratio / cooling;
                float x = n.x() + nLayout.dx;
                float y = n.y() + nLayout.dy;

                n.setX(x);
                n.setY(y);
            }
        }
    }
}
