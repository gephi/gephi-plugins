package org.gephi.plugins.layout.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.junit.Test;

public class GeoLayoutTest {

    @Test
    public void positionsStayFiniteWithPolesAndInvalidLatitudes() {
        for (String projection : GeoLayout.rows) {
            GraphModel graphModel = GraphModel.Factory.newInstance();
            addNode(graphModel, "paris", 48.85, 2.35);
            addNode(graphModel, "south-pole", -90.0, 0.0);
            addNode(graphModel, "north-pole", 90.0, 0.0);
            addNode(graphModel, "swapped", 120.0, 30.0);
            addNode(graphModel, "nan", Double.NaN, 10.0);
            addNode(graphModel, "infinite-longitude", 10.0, Double.POSITIVE_INFINITY);
            addNode(graphModel, "huge-longitude", 10.0, 1e40);
            addNode(graphModel, "missing", null, null);

            run(newLayout(graphModel, projection));

            for (Node n : graphModel.getGraph().getNodes()) {
                assertTrue(projection + " " + n.getId() + " x=" + n.x(), Float.isFinite(n.x()));
                assertTrue(projection + " " + n.getId() + " y=" + n.y(), Float.isFinite(n.y()));
            }
        }
    }

    @Test
    public void mercatorClampsPolesToWebMercatorBounds() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Node pole = addNode(graphModel, "south-pole", -90.0, 0.0);
        Node bound = addNode(graphModel, "bound", -85.0511287798, 0.0);
        Node equator = addNode(graphModel, "equator", 0.0, 0.0);

        GeoLayout layout = newLayout(graphModel, "Mercator");
        layout.setCentered(false);
        run(layout);

        assertEquals(bound.y(), pole.y(), 0f);
        assertEquals(0f, equator.y(), 1e-3f);
        assertTrue(pole.y() < 0f);
    }

    @Test
    public void outOfRangeLatitudesArePlacedWithNodesWithoutCoordinates() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Node a = addNode(graphModel, "a", 10.0, 0.0);
        Node b = addNode(graphModel, "b", 20.0, 10.0);
        Node swapped = addNode(graphModel, "swapped", 120.0, 30.0);
        Node missing = addNode(graphModel, "missing", null, null);

        GeoLayout layout = newLayout(graphModel, "Equirectangular");
        layout.setCentered(false);
        run(layout);

        float yMin = Math.min(a.y(), b.y());
        assertTrue(swapped.y() < yMin);
        assertEquals(missing.y(), swapped.y(), 0f);
    }

    @Test
    public void aSingleNodeWithoutCoordinatesIsPlacedBelowTheMap() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Node a = addNode(graphModel, "a", 10.0, 0.0);
        Node b = addNode(graphModel, "b", 20.0, 10.0);
        Node typo = addNode(graphModel, "typo", 95.0, 5.0);

        GeoLayout layout = newLayout(graphModel, "Equirectangular");
        layout.setCentered(false);
        run(layout);

        assertTrue(typo.y() < Math.min(a.y(), b.y()));
        assertEquals(Math.min(a.x(), b.x()), typo.x(), 0f);
    }

    @Test
    public void nodesWithoutCoordinatesDoNotStackWhenValidNodesShareALongitude() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Node valid = addNode(graphModel, "valid", 10.0, 0.0);
        Node first = addNode(graphModel, "first", null, null);
        Node second = addNode(graphModel, "second", null, null);

        GeoLayout layout = newLayout(graphModel, "Equirectangular");
        layout.setCentered(false);
        run(layout);

        assertTrue(first.x() != second.x());
        assertTrue(first.y() < valid.y());
        assertTrue(second.y() < valid.y());
    }

    @Test
    public void nonFiniteScaleIsIgnored() {
        GeoLayout layout = new GeoLayout(new GeoLayoutBuilder());
        layout.setScale(500.0);
        layout.setScale(Double.POSITIVE_INFINITY);
        layout.setScale(Double.NaN);
        layout.setScale(null);

        assertEquals(500.0, layout.getScale(), 0.0);
    }

    @Test
    public void noNodeWithCoordinatesLeavesPositionsUnchanged() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Node n = addNode(graphModel, "missing", null, null);
        n.setX(12f);
        n.setY(-7f);

        run(newLayout(graphModel, "Mercator"));

        assertEquals(12f, n.x(), 0f);
        assertEquals(-7f, n.y(), 0f);
    }

    @Test
    public void releasesGraphLockWhenLayoutFails() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        addNode(graphModel, "paris", 48.85, 2.35);
        GeoLayout layout = newLayout(graphModel, null);

        layout.initAlgo();
        try {
            layout.goAlgo();
            fail("Expected the null projection to fail");
        } catch (NullPointerException expected) {
        }

        assertEquals(0, graphModel.getGraphVisible().getLock().getReadHoldCount());
    }

    @Test
    public void columnsPickedInAnotherGraphModelAreReadInTheCurrentOne() {
        GraphModel other = graphModelWithMoreColumns("latitude", "longitude");

        GraphModel graphModel = GraphModel.Factory.newInstance();
        Node a = addNode(graphModel, "a", 10.0, 0.0);
        Node b = addNode(graphModel, "b", 20.0, 10.0);

        GeoLayout layout = newLayout(graphModel, "Equirectangular");
        layout.setCentered(false);
        // As when a layout preset saved in another workspace is applied: the columns belong to that workspace
        layout.setLatitude(other.getNodeTable().getColumn("latitude"));
        layout.setLongitude(other.getNodeTable().getColumn("longitude"));
        run(layout);

        assertEquals(Math.toRadians(10.0) * layout.getScale(), a.y(), 1e-2);
        assertEquals(0.0, a.x(), 1e-2);
        assertEquals(Math.toRadians(20.0) * layout.getScale(), b.y(), 1e-2);
        assertEquals(Math.toRadians(10.0) * layout.getScale(), b.x(), 1e-2);
    }

    @Test
    public void columnsPickedInAnotherGraphModelWithTheSameIndexesAreReadInTheCurrentOne() {
        GraphModel other = GraphModel.Factory.newInstance();
        other.getNodeTable().addColumn("latitude", Double.class);
        other.getNodeTable().addColumn("longitude", Double.class);

        GraphModel graphModel = GraphModel.Factory.newInstance();
        Node a = addNode(graphModel, "a", 10.0, 0.0);
        Node b = addNode(graphModel, "b", 20.0, 10.0);

        GeoLayout layout = newLayout(graphModel, "Equirectangular");
        layout.setCentered(false);
        layout.setLatitude(other.getNodeTable().getColumn("latitude"));
        layout.setLongitude(other.getNodeTable().getColumn("longitude"));
        run(layout);

        assertEquals(Math.toRadians(10.0) * layout.getScale(), a.y(), 1e-2);
        assertEquals(Math.toRadians(20.0) * layout.getScale(), b.y(), 1e-2);
        assertEquals(Math.toRadians(10.0) * layout.getScale(), b.x(), 1e-2);
    }

    @Test
    public void columnsOfAnotherGraphModelWithoutACounterpartLeavePositionsUnchanged() {
        GraphModel other = graphModelWithMoreColumns("lat_deg", "lon_deg");

        GraphModel graphModel = GraphModel.Factory.newInstance();
        Node n = addNode(graphModel, "paris", 48.85, 2.35);
        n.setX(12f);
        n.setY(-7f);

        GeoLayout layout = newLayout(graphModel, "Mercator");
        layout.setLatitude(other.getNodeTable().getColumn("lat_deg"));
        layout.setLongitude(other.getNodeTable().getColumn("lon_deg"));

        layout.initAlgo();
        assertFalse(layout.canAlgo());
        // A controller only calls goAlgo() while canAlgo() is true, but calling it anyway must not fail
        layout.goAlgo();

        assertEquals(12f, n.x(), 0f);
        assertEquals(-7f, n.y(), 0f);
        assertEquals(0, graphModel.getGraphVisible().getLock().getReadHoldCount());
    }

    @Test
    public void aColumnRemovedFromTheNodeTableLeavesPositionsUnchanged() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Node n = addNode(graphModel, "paris", 48.85, 2.35);
        n.setX(12f);
        n.setY(-7f);

        GeoLayout layout = newLayout(graphModel, "Mercator");
        graphModel.getNodeTable().removeColumn("latitude");

        layout.initAlgo();
        assertFalse(layout.canAlgo());
        layout.goAlgo();

        assertEquals(12f, n.x(), 0f);
        assertEquals(-7f, n.y(), 0f);
        assertEquals(0, graphModel.getGraphVisible().getLock().getReadHoldCount());
    }

    @Test
    public void aColumnReplacedInTheNodeTableIsReadAgain() {
        GraphModel graphModel = GraphModel.Factory.newInstance();
        Node n = addNode(graphModel, "paris", 48.85, 2.35);

        GeoLayout layout = newLayout(graphModel, "Equirectangular");
        layout.setCentered(false);
        graphModel.getNodeTable().removeColumn("latitude");
        graphModel.getNodeTable().addColumn("latitude", Double.class);
        n.setAttribute("latitude", 30.0);
        run(layout);

        assertEquals(Math.toRadians(30.0) * layout.getScale(), n.y(), 1e-2);
        assertEquals(Math.toRadians(2.35) * layout.getScale(), n.x(), 1e-2);
    }

    private static GraphModel graphModelWithMoreColumns(String latitudeId, String longitudeId) {
        // Two extra columns come first, so latitude and longitude get indexes that are empty in a model made by addNode()
        GraphModel graphModel = GraphModel.Factory.newInstance();
        graphModel.getNodeTable().addColumn("population", Double.class);
        graphModel.getNodeTable().addColumn("rank", Double.class);
        graphModel.getNodeTable().addColumn(latitudeId, Double.class);
        graphModel.getNodeTable().addColumn(longitudeId, Double.class);
        return graphModel;
    }

    private static GeoLayout newLayout(GraphModel graphModel, String projection) {
        GeoLayout layout = new GeoLayout(new GeoLayoutBuilder());
        layout.setGraphModel(graphModel);
        layout.setProjection(projection);
        return layout;
    }

    private static void run(GeoLayout layout) {
        layout.initAlgo();
        while (layout.canAlgo()) {
            layout.goAlgo();
        }
        layout.endAlgo();
    }

    private static Node addNode(GraphModel graphModel, String id, Double lat, Double lon) {
        if (!graphModel.getNodeTable().hasColumn("latitude")) {
            graphModel.getNodeTable().addColumn("latitude", Double.class);
            graphModel.getNodeTable().addColumn("longitude", Double.class);
        }
        Node n = graphModel.factory().newNode(id);
        n.setAttribute("latitude", lat);
        n.setAttribute("longitude", lon);
        graphModel.getGraph().addNode(n);
        return n;
    }
}
