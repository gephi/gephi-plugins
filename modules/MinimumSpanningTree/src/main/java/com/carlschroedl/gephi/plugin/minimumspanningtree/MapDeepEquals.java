/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.carlschroedl.gephi.plugin.minimumspanningtree;

import java.util.Map;

/**
 * Utility to compare two maps for equality.
 * Copied from Graphstore non-exported via Graph-API classes.
 */
public final class MapDeepEquals {

    private MapDeepEquals() {
        // Only static
    }

    /**
     * Compares two maps for equality. This is based around the idea that if the keys are deep equal and the values the keys return are deep equal then the maps are equal.
     *
     * @param m1 - first map
     * @param m2 - second map
     * @return - weather the maps are deep equal
     */
    public static boolean mapDeepEquals(Map<?, ?> m1, Map<?, ?> m2) {
        if (m1.size() != m2.size()) {
            return false;
        }

        for (Map.Entry<?, ?> e : m1.entrySet()) {
            Object o = m2.get(e.getKey());
            if (e.getValue() == null && o != null) {
                return false;
            }
            if (o == null && e.getValue() != null) {
                return false;
            }
            if (o != null && !e.getValue().equals(o)) {
                return false;
            }
        }
        return true;
    }
}
