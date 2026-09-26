package totetmatt.gephi.twitter.networklogic.utils;

/**
 *
 * @author totetmatt
 */
public enum TwitterEdgeColumn {
    EDGE_HASHTAG("hashtag",String.class);
  
    final public String label;
    final public Class<?> classType;

    private TwitterEdgeColumn(String label, Class<?> classType) {
        this.label = label;
        this.classType = classType;
    }
 
}
