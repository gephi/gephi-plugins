/*
 * author: Clement Levallois
 */
package net.clementlevallois.wordcloud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataManager {
    
    private static Map<String, List<String>> mapOfNodeIdsToTheirTextFragments = new HashMap();

    public static Map<String, List<String>> getMapOfNodeIdsToTheirTextFragments() {
        return mapOfNodeIdsToTheirTextFragments;
    }

    public static void setMapOfNodeIdsToTheirTextFragments(Map<String, List<String>> mapOfNodeIdsToTheirTextFragments) {
        DataManager.mapOfNodeIdsToTheirTextFragments = mapOfNodeIdsToTheirTextFragments;
    }

}
