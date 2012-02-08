/*
Copyright 2008-2010 Gephi
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
package org.gephi.io.importer.impl;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.Interval;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.data.attributes.type.TypeConvertor;
import org.gephi.dynamic.DynamicUtilities;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.graph.api.Node;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.NodeDraftGetter;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class NodeDraftImpl implements NodeDraft, NodeDraftGetter {

    //Architecture
    private final ImportContainerImpl container;
    //Flag
    private boolean autoId;
    private boolean createdAuto = false;
    //Basic
    private String id;
    private String label;
    private NodeDraftImpl[] parents;
    //Viz attributes
    private Color color;
    private float size;
    private float x;
    private float y;
    private float z;
    private boolean visible = true;
    private boolean fixed = false;
    //Text
    private float labelSize = -1f;
    private boolean labelVisible = true;
    private Color labelColor;
    //Dynamic
    private TimeInterval timeInterval;
    //Attributes
    private final AttributeRow attributeRow;
    //Result
    private Node node;
    private int height;

    public NodeDraftImpl(ImportContainerImpl container, String id) {
        this.container = container;
        this.id = id;
        this.autoId = true;
        this.attributeRow = container.getAttributeModel().rowFactory().newNodeRow(null);
    }

    //SETTERS
    public void setCreatedAuto(boolean createdAuto) {
        this.createdAuto = createdAuto;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setColor(String r, String g, String b) {
        setColor(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
    }

    public void setColor(float r, float g, float b) {
        r = Math.max(Math.min(r, 1f), 0f);
        g = Math.max(Math.min(g, 1f), 0f);
        b = Math.max(Math.min(b, 1f), 0f);
        setColor(new Color(r, g, b));
    }

    public void setColor(int r, int g, int b) {
        setColor(r / 255f, g / 255f, b / 255f);
    }

    public void setColor(String color) {
        setColor(Color.getColor(color));
    }

    public void setId(String id) {
        this.id = id;
        this.autoId = false;
        this.createdAuto = false;
    }

    public void setSize(float size) {
        this.size = size;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void setLabel(String label) {
        this.label = label;
        this.createdAuto = false;
    }

    public void setLabelSize(float size) {
        this.labelSize = size;
    }

    public void setLabelVisible(boolean labelVisible) {
        this.labelVisible = labelVisible;
    }

    public void setLabelColor(Color color) {
        this.labelColor = color;
    }

    public void setLabelColor(String r, String g, String b) {
        setColor(Integer.parseInt(r), Integer.parseInt(g), Integer.parseInt(b));
    }

    public void setLabelColor(float r, float g, float b) {
        r = Math.max(Math.min(r, 1f), 0f);
        g = Math.max(Math.min(g, 1f), 0f);
        b = Math.max(Math.min(b, 1f), 0f);
        setColor(new Color(r, g, b));
    }

    public void setLabelColor(int r, int g, int b) {
        setColor(r / 255f, g / 255f, b / 255f);
    }

    public void setLabelColor(String color) {
        setColor(Color.getColor(color));
    }

    public void setParent(NodeDraft draft) {
        NodeDraftImpl draftImpl = (NodeDraftImpl) draft;
        if (this.parents == null) {
            this.parents = new NodeDraftImpl[1];
            this.parents[0] = draftImpl;
        } else {
            this.parents = Arrays.copyOf(this.parents, this.parents.length + 1);
            this.parents[this.parents.length - 1] = draftImpl;
        }
        draftImpl.setHeight(height + 1);
        container.setHierarchicalGraph(true);
    }

    public void setHeight(int height) {
        if (height > this.height) {
            this.height = height;
            if (parents != null) {
                for (NodeDraftGetter p : parents) {
                    ((NodeDraftImpl) p).setHeight(height + 1);
                }
            }
        }
    }

    public void addChild(NodeDraft child) {
        child.setParent(this);
    }

    public void addAttributeValue(AttributeColumn column, Object value) {
        if (column.getType().isDynamicType() && !(value instanceof DynamicType)) {
            if (value instanceof String && !column.getType().equals(AttributeType.DYNAMIC_STRING)) {
                //Value needs to be parsed
                value = TypeConvertor.getStaticType(column.getType()).parse((String) value);
            }
            //Wrap value in a dynamic type
            value = DynamicUtilities.createDynamicObject(column.getType(), new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, value));
        }
        attributeRow.setValue(column, value);
    }

    public void addAttributeValue(AttributeColumn column, Object value, String dateFrom, String dateTo) throws IllegalArgumentException {
        addAttributeValue(column, value, dateFrom, dateTo, false, false);
    }

    public void addAttributeValue(AttributeColumn column, Object value, String dateFrom, String dateTo, boolean startOpen, boolean endOpen) throws IllegalArgumentException {
        if (!column.getType().isDynamicType()) {
            throw new IllegalArgumentException("The column must be dynamic");
        }
        Double start = Double.NEGATIVE_INFINITY;
        Double end = Double.POSITIVE_INFINITY;
        if (dateFrom != null && !dateFrom.isEmpty()) {
            try {
                if (container.getTimeFormat().equals(TimeFormat.DATE)) {
                    start = DynamicUtilities.getDoubleFromXMLDateString(dateFrom);
                } else {
                    start = Double.parseDouble(dateFrom);
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateFrom));
            }
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            try {
                if (container.getTimeFormat().equals(TimeFormat.DATE)) {
                    end = DynamicUtilities.getDoubleFromXMLDateString(dateTo);
                } else {
                    end = Double.parseDouble(dateTo);
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateTo));
            }
        }
        if ((start == null && end == null) || (start == Double.NEGATIVE_INFINITY && end == Double.POSITIVE_INFINITY)) {
            throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_Empty"));
        }
        if (value instanceof String && !column.getType().equals(AttributeType.DYNAMIC_STRING)) {
            //Value needs to be parsed
            AttributeType staticType = TypeConvertor.getStaticType(column.getType());
            value = staticType.parse((String) value);
        }
        Object sourceVal = attributeRow.getValue(column);
        if (sourceVal != null && sourceVal instanceof DynamicType) {
            value = DynamicUtilities.createDynamicObject(column.getType(), (DynamicType) sourceVal, new Interval(start, end, startOpen, endOpen, value));
        } else if (sourceVal != null && !(sourceVal instanceof DynamicType)) {
            List<Interval> intervals = new ArrayList<Interval>(2);
            intervals.add(new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, sourceVal));
            intervals.add(new Interval(start, end, startOpen, endOpen, value));
            value = DynamicUtilities.createDynamicObject(column.getType(), intervals);
        } else {
            value = DynamicUtilities.createDynamicObject(column.getType(), new Interval(start, end, startOpen, endOpen, value));
        }
        attributeRow.setValue(column, value);
    }

    public void addTimeInterval(String dateFrom, String dateTo) throws IllegalArgumentException {
        addTimeInterval(dateFrom, dateTo, false, false);
    }

    public void addTimeInterval(String dateFrom, String dateTo, boolean startOpen, boolean endOpen) throws IllegalArgumentException {
        if (timeInterval == null) {
            timeInterval = new TimeInterval();
        }
        Double start = null;
        Double end = null;
        if (dateFrom != null && !dateFrom.isEmpty()) {
            try {
                if (container.getTimeFormat().equals(TimeFormat.DATE) ||
                        container.getTimeFormat().equals(TimeFormat.DATETIME)) {
                    start = DynamicUtilities.getDoubleFromXMLDateString(dateFrom);
                } else {
                    start = Double.parseDouble(dateFrom);
                }

            } catch (Exception ex) {
                throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateFrom));
            }
        }
        if (dateTo != null && !dateTo.isEmpty()) {
            try {
                if (container.getTimeFormat().equals(TimeFormat.DATE) ||
                        container.getTimeFormat().equals(TimeFormat.DATETIME)) {
                    end = DynamicUtilities.getDoubleFromXMLDateString(dateTo);
                } else {
                    end = Double.parseDouble(dateTo);
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_ParseError", dateTo));
            }
        }
        if (start == null && end == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(NodeDraftImpl.class, "ImportContainerException_TimeInterval_Empty"));
        }
        timeInterval = new TimeInterval(timeInterval, start != null ? start : Double.NEGATIVE_INFINITY, end != null ? end : Double.POSITIVE_INFINITY, startOpen, endOpen);
    }

    public void setTimeInterval(TimeInterval timeInterval) {
        this.timeInterval = timeInterval;
    }

    //GETTERS
    public AttributeRow getAttributeRow() {
        return attributeRow;
    }

    public Color getColor() {
        return color;
    }

    public String getId() {
        return id;
    }

    public float getSize() {
        return size;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public boolean isFixed() {
        return fixed;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getLabel() {
        return label;
    }

    public float getLabelSize() {
        return labelSize;
    }

    public boolean isLabelVisible() {
        return labelVisible;
    }

    public Color getLabelColor() {
        return labelColor;
    }

    public int getHeight() {
        return height;
    }

    public NodeDraftGetter[] getParents() {
        return parents;
    }

    public TimeInterval getTimeInterval() {
        return timeInterval;
    }

    public boolean isCreatedAuto() {
        return createdAuto;
    }

    public boolean isAutoId() {
        return autoId;
    }

    @Override
    public String toString() {
        String res = "node";
        if (!autoId) {
            res += " id=" + id;
        } else if (label != null) {
            res += " label=" + label;
        } else {
            res += id;
        }
        return res;
    }

    //RESULT
    public void setNode(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }
}
