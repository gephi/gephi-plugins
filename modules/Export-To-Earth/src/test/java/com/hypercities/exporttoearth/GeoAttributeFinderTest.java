package com.hypercities.exporttoearth;

import org.gephi.graph.api.Column;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Dave Shepard
 */
public class GeoAttributeFinderTest {
    
    public GeoAttributeFinderTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void testFindGeoFieldsWithLatitudeAndLongitude() {
        Column[] columns = {
            new MockAttributeColumn("id", "abc"),
            new MockAttributeColumn("stringLatitude", "12.2345"),
            new MockAttributeColumn("latitude", 12.423),
            new MockAttributeColumn("longitude", 49.301)
        };
        GeoAttributeFinder instance = new GeoAttributeFinder();
        Column[] resultColumns = instance.findGeoFields(columns);
        // TODO review the generated test code and remove the default call to fail.
        assertNotNull("Longitude not found.", resultColumns[0]);
        assertNotNull("Latitude not found.", resultColumns[1]);
    }

    @Test
    public void testFindGeoFieldsWithLatitudeAndLongitudePartialNames() {
        Column[] columns = {
            new MockAttributeColumn("id", "abc"),
            new MockAttributeColumn("PlaceLatitudeInDegrees", 40.239),
            new MockAttributeColumn("PlaceLongitudeInDegrees", 21.123)
        };
        GeoAttributeFinder instance = new GeoAttributeFinder();
        Column[] resultColumns = instance.findGeoFields(columns);
        // TODO review the generated test code and remove the default call to fail.
        assertNotNull("PlaceLongitudeInDegrees not found.", resultColumns[0]);
        assertNotNull("PlaceLatitudeInDegrees not found.", resultColumns[1]);
    }

    @Test
    public void testFindGeoFieldsWithLatitudeAndLongitudeButWrongTypes() {
        Column[] columns = {
            new MockAttributeColumn("id", "abc"),
            new MockAttributeColumn("latitude", "123.30199"),
            new MockAttributeColumn("longitude", "91.49123")
        };
        GeoAttributeFinder instance = new GeoAttributeFinder();
        Column[] resultColumns = instance.findGeoFields(columns);
        assertNull("Longitude found even though it's a string.", resultColumns[0]);
        assertNull("Latitude found even though it's a string.", resultColumns[1]);
    }

    @Test
    public void testFindGeoFieldsWithLatAndLon() {
        Column[] columns = {
            new MockAttributeColumn("id", "abc"),
            new MockAttributeColumn("lat", 1.49123),
            new MockAttributeColumn("lon", 49.49123)
        };
        GeoAttributeFinder instance = new GeoAttributeFinder();
        Column[] resultColumns = instance.findGeoFields(columns);
        assertNotNull("Lon not found", resultColumns[0]);
        assertNotNull("Lat not found", resultColumns[1]);
    }

    @Test
    public void testFindGeoFieldsWithLatAndLng() {
        Column[] columns = {
            new MockAttributeColumn("id", "abc"),
            new MockAttributeColumn("lat", 13.492),
            new MockAttributeColumn("lng", 9.123)
        };
        GeoAttributeFinder instance = new GeoAttributeFinder();
        Column[] resultColumns = instance.findGeoFields(columns);
        assertNotNull("lng not found", resultColumns[0]);
        assertNotNull("lat not found", resultColumns[1]);
    }

    @Test
    public void testFindGeoFieldsWithXAndY() {
        Column[] columns = {
            new MockAttributeColumn("id", "abc"),
            new MockAttributeColumn("x", 13.492),
            new MockAttributeColumn("y", 9.123)
        };
        GeoAttributeFinder instance = new GeoAttributeFinder();
        Column[] resultColumns = instance.findGeoFields(columns);
        assertNotNull("Y not found", resultColumns[0]);
        assertNotNull("X not found", resultColumns[1]);
    }
}
