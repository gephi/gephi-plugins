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

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Maps the categories of a partition onto the shape vocabulary.
 * <p>
 * Categories are ordered by descending node count, which is the same order the
 * Appearance module uses when it assigns colours to a partition. Colouring and
 * shaping by the same column therefore agree: the largest category takes both
 * the first colour and the first shape.
 * <p>
 * Ties are broken by the string form of the value, ascending, so the assignment
 * is stable across runs rather than dependent on iteration order.
 * <p>
 * This class holds no Gephi or Swing types so that it can be unit tested
 * directly.
 *
 * @author Matt Artz
 */
public final class PartitionShapes {

    private PartitionShapes() {
    }

    /**
     * Orders the distinct non-null values by descending count, then by their
     * string form ascending.
     *
     * @param values one value per node, nulls permitted and ignored
     * @return the distinct categories in assignment order
     */
    public static List<Object> sortedCategories(Collection<?> values) {
        Map<Object, Integer> counts = new HashMap<>();
        for (Object value : values) {
            if (value != null) {
                counts.merge(value, 1, Integer::sum);
            }
        }
        List<Object> categories = new ArrayList<>(counts.keySet());
        categories.sort(Comparator
            .comparingInt((Object value) -> counts.get(value)).reversed()
            .thenComparing(String::valueOf));
        return categories;
    }

    /**
     * Assigns a shape to every distinct non-null value.
     * <p>
     * When there are more categories than shapes the vocabulary cycles, so two
     * categories can share a shape. That is a real limitation of encoding a
     * category as a shape, and the renderer reports it rather than hiding it.
     *
     * @param values one value per node, nulls permitted and ignored
     * @return the category to shape mapping, in assignment order
     */
    public static Map<Object, NodeShape> assign(Collection<?> values) {
        Map<Object, NodeShape> shapes = new LinkedHashMap<>();
        List<Object> categories = sortedCategories(values);
        for (int i = 0; i < categories.size(); i++) {
            shapes.put(categories.get(i), NodeShape.forIndex(i));
        }
        return shapes;
    }

    /**
     * Reports whether the assignment had to reuse a shape for more than one
     * category.
     *
     * @param categoryCount the number of distinct categories
     * @return true when the shape vocabulary is smaller than the partition
     */
    public static boolean isAmbiguous(int categoryCount) {
        return categoryCount > NodeShape.values().length;
    }
}
