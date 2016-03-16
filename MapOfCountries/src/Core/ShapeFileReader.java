/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.openide.util.Lookup;

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
public class ShapeFileReader {

    private Map<String, String> regionCodes = new HashMap();
    private Map<String, String> subregionCodes = new HashMap();
    private Map<String, String> districtCodes = new HashMap();

    public Graph read(Graph gr, String country, String subregion, String region, String district) throws IOException {

        regionsCodeLoader();
        subregionsCodeLoader();
        districtsCodeLoader();

        //removing previous map
        for (Node n : gr.getNodes().toArray()) {
            if (n.getAttributes().getValue("background_map") == null) {
                continue;
            } else if (n.getAttributes().getValue("background_map").equals(true)) {
                gr.removeNode(n);
            }
        }

        InputStream shapes = ShapeFileReader.class.getResourceAsStream("/resources/coordinates.txt");
        InputStream shapesDistricts = ShapeFileReader.class.getResourceAsStream("/resources/Australian states.csv");
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel gm = gc.getModel();

        gr = gm.getMixedGraph();

        String zone;

        Node node;
        Edge edge;

        BufferedReader br = new BufferedReader(new InputStreamReader(shapes));
        BufferedReader brDistricts = new BufferedReader(new InputStreamReader(shapesDistricts));

        String line;
        while ((line = br.readLine()) != null) {
            String[] fields = line.split("\t");
            String countryName = fields[0].split(":")[1];
            String regionName = fields[1].split(":")[1];
            String subregionName = fields[2].split(":")[1];
            System.out.println("country Name: \"" + countryName + "\"");

            if (countryName == null || countryName.isEmpty()) {
                continue;
            }
            if (!subregion.equals("No subregion")) {
                if (!subregionName.equals(subregionCodes.get(subregion))) {
                    continue;
                }
            } else if (!region.equals("No region")) {
                if (!regionName.equals(regionCodes.get(region))) {
                    continue;
                }
                if (region.equals("Europe (without Russia)")) {
                    if (countryName.equals("Russia")) {
                        continue;
                    }
                }
            } else {
                if (!country.equals("World") & !countryName.equals(country)) {
                    continue;
                }
            }
            if (!district.equals("No district")) {
                continue;
            }

            for (int i = 3; i < fields.length; i++) {
                if (fields[i].startsWith("node")) {
                    String nodeId = fields[i].split(":")[0].replace("node", "").trim();
                    String lat = fields[i].split(":")[1].split(" ")[1].trim();
                    String lng = fields[i].split(":")[1].split(" ")[0].trim();
                    node = gm.factory().newNode(nodeId);
                    node.getNodeData().setSize(0f);
                    node.getAttributes().setValue("background_map", true);
                    node.getAttributes().setValue("lat", Double.valueOf(lat));
                    node.getAttributes().setValue("lng", Double.valueOf(lng));
                    node.getNodeData().setFixed(true);
                    gr.addNode(node);
                }
                if (fields[i].startsWith("edge")) {
                    String sourceNode = fields[i].trim().split(":")[1].trim().split(" ")[0].trim();
                    String targetNode = fields[i].trim().split(":")[1].trim().split(" ")[1].trim();
                    edge = gm.factory().newEdge(gr.getNode(sourceNode), gr.getNode(targetNode), 1f, false);
                    gr.addEdge(edge);
                }
            }
        }
        br.close();

        if (district.equals("No district")) {
            return gr;
        }

        while ((line = brDistricts.readLine()) != null) {
            String[] fields = line.split("\t");
            String countryName = fields[0].split(":")[1];
            String regionName = fields[1].split(":")[1];
            String subregionName = fields[2].split(":")[1];
            String districtName = fields[3].split(":")[1];
            System.out.println("country Name: \"" + countryName + "\"");

            if (!districtName.equals(districtCodes.get(district))) {
                continue;
            }

            for (int i = 4; i < fields.length; i++) {
                if (fields[i].startsWith("node")) {
                    String nodeId = fields[i].split(":")[0].replace("node", "").trim();
                    String lat = fields[i].split(":")[1].split(" ")[1].trim();
                    String lng = fields[i].split(":")[1].split(" ")[0].trim();
                    node = gm.factory().newNode(nodeId);
                    node.getNodeData().setSize(0f);
                    node.getAttributes().setValue("background_map", true);
                    node.getAttributes().setValue("lat", Double.valueOf(lat));
                    node.getAttributes().setValue("lng", Double.valueOf(lng));
                    node.getNodeData().setFixed(true);
                    gr.addNode(node);
                }
                if (fields[i].startsWith("edge")) {
                    String sourceNode = fields[i].trim().split(":")[1].trim().split(" ")[0].trim();
                    String targetNode = fields[i].trim().split(":")[1].trim().split(" ")[1].trim();
                    edge = gm.factory().newEdge(gr.getNode(sourceNode), gr.getNode(targetNode), 1f, false);
                    gr.addEdge(edge);
                }
            }
        }
        brDistricts.close();
        return gr;

    }

    private void regionsCodeLoader() {

        regionCodes.put("Africa", "2");
        regionCodes.put("Americas", "19");
        regionCodes.put("Asia", "142");
        regionCodes.put("Europe", "150");
        regionCodes.put("Europe (without Russia)", "150");
        regionCodes.put("Oceania", "9");

    }

    private void districtsCodeLoader() {

        districtCodes.put("Australia_New South Wales", "1");
        districtCodes.put("Australia_Victoria", "2");
        districtCodes.put("Australia_Queensland", "3");
        districtCodes.put("Australia_South Australia", "4");
        districtCodes.put("Australia_Western Australia", "5");
        districtCodes.put("Australia_Tasmania", "6");
        districtCodes.put("Australia_Northern Territory", "7");
        districtCodes.put("Australia_Australian Capital Territory", "8");
        districtCodes.put("Australia_Other Territories", "9");

    }

    private void subregionsCodeLoader() {

        subregionCodes.put("Eastern Africa", "14");
        subregionCodes.put("Middle Africa", "17");
        subregionCodes.put("Northern Africa", "15");
        subregionCodes.put("Southern Africa", "18");
        subregionCodes.put("Western Africa", "11");

        subregionCodes.put("Caribbean", "29");
        subregionCodes.put("Central America", "13");
        subregionCodes.put("South America", "5");

        subregionCodes.put("Northern America", "21");

        subregionCodes.put("Central Asia", "143");
        subregionCodes.put("Eastern Asia", "30");
        subregionCodes.put("Southern Asia", "34");
        subregionCodes.put("South-Eastern Asia", "35");
        subregionCodes.put("Western Asia", "145");

        subregionCodes.put("Eastern Europe", "151");
        subregionCodes.put("Northern Europe", "154");
        subregionCodes.put("Southern Europe", "39");
        subregionCodes.put("Western Europe", "155");

        subregionCodes.put("Australia and New Zealand", "53");
        subregionCodes.put("Melanesia", "54");
        subregionCodes.put("Micronesia", "57");
        subregionCodes.put("Polynesia", "61");

    }
}
