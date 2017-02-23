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
import org.openide.util.Exceptions;

/**
 *
 * @author Jonas Persson
 */
public class VectorStatistics implements Statistics {

    private double GetStandardDeviation(List<Double> list) {
        // Add up all the variances
        Double totalVariations = 0.0;
        for (Double d : list) {
            totalVariations += d * d;
        }

        Double totalVariation = totalVariations / list.size();

        return Math.sqrt(totalVariation);
    }

    private double GetTotalAvgDistance() {
        double avgDist = 0.0;
        for (String label : labelAvgDistances.keySet()) {
            avgDist += labelAvgDistances.get(label);
        }
        return avgDist / labelAvgDistances.size();
    }

    private double latitude(Node e) {
        return ((Number) e.getAttribute(latAttribute)).doubleValue();
    }

    private double longitude(Node e) {
        return ((Number) e.getAttribute(lonAttribute)).doubleValue();
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
    private String latAttribute;
    private String lonAttribute;

    public String getLatAttribute() {
        return latAttribute;
    }

    public void setLatAttribute(String latAttribute) {
        this.latAttribute = latAttribute;
    }

    public String getLonAttribute() {
        return lonAttribute;
    }

    public void setLonAttribute(String lonAttribute) {
        this.lonAttribute = lonAttribute;
    }

    // Average distance and standard deviation
    private final Map<String, Double> labelAvgDistances = new HashMap<>();
    private final Map<String, List<Double>> labelAllDistances = new HashMap<>();
    private final Map<String, Integer> labelCounts = new HashMap<>();

    // Vector magnitude
    private final Map<String, Double> labelTotalX = new HashMap<>();
    private final Map<String, Double> labelTotalY = new HashMap<>();
    private final Map<String, Node> labelStartNode = new HashMap<>();
    private final Map<String, _Node> labelCurrentEndNode = new HashMap<>();
    private _Node firstNode, lastNode;

    public String resultText = "";

    @Override
    public void execute(GraphModel gm) {
        Graph graph = gm.getDirectedGraphVisible();

        graph.readLock();

        try {
            for (Edge e : graph.getEdges()) {
                String label = e.getLabel() != null ? e.getLabel() : "no label";

                // Intitialize new label values
                if (!labelAvgDistances.containsKey(label)) {
                    // Add the new label to the dictionaries (Map)
                    labelAvgDistances.put(label, 0.0);
                    labelAllDistances.put(label, new ArrayList<Double>());
                    labelCounts.put(label, 0);
                    labelTotalX.put(label, 0.0);
                    labelTotalY.put(label, 0.0);
                    labelStartNode.put(label, e.getSource());
                    labelCurrentEndNode.put(label, new _Node(
                            latitude(e.getSource()),
                            longitude(e.getSource())
                    ));
                }

                // set the firstNode and lastNode, if not set
                if (firstNode == null) {
                    firstNode = new _Node(
                            latitude(e.getSource()),
                            longitude(e.getSource())
                    );
                    lastNode = new _Node(
                            latitude(e.getSource()),
                            longitude(e.getSource())
                    );
                }

                // (Re-)calculate the average distance
                double dist = CalculateAverageDistance(label, e);

                // Set the end node for calculating the magnitude
                double bearing = GetBearing(e);
                FindEndNode(label, dist, bearing);

                // Also set the end node accumulated over all labels
                FindEndNode(null, dist, bearing);
            }

            CreateReport();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        } finally {
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
        double avgDist = labelAvgDistances.get(label);
        int count = labelCounts.get(label);
        List<Double> allDist = (List<Double>) labelAllDistances.get(label);

        // Get the distance from latitude and longitude
        double dist = distance(e.getSource(), e.getTarget());

        allDist.add(dist);

        avgDist = (dist + avgDist * count) / ++count;

        // update the dictionaries
        labelAvgDistances.put(label, avgDist);
        labelAllDistances.put(label, allDist);
        labelCounts.put(label, count);

        return dist;
    }

    private void CreateReport() {
        if (firstNode == null) {
            report = "The graph has no edges";
            return;
        }

        List<Double> standardDeviations = new ArrayList<>();

        // caluculate the totals
        double TotalAvgDistance = GetTotalAvgDistance();

        // Loop through the dictionaries and create the report
        Iterator<Map.Entry<String, Double>> it = labelAvgDistances.entrySet().iterator();
        while (it.hasNext()) {

            // Average distance
            Map.Entry<String, Double> pair = it.next();
            String label = pair.getKey();
            double avgDist = pair.getValue();

            double standardDeviation = GetStandardDeviation(label);
            standardDeviations.add(standardDeviation);

            // Magnitude and direction
            Node sNode = labelStartNode.get(label);
            _Node eNode = labelCurrentEndNode.get(label);
            double magnitude = CalculateDistance(
                    longitude(sNode), eNode.lon,
                    latitude(sNode), eNode.lat
            );

            double angle = GetBearing(
                    longitude(sNode), eNode.lon,
                    latitude(sNode), eNode.lat
            );
            String direction = DegToCompass(angle);

            // Create the report
            report += String.format("<p><b>Label: %s </b><br />"
                    + "Average distance: %.2f km with a standard deviation of %.2f km. <br/>"
                    + "Magnitude: %.2f km <br/>"
                    + "Direction: %s (%.2f degrees clock-wise from North)</p>", label, avgDist, standardDeviation, magnitude, direction, angle);

            it.remove(); // avoids a ConcurrentModificationException
        }

        // caluculate the totals
        double TotalStandardDeviation = GetStandardDeviation(standardDeviations);
        double TotalMagnitude = CalculateDistance(firstNode.lon, lastNode.lon, firstNode.lat, lastNode.lat);
        double TotalAngle = GetBearing(firstNode.lon, lastNode.lon, firstNode.lat, lastNode.lat);
        String TotalDirection = DegToCompass(TotalAngle);
        report += String.format("<p><b>Total over all edges:</b><br />"
                + "Average distance: %.2f km with a standard deviation of %.2f km. <br/>"
                + "Magnitude: %.2f km <br/>"
                + "Direction: %s (%.2f degrees clock-wise from North)</p>", TotalAvgDistance, TotalStandardDeviation, TotalMagnitude, TotalDirection, TotalAngle);

    }

    private String DegToCompass(double angle) {
        int val = (int) ((angle / 22.5) + .5);

        String directions[] = {"N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW"};

        return directions[val];
    }

    private double distance(Node source, Node target) {
        double lat1 = latitude(source);
        double lon1 = longitude(source);

        double lat2 = latitude(target);
        double lon2 = longitude(target);

        return CalculateDistance(lon1, lon2, lat1, lat2);
    }

    private double GetBearing(Edge e) {
        double lon1 = longitude(e.getSource());
        double lon2 = longitude(e.getTarget());
        double lat1 = latitude(e.getSource());
        double lat2 = latitude(e.getTarget());

        return GetBearing(lon1, lon2, lat1, lat2);
    }

    private double GetBearing(double lon1, double lon2, double lat1, double lat2) {
        double longitude1 = lon1;
        double longitude2 = lon2;
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longDiff = Math.toRadians(longitude2 - longitude1);
        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    private static final int EARTH_RADIUS = 6371; // Radius of the earth

    private void FindEndNode(String label, double dist, double bearing) {
        _Node startNode;
        if (label != null) {
            startNode = labelCurrentEndNode.get(label);
        } else {
            startNode = new _Node(lastNode.lat, lastNode.lon);
        }

        double lat1 = Math.toRadians(startNode.lat);
        double lon1 = Math.toRadians(startNode.lon);

        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist / EARTH_RADIUS)
                + Math.cos(lat1) * Math.sin(dist / EARTH_RADIUS) * Math.cos(Math.toRadians(bearing)));

        double a = Math.atan2(Math.sin(Math.toRadians(bearing)) * Math.sin(dist / EARTH_RADIUS) * Math.cos(lat1),
                Math.cos(dist / EARTH_RADIUS) - Math.sin(lat1) * Math.sin(lat2));

        double lon2 = lon1 + a;

        lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        SetEndNode(label, startNode, Math.toDegrees(lat2), Math.toDegrees(lon2));
    }

    private void SetEndNode(String label, _Node node, double lat, double lon) {
        node.lat = lat;
        node.lon = lon;

        if (label != null) {
            labelCurrentEndNode.put(label, node);
        } else {
            lastNode = node;
        }
    }

    private static double CalculateDistance(double lon1, double lon2, double lat1, double lat2) {
        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS * c;//* 1000; // convert to meters

        return distance;
    }

    private double GetStandardDeviation(String label) {
        // Get the label objects
        double avgDist = labelAvgDistances.get(label);
        int count = labelCounts.get(label);
        List<Double> allDist = labelAllDistances.get(label);

        // Calculate variance
        double a = 0.0;
        for (int i = 0; i < count; i++) {
            a += Math.pow((allDist.get(i) - avgDist), 2);
        }
        double variance = a / count;

        // return the standard deviation
        return Math.sqrt(variance);
    }
}
