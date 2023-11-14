/* Copyright 2015 Wouter Spekkink
Authors : Wouter Spekkink <wouterspekkink@gmail.com>
Website : http://www.wouterspekkink.org
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
Copyright 2015 Wouter Spekkink. All rights reserved.
The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License. When distributing the software, include this License Header
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
Contributor(s): Wouter Spekkink

The plugin structure was inspired by the structure of the GeoLayout plugin.
 */
package org.wouterspekkink.plugins.layout.mdslayout;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.spi.LayoutData;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.ui.propertyeditor.NodeColumnNumbersEditor;

/**
 *
 * @author wouter
 */
public class MDSLayout implements Layout {

    private final MDSLayoutBuilder builder;
    private GraphModel graphModel;
    //Params
    private double scale = 1000;
    private Column dimension1;
    private Column dimension2;
    private boolean converged = false;

    public MDSLayout(MDSLayoutBuilder builder) {
        this.builder = builder;
        resetPropertiesValues();
    }

    @Override
    public void resetPropertiesValues() {
        if (graphModel != null) {
            for (Column c : graphModel.getNodeTable()) {
                if (c.getId().equalsIgnoreCase("dimension1")
                        || c.getId().equalsIgnoreCase("dim1")
                        || c.getTitle().equalsIgnoreCase("dimension1")
                        || c.getTitle().equalsIgnoreCase("dim1")) {
                    dimension1 = c;
                } else if (c.getId().equalsIgnoreCase("dimension2")
                        || c.getId().equalsIgnoreCase("dim2")
                        || c.getTitle().equalsIgnoreCase("dimension2")
                        || c.getTitle().equalsIgnoreCase("dim2")) {
                    dimension2 = c;
                }
            }
        }
    }

    @Override
    public void initAlgo() {
        converged = false;
    }

    @Override
    public void goAlgo() {
        double dim1 = 0;
        double dim2 = 0;
        float nodeX = 0;
        float nodeY = 0;
        Graph graph = graphModel.getGraph();

        graph.readLock();

        Node[] nodes = graph.getNodes().toArray();
        List<Node> validNodes = new ArrayList<Node>();
        List<Node> unvalidNodes = new ArrayList<Node>();

        // Set valid and non valid nodes:
        for (Node n : nodes) {
            //AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();

            if (n.getAttribute(dimension1) != null && n.getAttribute(dimension2) != null) {
                validNodes.add(n);
            } else {
                unvalidNodes.add(n);
            }
        }

        for (Node n : validNodes) {
            if (n.getLayoutData() == null || !(n.getLayoutData() instanceof MDSLayoutData)) {
                n.setLayoutData(new MDSLayoutData());
            }
            dim1 = getDoubleValue(n, dimension1);
            dim2 = getDoubleValue(n, dimension2);

            nodeX = (float) (dim1 * scale);
            nodeY = (float) (dim2 * scale);

            n.setX(nodeX);
            n.setY(nodeY);
        }

        if (validNodes.size() > 0 && unvalidNodes.size() > 0) {
            Node tempNode = validNodes.get(0);
            double xMin = tempNode.x();
            double xMax = tempNode.x();
            double yMin = tempNode.y();
            double xTemp = 0;
            double yTemp = 0;

            for (Node n : validNodes) {
                xTemp = n.x();
                yTemp = n.y();

                if (xTemp < xMin) {
                    xMin = xTemp;
                }
                if (xTemp > xMax) {
                    xMax = xTemp;
                }
                if (yTemp < yMin) {
                    yMin = yTemp;
                }
            }

            if (unvalidNodes.size() > 1) {
                double i = 0;
                double step = (xMax - xMin) / (unvalidNodes.size() - 1);
                for (Node n : unvalidNodes) {
                    n.setX((float) (xMin + i * step));
                    n.setY((float) (yMin - step));
                    i++;
                }
            } else {
                tempNode = unvalidNodes.get(0);
                tempNode.setX(10000);
                tempNode.setY(10000);
            }
        }
        graph.readUnlock();
        
        converged = true;//Stop infinite iteration after 1 single step
    }

    public double getDoubleValue(Node node, Column column) {
        return ((Number) node.getAttribute(column)).doubleValue();
    }

    @Override
    public void endAlgo() {
    }

    @Override
    public boolean canAlgo() {
        return dimension1 != null && dimension2 != null
                && !converged;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String MDSLAYOUT = "MDS Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    "Network Scale",
                    MDSLAYOUT,
                    "Determines the scale of the network",
                    "getScale", "setScale"));
            properties.add(LayoutProperty.createProperty(
                    this, Column.class,
                    "Dimension 1",
                    MDSLAYOUT,
                    "Choose the first dimension to be used in the layout",
                    "getDimension1", "setDimension1", NodeColumnNumbersEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, Column.class,
                    "Dimension 2",
                    MDSLAYOUT,
                    "Choose the second dimension to be used in the layout",
                    "getDimension2", "setDimension2", NodeColumnNumbersEditor.class));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public Column getDimension1() {
        return dimension1;
    }

    public void setDimension1(Column dimension1) {
        this.dimension1 = dimension1;
    }

    public Column getDimension2() {
        return dimension2;
    }

    public void setDimension2(Column dimension2) {
        this.dimension2 = dimension2;
    }

    private static class MDSLayoutData implements LayoutData {

        //Data
        public double x = 0f;
        public double y = 0f;
    }

}
