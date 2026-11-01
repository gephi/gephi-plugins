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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.UndirectedGraph;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the partition to shape assignment.
 *
 * @author Matt Artz
 */
public class PartitionShapesTest {

    @Test
    public void largestCategoryTakesTheFirstShape() {
        List<Object> values = Arrays.asList("b", "a", "a", "a", "b", "c");

        Map<Object, NodeShape> shapes = PartitionShapes.assign(values);

        assertEquals(NodeShape.CIRCLE, shapes.get("a"));
        assertEquals(NodeShape.SQUARE, shapes.get("b"));
        assertEquals(NodeShape.TRIANGLE, shapes.get("c"));
    }

    @Test
    public void tiesAreBrokenByValueSoTheOrderIsStable() {
        List<Object> forwards = Arrays.asList("zebra", "apple");
        List<Object> backwards = Arrays.asList("apple", "zebra");

        assertEquals(PartitionShapes.sortedCategories(forwards),
            PartitionShapes.sortedCategories(backwards));
        assertEquals(Arrays.asList("apple", "zebra"), PartitionShapes.sortedCategories(forwards));
    }

    @Test
    public void nullValuesAreNotACategory() {
        List<Object> values = Arrays.asList("a", null, null, null, "b");

        Map<Object, NodeShape> shapes = PartitionShapes.assign(values);

        assertEquals(2, shapes.size());
        assertNull(shapes.get(null));
    }

    @Test
    public void theVocabularyCyclesWhenThereAreMoreCategoriesThanShapes() {
        int vocabulary = NodeShape.values().length;
        List<Object> values = new ArrayList<>();
        // Give each category a distinct count so the order is fully determined
        // by count rather than by the tie-break.
        for (int i = 0; i <= vocabulary; i++) {
            String category = "c" + i;
            values.addAll(Collections.nCopies(100 - i, category));
        }

        Map<Object, NodeShape> shapes = PartitionShapes.assign(values);

        assertEquals(vocabulary + 1, shapes.size());
        assertEquals(shapes.get("c0"), shapes.get("c" + vocabulary));
        assertTrue(PartitionShapes.isAmbiguous(shapes.size()));
        assertFalse(PartitionShapes.isAmbiguous(vocabulary));
    }

    @Test
    public void anEmptyPartitionAssignsNothing() {
        assertTrue(PartitionShapes.assign(Collections.emptyList()).isEmpty());
        assertTrue(PartitionShapes.assign(Arrays.asList(null, null)).isEmpty());
    }

    @Test
    public void assignsFromColumnValuesReadOffARealGraph() {
        GraphModel model = GraphModel.Factory.newInstance();
        model.getNodeTable().addColumn("team", String.class);
        UndirectedGraph graph = model.getUndirectedGraph();

        String[] teams = {"red", "red", "red", "blue", "blue", "green", null};
        for (int i = 0; i < teams.length; i++) {
            Node node = model.factory().newNode("n" + i);
            node.setAttribute("team", teams[i]);
            graph.addNode(node);
        }

        List<Object> values = new ArrayList<>();
        graph.readLock();
        try {
            for (Node node : graph.getNodes().toArray()) {
                values.add(node.getAttribute("team"));
            }
        } finally {
            graph.readUnlock();
        }

        Map<Object, NodeShape> shapes = PartitionShapes.assign(values);

        assertEquals(3, shapes.size());
        assertEquals(NodeShape.CIRCLE, shapes.get("red"));
        assertEquals(NodeShape.SQUARE, shapes.get("blue"));
        assertEquals(NodeShape.TRIANGLE, shapes.get("green"));
    }
}
