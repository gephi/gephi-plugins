package fr.totetmatt.gephi.twitter.networklogics.utils;

/**
 *
 * @author totetmatt
 */
public enum TwitterEdgeColumn {
    EDGE_HASHTAG("hashtag", String.class);

    final public String label;
    final public Class<?> classType;

    private TwitterEdgeColumn(String label, Class<?> classType) {
        this.label = label;
        this.classType = classType;
    }

}
