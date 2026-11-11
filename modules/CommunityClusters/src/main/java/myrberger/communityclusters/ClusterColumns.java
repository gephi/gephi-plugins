package myrberger.communityclusters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Decides which node columns can be used as cluster columns, and how they are named in the drop-down.
 * Kept free of Gephi classes so the rules can be unit tested.
 */
final class ClusterColumns {

    private ClusterColumns() {
    }

    /**
     * A column is offered when it holds whole numbers, text, true/false or single characters, one value
     * per node. Decimal numbers, lists and time-dependent values are not categories. Built-in columns
     * such as Id and Label (different for every node) are left out.
     */
    static boolean isCategorical(Class<?> type, boolean isArray, boolean isDynamic, boolean isBuiltIn) {
        if (type == null || isArray || isDynamic || isBuiltIn) {
            return false;
        }
        return type == String.class || type == Integer.class || type == Long.class || type == Short.class
                || type == Byte.class || type == Boolean.class || type == Character.class
                || type == int.class || type == long.class || type == short.class || type == byte.class
                || type == boolean.class || type == char.class;
    }

    /**
     * Display labels for columns, in the order given. The title is used (that is what the Data Laboratory
     * shows); if two columns share a title, the id is added to tell them apart.
     *
     * @return label to index in the input arrays
     */
    static Map<String, Integer> labels(String[] ids, String[] titles) {
        Map<String, Integer> titleCount = new LinkedHashMap<>();
        String[] base = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            String t = titles[i] == null || titles[i].trim().isEmpty() ? ids[i] : titles[i].trim();
            base[i] = t;
            titleCount.merge(t.toLowerCase(), 1, Integer::sum);
        }
        Map<String, Integer> out = new LinkedHashMap<>();
        for (int i = 0; i < ids.length; i++) {
            String label = titleCount.get(base[i].toLowerCase()) > 1 && !base[i].equalsIgnoreCase(ids[i])
                    ? base[i] + " (" + ids[i] + ")" : base[i];
            // last resort: never lose a column to a clash
            while (out.containsKey(label)) {
                label = label + "'";
            }
            out.put(label, i);
        }
        return out;
    }

    /** Index of the column the stored value refers to (matches id or title, ignoring case), or -1. */
    static int find(String value, String[] ids, String[] titles) {
        if (value == null || value.trim().isEmpty()) {
            return -1;
        }
        String v = value.trim();
        for (int i = 0; i < ids.length; i++) {
            if (v.equalsIgnoreCase(ids[i])) {
                return i;
            }
        }
        for (int i = 0; i < titles.length; i++) {
            if (titles[i] != null && v.equalsIgnoreCase(titles[i].trim())) {
                return i;
            }
        }
        return -1;
    }

    /** Convenience for building arrays from a list. */
    static String[] toArray(List<String> l) {
        return new ArrayList<>(l).toArray(new String[0]);
    }
}
