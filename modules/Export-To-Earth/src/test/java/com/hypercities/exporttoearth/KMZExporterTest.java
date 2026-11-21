package com.hypercities.exporttoearth;

import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import static org.junit.Assert.*;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Tests the KMZ files the exporter writes.
 *
 * The tests rely on the JVM running them not opening java.base/java.lang to the
 * unnamed module, which is how Gephi starts too.
 */
public class KMZExporterTest {

    private static final String KML_NAMESPACE = "http://www.opengis.net/kml/2.2";

    /**
     * Marshalling a Kml needs a JAXB implementation that works when java.lang is
     * not opened: the JAXB 2.2 reference implementation bundled with the Java API
     * for KML calls setAccessible on ClassLoader.defineClass when it first builds
     * a context, which JDK 17 refuses with an InaccessibleObjectException.
     */
    @Test
    public void testWriteKMZMarshalsKmlWithoutOpeningJavaLang() throws Exception {
        Kml kml = new Kml();
        Folder folder = kml.createAndSetFolder();

        Placemark node = folder.createAndAddPlacemark().withName("Berlin").withDescription("Population: 3600000\n");
        Style nodeStyle = folder.createAndAddStyle().withId("style_0");
        nodeStyle.createAndSetIconStyle().withScale(1.5).withIcon(new Icon().withHref("tiles-32-255-0-0.png"));
        node.setStyleUrl("#style_0");
        node.createAndSetPoint().addToCoordinates(13.4, 52.52);

        Placemark edge = folder.createAndAddPlacemark().withName("Berlin and Paris").withDescription("Weight: 2.0\n");
        Style edgeStyle = folder.createAndAddStyle().withId("style_1");
        edgeStyle.createAndSetLineStyle().withWidth(3.0).withColorMode(ColorMode.NORMAL).withColor("7f0000ff");
        edge.setStyleUrl("#style_1");
        edge.createAndSetLineString()
                .addToCoordinates(13.4, 52.52, 0)
                .addToCoordinates(2.35, 48.85, 0)
                .withTessellate(Boolean.TRUE).withExtrude(Boolean.TRUE);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        KMZExporter exporter = new KMZExporter();
        exporter.setOutputStream(bytes);
        exporter.writeKMZ(kml, new IconRenderer(32));

        Map<String, byte[]> entries = unzip(bytes.toByteArray());
        assertTrue("doc.kml missing from the KMZ file", entries.containsKey("doc.kml"));

        Document document = parse(entries.get("doc.kml"));
        Element root = document.getDocumentElement();
        assertEquals(KML_NAMESPACE, root.getNamespaceURI());
        assertEquals("kml", root.getLocalName());

        NodeList placemarks = document.getElementsByTagNameNS(KML_NAMESPACE, "Placemark");
        assertEquals(2, placemarks.getLength());

        Element nodePlacemark = (Element) placemarks.item(0);
        assertEquals("Berlin", text(nodePlacemark, "name"));
        assertEquals("Population: 3600000", text(nodePlacemark, "description").trim());
        assertEquals("#style_0", text(nodePlacemark, "styleUrl"));
        assertArrayEquals(new double[]{13.4, 52.52}, longitudesAndLatitudes(nodePlacemark), 0.0);

        Element edgePlacemark = (Element) placemarks.item(1);
        assertEquals("Berlin and Paris", text(edgePlacemark, "name"));
        assertEquals("#style_1", text(edgePlacemark, "styleUrl"));
        assertArrayEquals(new double[]{13.4, 52.52, 2.35, 48.85}, longitudesAndLatitudes(edgePlacemark), 0.0);

        NodeList styles = document.getElementsByTagNameNS(KML_NAMESPACE, "Style");
        assertEquals(2, styles.getLength());
        Element iconStyle = (Element) styles.item(0);
        assertEquals("style_0", iconStyle.getAttribute("id"));
        assertEquals(1.5, Double.parseDouble(text(iconStyle, "scale")), 0.0);
        assertEquals("tiles-32-255-0-0.png", text(iconStyle, "href"));
        Element lineStyle = (Element) styles.item(1);
        assertEquals("style_1", lineStyle.getAttribute("id"));
        assertEquals(3.0, Double.parseDouble(text(lineStyle, "width")), 0.0);
        assertEquals("7f0000ff", text(lineStyle, "color"));
    }

    private static Map<String, byte[]> unzip(byte[] kmz) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(kmz))) {
            for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
                entries.put(entry.getName(), in.readAllBytes());
            }
        }
        return entries;
    }

    private static Document parse(byte[] xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        return factory.newDocumentBuilder().parse(new ByteArrayInputStream(xml));
    }

    private static String text(Element parent, String name) {
        NodeList found = parent.getElementsByTagNameNS(KML_NAMESPACE, name);
        assertTrue("No " + name + " element", found.getLength() > 0);
        return found.item(0).getTextContent();
    }

    private static double[] longitudesAndLatitudes(Element placemark) {
        String[] points = text(placemark, "coordinates").trim().split("\\s+");
        double[] values = new double[2 * points.length];
        for (int i = 0; i < points.length; i++) {
            String[] point = points[i].split(",");
            values[2 * i] = Double.parseDouble(point[0]);
            values[2 * i + 1] = Double.parseDouble(point[1]);
        }
        return values;
    }
}
