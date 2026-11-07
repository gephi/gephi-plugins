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

import org.gephi.graph.api.Graph;
import org.gephi.preview.api.Item;
import org.gephi.preview.plugin.items.AbstractItem;
import org.gephi.preview.spi.ItemBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Supplies the single preview item the shape legend is drawn from.
 * <p>
 * Preview draws only items, and a legend belongs to no node or edge, so it
 * needs an item of its own. The item starts empty; the node renderer fills it
 * with the legend when it resolves the shapes, so the key is built from
 * exactly the assignment the nodes are drawn with.
 *
 * @author Matt Artz
 */
@ServiceProvider(service = ItemBuilder.class)
public class LegendItemBuilder implements ItemBuilder {

    /** Item type of the legend. */
    public static final String TYPE = "shapebypartition.legend";

    /** Item data holding the {@link ShapeLegend}, when there is one. */
    static final String LEGEND = "legend";

    @Override
    public Item[] getItems(Graph graph) {
        return new Item[]{new LegendItem()};
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /** The legend's item; its source is itself, since it stands for no graph element. */
    static final class LegendItem extends AbstractItem {

        LegendItem() {
            super(new Object(), TYPE);
        }
    }
}
