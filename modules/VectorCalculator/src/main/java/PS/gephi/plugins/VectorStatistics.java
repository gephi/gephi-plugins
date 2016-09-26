/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package PS.gephi.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Jonas Persson
 */

public class VectorStatistics implements Statistics {

    private double GetStandardDeviation(List<Double> list) {
        // Add up all the variances
        Double totalVariations = 0.0;
        for(Double d : list) 
            totalVariations += d*d;
        
        Double totalVariation = totalVariations / list.size();
        
        return Math.sqrt(totalVariation);
    }

    private double GetTotalAvgDistance() {
        double avgDist = 0.0;
        for(String label : labelAvgDistances.keySet()) {            
            avgDist += (Double) labelAvgDistances.get(label);            
        }
        return avgDist/labelAvgDistances.size();
    }

    public class _Node {
        public double lat;
        public double lon;
        public _Node(double latitude, double longitude) {
            this.lat = latitude;
            this.lon = longitude;
        }
    }
    
    private String report = "";
    
    // Column attribute names for longitude and latitude
    String lat;
    String lon;
    
    // Average distance and standard deviation
    private final Map<String, Object> labelAvgDistances = new HashMap<>();
    private final Map<String, Object> labelAllDistances = new HashMap<>();
    private final Map<String, Object> labelCounts = new HashMap<>();
    
    // Vector magnitude
    private final Map<String, Object> labelTotalX = new HashMap<>();
    private final Map<String, Object> labelTotalY = new HashMap<>();
    private final Map<String, Object> labelStartNode = new HashMap<>();
    private final Map<String, Object> labelCurrentEndNode = new HashMap<>();
    private _Node firstNode, lastNode;
    
    public String resultText = "";
    
    @Override
    public void execute(GraphModel gm) {
        
        Graph graph;
        //if (isDirected) {
            graph = gm.getDirectedGraphVisible();
        //} else {
        //    graph = gm.getUndirectedGraphVisible();
        //}
        
        graph.readLock();
        
        
        //Table edgeTable = gm.getEdgeTable();
        //edgeTable.addColumn("Average distance", String.class);
        
        try {  
            for (Edge e : graph.getEdges()) {

                String label = e.getLabel() != null ? e.getLabel() : "no label";
                
                // Intitialize new label values
                if(!labelAvgDistances.containsKey(label)) {
                    // Add the new label to the dictionaries (Map)
                    labelAvgDistances.put(label, 0.0);
                    labelAllDistances.put(label, new ArrayList<>());
                    labelCounts.put(label, 0);
                    labelTotalX.put(label, 0.0);
                    labelTotalY.put(label, 0.0);
                    labelStartNode.put(label, e.getSource());
                    labelCurrentEndNode.put(label, new _Node(
                            (Double) e.getSource().getAttribute(lat), 
                            (Double) e.getSource().getAttribute(lon)));
                }    
                
                // set the firstNode and lastNode, if not set
                if(firstNode == null) {
                    firstNode = new _Node((Double)e.getSource().getAttribute(lat), 
                                          (Double)e.getSource().getAttribute(lon));
                    lastNode = new _Node((Double)e.getSource().getAttribute(lat), 
                                          (Double)e.getSource().getAttribute(lon));
                }
                
                // (Re-)calculate the average distance
                double dist = CalculateAverageDistance(label, e);
                
                // Set the end node for calculating the magnitude
                double bearing = GetBearing(e);
                FindEndNode(label, dist, bearing);
                
                // Also set the end node accumulated over all labels
                FindEndNode(null, dist, bearing);
                
                //CalculateVectorMagnitude(label, e);
            }            
            
            CreateReport();          
                    
            graph.readUnlockAll();
            
        } catch (Exception e) {
            //Unlock graph
            graph.readUnlockAll();
       }
          
    }
    
    @Override
    public String getReport() {
        return report;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
    }
    
    private double CalculateAverageDistance(String label, Edge e) {
        
        // Get the label objects
        double avgDist = (Double) labelAvgDistances.get(label);
        int count = (Integer) labelCounts.get(label);
        List<Double> allDist = (List<Double>) labelAllDistances.get(label);
        
        // Get the distance from latitude and longitude
        double dist = distance(e.getSource(), e.getTarget());
        
        allDist.add(dist);
        
        avgDist = (dist + avgDist*count) / ++count;

        // update the dictionaries
        labelAvgDistances.put(label, avgDist);
        labelAllDistances.put(label, allDist);
        labelCounts.put(label, count);
        
        return dist;
    }

//    private void CalculateVectorMagnitude(String label, Edge e) {
//        
//        // Get the label objects
//        double xTotal = (Double) labelTotalX.get(label);
//        double yTotal = (Double) labelTotalY.get(label);
//        
//        // Get longitudes and latitudes
//        double lonSource = (Double) e.getSource().getAttribute(lon);
//        double lonTarget = (Double) e.getTarget().getAttribute(lon);
//        double latSource = (Double) e.getSource().getAttribute(lat);
//        double latTarget = (Double) e.getTarget().getAttribute(lat);
//        
//        
//        // calculate the distance between target and source,
//        // and add to the total distance (magnitude)
//        // use source nodes latitude for both source and target, to find only the longitude magnitude
//        double dist = distance(lonSource, lonTarget, latSource, latSource);
//        if(lonSource < lonTarget)
//            xTotal += dist;
//        else
//            xTotal -= dist;
//        
//        // use source nodes longitude for both source and target, to find only the latitude magnitude
//        dist = distance(lonSource,lonSource, latSource, latTarget);
//        if(latSource < latTarget)
//            yTotal += dist;
//        else
//            yTotal -= dist;
//        
//        // update the dictionaries
//        labelTotalX.put(label, xTotal);
//        labelTotalY.put(label, yTotal);
//    }

    private void CreateReport() {
        
        List<Double> standardDeviations = new ArrayList<>();
        
        // caluculate the totals
        double TotalAvgDistance = GetTotalAvgDistance();
        
        // Loop through the dictionaries and create the report
        Iterator it = labelAvgDistances.entrySet().iterator();
        while (it.hasNext()) {
            
            // Average distance
            Map.Entry pair = (Map.Entry)it.next();
            String label = pair.getKey().toString();
            double avgDist = (Double) pair.getValue();
            
            double standardDeviation = GetStandardDeviation(label);         
            standardDeviations.add(standardDeviation);
            
            // Magnitude and direction
            Node sNode = (Node)labelStartNode.get(label);
            _Node eNode = (_Node)labelCurrentEndNode.get(label);
            double magnitude = distance((Double)sNode.getAttribute(lon),eNode.lon,
                    (Double)sNode.getAttribute(lat), eNode.lat);
            
            double angle = GetBearing((Double)sNode.getAttribute(lon),eNode.lon,
                                    (Double)sNode.getAttribute(lat), eNode.lat);
            String direction = DegToCompass(angle);
            
            // Create the report
            report += String.format("<p><b>Label: %s </b><br />"+
                    "Average distance: %.2f km with a standard deviation of %.2f km. <br/>"+
                    "Magnitude: %.2f km <br/>"+
                    "Direction: %s (%.2f degrees clock-wise from North)</p>", label, avgDist, standardDeviation, magnitude, direction, angle);

            it.remove(); // avoids a ConcurrentModificationException
        }
        
        // caluculate the totals
        double TotalStandardDeviation = GetStandardDeviation(standardDeviations);
        double TotalMagnitude = distance(firstNode.lon, lastNode.lon, firstNode.lat, lastNode.lat);
        double TotalAngle = GetBearing(firstNode.lon, lastNode.lon, firstNode.lat, lastNode.lat);
        String TotalDirection = DegToCompass(TotalAngle);
        report += String.format("<p><b>Total over all edges:</b><br />"+
                    "Average distance: %.2f km with a standard deviation of %.2f km. <br/>"+
                    "Magnitude: %.2f km <br/>"+
                    "Direction: %s (%.2f degrees clock-wise from North)</p>", TotalAvgDistance, TotalStandardDeviation, TotalMagnitude, TotalDirection, TotalAngle);
        
        
    }

    private String DegToCompass(double angle) {
        int val = (int) ((angle/22.5)+.5);

        String directions[] = {"N","NNE","NE","ENE","E","ESE","SE","SSE","S","SSW","SW","WSW","W","WNW","NW","NNW"};
        
        return directions[val];
    }
    
    private double distance(Node source, Node target) {

        double lat1 = (Double) source.getAttribute(lat);
        double lon1 = (Double) source.getAttribute(lon);
        
        double lat2 = (Double) target.getAttribute(lat);
        double lon2 = (Double) target.getAttribute(lon);
        
        return CalculateDistance(lon1, lon2, lat1, lat2);
    }
    
    private double distance(double lon1, double lon2, double lat1, double lat2) {
        
        return CalculateDistance(lon1, lon2, lat1, lat2);
    }

    private double GetBearing(Edge e) {
        
        double lon1 = (Double) e.getSource().getAttribute(lon);
        double lon2 = (Double) e.getTarget().getAttribute(lon);
        double lat1 = (Double) e.getSource().getAttribute(lat);
        double lat2 = (Double) e.getTarget().getAttribute(lat);
               
        return GetBearing(lon1, lon2, lat1, lat2);
    }
   
    private double GetBearing(double lon1, double lon2, double lat1, double lat2) {
        
        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff= Math.toRadians(longitude2-longitude1);
        double y= Math.sin(longDiff)*Math.cos(latitude2);
        double x=Math.cos(latitude1)*Math.sin(latitude2)-Math.sin(latitude1)*Math.cos(latitude2)*Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x))+360)%360;
    }

    private void FindEndNode(String label, double dist, double bearing) {
        
        final int R = 6371; // Radius of the earth
        
        _Node startNode;
        if(label != null)
           startNode  = (_Node) labelCurrentEndNode.get(label);
        else 
            startNode = new _Node(lastNode.lat, lastNode.lon);
        
        
        double lat1 = Math.toRadians((Double) startNode.lat);
        double lon1 = Math.toRadians((Double) startNode.lon);
        
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist/R)
                      + Math.cos(lat1) * Math.sin(dist/R) * Math.cos(Math.toRadians(bearing)));
        
        double a = Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin(dist/R) * Math.cos(lat1), 
                Math.cos(dist/R) - Math.sin(lat1) * Math.sin(lat2));
        
        double lon2 = lon1 + a;
        
        lon2 = (lon2+ 3*Math.PI) % (2*Math.PI) - Math.PI; 
        
        SetEndNode(label, startNode, Math.toDegrees(lat2), Math.toDegrees(lon2));        
    }
    
    private void SetEndNode(String label, _Node node, double lat, double lon) {
        node.lat = lat;
        node.lon = lon;
        
        if(label != null)
            labelCurrentEndNode.put(label, node);
        else
            lastNode = node;
    }

    private static double CalculateDistance(double lon1, double lon2, double lat1, double lat2) {
        
        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        double distance = R * c ;//* 1000; // convert to meters

        return distance;
    }
    
    private double GetStandardDeviation(String label) {
        
        // Get the label objects
        double avgDist = (Double) labelAvgDistances.get(label);
        int count = (Integer) labelCounts.get(label);
        List<Double> allDist = (List<Double>) labelAllDistances.get(label);
        
        // Calculate variance
        double a = 0.0;
        for(int i = 0; i< count; i++) {
            a += Math.pow((allDist.get(i)-avgDist), 2);
        }
        double variance = a/count;
        
        // return the standard deviation
        return Math.sqrt(variance);
        
    }
}
