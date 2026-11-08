/*
 * author: Clement Levallois
 */
package net.clementlevallois.wordcloud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Node;

public class DataManager {
    
    private static Map<Node, List<String>> mapOfNodeIdsToTheirTextFragments = new HashMap();

    public static Map<Node, List<String>> getMapOfNodeIdsToTheirTextFragments() {
        return mapOfNodeIdsToTheirTextFragments;
    }

    public static void setMapOfNodeIdsToTheirTextFragments(Map<Node, List<String>> mapOfNodeIdsToTheirTextFragments) {
        DataManager.mapOfNodeIdsToTheirTextFragments = mapOfNodeIdsToTheirTextFragments;
    }

}
