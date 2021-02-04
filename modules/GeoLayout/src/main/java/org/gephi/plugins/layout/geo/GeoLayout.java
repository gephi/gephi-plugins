/*
 Copyright 2008-2011 Gephi
 Authors : Alexis Jacomy, Mathieu Bastian
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.plugins.layout.geo;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.spi.LayoutData;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.ui.propertyeditor.NodeColumnAllNumbersEditor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexis Jacomy
 */
public class GeoLayout implements Layout {

    private final GeoLayoutBuilder builder;
    private GraphModel graphModel;
    private boolean cancel;
    //Params
    private boolean looping = false;
    private final int loopingDelay = 50;//ms
    private final double focal = 150;
    private double scale = 1000;
    private boolean centered = true;
    private Column latitude;
    private Column longitude;
    private final boolean radian = false;
    private String projection = "Mercator";
    public static String[] rows = {
        "Mercator",
        "Transverse Mercator",
        "Miller cylindrical",
        "Gall–Peters",
        "Sinusoidal",
        "Lambert cylindrical",
        "Equirectangular",
        "Winkel tripel"};

    public GeoLayout(GeoLayoutBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void resetPropertiesValues() {
        if (graphModel != null) {
            for (Column c : graphModel.getNodeTable()) {
                if (c.isNumber() && !c.isArray() && !c.isDynamic()) {
                    if (c.getId().equalsIgnoreCase("latitude")
                            || c.getId().equalsIgnoreCase("lat")
                            || c.getTitle().equalsIgnoreCase("latitude")
                            || c.getTitle().equalsIgnoreCase("lat")) {
                        latitude = c;
                    } else if (c.getId().equalsIgnoreCase("longitude")
                            || c.getId().equalsIgnoreCase("lon")
                            || c.getTitle().equalsIgnoreCase("longitude")
                            || c.getTitle().equalsIgnoreCase("lon")) {
                        longitude = c;
                    }
                }
            }
        }
    }

    @Override
    public void initAlgo() {
        cancel = false;
    }

    @Override
    public void goAlgo() {
        double lon, lat;
        float nodeX, nodeY;
        float averageX = 0;
        float averageY = 0;
        Graph graph = graphModel.getGraphVisible();

        graph.readLock();

        List<Node> validNodes = new ArrayList<Node>();
        List<Node> invalidNodes = new ArrayList<Node>();

        // Set valid and non valid nodes:
        for (Node n : graph.getNodes()) {
            if (n.getAttribute(latitude) != null && n.getAttribute(longitude) != null) {
                validNodes.add(n);
            } else {
                invalidNodes.add(n);
            }
        }

        // Mercantor
        if (projection.equals("Mercator")) {
            double lambda0 = 0;

            //determine lambda0:
            for (Node n : validNodes) {
                lon = getNodeLongitude(graph, n);
                lambda0 += lon;
            }

            lambda0 = lambda0 / validNodes.size();
            lambda0 = Math.toRadians(lambda0);

            //apply the formula:
            for (Node n : validNodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                    n.setLayoutData(new GeoLayoutData());
                }
                lat = getNodeLatitude(graph, n);
                lon = getNodeLongitude(graph, n);

                lat = Math.toRadians(lat);
                lon = Math.toRadians(lon);

                nodeX = (float) ((lon - lambda0) * scale);
                nodeY = (float) ((Math.log(Math.tan(Math.PI / 4 + lat / 2))) * scale);

                averageX += nodeX;
                averageY += nodeY;

                n.setX(nodeX);
                n.setY(nodeY);
            }

            averageX = averageX / validNodes.size();
            averageY = averageY / validNodes.size();
        } // Transverse Mercantor
        else if (projection.equals("Transverse Mercator")) {
            //apply the formula:
            for (Node n : validNodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                    n.setLayoutData(new GeoLayoutData());
                }

                lat = getNodeLatitude(graph, n);
                lon = getNodeLongitude(graph, n);

                lat = Math.toRadians(lat);
                lon = Math.toRadians(lon);

                nodeX = (float) (lon * scale);
                nodeY = (float) (scale / 2 * Math.log((1 + Math.sin(lat)) / (1 - Math.sin(lat))));

                averageX += nodeX;
                averageY += nodeY;

                n.setX(nodeX);
                n.setY(nodeY);
            }

            averageX = averageX / validNodes.size();
            averageY = averageY / validNodes.size();
        } // Miller cylindrical
        else if (projection.equals("Miller cylindrical")) {
            //apply the formula:
            for (Node n : validNodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                    n.setLayoutData(new GeoLayoutData());
                }

                lat = getNodeLatitude(graph, n);
                lon = getNodeLongitude(graph, n);

                lat = Math.toRadians(lat);
                lon = Math.toRadians(lon);

                nodeX = (float) (lon * scale);
                nodeY = (float) (Math.log(Math.tan(Math.PI / 4 + 2 * lat / 5)) * scale * 5 / 4);

                averageX += nodeX;
                averageY += nodeY;

                n.setX(nodeX);
                n.setY(nodeY);
            }

            averageX = averageX / validNodes.size();
            averageY = averageY / validNodes.size();
        } // Gall–Peters
        else if (projection.equals("Gall–Peters")) {
            //apply the formula:
            for (Node n : validNodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                    n.setLayoutData(new GeoLayoutData());
                }

                lat = getNodeLatitude(graph, n);
                lon = getNodeLongitude(graph, n);

                lat = Math.toRadians(lat);
                lon = Math.toRadians(lon);

                nodeX = (float) (lon * scale);
                nodeY = (float) (2 * scale * Math.sin(lat));

                averageX += nodeX;
                averageY += nodeY;

                n.setX(nodeX);
                n.setY(nodeY);
            }

            averageX = averageX / validNodes.size();
            averageY = averageY / validNodes.size();
        } // Sinusoidal
        else if (projection.equals("Sinusoidal")) {
            double lambda0 = 0;

            //determine lambda0:
            for (Node n : validNodes) {
                lon = getNodeLongitude(graph, n);
                lambda0 += lon;
            }

            lambda0 = lambda0 / validNodes.size();
            lambda0 = Math.toRadians(lambda0);

            //apply the formula:
            for (Node n : validNodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                    n.setLayoutData(new GeoLayoutData());
                }

                lat = getNodeLatitude(graph, n);
                lon = getNodeLongitude(graph, n);

                lat = Math.toRadians(lat);
                lon = Math.toRadians(lon);

                nodeX = (float) ((lon - lambda0) * Math.cos(lat) * scale);
                nodeY = (float) (scale * lat);

                averageX += nodeX;
                averageY += nodeY;

                n.setX(nodeX);
                n.setY(nodeY);
            }

            averageX = averageX / validNodes.size();
            averageY = averageY / validNodes.size();
        } // Lambert cylindrical equal-area
        else if (projection.equals("Lambert cylindrical")) {
            double lambda0 = 0;
            double phi0 = 0;

            //determine lambda0:
            for (Node n : validNodes) {
                lat = getNodeLatitude(graph, n);
                lon = getNodeLongitude(graph, n);
                lambda0 += lon;
                phi0 += lat;
            }

            lambda0 = lambda0 / validNodes.size();
            phi0 = phi0 / validNodes.size();

            lambda0 = Math.toRadians(lambda0);
            phi0 = Math.toRadians(phi0);

            //apply the formula:
            for (Node n : validNodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                    n.setLayoutData(new GeoLayoutData());
                }

                lat = getNodeLatitude(graph, n);
                lon = getNodeLongitude(graph, n);

                lat = Math.toRadians(lat);
                lon = Math.toRadians(lon);

                nodeX = (float) ((lon - lambda0) * Math.cos(phi0) * scale);
                nodeY = (float) (scale * Math.sin(lat) / Math.cos(phi0));

                averageX += nodeX;
                averageY += nodeY;

                n.setX(nodeX);
                n.setY(nodeY);
            }

            averageX = averageX / validNodes.size();
            averageY = averageY / validNodes.size();
        } // Equirectangular
        else if (projection.equals("Equirectangular")) {

            //apply the formula:
            for (Node n : validNodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                    n.setLayoutData(new GeoLayoutData());
                }

                lat = getNodeLatitude(graph, n);
                lon = getNodeLongitude(graph, n);

                lat = Math.toRadians(lat);
                lon = Math.toRadians(lon);

                nodeX = (float) (scale * lon);
                nodeY = (float) (scale * lat);

                averageX += nodeX;
                averageY += nodeY;

                n.setX(nodeX);
                n.setY(nodeY);
            }

            averageX = averageX / validNodes.size();
            averageY = averageY / validNodes.size();
        } // Winkel tripel
        else if (projection.equals("Winkel tripel")) {
            double alpha;

            //apply the formula:
            for (Node n : validNodes) {
                if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                    n.setLayoutData(new GeoLayoutData());
                }

                lat = getNodeLatitude(graph, n);
                lon = getNodeLongitude(graph, n);

                lat = Math.toRadians(lat);
                lon = Math.toRadians(lon);

                alpha = Math.acos(Math.cos(lon / 2) * 2 / Math.PI);

                nodeX = (float) (scale * ((lon * 2 / Math.PI) + (2 * Math.cos(lat) * Math.sin(lon / 2) * alpha / Math.sin(alpha))));
                nodeY = (float) (scale * (lat + Math.sin(lat) * alpha / Math.sin(alpha)));

                averageX += nodeX;
                averageY += nodeY;

                n.setX(nodeX);
                n.setY(nodeY);
            }

            averageX = averageX / validNodes.size();
            averageY = averageY / validNodes.size();
        }

        if (validNodes.size() > 0 && invalidNodes.size() > 0) {
            Node tempNode = validNodes.get(0);
            double xMin = tempNode.x();
            double xMax = tempNode.x();
            double yMin = tempNode.y();
            double xTemp, yTemp;

            for (Node n : validNodes) {
                xTemp = n.x();
                yTemp = n.y();

                if (xTemp < xMin) {
                    xMin = xTemp;
                }
                if (xTemp > xMax) {
                    xMax = xTemp;
                }
                if (yTemp < yMin) {
                    yMin = yTemp;
                }
            }

            if (invalidNodes.size() > 1) {
                double i = 0;
                double step = (xMax - xMin) / (invalidNodes.size() - 1);
                for (Node n : invalidNodes) {
                    n.setX((float) (xMin + i * step));
                    n.setY((float) (yMin - step));
                    i++;
                }
            } else {
                tempNode = invalidNodes.get(0);
                tempNode.setX(10000);
                tempNode.setY(10000);
            }
        }

        //recenter the graph
        if (centered == true) {
            for (Node n : graph.getNodes()) {
                nodeX = n.x() - averageX;
                nodeY = n.y() - averageY;

                n.setX(nodeX);
                n.setY(nodeY);
            }
        }

        graph.readUnlock();

        if (!looping) {
            cancel = true;
        } else {
            try {
                //Sleep some time
                Thread.sleep(loopingDelay);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    private double getNodeLatitude(Graph graph, Node n) {
        Number lat = (Number) n.getAttribute(latitude, graph.getView());
        return lat.doubleValue();
    }

    private double getNodeLongitude(Graph graph, Node n) {
        Number lat = (Number) n.getAttribute(longitude, graph.getView());
        return lat.doubleValue();
    }

    @Override
    public void endAlgo() {
    }

    @Override
    public boolean canAlgo() {
        return !cancel && latitude != null && longitude != null;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String GEOLAYOUT = "Geo Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.scale.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.scale.desc"),
                    "getScale", "setScale"));
            properties.add(LayoutProperty.createProperty(
                    this, Column.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.latitude.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.latitude.desc"),
                    "getLatitude", "setLatitude", NodeColumnAllNumbersEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, Column.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.longitude.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.longitude.desc"),
                    "getLongitude", "setLongitude", NodeColumnAllNumbersEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.projection.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.projection.desc"),
                    "getProjection", "setProjection", CustomComboBoxEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.centered.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.centered.desc"),
                    "isCentered", "setCentered"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.looping.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.looping.desc"),
                    "isLooping", "setLooping"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    public Boolean isCentered() {
        return centered;
    }

    public void setCentered(Boolean centered) {
        this.centered = centered;
    }

    public Boolean isLooping() {
        return looping;
    }

    public void setLooping(Boolean looping) {
        this.looping = looping;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    @Override
    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
        resetPropertiesValues();
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public Column getLatitude() {
        return latitude;
    }

    public void setLatitude(Column latitude) {
        this.latitude = latitude;
    }

    public Column getLongitude() {
        return longitude;
    }

    public void setLongitude(Column longitude) {
        this.longitude = longitude;
    }

    private static class GeoLayoutData implements LayoutData {

        //Data
        public double x = 0f;
        public double y = 0f;
    }
}
