/*
 * Copyright 2026 Matt Artz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gephi.plugins.shapebypartition;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.renderers.SizeUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests that edges and arrowheads meet a shaped node's outline rather than
 * the circle Gephi's own renderers assume.
 *
 * @author Matt Artz
 */
public class EdgeEndsTest {

    private static final float EDGE_RADIUS = 2f;
    private static final float ARROW = 5f;
    private static final float SIZE = 20f;

    private GraphModel graphModel;
    private PreviewProperties properties;
    private Map<Item, NodeShape> shapes;

    @Before
    public void setUp() {
        graphModel = GraphModel.Factory.newInstance();
        properties = new PreviewProperties();
        properties.putValue(PreviewProperty.EDGE_RADIUS, EDGE_RADIUS);
        properties.putValue(PreviewProperty.ARROW_SIZE, ARROW);
        properties.putValue(PreviewProperty.EDGE_CURVED, false);
        properties.putValue("edge.arc-curveness", 2d);
        properties.putValue(PreviewProperty.NODE_SCALE_FACTOR, 1f);
        properties.putValue(PreviewProperty.NODE_BORDER_WIDTH, 0f);
        properties.putValue(PreviewProperty.NODE_BORDER_FIXED, true);
        shapes = new HashMap<>();
    }

    @Test
    public void anEdgeIntoAShapedNodeStopsAtItsOutline() {
        // The target sits directly above the source, so the edge arrives at
        // the triangle's flat base, which lies well inside the circle.
        Item source = node("s", 0, 100, null);
        Item target = node("t", 0, 0, NodeShape.TRIANGLE);
        Item edge = edge(source, target, true);

        EdgeEnds.fit(model(edge), shapes::get);

        double reach = NodeShape.TRIANGLE.reach(0, 1, SIZE);
        assertTrue("the base is inside the circle", reach < SIZE / 2d);
        assertEquals(-(EDGE_RADIUS + ARROW + reach), (Float) edge.getData(EdgeEnds.TARGET_RADIUS), 1e-4);
        assertEquals(reach, (Float) edge.getData(EdgeEnds.TARGET_REACH), 1e-4);
    }

    @Test
    public void theSourceEndIsFittedToo() {
        Item source = node("s", 0, 0, NodeShape.DIAMOND);
        Item target = node("t", 100, 0, null);
        Item edge = edge(source, target, true);

        EdgeEnds.fit(model(edge), shapes::get);

        double reach = NodeShape.DIAMOND.reach(1, 0, SIZE);
        assertEquals(-(EDGE_RADIUS + reach), (Float) edge.getData(EdgeEnds.SOURCE_RADIUS), 1e-4);
        assertEquals("a circular target is left as Gephi set it",
            -(EDGE_RADIUS + ARROW + SIZE / 2f), (Float) edge.getData(EdgeEnds.TARGET_RADIUS), 1e-4);
        assertNull(edge.getData(EdgeEnds.TARGET_REACH));
    }

    @Test
    public void theArrowTipIsMovedOntoTheOutline() {
        Item source = node("s", 0, 100, null);
        Item target = node("t", 0, 0, NodeShape.STAR);
        Item edge = edge(source, target, true);
        EdgeEnds.fit(model(edge), shapes::get);

        Item fitted = ShapeAwareArrowRenderer.fittedToOutline(edge, properties);

        // Gephi's arrow renderer puts the tip at edge radius + size / 2.
        Item resized = fitted.getData("target");
        assertEquals(NodeShape.STAR.reach(0, 1, SIZE),
            SizeUtils.getNodeSize(resized, properties) / 2f, 1e-4);
        assertEquals("the node itself is unchanged", SIZE, SizeUtils.getNodeSize(target, properties), 0);
        assertEquals("ids and colours still come from the real node",
            target.getSource(), resized.getSource());
    }

    @Test
    public void edgesBetweenCircularNodesAreUntouched() {
        Item edge = edge(node("s", 0, 100, null), node("t", 0, 0, NodeShape.CIRCLE), true);

        EdgeEnds.fit(model(edge), shapes::get);

        assertEquals(-(EDGE_RADIUS + ARROW + SIZE / 2f), (Float) edge.getData(EdgeEnds.TARGET_RADIUS), 1e-6);
        assertNull(ShapeAwareArrowRenderer.fittedToOutline(edge, properties));
    }

    @Test
    public void aCurvedEdgeIsFittedWhereItArrives() {
        properties.putValue(PreviewProperty.EDGE_CURVED, true);
        Item source = node("s", 0, 100, null);
        Item target = node("t", 0, 0, NodeShape.SQUARE);
        Item edge = edge(source, target, true);

        EdgeEnds.fit(model(edge), shapes::get);

        // An arc arrives at an angle, so it meets the square's side further
        // from the centre than a straight edge from below would.
        float reach = edge.getData(EdgeEnds.TARGET_REACH);
        assertTrue(reach > NodeShape.SQUARE.reach(0, 1, SIZE) + 1e-3);
        assertEquals(-(EDGE_RADIUS + ARROW + reach), (Float) edge.getData(EdgeEnds.TARGET_RADIUS), 1e-4);
    }

    @Test
    public void aBarelyCurvedEdgeArrivesAlmostStraight() {
        double[] towardsTip = EdgeEnds.curvedApproach(0, 0, 0, 100, 100, 1e-4, 15);
        assertEquals(0, towardsTip[0], 1e-3);
        assertEquals(1, towardsTip[1], 1e-3);
    }

    @Test
    public void selfLoopsAreLeftAlone() {
        Item node = node("n", 0, 0, NodeShape.SQUARE);
        Item edge = edge(node, node, true);
        edge.setData("self_loop", true);
        edge.setData(EdgeEnds.SOURCE_RADIUS, 7f);

        EdgeEnds.fit(model(edge), shapes::get);

        assertEquals(7f, (Float) edge.getData(EdgeEnds.SOURCE_RADIUS), 0);
        assertFalse(edge.hasData(EdgeEnds.TARGET_REACH));
    }

    private Item node(String id, float x, float y, NodeShape shape) {
        Node node = graphModel.factory().newNode(id);
        NodeItem item = new NodeItem(node);
        item.setData(NodeItem.X, x);
        item.setData(NodeItem.Y, y);
        item.setData(NodeItem.SIZE, SIZE);
        if (shape != null) {
            shapes.put(item, shape);
        }
        return item;
    }

    /** An edge item carrying what Gephi's edge renderer leaves on it. */
    private Item edge(Item source, Item target, boolean directed) {
        Node s = (Node) source.getSource();
        Node t = (Node) target.getSource();
        if (!graphModel.getGraph().contains(s)) {
            graphModel.getGraph().addNode(s);
        }
        if (!graphModel.getGraph().contains(t)) {
            graphModel.getGraph().addNode(t);
        }
        EdgeItem edge = new EdgeItem(graphModel.factory().newEdge(s, t, directed));
        edge.setData("source", source);
        edge.setData("target", target);
        edge.setData("self_loop", false);
        edge.setData(EdgeEnds.TARGET_RADIUS, -(EDGE_RADIUS + SIZE / 2f + ARROW));
        edge.setData(EdgeEnds.SOURCE_RADIUS, -(EDGE_RADIUS + SIZE / 2f));
        return edge;
    }

    private PreviewModel model(Item... edges) {
        return (PreviewModel) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{PreviewModel.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getProperties":
                        return properties;
                    case "getItems":
                        return Item.EDGE.equals(args[0]) ? edges : new Item[0];
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });
    }
}
