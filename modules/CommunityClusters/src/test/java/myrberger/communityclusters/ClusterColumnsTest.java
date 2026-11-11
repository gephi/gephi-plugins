package myrberger.communityclusters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.Test;

public class ClusterColumnsTest {

    @Test
    public void categoricalTypesAreAccepted() {
        assertTrue("String", ClusterColumns.isCategorical(String.class, false, false, false));
        assertTrue("Integer", ClusterColumns.isCategorical(Integer.class, false, false, false));
        assertTrue("Long", ClusterColumns.isCategorical(Long.class, false, false, false));
        assertTrue("Short", ClusterColumns.isCategorical(Short.class, false, false, false));
        assertTrue("Byte", ClusterColumns.isCategorical(Byte.class, false, false, false));
        assertTrue("Boolean", ClusterColumns.isCategorical(Boolean.class, false, false, false));
        assertTrue("Character", ClusterColumns.isCategorical(Character.class, false, false, false));
        assertTrue("primitive int", ClusterColumns.isCategorical(int.class, false, false, false));
    }

    @Test
    public void nonCategoricalColumnsAreRejected() {
        assertFalse("Double", ClusterColumns.isCategorical(Double.class, false, false, false));
        assertFalse("Float", ClusterColumns.isCategorical(Float.class, false, false, false));
        assertFalse("list of text", ClusterColumns.isCategorical(String[].class, true, false, false));
        assertFalse("time dependent", ClusterColumns.isCategorical(Integer.class, false, true, false));
        assertFalse("built-in column such as Id or Label", ClusterColumns.isCategorical(String.class, false, false, true));
        assertFalse("unknown type", ClusterColumns.isCategorical(null, false, false, false));
    }

    @Test
    public void labelsUseTitlesAndKeepTheOrder() {
        Map<String, Integer> labels = ClusterColumns.labels(new String[]{"modularity_class", "type"},
                new String[]{"Modularity Class", "type"});
        assertArrayEquals(new String[]{"Modularity Class", "type"}, labels.keySet().toArray(new String[0]));
        assertEquals("first column index", 0, labels.get("Modularity Class").intValue());
        assertEquals("second column index", 1, labels.get("type").intValue());
    }

    @Test
    public void blankTitleFallsBackToId() {
        Map<String, Integer> labels = ClusterColumns.labels(new String[]{"a", "b"}, new String[]{"", null});
        assertArrayEquals(new String[]{"a", "b"}, labels.keySet().toArray(new String[0]));
    }

    @Test
    public void clashingTitlesGetTheirIdAdded() {
        Map<String, Integer> labels = ClusterColumns.labels(new String[]{"x1", "x2", "other"},
                new String[]{"Group", "group", "Other"});
        assertEquals("all three columns are listed", 3, labels.size());
        assertTrue("first clash is told apart", labels.containsKey("Group (x1)"));
        assertTrue("second clash is told apart", labels.containsKey("group (x2)"));
        assertTrue("unique title stays plain", labels.containsKey("Other"));
    }

    @Test
    public void identicalLabelsAreNeverLost() {
        Map<String, Integer> labels = ClusterColumns.labels(new String[]{"a", "a"}, new String[]{"T", "T"});
        assertEquals("both columns listed", 2, labels.size());
    }

    @Test
    public void findMatchesIdOrTitleIgnoringCase() {
        String[] ids = {"modularity_class", "type"};
        String[] titles = {"Modularity Class", "Type of node"};
        assertEquals("by id", 0, ClusterColumns.find("MODULARITY_CLASS", ids, titles));
        assertEquals("by title", 1, ClusterColumns.find("type of node", ids, titles));
        assertEquals("with spaces around", 1, ClusterColumns.find("  type ", ids, titles));
        assertEquals("unknown", -1, ClusterColumns.find("nothing", ids, titles));
        assertEquals("empty", -1, ClusterColumns.find("", ids, titles));
        assertEquals("null", -1, ClusterColumns.find(null, ids, titles));
    }

    private static void assertArrayEquals(Object[] expected, Object[] actual) {
        org.junit.Assert.assertArrayEquals("labels", expected, actual);
    }
}
