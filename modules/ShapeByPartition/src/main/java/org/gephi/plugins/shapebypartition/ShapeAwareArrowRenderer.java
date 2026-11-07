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

import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.renderers.ArrowRenderer;
import org.gephi.preview.plugin.renderers.SizeUtils;
import org.gephi.preview.spi.Renderer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Points arrowheads at a shaped node's outline instead of at its circle.
 * <p>
 * Gephi's arrow renderer puts the tip at {@code edge radius + size / 2} from
 * the target's centre. Arrows are drawn before nodes, so where a shape reaches
 * past that circle the arrowhead is hidden under it, and where the shape falls
 * short of the circle the arrow stops in mid-air. For an edge whose target is
 * shaped, this renderer hands Gephi's own arrow code a view of the target whose
 * size puts the tip on the outline. Gephi then draws the arrow as it always
 * does, with the same colour, opacity, and SVG and PDF output.
 * <p>
 * Extending the built-in renderer makes this a replacement for it, enabled on
 * install, in the same way {@link ShapeByPartitionRenderer} replaces the node
 * renderer. Edges to circular nodes are passed through untouched.
 *
 * @author Matt Artz
 */
@ServiceProvider(service = Renderer.class, position = 210)
public class ShapeAwareArrowRenderer extends ArrowRenderer {

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ShapeAwareArrowRenderer.class, "ShapeAwareArrowRenderer.name");
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        Item fitted = fittedToOutline(item, properties);
        super.render(fitted == null ? item : fitted, target, properties);
    }

    /**
     * Returns the edge as the built-in renderer should see it so the tip lands
     * on the target's outline, or null when the target is not shaped.
     *
     * @param edge       the edge item
     * @param properties the preview properties
     * @return a view of the edge whose target is resized, or null
     */
    static Item fittedToOutline(Item edge, PreviewProperties properties) {
        Object reach = edge.getData(EdgeEnds.TARGET_REACH);
        Object targetNode = edge.getData("target");
        if (!(reach instanceof Float) || !(targetNode instanceof Item)) {
            return null;
        }
        Item node = (Item) targetNode;
        float drawn = SizeUtils.getNodeSize(node, properties);
        Object stored = node.getData(NodeItem.SIZE);
        if (drawn <= 0 || !(stored instanceof Float)) {
            return null;
        }
        // The size that makes Gephi's size / 2 equal the outline's reach.
        float size = (Float) stored * (2f * (Float) reach / drawn);
        return new Substituted(edge, "target", new Substituted(node, NodeItem.SIZE, size));
    }

    /**
     * An item that answers one key differently and delegates everything else,
     * including its source, so SVG class names and colours are unchanged.
     */
    static final class Substituted implements Item {

        private final Item item;
        private final String key;
        private final Object value;

        Substituted(Item item, String key, Object value) {
            this.item = item;
            this.key = key;
            this.value = value;
        }

        @Override
        public Object getSource() {
            return item.getSource();
        }

        @Override
        public String getType() {
            return item.getType();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <D> D getData(String requested) {
            return key.equals(requested) ? (D) value : item.getData(requested);
        }

        @Override
        public boolean hasData(String requested) {
            return key.equals(requested) || item.hasData(requested);
        }

        @Override
        public void setData(String requested, Object data) {
            item.setData(requested, data);
        }

        @Override
        public String[] getKeys() {
            return item.getKeys();
        }
    }
}
