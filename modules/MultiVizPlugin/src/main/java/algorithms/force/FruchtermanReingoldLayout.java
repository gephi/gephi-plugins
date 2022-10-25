/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Jacomy
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
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.ForceVectorNodeLayoutData;

/**
 *
 * @author J
 */
public class FruchtermanReingoldLayout {


    private static final float SPEED_DIVISOR = 800;
    private static final float AREA_MULTIPLICATOR = 10000;

    private final int layerDistance;
    private final HashMap<String, List<Node>> layers;
    private final String initialLayer;
    private final Node initialNode;
    private final boolean sortLayers;
    private final boolean splitAsLevel;
    private final int iteration;
    private final List<Edge> allEdges;
    private final List<Node> allNodes;
    private final double gravity;
    private final float speed;
    private final double area;

    public FruchtermanReingoldLayout(int noOfIterations, int layerDistance, List<Edge> edges, double area, double gravity, float speed, HashMap<String, List<Node>> layers, String initialLayer, Node initialNode, boolean sortLayers, boolean splitAsLevel, List<Node> nodes) {
        this.layerDistance = layerDistance;
        this.iteration = noOfIterations;
        this.initialLayer = initialLayer;
        this.initialNode = initialNode;
        this.sortLayers = sortLayers;
        this.splitAsLevel = splitAsLevel;
        if (this.sortLayers) {
            this.layers = VizUtils.sortedLayers(layers);
        } else {
            this.layers = layers;
        }
        this.allEdges = edges;
        this.allNodes = nodes;
        this.area = area;
        this.gravity = gravity;
        this.speed = speed;
    }

    public void start() {
        for (int i = 0; i < iteration; i++) {
            if(splitAsLevel) runAlgorithm(allNodes, allEdges, null);
            else {
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
        VizUtils.initiateLayerSplitter(layers, initialLayer, initialNode, splitAsLevel, layerDistance);
    }

    private void runAlgorithm(List<Node> nodes, List<Edge> edges, Point ghostPoint) {
        try {
            for (Node node : nodes) {
                if (node.getLayoutData() == null || !(node.getLayoutData() instanceof ForceVectorNodeLayoutData)) {
                    node.setLayoutData(new ForceVectorNodeLayoutData());
                }
                ForceVectorNodeLayoutData layoutData = node.getLayoutData();
                layoutData.dx = 0;
                layoutData.dy = 0;
            }

            // Déplacement limite : on peut le calibrer...
            float maxDisplace = (float) (Math.sqrt(AREA_MULTIPLICATOR * area) / 10f);

            // La variable k, l'idée principale du layout.
            float k = (float) Math.sqrt((AREA_MULTIPLICATOR * area) / (1f + nodes.size()));

            if (ghostPoint != null) {
                /**
                 * use ghost nodes to attract nodes
                 */
            }

            // On fait toutes les paires de noeuds
            nodes.forEach(N1 -> {
                nodes.stream().filter(N2 -> (N1 != N2)).forEachOrdered(N2 -> {
                    // distance en x entre les deux noeuds
                    float xDist = N1.x() - N2.x();
                    float yDist = N1.y() - N2.y();
                    // distance tout court
                    float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
                    if (dist > 0) {
                        // Force de répulsion
                        float repulsiveF = k * k / dist;
                        ForceVectorNodeLayoutData layoutData = N1.getLayoutData();
                        // on l'applique...
                        layoutData.dx += xDist / dist * repulsiveF;
                        layoutData.dy += yDist / dist * repulsiveF;
                    }
                });
            });
            edges.forEach(E -> {
                // Idem, pour tous les noeuds on applique la force d'attraction
                Node Nf = E.getSource();
                Node Nt = E.getTarget();
                float xDist = Nf.x() - Nt.x();
                float yDist = Nf.y() - Nt.y();
                float dist = (float) Math.sqrt(xDist * xDist + yDist * yDist);
                float attractiveF = dist * dist / k;
                if (dist > 0) {
                    ForceVectorNodeLayoutData sourceLayoutData = Nf.getLayoutData();
                    ForceVectorNodeLayoutData targetLayoutData = Nt.getLayoutData();
                    sourceLayoutData.dx -= xDist / dist * attractiveF;
                    sourceLayoutData.dy -= yDist / dist * attractiveF;
                    targetLayoutData.dx += xDist / dist * attractiveF;
                    targetLayoutData.dy += yDist / dist * attractiveF;
                }
            });
            // gravity
            nodes.forEach(n -> {
                ForceVectorNodeLayoutData layoutData = n.getLayoutData();
                float d = (float) Math.sqrt(n.x() * n.x() + n.y() * n.y());
                float gf = 0.01f * k * (float) gravity * d;
                layoutData.dx -= gf * n.x() / d;
                layoutData.dy -= gf * n.y() / d;
            });
            // speed
            nodes.forEach(n -> {
                ForceVectorNodeLayoutData layoutData = n.getLayoutData();
                layoutData.dx *= speed / SPEED_DIVISOR;
                layoutData.dy *= speed / SPEED_DIVISOR;

            });
            nodes.forEach(n -> {
                // Maintenant on applique le déplacement calculé sur les noeuds.
                // nb : le déplacement à chaque passe "instantanné" correspond à la force : c'est une sorte d'accélération.
                ForceVectorNodeLayoutData layoutData = n.getLayoutData();
                float xDist = layoutData.dx;
                float yDist = layoutData.dy;
                float dist = (float) Math.sqrt(layoutData.dx * layoutData.dx + layoutData.dy * layoutData.dy);
                if (dist > 0 && !n.isFixed()) {
                    float limitedDist = Math.min(maxDisplace * (speed / SPEED_DIVISOR), dist);
                    n.setX(n.x() + xDist / dist * limitedDist);
                    n.setY(n.y() + yDist / dist * limitedDist);
                }
            });
        } catch (Exception e) {
            System.out.println("Frutcherman " + e.getMessage());
        }
    }
}
