/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
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
package org.gephi.ui.tools.plugin.edit;

import java.awt.Color;
import java.util.EnumSet;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeValue;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * PropertySheet that allows to edit one or more nodes.
 * If multiple node edition mode is used at first all values will be shown as blank
 * but will change with the editions and all nodes will be set the values that the user inputs.
 * @author Mathieu Bastian
 */
public class EditNodes extends AbstractNode {

    private PropertySet[] propertySets;
    private Node[] nodes;
    private boolean multipleNodes;
    private TimeFormat currentTimeFormat=TimeFormat.DOUBLE;

    /**
     * Single node edition mode will always be enabled with this single node constructor
     * @param node
     */
    public EditNodes(Node node) {
        super(Children.LEAF);
        this.nodes = new Node[]{node};
        setName(node.getNodeData().getLabel());
        multipleNodes = false;
    }

    /**
     * If the nodes array has more than one element, multiple nodes edition mode will be enabled.
     * @param nodes
     */
    public EditNodes(Node[] nodes) {
        super(Children.LEAF);
        this.nodes = nodes;
        multipleNodes = nodes.length > 1;
        if (multipleNodes) {
            setName(NbBundle.getMessage(EditNodes.class, "EditNodes.multiple.elements"));
        } else {
            setName(nodes[0].getNodeData().getLabel());
        }
    }

    @Override
    public PropertySet[] getPropertySets() {
        propertySets = new PropertySet[]{prepareNodesProperties(), prepareNodesAttributes()};
        return propertySets;
    }

    /**
     * Prepare set of attributes of the node(s).
     * @return Set of these attributes
     */
    private Sheet.Set prepareNodesAttributes() {
        try {
            DynamicModel dm=Lookup.getDefault().lookup(DynamicController.class).getModel();
            if(dm!=null){
                currentTimeFormat=dm.getTimeFormat();
            }
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            Sheet.Set set = new Sheet.Set();
            set.setName("attributes");
            if (nodes.length > 1) {
                set.setDisplayName(NbBundle.getMessage(EditNodes.class, "EditNodes.attributes.text.multiple"));
            } else {
                set.setDisplayName(NbBundle.getMessage(EditNodes.class, "EditNodes.attributes.text", nodes[0].getNodeData().getLabel()));
            }

            AttributeRow row = (AttributeRow) nodes[0].getNodeData().getAttributes();
            AttributeValueWrapper wrap;
            for (AttributeValue value : row.getValues()) {

                if (multipleNodes) {
                    wrap = new MultipleNodesAttributeValueWrapper(nodes, value.getColumn());
                } else {
                    wrap = new SingleNodeAttributeValueWrapper(row, value.getColumn());
                }
                AttributeType type = value.getColumn().getType();
                Property p;
                if (ac.canChangeColumnData(value.getColumn())) {
                    //Editable column, provide "set" method:
                    if (!NotSupportedTypes.contains(type)) {//The AttributeType can be edited by default:
                        p = new PropertySupport.Reflection(wrap, type.getType(), "getValue" + type.getType().getSimpleName(), "setValue" + type.getType().getSimpleName());
                    } else {//Use the AttributeType as String:
                        p = new PropertySupport.Reflection(wrap, String.class, "getValueAsString", "setValueAsString");
                    }
                } else {
                    //Not editable column, do not provide "set" method:
                    if (!NotSupportedTypes.contains(type)) {//The AttributeType can be edited by default:
                        p = new PropertySupport.Reflection(wrap, type.getType(), "getValue" + type.getType().getSimpleName(), null);
                    } else {//Use the AttributeType as String:
                        p = new PropertySupport.Reflection(wrap, String.class, "getValueAsString", null);
                    }
                }
                p.setDisplayName(value.getColumn().getTitle());
                p.setName(value.getColumn().getId());
                set.put(p);
            }
            return set;
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    /**
     * Prepare set of editable properties of the node(s): size, position.
     * @return Set of these properties
     */
    private Sheet.Set prepareNodesProperties() {
        try {
            if (multipleNodes) {
                MultipleNodesPropertiesWrapper nodesWrapper = new MultipleNodesPropertiesWrapper(nodes);
                Sheet.Set set = new Sheet.Set();
                set.setName("properties");
                set.setDisplayName(NbBundle.getMessage(EditNodes.class, "EditNodes.properties.text.multiple"));

                Property p;
                //Size:
                p = new PropertySupport.Reflection(nodesWrapper, Float.class, "getNodesSize", "setNodesSize");
                p.setDisplayName(NbBundle.getMessage(EditNodes.class, "EditNodes.size.text"));
                p.setName("size");
                set.put(p);

                //All position coordinates:
                set.put(buildMultipleNodesGeneralPositionProperty(nodesWrapper, "x"));
                set.put(buildMultipleNodesGeneralPositionProperty(nodesWrapper, "y"));
                set.put(buildMultipleNodesGeneralPositionProperty(nodesWrapper, "z"));

                //Color:
                p = new PropertySupport.Reflection(nodesWrapper, Color.class, "getNodesColor", "setNodesColor");
                p.setDisplayName(NbBundle.getMessage(EditNodes.class, "EditNodes.color.text"));
                p.setName("color");
                set.put(p);

                return set;
            } else {
                Node node = nodes[0];
                Sheet.Set set = new Sheet.Set();
                set.setName("properties");
                set.setDisplayName(NbBundle.getMessage(EditNodes.class, "EditNodes.properties.text", node.getNodeData().getLabel()));
                NodeData data = node.getNodeData();

                Property p;
                //Size:
                p = new PropertySupport.Reflection(data, Float.TYPE, "getSize", "setSize");
                p.setDisplayName(NbBundle.getMessage(EditNodes.class, "EditNodes.size.text"));
                p.setName("size");
                set.put(p);

                //All position coordinates:
                set.put(buildGeneralPositionProperty(data, "x"));
                set.put(buildGeneralPositionProperty(data, "y"));
                set.put(buildGeneralPositionProperty(data, "z"));

                //Color:
                SingleNodePropertiesWrapper nodeWrapper = new SingleNodePropertiesWrapper(node);
                p = new PropertySupport.Reflection(nodeWrapper, Color.class, "getNodeColor", "setNodeColor");
                p.setDisplayName(NbBundle.getMessage(EditNodes.class, "EditNodes.color.text"));
                p.setName("color");
                set.put(p);

                return set;
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }

    public class SingleNodePropertiesWrapper {

        private Node node;

        public SingleNodePropertiesWrapper(Node node) {
            this.node = node;
        }

        public Color getNodeColor() {
            NodeData data = node.getNodeData();
            return new Color(data.r(), data.g(), data.b(), data.alpha());
        }

        public void setNodeColor(Color c) {
            if (c != null) {
                NodeData data = node.getNodeData();
                data.setR(c.getRed() / 255f);
                data.setG(c.getGreen() / 255f);
                data.setB(c.getBlue() / 255f);
                data.setAlpha(c.getAlpha() / 255f);
            }
        }
    }

    public class MultipleNodesPropertiesWrapper {

        Node[] nodes;

        public MultipleNodesPropertiesWrapper(Node[] nodes) {
            this.nodes = nodes;
        }
        //Methods and fields for multiple nodes editing:
        private Float nodesX = null;
        private Float nodesY = null;
        private Float nodesZ = null;
        private Float nodesSize = null;
        private Color nodesColor = null;

        public Float getNodesX() {
            return nodesX;
        }

        public void setNodesX(Float x) {
            nodesX = x;
            for (Node node : nodes) {
                node.getNodeData().setX(x);
            }
        }

        public Float getNodesY() {
            return nodesY;
        }

        public void setNodesY(Float y) {
            nodesY = y;
            for (Node node : nodes) {
                node.getNodeData().setY(y);
            }
        }

        public Float getNodesZ() {
            return nodesZ;
        }

        public void setNodesZ(Float z) {
            nodesZ = z;
            for (Node node : nodes) {
                node.getNodeData().setZ(z);
            }
        }

        public Color getNodesColor() {
            return nodesColor;
        }

        public void setNodesColor(Color c) {
            if (c != null) {
                nodesColor = c;
                NodeData data;
                for (Node node : nodes) {
                    data = node.getNodeData();
                    data.setR(c.getRed() / 255f);
                    data.setG(c.getGreen() / 255f);
                    data.setB(c.getBlue() / 255f);
                    data.setAlpha(c.getAlpha() / 255f);
                }
            }
        }

        public Float getNodesSize() {
            return nodesSize;
        }

        public void setNodesSize(Float size) {
            nodesSize = size;
            for (Node node : nodes) {
                node.getNodeData().setSize(size);
            }
        }
    }

    /**
     * Used to build property for each position coordinate (x,y,z) in the same way.
     * @return Property for that coordinate
     */
    private Property buildGeneralPositionProperty(NodeData data, String coordinate) throws NoSuchMethodException {
        //Position:
        Property p = new PropertySupport.Reflection(data, Float.TYPE, coordinate, "set" + coordinate.toUpperCase());
        p.setDisplayName(NbBundle.getMessage(EditNodes.class, "EditNodes.position.text", coordinate));
        p.setName(coordinate);
        return p;
    }

    /**
     * Used to build property for each position coordinate of various nodes (x,y,z) in the same way.
     * @return Property for that coordinate
     */
    private Property buildMultipleNodesGeneralPositionProperty(MultipleNodesPropertiesWrapper nodesWrapper, String coordinate) throws NoSuchMethodException {
        //Position:
        Property p = new PropertySupport.Reflection(nodesWrapper, Float.class, "getNodes" + coordinate.toUpperCase(), "setNodes" + coordinate.toUpperCase());
        p.setDisplayName(NbBundle.getMessage(EditNodes.class, "EditNodes.position.text", coordinate));
        p.setName(coordinate);
        return p;
    }
    /**
     * These AttributeTypes are not supported by default by netbeans property editor.
     * We will use attributes of these types as Strings and parse them.
     */
    private static EnumSet<AttributeType> NotSupportedTypes = EnumSet.of(
            AttributeType.BIGINTEGER,
            AttributeType.BIGDECIMAL,
            AttributeType.LIST_BIGDECIMAL,
            AttributeType.LIST_BIGINTEGER,
            AttributeType.LIST_BOOLEAN,
            AttributeType.LIST_BYTE,
            AttributeType.LIST_CHARACTER,
            AttributeType.LIST_DOUBLE,
            AttributeType.LIST_FLOAT,
            AttributeType.LIST_INTEGER,
            AttributeType.LIST_LONG,
            AttributeType.LIST_SHORT,
            AttributeType.LIST_STRING,
            AttributeType.TIME_INTERVAL,
            AttributeType.DYNAMIC_BIGDECIMAL,
            AttributeType.DYNAMIC_BIGINTEGER,
            AttributeType.DYNAMIC_BOOLEAN,
            AttributeType.DYNAMIC_BYTE,
            AttributeType.DYNAMIC_CHAR,
            AttributeType.DYNAMIC_DOUBLE,
            AttributeType.DYNAMIC_FLOAT,
            AttributeType.DYNAMIC_INT,
            AttributeType.DYNAMIC_LONG,
            AttributeType.DYNAMIC_SHORT,
            AttributeType.DYNAMIC_STRING);

    public interface AttributeValueWrapper {

        public Byte getValueByte();

        public void setValueByte(Byte object);

        public Short getValueShort();

        public void setValueShort(Short object);

        public Character getValueCharacter();

        public void setValueCharacter(Character object);

        public String getValueString();

        public void setValueString(String object);

        public Double getValueDouble();

        public void setValueDouble(Double object);

        public Float getValueFloat();

        public void setValueFloat(Float object);

        public Integer getValueInteger();

        public void setValueInteger(Integer object);

        public Boolean getValueBoolean();

        public void setValueBoolean(Boolean object);

        public Long getValueLong();

        public void setValueLong(Long object);

        /****** Other types are not supported by property editors by default so they are used and parsed as Strings ******/
        public String getValueAsString();

        public void setValueAsString(String value);
    }

    public class SingleNodeAttributeValueWrapper implements AttributeValueWrapper {

        private AttributeRow row;
        private AttributeColumn column;

        public SingleNodeAttributeValueWrapper(AttributeRow row, AttributeColumn column) {
            this.row = row;
            this.column = column;
        }

        private String convertToStringIfNotNull() {
            Object value = row.getValue(column.getIndex());
            if (value != null) {
                if (value instanceof DynamicType) {
                    return ((DynamicType) value).toString(currentTimeFormat==TimeFormat.DOUBLE);
                } else {
                    return value.toString();
                }
            } else {
                return null;
            }
        }

        public Byte getValueByte() {
            return (Byte) row.getValue(column.getIndex());
        }

        public void setValueByte(Byte object) {
            row.setValue(column.getIndex(), object);
        }

        public Short getValueShort() {
            return (Short) row.getValue(column.getIndex());
        }

        public void setValueShort(Short object) {
            row.setValue(column.getIndex(), object);
        }

        public Character getValueCharacter() {
            return (Character) row.getValue(column.getIndex());
        }

        public void setValueCharacter(Character object) {
            row.setValue(column.getIndex(), object);
        }

        public String getValueString() {
            return (String) row.getValue(column.getIndex());
        }

        public void setValueString(String object) {
            row.setValue(column.getIndex(), object);
        }

        public Double getValueDouble() {
            return (Double) row.getValue(column.getIndex());
        }

        public void setValueDouble(Double object) {
            row.setValue(column.getIndex(), object);
        }

        public Float getValueFloat() {
            return (Float) row.getValue(column.getIndex());
        }

        public void setValueFloat(Float object) {
            row.setValue(column.getIndex(), object);
        }

        public Integer getValueInteger() {
            return (Integer) row.getValue(column.getIndex());
        }

        public void setValueInteger(Integer object) {
            row.setValue(column.getIndex(), object);
        }

        public Boolean getValueBoolean() {
            return (Boolean) row.getValue(column.getIndex());
        }

        public void setValueBoolean(Boolean object) {
            row.setValue(column.getIndex(), object);
        }

        public Long getValueLong() {
            return (Long) row.getValue(column.getIndex());
        }

        public void setValueLong(Long object) {
            row.setValue(column.getIndex(), object);
        }

        public String getValueAsString() {
            return convertToStringIfNotNull();
        }

        public void setValueAsString(String value) {
            row.setValue(column.getIndex(), column.getType().parse(value));
        }
    }

    public class MultipleNodesAttributeValueWrapper implements AttributeValueWrapper {

        private Node[] nodes;
        private AttributeColumn column;
        private Object value;

        public MultipleNodesAttributeValueWrapper(Node[] nodes, AttributeColumn column) {
            this.nodes = nodes;
            this.column = column;
            this.value = null;
        }

        private String convertToStringIfNotNull() {
            if (value != null) {
                if (value instanceof DynamicType) {
                    return ((DynamicType) value).toString(currentTimeFormat==TimeFormat.DOUBLE);
                } else {
                    return value.toString();
                }
            } else {
                return null;
            }
        }

        private void setValueToAllNodes(Object object) {
            this.value = object;
            for (Node node : nodes) {
                node.getNodeData().getAttributes().setValue(column.getIndex(), value);
            }
        }

        public Byte getValueByte() {
            return (Byte) value;
        }

        public void setValueByte(Byte object) {
            setValueToAllNodes(object);
        }

        public Short getValueShort() {
            return (Short) value;
        }

        public void setValueShort(Short object) {
            setValueToAllNodes(object);
        }

        public Character getValueCharacter() {
            return (Character) value;
        }

        public void setValueCharacter(Character object) {
            setValueToAllNodes(object);
        }

        public String getValueString() {
            return (String) value;
        }

        public void setValueString(String object) {
            setValueToAllNodes(object);
        }

        public Double getValueDouble() {
            return (Double) value;
        }

        public void setValueDouble(Double object) {
            setValueToAllNodes(object);
        }

        public Float getValueFloat() {
            return (Float) value;
        }

        public void setValueFloat(Float object) {
            setValueToAllNodes(object);
        }

        public Integer getValueInteger() {
            return (Integer) value;
        }

        public void setValueInteger(Integer object) {
            setValueToAllNodes(object);
        }

        public Boolean getValueBoolean() {
            return (Boolean) value;
        }

        public void setValueBoolean(Boolean object) {
            setValueToAllNodes(object);
        }

        public Long getValueLong() {
            return (Long) value;
        }

        public void setValueLong(Long object) {
            setValueToAllNodes(object);
        }

        public String getValueAsString() {
            return convertToStringIfNotNull();
        }

        public void setValueAsString(String value) {
            setValueToAllNodes(column.getType().parse(value));
        }
    }
}
