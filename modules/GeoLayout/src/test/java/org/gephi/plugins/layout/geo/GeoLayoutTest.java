package org.gephi.plugins.layout.geo;

import static org.junit.Assert.assertEquals;
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
