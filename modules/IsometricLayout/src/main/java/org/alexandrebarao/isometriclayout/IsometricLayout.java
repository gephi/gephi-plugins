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
package org.alexandrebarao.isometriclayout;

/**
 *
 * @author Alexandre Barão (IsometricLayout Algorithm for Gephi API 0.9.1)
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

public class IsometricLayout implements Layout {

    // Global isometric layout settings
    public static final String ISOMETRIC_Z_VALUE = "ISOMETRIC_Z_VALUE";

    // grid origin (may be used for smooth translation)
    float xOrigin = 0f;
    float yOrigin = 0f;

    // Layout properties
    private int zlevels = 0;
    private int zdistance = 1;
    private float horizontalScale = 100f;
    private float verticalScale = getHorizontalScale() * 0.577f; // tan 30º (isometric 3D system)
    private boolean horizontal = false;
    private boolean reverse = false;

    // Data Laboratory
    private Column columnZ;
    private Column colResult;

    //Architecture
    private final LayoutBuilder builder;
    private GraphModel graphModel;

    //Flags
    private boolean executing = false;

    public IsometricLayout(IsometricLayoutBuilder builder) {
        this.builder = builder;
    }

    // 3D to 2D 
    float xGridToScreen(float xg, float yg) {
        return ((xOrigin + xg * getHorizontalScale() + yg * getHorizontalScale()));
    }

    // 3D to 2D 
    float yGridToScreen(float xg, float yg, float zg) {

        return ((yOrigin - yg * getVerticalScale() + xg * getVerticalScale() - zg * 2f * getVerticalScale()));
    }

    @Override
    public void resetPropertiesValues() {
        zlevels = 0;
        zdistance = 10;
        horizontalScale = 100f;
        verticalScale = getHorizontalScale() * 0.577f; // tan 30º (isometric 3D system)
        setHorizontal(false);
        setReverse(false);

    }

    @Override
    public void initAlgo() {
        executing = true;
    }

    @Override
    public void goAlgo() {

        Graph graph = graphModel.getGraphVisible();

        // Try to find an updated column [z] in data table (nodes).
        columnZ = null;

        Table nodeTable = graph.getModel().getNodeTable();

        for (int i = 0; i < nodeTable.countColumns(); i++) {

            Column c = nodeTable.getColumn(i);

            if (c.getTitle().toLowerCase().contains("[z]")) {
                columnZ = c;
                break;
            }
        }

        // Look if the result column already exist and create it if needed.
        // Results may be used later in partition and ranking procedures.
        colResult = nodeTable.getColumn(ISOMETRIC_Z_VALUE);
        if (colResult == null) {
            colResult = nodeTable.addColumn(ISOMETRIC_Z_VALUE, "Computed Z-Level", Integer.class, 0);
        }

        graph.readLock();

        // Iterate on all nodes
        int nodeCount = graph.getNodeCount();

        Node[] nodes = graph.getNodes().toArray();

        // Detect maximum value of the user-defined [z] column.
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

        // Compute z-level of each node and store it in the Gephi Data Laboratory. 
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

        // Detect how many isometric x,y grid scales are needed (z grid scales are on properties user demand)
        int xGridScales = (int) Math.round(Math.sqrt(nodeCount)) + 1;
        int yGridScales = (int) Math.round(Math.sqrt(nodeCount)) + 1;

        // Draw isometric network (splitting layers if needed)
        int k = 0;
        for (int i = 0; i < xGridScales; i++) {

            for (int j = 0; j < yGridScales; j++) {

                if (k < nodeCount) {
                    Node node = nodes[k];

                    int zGrid = 0; // 3D height of each node (z-axis)

                    if (colResult != null) {
                        zGrid = (Integer) (node.getAttribute(colResult));
                        zGrid *= zdistance;
                    }

                    if (isHorizontal()) {
                        node.setX(yGridToScreen(i, j, (float) ((isReverse() ? zGrid : maxZLevel - zGrid))));
                        node.setY(xGridToScreen(i, j));
                    } else {
                        node.setX(xGridToScreen(i, j));
                        node.setY(yGridToScreen(i, j, (float) ((isReverse() ? zGrid : maxZLevel - zGrid))));
                    }

                    ++k;
                }
            }

        }

        graph.readUnlock();
        endAlgo();

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

    @Override
    public void endAlgo() {
        executing = false;
    }

    @Override
    public boolean canAlgo() {
        return executing;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String ISOMETRICLAYOUT = "Isometric Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    "Z-Maximum Level",
                    ISOMETRICLAYOUT,
                    "Z-maximum levels to compute node clusters. Node column '[z]' is needed in Gephi Data Laboratory.",
                    "getZlevels", "setZlevels"));

            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    "Z-Distance",
                    ISOMETRICLAYOUT,
                    "Layer distance between each Z-Level layout. Use 'Z' units.",
                    "getZdistance", "setZdistance"));

            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    "Scale",
                    ISOMETRICLAYOUT,
                    "The isometric grid scale (pixels).",
                    "getHorizontalScale", "setHorizontalScale"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    "Horizontal Z-Axis",
                    ISOMETRICLAYOUT,
                    "Execute layout with Z-Axis horizontal.",
                    "isHorizontal", "setHorizontal"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    "Reverse 0-Level Origin",
                    ISOMETRICLAYOUT,
                    "0-Level is showed on: top layer (vertical layout); or right layer (horizontal layout).",
                    "isReverse", "setReverse"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    @Override
    public void setGraphModel(GraphModel gm) {
        this.graphModel = gm;
    }

    /**
     * @return the scale
     */
    public Float getHorizontalScale() {
        return horizontalScale;
    }

    /**
     * @param scale the scale to set
     */
    public void setHorizontalScale(Float scale) {

        this.horizontalScale = scale;
        setVerticalScale(horizontalScale * 0.577f); // tan 30º

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
     * @return the horizontal
     */
    public Boolean isHorizontal() {
        return horizontal;
    }

    /**
     * @param horizontal the horizontal to set
     */
    public void setHorizontal(Boolean horizontal) {
        this.horizontal = horizontal;
    }

    /**
     * @return the reverse
     */
    public Boolean isReverse() {
        return reverse;
    }

    /**
     * @param reverse the reverse to set
     */
    public void setReverse(Boolean reverse) {
        this.reverse = reverse;
    }

}
