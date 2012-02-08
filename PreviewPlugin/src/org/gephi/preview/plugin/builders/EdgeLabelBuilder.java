/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
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
package org.gephi.preview.plugin.builders;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.TextData;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.EdgeLabelItem;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.visualization.api.VisualizationController;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ItemBuilder.class)
public class EdgeLabelBuilder implements ItemBuilder {

    public Item[] getItems(Graph graph, AttributeModel attributeModel) {
        HierarchicalGraph hgraph = (HierarchicalGraph) graph;

        boolean useTextData = false;
        for (Edge e : hgraph.getEdgesAndMetaEdges()) {
            TextData textData = e.getEdgeData().getTextData();
            if (textData != null && textData.getText() != null && !textData.getText().isEmpty()) {
                useTextData = true;
            }
        }

        //Build text
        DynamicController dynamicController = Lookup.getDefault().lookup(DynamicController.class);
        DynamicModel model = dynamicController != null ? dynamicController.getModel(graph.getGraphModel().getWorkspace()) : null;
        TimeInterval timeInterval = model != null ? model.getVisibleInterval() : null;
        Estimator estimator = model != null ? model.getEstimator() : null;
        Estimator numberEstimator = model != null ? model.getNumberEstimator() : null;
        VisualizationController vizController = Lookup.getDefault().lookup(VisualizationController.class);
        AttributeColumn[] edgeColumns = vizController != null ? vizController.getEdgeTextColumns() : null;

        List<Item> items = new ArrayList<Item>();
        for (Edge e : hgraph.getEdgesAndMetaEdges()) {
            EdgeLabelItem labelItem = new EdgeLabelItem(e);
            String label = getLabel(e, edgeColumns, timeInterval, estimator, numberEstimator);
            labelItem.setData(EdgeLabelItem.LABEL, label);
            TextData textData = e.getEdgeData().getTextData();
            if (textData != null && useTextData) {
                if (textData.getR() != -1) {
                    labelItem.setData(EdgeLabelItem.COLOR, new Color((int) (textData.getR() * 255),
                            (int) (textData.getG() * 255),
                            (int) (textData.getB() * 255),
                            (int) (textData.getAlpha() * 255)));
                }
                labelItem.setData(EdgeLabelItem.WIDTH, textData.getWidth());
                labelItem.setData(EdgeLabelItem.HEIGHT, textData.getHeight());
                labelItem.setData(EdgeLabelItem.SIZE, textData.getSize());
                labelItem.setData(EdgeLabelItem.VISIBLE, textData.isVisible());
                if (textData.isVisible() && textData.getText() != null && !textData.getText().isEmpty()) {
                    items.add(labelItem);
                }
            } else if (label != null && !label.isEmpty()) {
                items.add(labelItem);
            }
        }
        return items.toArray(new Item[0]);
    }

    private String getLabel(Edge n, AttributeColumn[] cols, TimeInterval interval, Estimator estimator, Estimator numberEstimator) {
        EdgeData edgeData = n.getEdgeData();
        String str = "";
        if (cols != null) {
            int i = 0;
            for (AttributeColumn c : cols) {
                if (i++ > 0) {
                    str += " - ";
                }
                Object val = edgeData.getAttributes().getValue(c.getIndex());
                if (val instanceof DynamicType) {
                    DynamicType dynamicType = (DynamicType) val;
                    if (estimator == null) {
                        estimator = Estimator.FIRST;
                    }
                    if (Number.class.isAssignableFrom(dynamicType.getUnderlyingType())) {
                        estimator = numberEstimator;
                    }
                    if (interval != null) {
                        val = dynamicType.getValue(interval.getLow(), interval.getHigh(), estimator);
                    } else {
                        val = dynamicType.getValue(estimator);
                    }
                }
                str += val != null ? val : "";
            }
        }
        if (str.isEmpty()) {
            str = edgeData.getLabel();
        }
        if (str == null) {
            str = "";
        }
        return str;
    }

    public String getType() {
        return ItemBuilder.EDGE_LABEL_BUILDER;
    }
}
