/*
Copyright 2008-2011 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
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
/cddl-1.0.txt and /gpl-3.0.txt.
Portions Copyrighted 2011 Gephi Consortium.

Updated by Daniel Iachan 2017, for Gephi 0.9.1
 */
package org.iachan.polygonshapednodes;

import java.awt.*;
import org.gephi.graph.api.Node;
import org.gephi.preview.api.*;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.renderers.NodeRenderer;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * Extends and replaces default node renderer and implements polygon shaped nodes.
 * <p>
 * Allows for nodes to be rendered as a regular polygon with an arbitrary number of sides by 
 * adding a column of Integers in the data table named "Polygon." The value corresponds to the number of sides.
 * NOTE: the renderer must be enabled in the "Manage Renderers" tab.
 * @author zde <zde6919@rit.edu>
 */
@ServiceProvider(service = Renderer.class)
public class PolygonShapedNodes extends NodeRenderer {
    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(PolygonShapedNodes.class, "PolygonShapedNodes.name");
    }

    //Overrides the default Node render method
    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        if (target instanceof G2DTarget) {
            int renderAsNgon = -1;
            if(properties.getBooleanValue("PolygonShapedNodes.property.enable")) {
                try {
                    Node n = (Node) item.getSource();
                    if((Integer) n.getAttribute("Polygon") >= 3) {
                        renderAsNgon = (Integer) n.getAttribute("Polygon");
                    }
                } catch(Exception e) {}
                if(renderAsNgon != -1) {
                    renderPolygonG2D(item, (G2DTarget) target, properties, renderAsNgon);
                }
                else {
                    super.render(item, target, properties);
                }
            }
            else {
                super.render(item, target, properties);
            }
        } 
        else if (target instanceof SVGTarget) {
            renderPolygonSVG(item, (SVGTarget) target, properties);
        } 
        else if (target instanceof PDFTarget) {
            renderPolygonPDF(item, (PDFTarget) target, properties);
        }
        
    }

    public void renderPolygonG2D(Item item, G2DTarget target, PreviewProperties properties, int numSides) {
        //Get data about the polygon to be rendered
        Float x = item.getData(NodeItem.X);
        Float y = item.getData(NodeItem.Y);
        Float size = item.getData(NodeItem.SIZE);
        Color color = item.getData(NodeItem.COLOR);
        Color borderColor = ((DependantColor) properties.getValue(PreviewProperty.NODE_BORDER_COLOR)).getColor(color);
        float borderSize = properties.getFloatValue(PreviewProperty.NODE_BORDER_WIDTH);
        int alpha = (int) ((properties.getFloatValue(PreviewProperty.NODE_OPACITY) / 100f) * 255f);
        if (alpha > 255) {
            alpha = 255;
        }

        //Graphics
        Graphics2D graphics = target.getGraphics();

        //Determine vertices of polygon and create Shape object to be drawn/filled
        int[] xpoints = new int[numSides];
        int[] ypoints = new int[numSides];
        for (int i = 0; i < numSides; i++){
            double angle = 2 * Math.PI / numSides;
            float calcX, calcY;
            if (numSides % 2 == 0) {
                calcX = (float)(x + (size * .6) * Math.cos(i * angle - Math.PI/4));
                calcY = (float)(y - (size * .6) * Math.sin(i * angle - Math.PI/4));
            } else {
                calcX = (float)(x + (size * .6) * Math.cos(i * angle));
                calcY = (float)(y - (size * .6) * Math.sin(i * angle));
            }
            xpoints[i] = (int) calcX;
            ypoints[i] = (int) calcY;
        }
        Shape toRender = new Polygon(xpoints,ypoints,numSides);
        //Draw border of polygon if applicable
        if (borderSize > 0) {
            Color border = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), alpha);
            Stroke borderStroke = new BasicStroke(borderSize);
            graphics.setPaint(border);
            graphics.setStroke(borderStroke);
            graphics.draw(toRender);
        }
        //Fill the polygon
        Color fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        graphics.setPaint(fill);
        graphics.fill(toRender);
    }
    
    public void renderPolygonPDF(Item item, PDFTarget target, PreviewProperties properties) {
        //Not implemented
    }

    public void renderPolygonSVG(Item item, SVGTarget target, PreviewProperties properties) {
        //Not implemented
    }

    @Override
    public PreviewProperty[] getProperties() {
        //Creates the same properties as the default renderer 
        //but adds a new one to control polygon shaped nodes rendering
        PreviewProperty[] props = super.getProperties();
        PreviewProperty[] newProps = new PreviewProperty[props.length + 1];

        System.arraycopy(props, 0, newProps, 0, props.length);

        newProps[newProps.length - 1] = PreviewProperty.createProperty(this, "PolygonShapedNodes.property.enable", Boolean.class,
                NbBundle.getMessage(PolygonShapedNodes.class, "PolygonShapedNodes.property.name"),
                NbBundle.getMessage(PolygonShapedNodes.class, "PolygonShapedNodes.property.description"),
                PreviewProperty.CATEGORY_NODES).setValue(true);
        return newProps;
    }
    
    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return item.getType().equals(Item.NODE);
    }
    
    @Override
    public void preProcess(PreviewModel previewModel) {
        //Not implemented
    }

    @Override
    public void postProcess(PreviewModel previewModel, RenderTarget renderTarget,
                            PreviewProperties properties) {
        //Not implemented
    }
}