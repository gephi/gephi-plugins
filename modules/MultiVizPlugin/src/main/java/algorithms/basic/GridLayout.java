/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package algorithms.basic;

import java.util.HashMap;
import java.util.List;
import helpers.VizUtils;
import org.gephi.graph.api.Node;

/**
 *
 * @author J
 */
public class GridLayout {

    private int layerDistance;
    private HashMap<String, List<Node>> layers;
    private String initialLayer;
    private Node initialNode;
    private boolean sortLayers;

    public GridLayout(int layerDistance, HashMap<String, List<Node>> layers, String initialLayer, Node initialNode, boolean sortLayers) {
        this.layerDistance = layerDistance;
        this.initialLayer = initialLayer;
        this.initialNode = initialNode;
        this.sortLayers = sortLayers;
        if(this.sortLayers) {
            this.layers = VizUtils.sortedLayers(layers);
        } else this.layers = layers;
    }
    
    public void start(){
        String previousLayer = initialLayer;
        for (String currentLayer : layers.keySet()) {
            Node biggestNode = VizUtils.getBiggestNode(layers, currentLayer);
            float y = initialNode.y();            

            if(!previousLayer.equals(currentLayer)){
                y =  VizUtils.getLayerPlacement(layers, previousLayer, layerDistance);
            }

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
            }
            previousLayer = currentLayer;
        }
    }
    
}
