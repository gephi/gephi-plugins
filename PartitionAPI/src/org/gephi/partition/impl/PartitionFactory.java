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
package org.gephi.partition.impl;

import com.google.common.collect.ArrayListMultimap;
import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.NodeData;
import org.gephi.partition.api.EdgePartition;
import org.gephi.partition.api.NodePartition;
import org.gephi.partition.api.Part;
import org.gephi.partition.api.Partition;

/**
 *
 * @author Mathieu Bastian
 */
public class PartitionFactory {

    public static boolean isPartitionColumn(AttributeColumn column) {
        return column.getType().equals(AttributeType.STRING)
                || column.getType().equals(AttributeType.BOOLEAN)
                || column.getType().equals(AttributeType.INT)
                || column.getType().equals(AttributeType.SHORT);
    }

    public static boolean isDynamicPartitionColumn(AttributeColumn column) {
        return column.getType().equals(AttributeType.DYNAMIC_STRING)
                || column.getType().equals(AttributeType.DYNAMIC_BOOLEAN)
                || column.getType().equals(AttributeType.DYNAMIC_INT)
                || column.getType().equals(AttributeType.DYNAMIC_SHORT);
    }

    public static boolean isNodePartitionColumn(AttributeColumn column, Graph graph) {
        Set values = new HashSet();
        int nonNullvalues = 0;
        for (Node n : graph.getNodes()) {
            Object value = n.getNodeData().getAttributes().getValue(column.getIndex());
            if (value != null) {
                nonNullvalues++;
            }
            values.add(value);
        }
        if (values.size() < 9f / 10f * nonNullvalues) {      //If #different values is < 9:10 of total non-null values
            return true;
        }
        return false;
    }

    public static boolean isEdgePartitionColumn(AttributeColumn column, Graph graph) {
        Set values = new HashSet();
        int nonNullvalues = 0;
        for (Edge n : graph.getEdges()) {
            Object value = n.getEdgeData().getAttributes().getValue(column.getIndex());
            if (value != null) {
                nonNullvalues++;
            }
            values.add(value);
        }
        if (values.size() < 9f / 10f * nonNullvalues) {      //If #different values is < 9:10 of total non-null values
            return true;
        }
        return false;
    }

    public static boolean isDynamicNodePartitionColumn(AttributeColumn column, Graph graph, TimeInterval timeInterval, Estimator estimator) {
        Set values = new HashSet();
        int nonNullvalues = 0;
        for (Node n : graph.getNodes()) {
            Object value = n.getNodeData().getAttributes().getValue(column.getIndex());
            value = getDynamicValue(value, timeInterval, estimator);
            if (value != null) {
                nonNullvalues++;
            }
            values.add(value);
        }
        if (values.size() < 9f / 10f * nonNullvalues) {      //If #different values is < 9:10 of total non-null values
            return true;
        }
        return false;
    }

    public static boolean isDynamicEdgePartitionColumn(AttributeColumn column, Graph graph, TimeInterval timeInterval, Estimator estimator) {
        Set values = new HashSet();
        int nonNullvalues = 0;
        for (Edge n : graph.getEdges()) {
            Object value = n.getEdgeData().getAttributes().getValue(column.getIndex());
            value = getDynamicValue(value, timeInterval, estimator);
            if (value != null) {
                nonNullvalues++;
            }
            values.add(value);
        }
        if (values.size() < 9f / 10f * nonNullvalues) {      //If #different values is < 9:10 of total non-null values
            return true;
        }
        return false;
    }

    private static Object getDynamicValue(Object object, TimeInterval timeInterval, Estimator estimator) {
        if (object != null && object instanceof DynamicType) {
            DynamicType dynamicType = (DynamicType) object;
            return dynamicType.getValue(timeInterval == null ? Double.NEGATIVE_INFINITY : timeInterval.getLow(),
                    timeInterval == null ? Double.POSITIVE_INFINITY : timeInterval.getHigh(), estimator);
        }
        return object;
    }

    public static NodePartition createNodePartition(AttributeColumn column) {
        return new NodePartitionImpl(column);
    }

    public static EdgePartition createEdgePartition(AttributeColumn column) {
        return new EdgePartitionImpl(column);
    }

    public static boolean isPartitionBuilt(Partition partition) {
        return partition.getParts().length > 0;
    }

    public static void buildNodePartition(NodePartition partition, Graph graph) {
        buildNodePartition(partition, graph, null, null);
    }

    public static void buildNodePartition(NodePartition partition, Graph graph, TimeInterval timeInterval, Estimator estimator) {

        NodePartitionImpl partitionImpl = (NodePartitionImpl) partition;
        ArrayListMultimap<Object, Node> multimap = ArrayListMultimap.create();
        for (Node n : graph.getNodes()) {
            Object value = n.getNodeData().getAttributes().getValue(partitionImpl.column.getIndex());
            value = getDynamicValue(value, timeInterval, estimator);
            multimap.put(value, n);
        }

        PartImpl<Node>[] parts = new PartImpl[multimap.keySet().size()];
        Map<Object, Collection<Node>> map = multimap.asMap();
        int i = 0;
        for (Entry<Object, Collection<Node>> entry : map.entrySet()) {
            PartImpl<Node> part = new PartImpl<Node>(partition, entry.getKey(), entry.getValue().toArray(new Node[0]));
            parts[i] = part;
            i++;
        }
        partitionImpl.setParts(parts);
    }

    public static void buildEdgePartition(EdgePartition partition, Graph graph) {
        buildEdgePartition(partition, graph, null, null);
    }

    public static void buildEdgePartition(EdgePartition partition, Graph graph, TimeInterval timeInterval, Estimator estimator) {
        EdgePartitionImpl partitionImpl = (EdgePartitionImpl) partition;

        ArrayListMultimap<Object, Edge> multimap = ArrayListMultimap.create();
        for (Edge n : graph.getEdges()) {
            Object value = n.getEdgeData().getAttributes().getValue(partitionImpl.column.getIndex());
            value = getDynamicValue(value, timeInterval, estimator);
            multimap.put(value, n);
        }

        PartImpl<Edge>[] parts = new PartImpl[multimap.keySet().size()];
        Map<Object, Collection<Edge>> map = multimap.asMap();
        int i = 0;
        for (Entry<Object, Collection<Edge>> entry : map.entrySet()) {
            PartImpl<Edge> part = new PartImpl<Edge>(partition, entry.getKey(), entry.getValue().toArray(new Edge[0]));
            parts[i] = part;
            i++;
        }
        partitionImpl.setParts(parts);
    }

    private static class NodePartitionImpl implements NodePartition {

        private HashMap<NodeData, Part<Node>> nodeMap;
        private HashMap<Object, Part<Node>> valueMap;
        private PartImpl<Node>[] parts;
        private AttributeColumn column;

        public NodePartitionImpl(AttributeColumn column) {
            this.column = column;
            nodeMap = new HashMap<NodeData, Part<Node>>();
            valueMap = new HashMap<Object, Part<Node>>();
            parts = new PartImpl[0];
        }

        public int getPartsCount() {
            return parts.length;
        }

        public Part<Node> getPartFromValue(Object value) {
            return valueMap.get(value);
        }

        public Part<Node>[] getParts() {
            return parts;
        }

        public Map<NodeData, Part<Node>> getMap() {
            return nodeMap;
        }

        public Part<Node> getPart(Node element) {
            return nodeMap.get(element.getNodeData());
        }

        public void setParts(PartImpl<Node>[] parts) {
            this.parts = parts;
            List<Color> colors = getSequenceColors(parts.length);
            int i = 0;
            for (PartImpl<Node> p : parts) {
                for (Node n : p.objects) {
                    nodeMap.put(n.getNodeData(), p);
                }
                p.setColor(colors.get(i));
                valueMap.put(p.getValue(), p);
                i++;
            }
        }

        public AttributeColumn getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return column.getTitle();
        }

        public int getElementsCount() {
            return nodeMap.size();
        }
    }

    private static class EdgePartitionImpl implements EdgePartition {

        private HashMap<EdgeData, Part<Edge>> edgeMap;
        private PartImpl<Edge>[] parts;
        private HashMap<Object, Part<Edge>> valueMap;
        private AttributeColumn column;

        public EdgePartitionImpl(AttributeColumn column) {
            this.column = column;
            edgeMap = new HashMap<EdgeData, Part<Edge>>();
            valueMap = new HashMap<Object, Part<Edge>>();
            parts = new PartImpl[0];
        }

        public int getPartsCount() {
            return parts.length;
        }

        public Part<Edge>[] getParts() {
            return parts;
        }

        public Part<Edge> getPartFromValue(Object value) {
            return valueMap.get(value);
        }

        public Map<EdgeData, Part<Edge>> getMap() {
            return edgeMap;
        }

        public Part<Edge> getPart(Edge element) {
            return edgeMap.get(element.getEdgeData());
        }

        public void setParts(PartImpl<Edge>[] parts) {
            this.parts = parts;
            List<Color> colors = getSequenceColors(parts.length);
            int i = 0;
            for (PartImpl<Edge> p : parts) {
                for (Edge e : p.objects) {
                    edgeMap.put(e.getEdgeData(), p);
                }
                p.setColor(colors.get(i));
                valueMap.put(p.getValue(), p);
                i++;
            }
        }

        public AttributeColumn getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return column.getTitle();
        }

        public int getElementsCount() {
            return edgeMap.size();
        }
    }

    private static class PartImpl<Element> implements Part<Element> {

        private static final String NULL = "null";
        private Partition<Element> partition;
        private Element[] objects;
        private Object value;
        private Color color;

        public PartImpl(Partition<Element> partition, Object value, Element[] objects) {
            this.partition = partition;
            this.value = value;
            this.objects = objects;
        }

        public Element[] getObjects() {
            return objects;
        }

        public Object getValue() {
            return value;
        }

        public String getDisplayName() {
            return value != null ? value.toString() : NULL;
        }

        public boolean isInPart(Element element) {
            return partition.getPart(element) == this;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        public Color getColor() {
            return color;
        }

        public float getPercentage() {
            return objects.length / (float) partition.getElementsCount();
        }

        public Partition getPartition() {
            return partition;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        public int compareTo(Object o) {
            int thisCount = objects.length;
            int theirCount = ((PartImpl) o).objects.length;
            return thisCount == theirCount ? 0 : thisCount > theirCount ? 1 : -1;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj != null && obj instanceof PartImpl) {
                if (value == null && ((PartImpl) obj).value == null) {
                    return true;
                } else if (((PartImpl) obj).value != null && value != null) {
                    return ((PartImpl) obj).value.equals(value);
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + (this.value != null ? this.value.hashCode() : 0);
            return hash;
        }
    }

    public static List<Color> getSequenceColors(int num) {
        List<Color> colors = new LinkedList<Color>();

        //On choisit H et S au random
        Random random = new Random();
        float B = random.nextFloat() * 2 / 5f + 0.6f;		//		0.6 <=   B   < 1
        float S = random.nextFloat() * 2 / 5f + 0.6f;		//		0.6 <=   S   < 1
        //System.out.println("B : "+B+"  S : "+S);

        for (int i = 1; i <= num; i++) {
            float H = i / (float) num;
            //System.out.println(H);
            Color c = Color.getHSBColor(H, S, B);
            colors.add(c);
        }

        Collections.shuffle(colors);

        return colors;
    }
}
