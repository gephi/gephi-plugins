/**
 * Created by pmurray on 6/13/2017.
 */

package org.gephi.plugin.CirclePack;

import java.util.*;

import javafx.scene.shape.Circle;
import org.gephi.graph.api.*;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;


public class CirclePackLayout implements Layout {

    private static Object Bundle;
    //Architecture
    private final LayoutBuilder builder;
    private GraphModel graphModel;
    //Flags
    private boolean executing = false;
    //Properties
    private int radiusSize;
    private String  hierarchy1;
    private String  hierarchy2;
    private String  hierarchy3;
    private String  hierarchy4;
    private String  hierarchy5;

    public CirclePackLayout(CirclePackLayoutBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void resetPropertiesValues() {
        radiusSize = 1000;
    }

    @Override
    public void initAlgo() {
        executing = true;
    }


    @Override
    public void goAlgo() {
        Graph graph = graphModel.getGraphVisible();
        graph.readLock();
        int nodeCount = graph.getNodeCount();
        Node[] nodes = graph.getNodes().toArray();

        CircleWrap container = new CircleWrap();

        for(int i = 0; i < nodeCount; i++){
            Node node =  nodes[i];

            ArrayList attributes = new ArrayList<String>();
            if (this.hierarchy1 != null && this.hierarchy1 != "No Selection"){
                attributes.add(String.valueOf(node.getAttribute(this.hierarchy1)));
            }
            if (this.hierarchy2 != null && this.hierarchy2 != "No Selection"){
                attributes.add(String.valueOf(node.getAttribute(this.hierarchy2)));
            }
            if (this.hierarchy3 != null && this.hierarchy3 != "No Selection"){
                attributes.add(String.valueOf(node.getAttribute(this.hierarchy3)));
            }
            if (this.hierarchy4 != null && this.hierarchy4 != "No Selection"){
                attributes.add(String.valueOf(node.getAttribute(this.hierarchy4)));
            }
            if (this.hierarchy5 != null && this.hierarchy5 != "No Selection"){
                attributes.add(String.valueOf(node.getAttribute(this.hierarchy5)));
            }

            CircleWrap newCircleWrap = new CircleWrap((String) node.getId());
            newCircleWrap.r = node.size();

            CircleWrap parentContainer = container;
            for (Object attribute : attributes) {
                parentContainer = parentContainer.getChild((String) attribute);
            }
            parentContainer.addChild((String) node.getId(), newCircleWrap);
        }

        CirclePackAlgo PackAlgo = new CirclePackAlgo();

        PackAlgo.packHierarchyAndShift(container);
        setNode(graph, container);

        graph.readUnlock();
        endAlgo();
    }

    public void setNode(Graph graph, CircleWrap parentCircle){
        for (CircleWrap circle : parentCircle.children.values()) {
            if (circle.hasChildren()){
                setNode(graph, circle);
            } else {
                Node node = graph.getNode(circle.getId());
                node.setX((float) circle.x);
                node.setY((float) circle.y);
            }
        }
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
        final String CIRCLELAYOUT = "Circle Pack Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    "Hierarchy1",
                    "Hierarchy",
                     "Hierarchy1",
                    "getHierarchy1", "setHierarchy1", LayoutComboBoxEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    "Hierarchy2",
                    "Hierarchy",
                     "Hierarchy2",
                    "getHierarchy2", "setHierarchy2", LayoutComboBoxEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    "Hierarchy3",
                    "Hierarchy",
                    "Hierarchy3",
                    "getHierarchy3", "setHierarchy3", LayoutComboBoxEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    "Hierarchy4",
                    "Hierarchy",
                    "Hierarchy4",
                    "getHierarchy4", "setHierarchy4", LayoutComboBoxEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    "Hierarchy5",
                    "Hierarchy",
                    "Hierarchy5",
                    "getHierarchy5", "setHierarchy5", LayoutComboBoxEditor.class));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void setGraphModel(GraphModel gm) {
        this.graphModel = gm;
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    public String getHierarchy1()
    {
        return this.hierarchy1;
    }

    public void setHierarchy1(String attribute)
    {
        this.hierarchy1 = attribute;
    }

    public String getHierarchy2()
    {
        return this.hierarchy2;
    }

    public void setHierarchy2(String attribute)
    {
        this.hierarchy2 = attribute;
    }

    public void setHierarchy3(String attribute)
    {
        this.hierarchy3 = attribute;
    }

    public String getHierarchy3()
    {
        return this.hierarchy3;
    }

    public void setHierarchy4(String attribute)
    {
        this.hierarchy4 = attribute;
    }

    public String getHierarchy4()
    {
        return this.hierarchy4;
    }

    public void setHierarchy5(String attribute)
    {
        this.hierarchy5 = attribute;
    }

    public String getHierarchy5()
    {
        return this.hierarchy5;
    }

}
