/*
 Copyright 2008-2013 Clement Levallois
 Authors : Clement Levallois <clementlevallois@gmail.com>
 Website : http://www.clementlevallois.net


 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Clement Levallois. All rights reserved.

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

 Contributor(s): Clement Levallois
 */
package net.clementlevallois.mapofcountries;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ArrayList;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.spi.LayoutData;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.NbBundle;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.openide.util.Exceptions;

/**
 *
 * @author Clement Levallois
 */
public final class GeoLayout implements Layout {

    private final GeoLayoutBuilder builder;
    private GraphModel graphModel;
    private boolean cancel;
    //Params
    private double scale = 1000;
    private boolean centered = true;
    private Column latitude;
    private Column longitude;
    private String projection = "Mercator";
    private String region = "No region";
    private String subregion = "No subregion";
    private String district = "No district";
    private String country = "World";
    public static String[] PROJECTIONS = {"Mercator", "Transverse Mercator", "Miller cylindrical", "Gall–Peters", "Sinusoidal", "Lambert cylindrical", "Equirectangular", "Winkel tripel"};
    public static String[] COUNTRIES = {"World", "Afghanistan", "Albania", "Algeria", "American Samoa", "Andorra", "Angola", "Anguilla", "Antarctica", "Antigua and Barbuda", "Argentina", "Armenia", "Aruba", "Australia", "Austria", "Azerbaijan", "Bahamas", "Bahrain", "Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin", "Bermuda", "Bhutan", "Bolivia", "Bosnia and Herzegovina", "Botswana", "Bouvet Island", "Brazil", "British Indian Ocean Territory", "British Virgin Islands", "Brunei Darussalam", "Bulgaria", "Burkina Faso", "Burma", "Burundi", "Cambodia", "Cameroon", "Canada", "Cape Verde", "Cayman Islands", "Central African Republic", "Chad", "Chile", "China", "Christmas Island", "Cocos (Keeling) Islands", "Colombia", "Comoros", "Congo", "Cook Islands", "Costa Rica", "Cote d'Ivoire", "Croatia", "Cuba", "Cyprus", "Czech Republic", "Democratic Republic of the Congo", "Denmark", "Djibouti", "Dominica", "Dominican Republic", "Ecuador", "Egypt", "El Salvador", "Equatorial Guinea", "Eritrea", "Estonia", "Ethiopia", "Falkland Islands (Malvinas)", "Faroe Islands", "Fiji", "Finland", "France", "French Guiana", "French Polynesia", "French Southern and Antarctic Lands", "Gabon", "Gambia", "Georgia", "Germany", "Ghana", "Gibraltar", "Greece", "Greenland", "Grenada", "Guadeloupe", "Guam", "Guatemala", "Guernsey", "Guinea", "Guinea-Bissau", "Guyana", "Haiti", "Heard Island and McDonald Islands", "Holy See (Vatican City)", "Honduras", "Hong Kong", "Hungary", "Iceland", "India", "Indonesia", "Iran (Islamic Republic of)", "Iraq", "Ireland", "Isle of Man", "Israel", "Italy", "Jamaica", "Japan", "Jersey", "Jordan", "Kazakhstan", "Kenya", "Kiribati", "Korea, Democratic People's Republic of", "Korea, Republic of", "Kuwait", "Kyrgyzstan", "Lao People's Democratic Republic", "Latvia", "Lebanon", "Lesotho", "Liberia", "Libyan Arab Jamahiriya", "Liechtenstein", "Lithuania", "Luxembourg", "Macau", "Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta", "Marshall Islands", "Martinique", "Mauritania", "Mauritius", "Mayotte", "Mexico", "Micronesia, Federated States of", "Monaco", "Mongolia", "Montenegro", "Montserrat", "Morocco", "Mozambique", "Namibia", "Nauru", "Nepal", "Netherlands", "Netherlands Antilles", "New Caledonia", "New Zealand", "Nicaragua", "Niger", "Nigeria", "Niue", "Norfolk Island", "Northern Mariana Islands", "Norway", "Oman", "Pakistan", "Palau", "Palestine", "Panama", "Papua New Guinea", "Paraguay", "Peru", "Philippines", "Pitcairn Islands", "Poland", "Portugal", "Puerto Rico", "Qatar", "Republic of Moldova", "Reunion", "Romania", "Russia", "Rwanda", "Saint Barthelemy", "Saint Helena", "Saint Kitts and Nevis", "Saint Lucia", "Saint Martin", "Saint Pierre and Miquelon", "Saint Vincent and the Grenadines", "Samoa", "San Marino", "Sao Tome and Principe", "Saudi Arabia", "Senegal", "Serbia", "Seychelles", "Sierra Leone", "Singapore", "Slovakia", "Slovenia", "Solomon Islands", "Somalia", "South Africa", "South Georgia South Sandwich Islands", "Spain", "Sri Lanka", "Sudan", "Suriname", "Svalbard", "Swaziland", "Sweden", "Switzerland", "Syrian Arab Republic", "Taiwan", "Tajikistan", "Thailand", "The former Yugoslav Republic of Macedonia", "Timor-Leste", "Togo", "Tokelau", "Tonga", "Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan", "Turks and Caicos Islands", "Tuvalu", "Uganda", "Ukraine", "United Arab Emirates", "United Kingdom", "United Republic of Tanzania", "United States", "United States Minor Outlying Islands", "United States Virgin Islands", "Uruguay", "Uzbekistan", "Vanuatu", "Venezuela", "Viet Nam", "Wallis and Futuna Islands", "Western Sahara", "Yemen", "Zambia", "Zimbabwe", "Åland Islands"};
    public static String[] REGIONS = {"No region", "Africa", "Americas", "Asia", "Europe", "Europe (without Russia)", "Oceania"};
    public static String[] SUBREGIONS = {"No subregion", "Eastern Africa", "Niddle Africa", "Northern Africa", "Southern Africa", "Western Africa", "Caribbean", "Central America", "South America", "Northern America", "Central Asia", "Eastern Asia", "Southern Asia", "South-Eastern Asia", "Western Asia", "Eastern Europe", "Northern Europe", "Southern Europe", "Western Europe", "Australia and New Zealand", "Melanesia", "Micronesia", "Polynesia"};
    public static String[] DISTRICTS = {"No district", "Australia_Victoria", "Australia_New South Wales", "Australia_Queensland", "Australia_Tasmania", "Australia_Northern Territory", "Australia_Western Australia", "Australia_South Australia", "Australia_Other Territories", "Australia_Australian Capital Territory"};
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
            Graph graph = graphModel.getGraph();
            
            if (!graphModel.getNodeTable().hasColumn("background_map_node")){
                graphModel.getNodeTable().addColumn("background_map_node", Boolean.class);
            }
            
            if (!graphModel.getEdgeTable().hasColumn("background_map_edge")){
                graphModel.getEdgeTable().addColumn("background_map_edge", Boolean.class);
            }
            
            if (!graphModel.getNodeTable().hasColumn("lat")){
                graphModel.getNodeTable().addColumn("lat", Double.class);
            }
            
            if (!graphModel.getNodeTable().hasColumn("lng")){
                graphModel.getNodeTable().addColumn("lng", Double.class);
            }
            
            ShapeFileReader shapeFileReader = new ShapeFileReader();
            graph = shapeFileReader.read(graph, country, subregion, region, district);
            Iterator<Column> it = graphModel.getNodeTable().iterator();
            Column c;
            while (it.hasNext()) {
                c = it.next();
                if (c.getId().equalsIgnoreCase("lat") || c.getTitle().equalsIgnoreCase("lat")) {
                    latitude = c;
                } else if (c.getId().equalsIgnoreCase("lng") || c.getTitle().equalsIgnoreCase("lng")) {
                    longitude = c;
                }
            }
            List<Node> nodesList = new ArrayList();
            for (Node n : graph.getNodes().toArray()) {
                if (Boolean.TRUE.equals(n.getAttribute("background_map_node"))) {
                    nodesList.add(n);
                }
            }
            Node[] nodes = nodesList.toArray(new Node[nodesList.size()]);
            ArrayList<Node> validNodes = new ArrayList<Node>();
            ArrayList<Node> invalidNodes = new ArrayList<Node>();
            for (Node n : nodes) {
                if (n.getAttribute(latitude) != null && n.getAttribute(longitude) != null) {
                    if (Math.abs(((Number) n.getAttribute(longitude)).doubleValue()) == 180d) {
                        graph.removeNode(n);
                        continue;
                    }

                    if (Math.abs(((Number) n.getAttribute(latitude)).doubleValue()) == 90d) {
                        graph.removeNode(n);
                        continue;
                    }

                    validNodes.add(n);
                } else {
                    invalidNodes.add(n);
                }
            }
            for (Edge e : graph.getEdges().toArray()) {
                if (Boolean.TRUE.equals(e.getAttribute("background_map_edge"))) {
                    e.setWeight(this.weight);
                }
            }

            if (projection.equals("Mercator")) {
                double lambda0 = 0;

                //determine lambda0:
                for (Node n : validNodes) {
                    lon = ((Number) n.getAttribute(longitude)).doubleValue();
                    lambda0 += lon;
                }

                lambda0 = lambda0 / validNodes.size();
                lambda0 = Math.toRadians(lambda0);

                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                        n.setLayoutData(new GeoLayoutData());
                    }


                    lat = ((Number) n.getAttribute(latitude)).doubleValue();
                    lon = ((Number) n.getAttribute(longitude)).doubleValue();

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
            } // Transverse Mercator
            else if (projection.equals("Transverse Mercator")) {
                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                        n.setLayoutData(new GeoLayoutData());
                    }


                    lat = ((Number) n.getAttribute(latitude)).doubleValue();
                    lon = ((Number) n.getAttribute(longitude)).doubleValue();

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

                    

                    lat = ((Number) n.getAttribute(latitude)).doubleValue();
                    lon = ((Number) n.getAttribute(longitude)).doubleValue();

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

                    lat = ((Number) n.getAttribute(latitude)).doubleValue();
                    lon = ((Number) n.getAttribute(longitude)).doubleValue();

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
                    
                    lon = ((Number) n.getAttribute(longitude)).doubleValue();
                    lambda0 += lon;
                }

                lambda0 = lambda0 / validNodes.size();
                lambda0 = Math.toRadians(lambda0);

                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                        n.setLayoutData(new GeoLayoutData());
                    }

                    

                    lat = ((Number) n.getAttribute(latitude)).doubleValue();
                    lon = ((Number) n.getAttribute(longitude)).doubleValue();

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
                    
                    lat = ((Number) n.getAttribute(latitude)).doubleValue();
                    lon = ((Number) n.getAttribute(longitude)).doubleValue();
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

                    
                    lat = ((Number) n.getAttribute(latitude)).doubleValue();
                    lon = ((Number) n.getAttribute(longitude)).doubleValue();

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

                    
                    lat = ((Number) n.getAttribute(latitude)).doubleValue();
                    lon = ((Number) n.getAttribute(longitude)).doubleValue();

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
                double alpha = 0;

                //apply the formula:
                for (Node n : validNodes) {
                    if (n.getLayoutData() == null || !(n.getLayoutData() instanceof GeoLayoutData)) {
                        n.setLayoutData(new GeoLayoutData());
                    }

                    
                    lat = ((Number) n.getAttribute(latitude)).doubleValue();
                    lon = ((Number) n.getAttribute(longitude)).doubleValue();

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
                double xTemp = 0;
                double yTemp = 0;

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
            
            if (centered == true) {
                for (Node n : nodes) {
                    nodeX = n.x() - averageX;
                    nodeY = n.y() - averageY;

                    n.setX(nodeX);
                    n.setY(nodeY);
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
                    "getProjection", "setProjection", CustomComboBoxEditorForProjections.class));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.centered.name"),
                    GEOLAYOUT,
                    NbBundle.getMessage(GeoLayout.class, "GeoLayout.centered.desc"),
                    "isCentered", "setCentered"));
        } catch (MissingResourceException e) {
            Exceptions.printStackTrace(e);
        } catch (NoSuchMethodException e) {
            Exceptions.printStackTrace(e);
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
