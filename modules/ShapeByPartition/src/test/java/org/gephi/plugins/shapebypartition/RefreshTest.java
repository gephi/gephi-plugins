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
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.plugin.items.NodeItem;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Tests that shapes survive redraws between refreshes.
 * <p>
 * Gephi calls {@code preProcess} only when the preview is refreshed, but calls
 * {@code render} and then {@code postProcess} on every redraw, including each
 * pan and zoom of the preview. Shapes that were dropped after a render came
 * back as circles the moment the view moved.
 *
 * @author Matt Artz
 */
public class RefreshTest {

    @Test
    public void shapesSurviveTheEndOfARender() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        graphModel.getNodeTable().addColumn(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, String.class);
        Item[] items = new Item[3];
        String[] values = {"a", "a", "b"};
        for (int i = 0; i < items.length; i++) {
            Node node = graphModel.factory().newNode("n" + i);
            node.setAttribute(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, values[i]);
            graphModel.getGraph().addNode(node);
            items[i] = new NodeItem(node);
        }
        PreviewProperties properties = new PreviewProperties();
        properties.putValue(ShapeByPartitionRenderer.ENABLED, true);
        properties.putValue(ShapeByPartitionRenderer.COLUMN, "");
        PreviewModel model = (PreviewModel) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{PreviewModel.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getProperties":
                        return properties;
                    case "getItems":
                        return Item.NODE.equals(args[0]) ? items : new Item[0];
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });

        ShapeByPartitionRenderer renderer = new ShapeByPartitionRenderer();
        renderer.preProcess(model);
        assertEquals(NodeShape.SQUARE, renderer.shapeFor(items[2]));

        // One render ends, then the view is panned and drawn again with no refresh.
        renderer.postProcess(model, null, properties);
        assertEquals(NodeShape.CIRCLE, renderer.shapeFor(items[0]));
        assertEquals(NodeShape.SQUARE, renderer.shapeFor(items[2]));
    }

    @Test
    public void refreshingAnotherWorkspaceLeavesThisOnesShapes() {
        // Gephi keeps one renderer for every workspace. Shapes held on the renderer
        // were replaced when another workspace refreshed, so panning back on the
        // first one drew its nodes as circles while its edges kept shape-fitted ends.
        Item[] first = items("a", "a", "b");
        Item[] second = items("x", "y", "y", "y");
        ShapeByPartitionRenderer renderer = new ShapeByPartitionRenderer();

        renderer.preProcess(model(first));
        renderer.preProcess(model(second));

        assertEquals(NodeShape.CIRCLE, renderer.shapeFor(first[0]));
        assertEquals(NodeShape.SQUARE, renderer.shapeFor(first[2]));
        assertEquals(NodeShape.SQUARE, renderer.shapeFor(second[0]));
        assertEquals(NodeShape.CIRCLE, renderer.shapeFor(second[1]));
    }

    private static Item[] items(String... values) {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        graphModel.getNodeTable().addColumn(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, String.class);
        Item[] items = new Item[values.length];
        for (int i = 0; i < values.length; i++) {
            Node node = graphModel.factory().newNode("n" + i);
            node.setAttribute(ShapeByPartitionRenderer.CONVENTIONAL_COLUMN, values[i]);
            graphModel.getGraph().addNode(node);
            items[i] = new NodeItem(node);
        }
        return items;
    }

    private PreviewModel model(Item[] items) {
        PreviewProperties properties = new PreviewProperties();
        properties.putValue(ShapeByPartitionRenderer.ENABLED, true);
        properties.putValue(ShapeByPartitionRenderer.COLUMN, "");
        return (PreviewModel) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{PreviewModel.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getProperties":
                        return properties;
                    case "getItems":
                        return Item.NODE.equals(args[0]) ? items : new Item[0];
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });
    }
}
