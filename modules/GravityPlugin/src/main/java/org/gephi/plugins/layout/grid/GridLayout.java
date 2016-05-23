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
package org.gephi.plugins.example.layout;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;

/**
 * Example of a layout algorithm which places all nodes in a grid.
 * <p>
 * The algorithm calculates nodes' position but moved them slowly at each iteration
 * so we can see transitions.
 * <p>
 * This class also defines the properties the user can manipulate: area size and speed.
 * It shows how to create {@link LayoutProperty} objects. Make sure you also set
 * the getter and setter for each property.
 * 
 * @author Mathieu Bastian
 */
public class GridLayout implements Layout {

    //Architecture
    private final LayoutBuilder builder;
    private GraphModel graphModel;
    //Flags
    private boolean executing = false;
    //Properties
    private int areaSize;
    private float speed;

    public GridLayout(GridLayoutBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void resetPropertiesValues() {
        areaSize = 1000;
        speed = 1f;
    }

    @Override
    public void initAlgo() {
        executing = true;
    }

    @Override
    public void goAlgo() {
        Graph graph = graphModel.getGraphVisible();
        graph.readLock();
        int nodeCount = graph.getNodeCount();
        Node[] nodes = graph.getNodes().toArray();

        int rows = (int) Math.round(Math.sqrt(nodeCount)) + 1;
        int cols = (int) Math.round(Math.sqrt(nodeCount)) + 1;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols && (i * rows + j) < nodes.length; j++) {
                Node node = nodes[i * rows + j];
                float x = (-areaSize / 2f) + ((float) j / cols) * areaSize;
                float y = (areaSize / 2f) - ((float) i / rows) * areaSize;
                float px = node.x();
                float py = node.y();
                node.setX(px + (x - px) * (speed / 10000f));
                node.setY(py + (y - py) * (speed / 10000f));
            }
        }

        graph.readUnlock();
    }

    @Override
    public void endAlgo() {
        executing = false;
    }

    @Override
    public boolean canAlgo() {
        return executing;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String GRIDLAYOUT = "Grid Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    "Area size",
                    GRIDLAYOUT,
                    "The area size",
                    "getAreaSize", "setAreaSize"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    "Speed",
                    GRIDLAYOUT,
                    "How fast are moving nodes",
                    "getSpeed", "setSpeed"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    @Override
    public void setGraphModel(GraphModel gm) {
        this.graphModel = gm;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Integer getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(Integer area) {
        this.areaSize = area;
    }
}
