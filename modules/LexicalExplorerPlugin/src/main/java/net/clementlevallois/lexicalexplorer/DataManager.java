/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.clementlevallois.lexicalexplorer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author LEVALLOIS
 */
public class DataManager {
    
    private static Map<String, List<String>> mapOfNodeIdsToTheirTextFragments = new HashMap();

    public static Map<String, List<String>> getMapOfNodeIdsToTheirTextFragments() {
        return mapOfNodeIdsToTheirTextFragments;
    }

    public static void setMapOfNodeIdsToTheirTextFragments(Map<String, List<String>> mapOfNodeIdsToTheirTextFragments) {
        DataManager.mapOfNodeIdsToTheirTextFragments = mapOfNodeIdsToTheirTextFragments;
    }

}
