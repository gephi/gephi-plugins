/*
 * Copyright (c) 2017 by Roman Seidl - romanAeTgranul.at
 * 
 *  This Program uses code copyright (c) 2012 by David Shepard
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.granul.gephi.shpexporter;

import at.granul.gephi.shpexporter.ui.SHPExporterDialog;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gephi.graph.api.*;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.items.EdgeItem;

import org.openide.util.Lookup;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;

import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.openide.util.Exceptions;

/**
 *
 * @author SeidlR
 */
public class SHPExporter {

    private static final String LOCATION_FIELD = "the_geom";
    private static final String SIZE_FIELD = "gSize";
    private static final String COLOR_FIELD = "gColor";

    public boolean execute() {
        try {
            PreviewModel previewModel;
            final PreviewController previewController = Lookup.getDefault().lookup(PreviewController.class);

            //there seems to be a bug in gephi or in gephi & eclipse that needs a refresh of the preview - else the model is empty
            previewController.refreshPreview();

            previewModel = previewController.getModel();
            GraphModel model = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
            Column[] nodeColums = model.getNodeTable().toArray();

            //try to find the GeoFields
            Column[] geoFields;
            GeoAttributeFinder gaf = new GeoAttributeFinder();
            geoFields = gaf.findGeoFields(nodeColums);

            SHPExporterDialog exporterDialog;
            exporterDialog = new SHPExporterDialog(nodeColums, geoFields);
            exporterDialog.setTitle("SHP Export Options");
            if (exporterDialog.showDialog()) {
                geoFields = exporterDialog.getGeoFields();
                File exportFile = exporterDialog.getFile();

                //Construct Export Filenames
                String baseName = exportFile.getName();
                baseName = baseName.substring(0, baseName.lastIndexOf("."));
                File pointFile = new File(exportFile.getParentFile(), baseName + ".node.shp");
                File edgeFile = new File(exportFile.getParentFile(), baseName + ".edge.shp");

                //convert data to pointFeatureSource
                SimpleFeatureType pointFeatureType = getFeatureTypeForAttributes(Point.class, nodeColums);
                List<SimpleFeature> pointFeatureSource = getPointFeatureSource(previewModel, pointFeatureType, geoFields);
                writeSHP(pointFile.toURI().toURL(), pointFeatureType, pointFeatureSource);

                //convert data to edgeFeatureSource
                Column[] edgeColums = model.getEdgeTable().toArray();
                SimpleFeatureType edgeFeatureType = getFeatureTypeForAttributes(LineString.class, edgeColums);
                List<SimpleFeature> edgeFeatureSource = getFeatureSource(false, previewModel, edgeFeatureType, geoFields);
                writeSHP(edgeFile.toURI().toURL(), edgeFeatureType, edgeFeatureSource);

                return true;
            }
        } catch (IOException ex) {
            Logger.getLogger(SHPExporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    //Builds a SimpleFeatureType from all exportable Columns plus location, color and size
    private SimpleFeatureType getFeatureTypeForAttributes(Class geometryClass, Column[] nodeColums) {
        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
        builder.setName("Location");

        //builder.setName(geometryClass.getName());
        //Add a geometry
        builder.add(LOCATION_FIELD, geometryClass);

        for (Column col : nodeColums) {
            String id = col.getId();
            Class typ = col.getTypeClass();

            //ignore Lists and Interval
            if (!AttributeUtils.isCollectionType(typ) && !id.equals("timeset")) {
                builder.add(id, typ);
            }
        }
        //add size and color attributes
        builder.add(SIZE_FIELD, Float.class);
        builder.add(COLOR_FIELD, String.class);

        //build the type
        SimpleFeatureType featureType = builder.buildFeatureType();
        return featureType;
    }

    //Converts via parsing a String - might be risky as for localisation?
    private double getDoubleForCoordinateFieldObject(Object value) {
        return Double.parseDouble(value.toString());
    }

    //Produce a geotools Point FeatureCollection from the Graph
    private List<SimpleFeature> getPointFeatureSource(PreviewModel previewModel, SimpleFeatureType featureType, Column[] geoFields) {
        boolean isPoints = true;
        List<SimpleFeature> collection = getFeatureSource(isPoints, previewModel, featureType, geoFields);
        return collection;
    }

    //Produce a geotools FeatureCollection from the Graph
    private List<SimpleFeature> getFeatureSource(boolean isPoints, PreviewModel previewModel, SimpleFeatureType featureType, Column[] geoFields) {
        String sizeField = isPoints ? NodeItem.SIZE : EdgeItem.WEIGHT;
        String itemType = isPoints ? Item.NODE : Item.EDGE;
        String colorType = isPoints ? NodeItem.COLOR : EdgeItem.COLOR;
        //Helper to create the Point
        List<SimpleFeature> features = new ArrayList<SimpleFeature>();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(featureType);
        //Iterate through Nodes
        for (org.gephi.preview.api.Item ni : previewModel.getItems(itemType)) {

            Element row = (Element) ni.getSource();

            //Iterate over the columns to fill
            for (org.opengis.feature.type.AttributeType at : featureType.getTypes()) {

                String name = at.getName().getLocalPart();
                if (name.equals(LOCATION_FIELD)) {
                    if (isPoints) {
                        Coordinate coordinate = getCoordinateForNode((Node) ni.getSource(), geoFields);
                        Point point = geometryFactory.createPoint(coordinate);
                        featureBuilder.add(point);
                    } else {
                        final Edge e = (Edge) ni.getSource();
                        Coordinate[] coordinates = {getCoordinateForNode(e.getSource(), geoFields),
                            getCoordinateForNode(e.getTarget(), geoFields)};
                        LineString line = geometryFactory.createLineString(coordinates);
                        featureBuilder.add(line);
                    };
                } else if (name.equals(SIZE_FIELD)) {
                    Float size = Float.parseFloat(ni.getData(sizeField) + "");
                    size = new Float(size * 0.05); //Scale down as it is a bit large in qgis
                    featureBuilder.add(size);
                } else if (name.equals(COLOR_FIELD)) {
                    String rgb = "";
                    Color color = (Color) ni.getData(colorType);
                    if (color != null) {
                        rgb = Integer.toHexString(color.getRGB());
                        rgb = rgb.substring(2, rgb.length());
                    }
                    featureBuilder.add(rgb);
                } else {
                    featureBuilder.add(row.getAttribute(name));
                }
            }
            SimpleFeature feature = featureBuilder.buildFeature(null);
            features.add(feature);
        }
        return features;
    }

    private void writeSHP(URL url, SimpleFeatureType featureType, List<SimpleFeature> features) throws IOException {

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", url);
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        newDataStore.createSchema(featureType);

        //Write to the Shapefile
        Transaction transaction = new DefaultTransaction("create");

        String typeName = newDataStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

        if (featureSource instanceof SimpleFeatureStore) {
            SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
            SimpleFeatureCollection collection = new ListFeatureCollection(featureType, features);
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                transaction.rollback();
            } finally {
                transaction.close();
            }
        } else {
            Logger.getLogger(SHPExporter.class.getName()).log(Level.SEVERE, null, typeName + " does not support read/write access");
        }
    }

    private Coordinate getCoordinateForNode(Node node, Column[] geoFields) {
        double latitude, longitude;

        //is there a location set? else use pseudo-coordinates...
        if (geoFields[0] != null) {
            latitude = getDoubleForCoordinateFieldObject(node.getAttribute(geoFields[0]));
            longitude = getDoubleForCoordinateFieldObject(node.getAttribute(geoFields[1]));
        } else {
            latitude = new Double(node.x() + "");
            longitude = new Double(node.y() + "");
        }
        Coordinate coordinate = new Coordinate(latitude, longitude);
        return coordinate;
    }
}
