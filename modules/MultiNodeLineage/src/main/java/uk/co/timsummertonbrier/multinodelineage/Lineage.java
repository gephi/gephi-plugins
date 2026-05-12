package uk.co.timsummertonbrier.multinodelineage;

import java.util.Set;

public class Lineage {
    private final Set<String> ancestorIds;
    private final Set<String> descendantIds;
    
    public Lineage(Set<String> ancestorIds, Set<String> descendantIds) {
        this.ancestorIds = ancestorIds;
        this.descendantIds = descendantIds;
    }
    
    public Set<String> getAncestorIds() {
        return ancestorIds;
    }
    
    public Set<String> getDescendantIds() {
        return descendantIds;
    }
}