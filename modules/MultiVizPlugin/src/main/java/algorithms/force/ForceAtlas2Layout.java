/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
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
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import multiviz.MLVBuilder;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2LayoutData;
import org.gephi.layout.plugin.forceAtlas2.ForceFactory;
import org.gephi.layout.plugin.forceAtlas2.ForceFactory.RepulsionForce;
import org.gephi.layout.plugin.forceAtlas2.NodesThread;
import org.gephi.layout.plugin.forceAtlas2.Region;
import org.gephi.layout.spi.LayoutBuilder;

/**
 *
 * @author J
 */
public class ForceAtlas2Layout {

    private final int layerDistance;
    private final HashMap<String, List<Node>> layers;
    private final String initialLayer;
    private final Node initialNode;
    private float speed;
    private final double gravity;
    private final boolean splitAsLevel;
    private final boolean sortLayers;

    private double outboundAttCompensation;
    private final double edgeWeightInfluence;
    private final double jitterTolerance;
    private double scalingRatio;
    private double speedEfficiency;
    private final boolean isOutboundAttractionDistribution;
    private final boolean isAdjustSizes;
    private boolean barnesHutOptimize;
    private final double barnesHutTheta;
    private final boolean linLogMode;
    private final boolean normalizeEdgeWeights;
    private final boolean strongGravityMode;
    private int threadCount;

    private final int iterations;
    private final int size;
    boolean dynamicWeight;
    private final List<Node> allNodes;
    private final List<Edge> allEdges;
    Graph graph;
    Interval timeInterval;
    GraphModel graphModel;

    public ForceAtlas2Layout(int layerDistance, HashMap<String, List<Node>> layers, String initialLayer, Node initialNode, boolean splitAsLevel, boolean sortLayers, int noOfIterations, double area, float speed, double gravity, boolean isDynamicWeight, Interval interval, List<Node> nodes, List<Edge> edges, Graph graph, GraphModel graphModel) {
        this.strongGravityMode = false;
        this.normalizeEdgeWeights = false;
        this.linLogMode = false;
        this.barnesHutTheta = 1.2;
        this.barnesHutOptimize = false;
        this.isAdjustSizes = true;
        this.isOutboundAttractionDistribution = false;
        this.speedEfficiency = 1.;
        this.jitterTolerance = 1d;
        this.edgeWeightInfluence = 1.;
        this.outboundAttCompensation = 1;
        this.layerDistance = layerDistance;
        this.initialLayer = initialLayer;
        this.initialNode = initialNode;
        this.splitAsLevel = splitAsLevel;
        this.sortLayers = sortLayers;
        if (this.sortLayers) {
            this.layers = VizUtils.sortedLayers(layers);
        } else {
            this.layers = layers;
        }
        this.speed = speed;
        this.gravity = gravity;
        this.iterations = noOfIterations;
        this.size = nodes.size();
        this.dynamicWeight = isDynamicWeight;
        this.graph = graph;
        this.allNodes = nodes;
        this.allEdges = edges;
        this.timeInterval = interval;
        this.barnesHutOptimize = size >= 1000;
        this.threadCount = Math.min(4, Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
        this.graphModel = graphModel;
    }

    public void start() {
        threadCount = (Math.max(1, Runtime.getRuntime().availableProcessors() - 1));

        for (int i = 0; i < iterations; i++) {
            if (splitAsLevel) {
                if (size >= 100) {
                    scalingRatio = 2.0;
                } else {
                    scalingRatio = 10.0;
                }
                runAlgorithm(allNodes, allEdges, null);
            } else {
                int counter = 0;
                for (String layer : layers.keySet()) {
                    List<Node> nodes = layers.get(layer);
                    List<Edge> edges = VizUtils.getLayerEdges(allEdges, nodes);
                    Point ghostLayer = new Point(initialNode.x(), initialNode.y() + layerDistance * counter);
                    barnesHutOptimize = nodes.size() >= 1000;
                    if (nodes.size() >= 100) {
                        scalingRatio = 2.0;
                    } else {
                        scalingRatio = 10.0;
                    }
                    runAlgorithm(nodes, edges, ghostLayer);
//                    ForceAtlas2 forceAtlas2Layout = new ForceAtlas2(new ForceAtlas2Builder());
//                    forceAtlas2Layout.setAdjustSizes(true);
//                    forceAtlas2Layout.setGraphModel(graphModel);
//                    forceAtlas2Layout.setGravity(gravity);
//                    forceAtlas2Layout.goAlgo();
                counter++;}
            }
        }
        VizUtils.initiateLayerSplitter(layers, initialLayer, initialNode, splitAsLevel, layerDistance);
    }

    private void runAlgorithm(List<Node> nodes, List<Edge> edges, Point ghost) {
        Region rootRegion = null;
        int currentThreadCount;
        ExecutorService pool;

        for (Node node : nodes) {
            if (node.getLayoutData() == null || !(node.getLayoutData() instanceof ForceAtlas2LayoutData)) {
                ForceAtlas2LayoutData nLayout = new ForceAtlas2LayoutData();
                node.setLayoutData(nLayout);
            }
            org.gephi.layout.plugin.forceAtlas2.ForceAtlas2LayoutData nLayout = node.getLayoutData();
            nLayout.mass = 1 + graph.getDegree(node);
            nLayout.old_dx = 0;
            nLayout.old_dy = 0;
            nLayout.dx = 0;
            nLayout.dy = 0;
        }

        pool = Executors.newFixedThreadPool(threadCount);
        currentThreadCount = threadCount;

        /**
         * If Barnes Hut active, initialize root region*
         */
        Node[] nodes_arr = nodes.toArray(Node[]::new);
        if (barnesHutOptimize) {
            rootRegion = new Region(nodes_arr);
            rootRegion.buildSubRegions();
        }

        /**
         * If outboundAttractionDistribution active, compensate.*
         */
        if (isOutboundAttractionDistribution) {
            outboundAttCompensation = 0;
            for (Node n : nodes) {
                ForceAtlas2LayoutData nLayout = n.getLayoutData();
                outboundAttCompensation += nLayout.mass;
            }
            outboundAttCompensation /= nodes.size();
        }

        /**
         * Repulsion (and gravity)Multi-threaded*
         */
        RepulsionForce Repulsion = ForceFactory.builder.buildRepulsion(isAdjustSizes, scalingRatio);
        int taskCount = 8 * currentThreadCount;
        // The threadPool Executor Service will manage the fetching of tasks and threads.

        // We make more tasks than threads because some tasks may need more time to compute.
        ArrayList<Future> threads = new ArrayList<>();

        for (int t = taskCount; t > 0; t--) {
            int from = (int) Math.floor(nodes.size() * (t - 1) / taskCount);
            int to = (int) Math.floor(nodes.size() * t / taskCount);
            java.util.concurrent.Future future = pool.submit(new NodesThread(nodes_arr, from, to, barnesHutOptimize, barnesHutTheta, gravity, (strongGravityMode) ? (ForceFactory.builder.getStrongGravity(scalingRatio)) : (Repulsion), scalingRatio, rootRegion, Repulsion));
            threads.add(future);
        }

        threads.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException("Unable to layout " + this.getClass().getSimpleName() + ".", e);
            }
        });

        // Attraction
        ForceFactory.AttractionForce Attraction = ForceFactory.builder.buildAttraction(linLogMode, isOutboundAttractionDistribution, isAdjustSizes, 1 * ((isOutboundAttractionDistribution) ? (outboundAttCompensation) : (1)));
        if (edgeWeightInfluence == 0) {
            for (Edge e : edges) {
                Attraction.apply(e.getSource(), e.getTarget(), 1);
            }
        } else if (edgeWeightInfluence == 1) {
            if (normalizeEdgeWeights) {
                Double w;
                Double edgeWeightMin = Double.MAX_VALUE;
                Double edgeWeightMax = Double.MIN_VALUE;
                for (Edge e : edges) {
                    w = VizUtils.getEdgeWeight(e, dynamicWeight, timeInterval);
                    edgeWeightMin = Math.min(w, edgeWeightMin);
                    edgeWeightMax = Math.max(w, edgeWeightMax);
                }
                if (edgeWeightMin < edgeWeightMax) {
                    for (Edge e : edges) {
                        w = (VizUtils.getEdgeWeight(e, dynamicWeight, timeInterval) - edgeWeightMin) / (edgeWeightMax - edgeWeightMin);
                        Attraction.apply(e.getSource(), e.getTarget(), w);
                    }
                } else {
                    for (Edge e : edges) {
                        Attraction.apply(e.getSource(), e.getTarget(), 1.);
                    }
                }
            } else {
                for (Edge e : edges) {
                    Attraction.apply(e.getSource(), e.getTarget(), VizUtils.getEdgeWeight(e, dynamicWeight, timeInterval));
                }
            }
        } else {
            if (normalizeEdgeWeights) {
                Double w;
                Double edgeWeightMin = Double.MAX_VALUE;
                Double edgeWeightMax = Double.MIN_VALUE;
                for (Edge e : edges) {
                    w = VizUtils.getEdgeWeight(e, dynamicWeight, timeInterval);
                    edgeWeightMin = Math.min(w, edgeWeightMin);
                    edgeWeightMax = Math.max(w, edgeWeightMax);
                }
                if (edgeWeightMin < edgeWeightMax) {
                    for (Edge e : edges) {
                        w = (VizUtils.getEdgeWeight(e, dynamicWeight, timeInterval) - edgeWeightMin) / (edgeWeightMax - edgeWeightMin);
                        Attraction.apply(e.getSource(), e.getTarget(), Math.pow(w, edgeWeightInfluence));
                    }
                } else {
                    edges.forEach(e -> {
                        Attraction.apply(e.getSource(), e.getTarget(), 1.);
                    });
                }
            } else {
                edges.forEach(e -> {
                    Attraction.apply(e.getSource(), e.getTarget(), Math.pow(VizUtils.getEdgeWeight(e, dynamicWeight, timeInterval), edgeWeightInfluence));
                });
            }
        }

        double totalSwinging = 0d;  // How much irregular movement
        double totalEffectiveTraction = 0d;  // Hom much useful movement
        for (Node n : nodes) {
            ForceAtlas2LayoutData nLayout = n.getLayoutData();
            if (!n.isFixed()) {
                double swinging = Math.sqrt(Math.pow(nLayout.old_dx - nLayout.dx, 2) + Math.pow(nLayout.old_dy - nLayout.dy, 2));
                totalSwinging += nLayout.mass * swinging;   // If the node has a burst change of direction, then it's not converging.
                totalEffectiveTraction += nLayout.mass * 0.5 * Math.sqrt(Math.pow(nLayout.old_dx + nLayout.dx, 2) + Math.pow(nLayout.old_dy + nLayout.dy, 2));
            }
        }

        // We want that swingingMovement < tolerance * convergenceMovement
        // Optimize jitter tolerance
        // The 'right' jitter tolerance for this network. Bigger networks need more tolerance. Denser networks need less tolerance. Totally empiric.
        double estimatedOptimalJitterTolerance = 0.05 * Math.sqrt(nodes.size());
        double minJT = Math.sqrt(estimatedOptimalJitterTolerance);
        double maxJT = 10;
        double jt = jitterTolerance * Math.max(minJT, Math.min(maxJT, estimatedOptimalJitterTolerance * totalEffectiveTraction / Math.pow(nodes.size(), 2)));

        double minSpeedEfficiency = 0.05;

        // Protection against erratic behavior
        if (totalSwinging / totalEffectiveTraction > 2.0) {
            if (speedEfficiency > minSpeedEfficiency) {
                speedEfficiency *= 0.5;
            }
            jt = Math.max(jt, jitterTolerance);
        }
        double targetSpeed = jt * speedEfficiency * totalEffectiveTraction / totalSwinging;

        // Speed efficiency is how the speed really corresponds to the swinging vs. convergence tradeoff
        // We adjust it slowly and carefully
        if (totalSwinging > jt * totalEffectiveTraction) {
            if (speedEfficiency > minSpeedEfficiency) {
                speedEfficiency *= 0.7;
            }
        } else if (speed < 1000) {
            speedEfficiency *= 1.3;
        }

        // But the speed shoudn't rise too much too quickly, since it would make the convergence drop dramatically.
        double maxRise = 0.5;   // Max rise: 50%
        speed = (float) (speed + Math.min(targetSpeed - speed, maxRise * speed));

        if (isAdjustSizes) {
            // If nodes overlap prevention is active, it's not possible to trust the swinging mesure.
            for (Node n : nodes) {
                org.gephi.layout.plugin.forceAtlas2.ForceAtlas2LayoutData nLayout = n.getLayoutData();
                if (!n.isFixed()) {

                    // Adaptive auto-speed: the speed of each node is lowered
                    // when the node swings.
                    double swinging = nLayout.mass * Math.sqrt(
                            (nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx)
                            + (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy));
                    double factor = 0.1 * speed / (1f + Math.sqrt(speed * swinging));

                    double df = Math.sqrt(Math.pow(nLayout.dx, 2) + Math.pow(nLayout.dy, 2));
                    factor = Math.min(factor * df, 10.) / df;

                    double x = n.x() + nLayout.dx * factor;
                    double y = n.y() + nLayout.dy * factor;

                    n.setX((float) x);
                    n.setY((float) y);
                }
            }
        } else {
            nodes.forEach(n -> {
                org.gephi.layout.plugin.forceAtlas2.ForceAtlas2LayoutData nLayout = n.getLayoutData();
                if (!n.isFixed()) {
                    // Adaptive auto-speed: the speed of each node is lowered
                    // when the node swings.
                    double swinging = nLayout.mass * Math.sqrt((nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx) + (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy));
                    double factor = speed / (1f + Math.sqrt(speed * swinging));

                    double x = n.x() + nLayout.dx * factor;
                    double y = n.y() + nLayout.dy * factor;

                    n.setX((float) x);
                    n.setY((float) y);
                }
            });
        }
    }
}