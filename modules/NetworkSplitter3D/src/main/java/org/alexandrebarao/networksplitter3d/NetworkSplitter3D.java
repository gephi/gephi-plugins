/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.alexandrebarao.networksplitter3d;

/**
 *
 * @author Alexandre Barão (Network Splitter 3D - for Gephi API 0.9.1)
 *
 */
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Lookup;

public class NetworkSplitter3D implements Layout {

    // Network Splitter 3D Data Structures & Settings
    private List<Node3D> nodes3D;

    private boolean firstTime;

    public static final String SPLITTER_Z_VALUE = "SPLITTER_Z_VALUE";

    // Layout properties 
    private int zlevels = 0;
    private double alfa = 65;
    private int zdistance = 10;
    private float verticalScale = 100f;

    // Gephi Data Laboratory
    private Column columnZ;
    private Column colResult;

    //Architecture
    private final LayoutBuilder builder;
    private GraphModel graphModel;

    //Flags
    private boolean executing = false;

    public NetworkSplitter3D(NetworkSplitter3DBuilder builder) {
        this.builder = builder;

        // Network Splitter 3D
        nodes3D = new ArrayList<Node3D>();

        firstTime = true;

    }

    @Override
    public void resetPropertiesValues() {

        if (graphModel != null) {
            Graph graph = graphModel.getGraphVisible();
            graph.readLock();

            for (Node3D p : nodes3D) {

                // safe reset node coordinates to the previous state 
                Node detectedNode = searchSavedNode(graph, p.getId());

                if (detectedNode != null) {
                    detectedNode.setX(p.getX());
                    detectedNode.setY(p.getY());
                    detectedNode.setZ(p.getZ());
                }

            }
            graph.readUnlockAll();

        }

        firstTime = true;

        nodes3D = new ArrayList<Node3D>();

        zlevels = 0;
        zdistance = 10;
        verticalScale = 100f;
        setAlfa(65.0);

    }

    @Override
    public void initAlgo() {
        executing = true;
    }

    @Override
    public void goAlgo() {

        Graph graph = graphModel.getGraphVisible();
        graph.readLock();

        ////////////////////////////////////////////////////
        // Store X,Y,Z coordinates of the current layout
        ////////////////////////////////////////////////////
        if (firstTime) {
            for (Node n : graph.getNodes()) {
                Node3D p = new Node3D(n.getStoreId(), n.x(), n.y(), n.z());
                nodes3D.add(p);
            }
            firstTime = false;
        }

        int nodeCount = graph.getNodeCount();

        Node[] nodes = graph.getNodes().toArray();

        /////////////////////////////////////////////////////////////////////////
        // Try to find an updated column [z] in data table (nodes).
        /////////////////////////////////////////////////////////////////////////
        columnZ = null;

        Table nodeTable = graph.getModel().getNodeTable();

        for (int i = 0; i < nodeTable.countColumns(); i++) {

            Column c = nodeTable.getColumn(i);

            if (c.getTitle().toLowerCase().contains("[z]")) {
                columnZ = c;
                break;
            }
        }

        /////////////////////////////////////////////////////////////////////////
        // Look if the result column already exist and create it if needed.
        // Results may be used later in Gephi partition and ranking procedures.
        /////////////////////////////////////////////////////////////////////////
        colResult = nodeTable.getColumn(SPLITTER_Z_VALUE);
        if (colResult == null) {
            colResult = nodeTable.addColumn(SPLITTER_Z_VALUE, "Splitter Z-Level", Integer.class, 0);
        }

        ////////////////////////////////////////////////////////////
        // Detect maximum value of the user-defined [z] column.
        ////////////////////////////////////////////////////////////
        double maxZvalueFromColumnZ = 0;

        for (int i = 0; i < nodeCount; i++) {
            Node n = nodes[i];

            if (columnZ != null) {
                double zV;
                zV = getDataLaboratoryValue(n, columnZ);

                if (zV > maxZvalueFromColumnZ) {
                    maxZvalueFromColumnZ = zV;
                }
            }
        }

        //////////////////////////////////////////////////////////////////////////
        // Compute z-level of each node and store it in the Gephi Data Laboratory. 
        //////////////////////////////////////////////////////////////////////////
        int maxZLevel = 0;
        for (int i = 0; i < nodeCount; i++) {
            Node n = nodes[i];
            double zV = 0f;
            if (columnZ != null) {
                zV = getDataLaboratoryValue(n, columnZ);
            }
            int z_level = 0;
            if (maxZvalueFromColumnZ != 0) {
                z_level = (int) Math.round((((zV * (double) (zlevels) / maxZvalueFromColumnZ))));
                if (z_level > maxZLevel) {
                    maxZLevel = z_level;
                }
            }

            n.setAttribute(colResult, z_level);

        }

        ////////////////////////////////////////////////////////
        // Execute Network Splitter 3D Layout
        ////////////////////////////////////////////////////////
        for (Node n : nodes) {

            double newX, newY, newZ;

            Node3D n3D = searchNode3D(n.getStoreId());

            if (n3D != null) {

                // convert degrees to radians
                double angle = getAlfa() * (Math.PI / 180.0);

                // graph rotation 
                newX = n3D.getX();
                newY = n3D.getY() * Math.cos(angle) - n3D.getZ() * Math.sin(angle);
                newZ = n3D.getZ();

                // search node Z-Level 
                int zLevel = 0;

                if (colResult != null) {
                    zLevel = (Integer) (n.getAttribute(colResult));
                }

                // update new node coordinates 
                newY = newY + (zLevel * getZdistance() * getVerticalScale());

                n.setX((float) newX);
                n.setY((float) newY);
                n.setZ((float) newZ);
            }

        }

        graph.readUnlockAll();

        endAlgo();
    }

    @Override
    public void endAlgo() {
        executing = false;
        Graph graph = graphModel.getGraphVisible();

        for (Node n : graph.getNodes()) {
            n.setLayoutData(null);
        }

        graph.readUnlockAll();

    }

    @Override
    public boolean canAlgo() {
        return executing;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String NETWORKSPLITTER3D = "Network Splitter 3D";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    "Z-Maximum Level",
                    NETWORKSPLITTER3D,
                    "Z-maximum levels to compute node clusters. Node column '[z]' is needed in Gephi Data Laboratory. Note: you can use the RESET button to layout your original network.",
                    "getZlevels", "setZlevels"));

            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    "Z-Distance Factor",
                    NETWORKSPLITTER3D,
                    "Layer distance between each Z-Level layout. Used to multiply Z-Scale. Note: you can use the RESET button to layout your original network.",
                    "getZdistance", "setZdistance"));

            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    "Z-Scale",
                    NETWORKSPLITTER3D,
                    "The vertical Z-Scale (pixels).  Note: you can use the RESET button to layout your original network.",
                    "getVerticalScale", "setVerticalScale"));

            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    "Alfa",
                    NETWORKSPLITTER3D,
                    "The rotation angle (degrees from 0 to 360). Note: you can use the RESET button to layout your original network.",
                    "getAlfa", "setAlfa"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    public double getDataLaboratoryValue(Node n, Column col) {

        double z = 0f;

        if (n.getAttribute(col) instanceof Number) {

            z = ((Number) (n.getAttribute(col))).doubleValue();

        } else if (col.getTypeClass() == String.class) {

            try {
                z = Double.parseDouble(((String) (n.getAttribute(col))).trim());
            } catch (NumberFormatException nfe) {
                Logger.getLogger("").log(Level.SEVERE, "NumberFormatException from data laboratory cell: {0}", nfe.getMessage());
            }
        }

        return z;
    }

    public Node searchSavedNode(Graph graph, int id) {
        Node n = null;

        int nodeK = 0;

        if (graph != null) {
            while (nodeK < graph.getNodeCount()) {

                for (Node currentNode : graph.getNodes()) {
                    if (currentNode.getStoreId() == id) {
                        n = currentNode;
                        nodeK = graph.getNodeCount();
                    }
                }

                ++nodeK;
            }
        }
        return n;
    }

    public Node3D searchNode3D(int id) {
        Node3D n3D = null;

        int nodeK = 0;

        if (nodes3D != null) {
            while (nodeK < nodes3D.size()) {

                Node3D currentNode = nodes3D.get(nodeK);

                if (currentNode.getId() == id) {
                    n3D = currentNode;
                    nodeK = nodes3D.size();
                }

                ++nodeK;
            }
        }
        return n3D;
    }

    // Network Splitter 3D "getters" and "setters"  
    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    @Override
    public void setGraphModel(GraphModel gm) {
        this.graphModel = gm;
    }

    /**
     * @return the zLevels
     */
    public Integer getZlevels() {
        return zlevels;
    }

    /**
     * @param zLevels the zLevels to set
     */
    public void setZlevels(Integer zLevels) {
        this.zlevels = zLevels;
    }

    /**
     * @return the verticalScale
     */
    public Float getVerticalScale() {
        return verticalScale;
    }

    /**
     * @param verticalScale the verticalScale to set
     */
    public void setVerticalScale(Float verticalScale) {
        this.verticalScale = verticalScale;

    }

    /**
     * @return the zdistance
     */
    public Integer getZdistance() {
        return zdistance;
    }

    /**
     * @param zdistance the zdistance to set
     */
    public void setZdistance(Integer zdistance) {
        this.zdistance = zdistance;
    }

    /**
     * @return the alfa
     */
    public Double getAlfa() {
        return alfa;
    }

    /**
     * @param alfa the alfa to set
     */
    public void setAlfa(Double alfa) {
        this.alfa = alfa;
    }

}
