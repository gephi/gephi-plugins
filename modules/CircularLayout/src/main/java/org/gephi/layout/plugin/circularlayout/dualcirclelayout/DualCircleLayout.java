/*
 Copyright (c) 2010, Matt Groeninger
 All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this list of
 conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice, this list
 of conditions and the following disclaimer in the documentation and/or other materials
 provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gephi.layout.plugin.circularlayout.dualcirclelayout;

import java.util.*;
import javax.swing.JOptionPane;
import org.gephi.graph.api.*;
import org.gephi.graph.spi.LayoutData;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.plugin.circularlayout.nodecomparator.NodeComparator;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Matt Groeninger
 */
public class DualCircleLayout extends AbstractLayout implements Layout {

    private Graph graph;
    private boolean converged;
    private boolean highdegreeoutside;
    private int secondarynodecount;
    static double TWO_PI = (2 * Math.PI);
    private String strNodePlacementDirection;
    private String attribute;
    private Double intSteps = 1.0;
    private boolean boolTransition = true;

    public static Map getAttributeMap() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel objGraphModel = graphController.getGraphModel();
        Map<String, String> map = new TreeMap<String, String>();
        map.put("NodeID", NbBundle.getMessage(DualCircleLayout.class, "DualCircleLayout.NodePlacement.NodeID.name"));
        map.put("Degree", NbBundle.getMessage(DualCircleLayout.class, "DualCircleLayout.NodePlacement.Degree.name"));
        if (objGraphModel.isDirected()) {
            map.put("InDegree", NbBundle.getMessage(DualCircleLayout.class, "DualCircleLayout.NodePlacement.InDegree.name"));
            map.put("OutDegree", NbBundle.getMessage(DualCircleLayout.class, "DualCircleLayout.NodePlacement.OutDegree.name"));
            map.put("MutualDegree", NbBundle.getMessage(DualCircleLayout.class, "DualCircleLayout.NodePlacement.Mutual.name"));
        }
        for (Column c : objGraphModel.getNodeTable()) {
            map.put(c.getTitle() + "-Att", c.getTitle() + " (Attribute)");
        }
        return map;
    }

    public static Map getRotationMap() {
        Map<String, String> map = new TreeMap<String, String>();
        map.put("CCW", NbBundle.getMessage(DualCircleLayout.class, "DualCircleLayout.NodePlacement.CCW"));
        map.put("CW", NbBundle.getMessage(DualCircleLayout.class, "DualCircleLayout.NodePlacement.CW"));
        return map;
    }

    public DualCircleLayout(LayoutBuilder layoutBuilder, int secondarynodecount) {
        super(layoutBuilder);
        this.secondarynodecount = secondarynodecount;
    }

    @Override
    public void initAlgo() {
        converged = false;
        graph = graphModel.getGraphVisible();
        float[] nodeCoords = new float[2];
        double tmpsecondarycirc = 0;
        double tmpprimarycirc = 0;
        int index = 0;
        double twopi = TWO_PI;
        double lasttheta = 0;
        double primary_theta = 0;
        double secondary_theta = 0;
        double primary_scale = 1;
        double secondary_scale = 1;
        double correct_theta = 0;
        if (this.strNodePlacementDirection.equals("CW")) {
            twopi = -twopi;
        }
        Node[] nodes = graph.getNodes().toArray();

        if (this.attribute.equals("NodeID")) {
            Arrays.sort(nodes, new NodeComparator(graph, nodes, NodeComparator.CompareType.NODEID, null, false));
        } else if (this.attribute.endsWith("-Att")) {
            Arrays.sort(nodes, new NodeComparator(graph, nodes, NodeComparator.CompareType.ATTRIBUTE, this.attribute.substring(0, this.attribute.length() - 4), false));
        } else if (getAttributeMap().containsKey(this.attribute)) {
            Arrays.sort(nodes, new NodeComparator(graph, nodes, NodeComparator.CompareType.METHOD, this.attribute, false));
        }

        for (Node n : nodes) {
            if (!n.isFixed()) {
                if (index < this.secondarynodecount) {
                    tmpsecondarycirc += (n.size() * 2);
                } else {
                    tmpprimarycirc += (n.size() * 2);
                }
                index++;
            }
        }
        index = 0;//reset index

        double circum_ratio = tmpprimarycirc / tmpsecondarycirc;

        if (circum_ratio < 2) {
            primary_scale = (2 / circum_ratio);
            tmpprimarycirc = 2 * tmpsecondarycirc;
        }

        if (this.isHighDegreeOutside()) {
            secondary_scale = ((2 * tmpprimarycirc) / tmpsecondarycirc); //Need to know how much the circumference has changed from the original
            tmpsecondarycirc = tmpprimarycirc * 2; //Scale to a better relationship
        } else {
            secondary_scale = (tmpprimarycirc / (2 * tmpsecondarycirc)); //Need to know how much the circumference has changed from the original
            tmpsecondarycirc = tmpprimarycirc / 2; //Scale to a better relationship
        }

        tmpprimarycirc = tmpprimarycirc * 1.2;
        primary_theta = (twopi / tmpprimarycirc);
        double primaryradius = (tmpprimarycirc / Math.PI) / 2;


        tmpsecondarycirc = tmpsecondarycirc * 1.2;
        secondary_theta = (twopi / tmpsecondarycirc);
        double secondaryradius = (tmpsecondarycirc / Math.PI) / 2;

        for (Node n : nodes) {
            TempLayoutData posData = new TempLayoutData();
            if (!n.isFixed()) {
                if (index < this.secondarynodecount) {
                    //Draw secondary circle
                    double noderadius = (n.size());
                    //This step is hackish... but it makes small numbers of nodes symetrical on both the secondary circles.
                    if (secondary_scale > 2) {
                        noderadius = (tmpsecondarycirc / (2 * this.secondarynodecount * secondary_scale * 1.2));
                    }
                    double noderadian = (secondary_theta * noderadius * 1.2 * secondary_scale);
                    if (index == 0) {
                        correct_theta = noderadian; //correct for cosmetics... overlap prevention offsets the first node by it's radius which looks weird.
                    }
                    nodeCoords = this.cartCoors(secondaryradius, 1, lasttheta + noderadian - correct_theta);
                    lasttheta += (noderadius * 2 * secondary_theta * 1.2 * secondary_scale);
                } else {
                    double noderadius = (n.size());
                    double noderadian = (primary_theta * noderadius * 1.2 * primary_scale);
                    if (index == this.secondarynodecount) {
                        lasttheta = 0;
                        correct_theta = noderadian; //correct for cosmetics... overlap prevention offsets the first node by it's radius which looks weird.
                    }
                    //Draw primary circle
                    nodeCoords = this.cartCoors(primaryradius, 1, lasttheta + noderadian - correct_theta);
                    lasttheta += (noderadius * 2 * primary_theta * 1.2 * primary_scale);
                }
                posData.finishx = nodeCoords[0];
                posData.finishy = nodeCoords[1];
                index++;
            } else {
                posData.finishx = n.x();
                posData.finishy = n.y();
            }
            posData.xdistance = (float) (1 / intSteps) * (nodeCoords[0] - n.x());
            posData.ydistance = (float) (1 / intSteps) * (nodeCoords[1] - n.y());
            n.setLayoutData(posData);
        }
    }

    @Override
    public void goAlgo() {
        converged = true;
        TempLayoutData position = null;
        Node[] nodes = graph.getNodes().toArray();
        for (Node n : nodes) {
            if (n.getLayoutData() != null) {
                position = n.getLayoutData();
                if (boolTransition) {
                    float currentDistance = Math.abs(n.x() - position.finishx);
                    float nextDistance = Math.abs(n.x() + position.xdistance - position.finishx);
                    if (nextDistance < currentDistance) {
                        n.setX(n.x() + position.xdistance);
                        converged = false;
                    } else {
                        n.setX(position.finishx);
                    }
                    currentDistance = Math.abs(n.y() - position.finishy);
                    nextDistance = Math.abs(n.y() + position.ydistance - position.finishy);
                    if (nextDistance < currentDistance) {
                        n.setY(n.y() + position.ydistance);
                        converged = false;
                    } else {
                        n.setY(position.finishy);
                    }
                    if (n.y()==position.finishy && n.x()==position.finishx) {
                        n.setLayoutData(null);
                    }
                } else {
                    n.setX(position.finishx);
                    n.setY(position.finishy);
                    n.setLayoutData(null);

                }
            }
        }
    }

    @Override
    public boolean canAlgo() {
        return !converged;
    }

    @Override
    public void endAlgo() {
    }

    @Override
    public LayoutProperty[] getProperties() {
        final String TRANSITION_CATEGORY = NbBundle.getMessage(getClass(), "DualCircleLayout.Category.Transition.name");

        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        try {
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "DualCircleLayout.HighDegreeOutside.name"),
                    NbBundle.getMessage(getClass(), "DualCircleLayout.Category.NodePlacement.name"),
                    NbBundle.getMessage(getClass(), "DualCircleLayout.HighDegreeOutside.desc"),
                    "isHighDegreeOutside", "setHighDegreeOutside"));
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    NbBundle.getMessage(getClass(), "DualCircleLayout.InnerNodeCount.name"),
                    NbBundle.getMessage(getClass(), "DualCircleLayout.Category.NodePlacement.name"),
                    NbBundle.getMessage(getClass(), "DualCircleLayout.InnerNodeCount.desc"),
                    "getInnerNodeCount", "setInnerNodeCount"));
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(getClass(), "DualCircleLayout.attribue.name"),
                    NbBundle.getMessage(getClass(), "DualCircleLayout.Category.Sorting.name"),
                    NbBundle.getMessage(getClass(), "DualCircleLayout.attribue.desc"),
                    "getAttribute", "setAttribute", LayoutComboBoxEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(getClass(), "DualCircleLayout.NodePlacement.Direction.name"),
                    NbBundle.getMessage(getClass(), "DualCircleLayout.Category.NodePlacement.name"),
                    NbBundle.getMessage(getClass(), "DualCircleLayout.NodePlacement.Direction.desc"),
                    "getNodePlacementDirection", "setNodePlacementDirection", RotationComboBoxEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "DualCircleLayout.Transition.EnableTransition.name"),
                    TRANSITION_CATEGORY,
                    NbBundle.getMessage(getClass(), "DualCircleLayout.Transition.EnableTransition.desc"),
                    "isNodePlacementTransition", "setNodePlacementTransition"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    NbBundle.getMessage(getClass(), "DualCircleLayout.Transition.TransitionSteps.name"),
                    TRANSITION_CATEGORY,
                    NbBundle.getMessage(getClass(), "DualCircleLayout.Transition.TransitionSteps.desc"),
                    "getTransitionSteps", "setTransitionSteps"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetPropertiesValues() {
        setInnerNodeCount(4);
        setHighDegreeOutside(false);
        setNodePlacementDirection("CCW");
        setAttribute("NodeID");
        setNodePlacementTransition(false);
        setTransitionSteps(100000.0);
    }

    public void setInnerNodeCount(Integer intsecondarynodecount) {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel objGraphModel = graphController.getGraphModel();
        graph = objGraphModel.getGraphVisible();
        if (intsecondarynodecount > graph.getNodeCount()) {
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(getClass(), "DualCircleLayout.setInnerNodeCount.TooHigh.message"),
                    NbBundle.getMessage(getClass(), "DualCircleLayout.setInnerNodeCount.TooHigh.title"),
                    JOptionPane.WARNING_MESSAGE);
        } else if (intsecondarynodecount < 1) {
            JOptionPane.showMessageDialog(null,
                    NbBundle.getMessage(getClass(), "DualCircleLayout.setInnerNodeCount.TooLow.message"),
                    NbBundle.getMessage(getClass(), "DualCircleLayout.setInnerNodeCount.TooLow.title"),
                    JOptionPane.WARNING_MESSAGE);
        } else {
            //TODO: add node count check to do boundary checking on user input
            this.secondarynodecount = intsecondarynodecount;
        }
    }

    public Integer getInnerNodeCount() {
        return secondarynodecount;
    }

    public Boolean isHighDegreeOutside() {
        return highdegreeoutside;
    }

    public void setHighDegreeOutside(Boolean highdegreeoutside) {
        this.highdegreeoutside = highdegreeoutside;
    }

    public String getNodePlacementDirection() {
        return this.strNodePlacementDirection;
    }

    public void setNodePlacementDirection(String strNodePlacementDirection) {
        this.strNodePlacementDirection = strNodePlacementDirection;
    }

    public String getAttribute() {
        return this.attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public boolean isNodePlacementTransition() {
        return boolTransition;
    }

    public void setNodePlacementTransition(Boolean boolTransition) {
        this.boolTransition = boolTransition;
    }

    public Double getTransitionSteps() {
        return intSteps;
    }

    public void setTransitionSteps(Double steps) {
        intSteps = steps;
    }

    private float[] cartCoors(double radius, int whichInt, double theta) {
        float[] coOrds = new float[2];
        coOrds[0] = (float) (radius * (Math.cos((theta * whichInt) + (Math.PI / 2))));
        coOrds[1] = (float) (radius * (Math.sin((theta * whichInt) + (Math.PI / 2))));
        return coOrds;
    }
}

class TempLayoutData implements LayoutData {

    public float finishx = 0;
    public float finishy = 0;
    public float xdistance = 0;
    public float ydistance = 0;
}
