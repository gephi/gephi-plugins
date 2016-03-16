/*
 Copyright 2008 WebAtlas
 Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
 Website : http://www.gephi.org

 This file is part of Gephi.

 Gephi is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Gephi is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package Core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Vector;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.spi.LayoutData;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.gephi.data.attributes.api.*;
import org.gephi.graph.api.Edge;
import org.openide.util.Exceptions;

/**
 *
 * @author Alexis Jacomy
 */
public class GeoLayout implements Layout {

    private GeoLayoutBuilder builder;
    private GraphModel graphModel;
    private boolean cancel;
    //Params
    private double focal = 150;
    private double scale = 1000;
    private boolean centered = true;
    private AttributeColumn latitude;
    private AttributeColumn longitude;
    private boolean radian = false;
    private String projection = "Mercator";
    private String region = "No region";
    private String subregion = "No subregion";
    private String district = "No district";
    private String country = "World";
    public static String[] rows = {"Mercator", "Transverse Mercator", "Miller cylindrical", "Gall–Peters", "Sinusoidal", "Lambert cylindrical", "Equirectangular", "Winkel tripel"};
    public static String[] countries = {"World", "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory", "British Virgin Islands", "Brunei Darussalam", "Bulgaria", "Burkina Faso", "Burma", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia", "Cuba", "Cyprus", "Czech Republic", "Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "French Guiana", "French Polynesia", "French Southern and Antarctic Lands", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guernsey", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Heard Island and McDonald Islands", "Holy See (Vatican City)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran (Islamic Republic of)", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Jamaica", "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, Democratic People's Republic of", "Korea, Republic of", "Kuwait", "Kyrgyzstan", "Lao People's Democratic Republic", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libyan Arab Jamahiriya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia, Federated States of", "Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco", "Mozambique", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Palestine", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar", "Republic of Moldova", "Reunion", "Romania", "Russia", "Rwanda", "Saint Barthelemy", "Saint Helena", "Saint Kitts and Nevis", "Saint Lucia", "Saint Martin", "Saint Pierre and Miquelon", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Georgia South Sandwich Islands", "Spain", "Sri Lanka", "Sudan", "Suriname", "Svalbard", "Swaziland", "Sweden", "Switzerland", "Syrian Arab Republic", "Taiwan", "Tajikistan", "Thailand", "The former Yugoslav Republic of Macedonia", "Timor-Leste", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United Republic of Tanzania", "United States", "United States Minor Outlying Islands", "United States Virgin Islands", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Viet Nam", "Wallis and Futuna Islands", "Western Sahara", "Yemen", "Zambia", "Zimbabwe", "Åland Islands"};
    public static String[] regions = {"No region", "Africa", "Americas", "Asia", "Europe", "Europe (without Russia)", "Oceania"};
    public static String[] subregions = {"No subregion", "Eastern Africa", "Niddle Africa", "Northern Africa", "Southern Africa", "Western Africa", "Caribbean", "Central America", "South America", "Northern America", "Central Asia", "Eastern Asia", "Southern Asia", "South-Eastern Asia", "Western Asia", "Eastern Europe", "Northern Europe", "Southern Europe", "Western Europe", "Australia and New Zealand", "Melanesia", "Micronesia", "Polynesia"};
    public static String[] districts = {"No district", "Australia_Victoria", "Australia_New South Wales", "Australia_Queensland", "Australia_Tasmania", "Australia_Northern Territory", "Australia_Western Australia", "Australia_South Australia", "Australia_Other Territories", "Australia_Australian Capital Territory"};
    public Float weight = 1f;

    public GeoLayout(GeoLayoutBuilder builder) {
        this.builder = builder;
        resetPropertiesValues();
    }

    public void resetPropertiesValues() {
    }

    public void initAlgo() {
        cancel = false;
    }

    public void goAlgo() {
        try {
            double lon = 0;
            double lat = 0;
            float nodeX = 0;
            float nodeY = 0;
            float averageX = 0;
            float averageY = 0;
            Graph gr = graphModel.getGraph();
            Graph graph = null;
            Estimator estimator = null;
            graph = gr;
            ShapeFileReader shapeFileReader = new ShapeFileReader();
            graph = shapeFileReader.read(graph, country, subregion, region, district);
            AttributeModel attModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
            for (AttributeColumn c : attModel.getNodeTable().getColumns()) {
                if (c.getId().equalsIgnoreCase("lat") | c.getTitle().equalsIgnoreCase("lat")) {
                    latitude = c;
                } else if (c.getId().equalsIgnoreCase("lng") | c.getTitle().equalsIgnoreCase("lng")) {
                    longitude = c;
                }
            }
            List<Node> nodesList = new ArrayList();
            for (Node n : graph.getNodes().toArray()) {
                if (n.getAttributes().getValue("background_map") == null) {
                    continue;
                }
                if (n.getAttributes().getValue("background_map").equals(true)) {
                    nodesList.add(n);
                }
            }
            Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
            Vector<Node> validNodes = new Vector<Node>();
            Vector<Node> unvalidNodes = new Vector<Node>();
            for (Node n : nodes) {
                AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
                if (row.getValue(latitude) != null && row.getValue(longitude) != null) {
                    if (Math.abs(((Number) row.getValue(longitude)).doubleValue()) == 180d) {
                        graph.removeNode(n);
                        continue;
                    }

                    if (Math.abs(((Number) row.getValue(latitude)).doubleValue()) == 90d) {
                        graph.removeNode(n);
                        continue;
                    }

                    validNodes.add(n);
                } else {
                    unvalidNodes.add(n);
                }
            }
            for (Edge e : graph.getEdges().toArray()) {
                Node nodeSource  = e.getSource();
                Node nodeTarget  = e.getTarget();
                if (nodeSource.getAttributes().getValue("background_map") != null ||nodeTarget.getAttributes().getValue("background_map") != null ) 
                e.setWeight(this.weight);
            }

            if (projection.equals("Mercator")) {
                double lambda0 = 0;

                //determine lambda0:
                for (Node n : validNodes) {
                    AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
                    lon = ((Number) row.getValue(longitude)).doubleValue();
                    lambda0 += lon;
                }

                lambda0 = lambda0 / validNodes.size();
                lambda0 = Math.toRadians(lambda0);

                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof GeoLayoutData)) {
                        n.getNodeData().setLayoutData(new GeoLayoutData());
                    }

                    AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();

                    lat = ((Number) row.getValue(latitude)).doubleValue();
                    lon = ((Number) row.getValue(longitude)).doubleValue();

                    lat = Math.toRadians(lat);
                    lon = Math.toRadians(lon);

                    nodeX = (float) ((lon - lambda0) * scale);
                    nodeY = (float) ((Math.log(Math.tan(Math.PI / 4 + lat / 2))) * scale);

                    averageX += nodeX;
                    averageY += nodeY;

                    n.getNodeData().setX(nodeX);
                    n.getNodeData().setY(nodeY);
                }

                averageX = averageX / validNodes.size();
                averageY = averageY / validNodes.size();
            } // Transverse Mercantor
            else if (projection.equals("Transverse Mercator")) {
                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof GeoLayoutData)) {
                        n.getNodeData().setLayoutData(new GeoLayoutData());
                    }

                    AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();

                    lat = ((Number) row.getValue(latitude)).doubleValue();
                    lon = ((Number) row.getValue(longitude)).doubleValue();

                    lat = Math.toRadians(lat);
                    lon = Math.toRadians(lon);

                    nodeX = (float) (lon * scale);
                    nodeY = (float) (scale / 2 * Math.log((1 + Math.sin(lat)) / (1 - Math.sin(lat))));

                    averageX += nodeX;
                    averageY += nodeY;

                    n.getNodeData().setX(nodeX);
                    n.getNodeData().setY(nodeY);
                }

                averageX = averageX / validNodes.size();
                averageY = averageY / validNodes.size();
            } // Miller cylindrical
            else if (projection.equals("Miller cylindrical")) {
                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof GeoLayoutData)) {
                        n.getNodeData().setLayoutData(new GeoLayoutData());
                    }

                    AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();

                    lat = ((Number) row.getValue(latitude)).doubleValue();
                    lon = ((Number) row.getValue(longitude)).doubleValue();

                    lat = Math.toRadians(lat);
                    lon = Math.toRadians(lon);

                    nodeX = (float) (lon * scale);
                    nodeY = (float) (Math.log(Math.tan(Math.PI / 4 + 2 * lat / 5)) * scale * 5 / 4);

                    averageX += nodeX;
                    averageY += nodeY;

                    n.getNodeData().setX(nodeX);
                    n.getNodeData().setY(nodeY);
                }

                averageX = averageX / validNodes.size();
                averageY = averageY / validNodes.size();
            } // Gall–Peters
            else if (projection.equals("Gall–Peters")) {
                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof GeoLayoutData)) {
                        n.getNodeData().setLayoutData(new GeoLayoutData());
                    }

                    AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();

                    lat = ((Number) row.getValue(latitude)).doubleValue();
                    lon = ((Number) row.getValue(longitude)).doubleValue();

                    lat = Math.toRadians(lat);
                    lon = Math.toRadians(lon);

                    nodeX = (float) (lon * scale);
                    nodeY = (float) (2 * scale * Math.sin(lat));

                    averageX += nodeX;
                    averageY += nodeY;

                    n.getNodeData().setX(nodeX);
                    n.getNodeData().setY(nodeY);
                }

                averageX = averageX / validNodes.size();
                averageY = averageY / validNodes.size();
            } // Sinusoidal
            else if (projection.equals("Sinusoidal")) {
                double lambda0 = 0;
                //determine lambda0:
                for (Node n : validNodes) {
                    AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
                    lon = ((Number) row.getValue(longitude)).doubleValue();
                    lambda0 += lon;
                }

                lambda0 = lambda0 / validNodes.size();
                lambda0 = Math.toRadians(lambda0);

                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof GeoLayoutData)) {
                        n.getNodeData().setLayoutData(new GeoLayoutData());
                    }

                    AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();

                    lat = ((Number) row.getValue(latitude)).doubleValue();
                    lon = ((Number) row.getValue(longitude)).doubleValue();

                    lat = Math.toRadians(lat);
                    lon = Math.toRadians(lon);

                    nodeX = (float) ((lon - lambda0) * Math.cos(lat) * scale);
                    nodeY = (float) (scale * lat);

                    averageX += nodeX;
                    averageY += nodeY;

                    n.getNodeData().setX(nodeX);
                    n.getNodeData().setY(nodeY);
                }

                averageX = averageX / validNodes.size();
                averageY = averageY / validNodes.size();
            } // Lambert cylindrical equal-area
            else if (projection.equals("Lambert cylindrical")) {
                double lambda0 = 0;
                double phi0 = 0;

                //determine lambda0:
                for (Node n : validNodes) {
                    AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
                    lat = ((Number) row.getValue(latitude)).doubleValue();
                    lon = ((Number) row.getValue(longitude)).doubleValue();
                    lambda0 += lon;
                    phi0 += lat;
                }

                lambda0 = lambda0 / validNodes.size();
                phi0 = phi0 / validNodes.size();

                lambda0 = Math.toRadians(lambda0);
                phi0 = Math.toRadians(phi0);

                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof GeoLayoutData)) {
                        n.getNodeData().setLayoutData(new GeoLayoutData());
                    }

                    AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
                    lat = ((Number) row.getValue(latitude)).doubleValue();
                    lon = ((Number) row.getValue(longitude)).doubleValue();

                    lat = Math.toRadians(lat);
                    lon = Math.toRadians(lon);

                    nodeX = (float) ((lon - lambda0) * Math.cos(phi0) * scale);
                    nodeY = (float) (scale * Math.sin(lat) / Math.cos(phi0));

                    averageX += nodeX;
                    averageY += nodeY;

                    n.getNodeData().setX(nodeX);
                    n.getNodeData().setY(nodeY);
                }

                averageX = averageX / validNodes.size();
                averageY = averageY / validNodes.size();
            } // Equirectangular
            else if (projection.equals("Equirectangular")) {

                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof GeoLayoutData)) {
                        n.getNodeData().setLayoutData(new GeoLayoutData());
                    }

                    AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
                    lat = ((Number) row.getValue(latitude)).doubleValue();
                    lon = ((Number) row.getValue(longitude)).doubleValue();

                    lat = Math.toRadians(lat);
                    lon = Math.toRadians(lon);

                    nodeX = (float) (scale * lon);
                    nodeY = (float) (scale * lat);

                    averageX += nodeX;
                    averageY += nodeY;

                    n.getNodeData().setX(nodeX);
                    n.getNodeData().setY(nodeY);
                }

                averageX = averageX / validNodes.size();
                averageY = averageY / validNodes.size();
            } // Winkel tripel
            else if (projection.equals("Winkel tripel")) {
                double alpha = 0;

                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getNodeData().getLayoutData() == null || !(n.getNodeData().getLayoutData() instanceof GeoLayoutData)) {
                        n.getNodeData().setLayoutData(new GeoLayoutData());
                    }

                    AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
                    lat = ((Number) row.getValue(latitude)).doubleValue();
                    lon = ((Number) row.getValue(longitude)).doubleValue();

                    lat = Math.toRadians(lat);
                    lon = Math.toRadians(lon);

                    alpha = Math.acos(Math.cos(lon / 2) * 2 / Math.PI);

                    nodeX = (float) (scale * ((lon * 2 / Math.PI) + (2 * Math.cos(lat) * Math.sin(lon / 2) * alpha / Math.sin(alpha))));
                    nodeY = (float) (scale * (lat + Math.sin(lat) * alpha / Math.sin(alpha)));

                    averageX += nodeX;
                    averageY += nodeY;

                    n.getNodeData().setX(nodeX);
                    n.getNodeData().setY(nodeY);
                }

                averageX = averageX / validNodes.size();
                averageY = averageY / validNodes.size();
            }
            if (validNodes.size() > 0 && unvalidNodes.size() > 0) {
                Node tempNode = validNodes.elementAt(0);
                double xMin = tempNode.getNodeData().x();
                double xMax = tempNode.getNodeData().x();
                double yMin = tempNode.getNodeData().y();
                double xTemp = 0;
                double yTemp = 0;

                for (Node n : validNodes) {
                    xTemp = n.getNodeData().x();
                    yTemp = n.getNodeData().y();

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

                if (unvalidNodes.size() > 1) {
                    double i = 0;
                    double step = (xMax - xMin) / (unvalidNodes.size() - 1);
                    for (Node n : unvalidNodes) {
                        n.getNodeData().setX((float) (xMin + i * step));
                        n.getNodeData().setY((float) (yMin - step));
                        i++;
                    }
                } else {
                    tempNode = unvalidNodes.elementAt(0);
                    tempNode.getNodeData().setX(10000);
                    tempNode.getNodeData().setY(10000);
                }
            }
            if (centered == true) {
                for (Node n : nodes) {
                    nodeX = n.getNodeData().x() - averageX;
                    nodeY = n.getNodeData().y() - averageY;

                    n.getNodeData().setX(nodeX);
                    n.getNodeData().setY(nodeY);
                }
            }
            cancel = true;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void endAlgo() {
    }

    @Override
    public boolean canAlgo() {
        return !cancel;
    }

    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String GEOLAYOUT = "Geo Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.countries.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.countries.desc"),
                    "getCountry", "setCountry", CustomComboBoxEditorForCountries.class));

            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.subregions.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.subregions.desc"),
                    "getSubregion", "setSubregion", CustomComboBoxEditorForSubRegions.class));

            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.regions.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.regions.desc"),
                    "getRegion", "setRegion", CustomComboBoxEditorForRegions.class));

            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.districts.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.districts.desc"),
                    "getDistrict", "setDistrict", CustomComboBoxEditorForDistricts.class));

            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.scale.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.scale.desc"),
                    "getScale", "setScale"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.weight.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.weight.desc"),
                    "getWeight", "setWeight"));
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
        } catch (MissingResourceException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
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

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
    }

    public LayoutBuilder getBuilder() {
        return builder;
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public AttributeColumn getLatitude() {
        return latitude;
    }

    public void setLatitude(AttributeColumn latitude) {
        this.latitude = latitude;
    }

    public AttributeColumn getLongitude() {
        return longitude;
    }

    public void setLongitude(AttributeColumn longitude) {
        this.longitude = longitude;
    }

    private static class GeoLayoutData implements LayoutData {

        //Data
        public double x = 0f;
        public double y = 0f;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSubregion() {
        return subregion;
    }

    public void setSubregion(String subregion) {
        this.subregion = subregion;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

}
