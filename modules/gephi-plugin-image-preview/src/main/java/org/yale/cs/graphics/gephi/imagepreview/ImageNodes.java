/*
Copyright 2012 Yale Computer Graphics Group
Authors : Yitzchak Lockerman
Website : http://graphics.cs.yale.edu/

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2012 Yale Computer Graphics Group. All rights reserved.

The contents of this file are subject to the terms of  the GNU
General Public License Version 3 only ("GPL" or "License"). 
You may not use this file except in compliance with the
License. You can obtain a copy of the License at /gpl-3.0.txt.
See the License for the specific language governing permissions and limitations 
under the License.  When distributing the software, include this License Header
Notice in each file and include the License file at /gpl-3.0.txt. 
If applicable, add the following below the License Header, with the fields
enclosed by brackets [] replaced by your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

This file is based on, and meant to be used with, Gephi. (http://gephi.org/)
*/

package org.yale.cs.graphics.gephi.imagepreview;


import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.preview.api.*;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.plugin.renderers.NodeRenderer;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;
import processing.core.PGraphics;
import processing.core.PImage;

/**
 * A service that renders Nodes as images. 
 * <p>
 * This class works in conjunction with {@link ImageItem} and 
 * {@link NodeImageItemBuilder}. 
 * <p>
 * This class provides the properties and last step rendering code for the images
 * themselves. It can easily be overloaded to support other images. All that would
 * be needed would be the creation of a new {@link ItemBuilder} that creates
 * proper images. (This class may need to be overidden to modify 
 * <code>needsItemBuilder</code>.
 * <p>
 * 
 * @author Yirzchak Lockerman (Yale Computer Graphics Group)
 */
@ServiceProvider(service = Renderer.class, position=200)
public class ImageNodes implements Renderer{
    
    final static String IMAGE_DESCRIPTION = "ImageNodes.property.imageDescription";
    final static String IMAGE_DIRECTORY = "ImageNodes.property.path";
    final static String IMAGE_OPACITY = "ImageNodes.property.opacity";
    final static String CATEGORY_NODE_IMAGE = "Node Images";
    
    private static final Logger logger = Logger.getLogger(ImageNodes.class.getName());
    

    
    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ImageNodes.class, "ImageNodes.name");
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        if(!(item instanceof ImageItem)) return;
        
        String imagesPath = properties.getValue(IMAGE_DIRECTORY);
        if(imagesPath == null || imagesPath.isEmpty()){
            return;
        }
        
        File imagesDir = new File(imagesPath);
        if(!imagesDir.exists() || !imagesDir.isDirectory()){
            return;
        }
        
        if (showNodes(properties)) {
            if (target instanceof G2DTarget) {
                renderImageProcessing((ImageItem)item, (G2DTarget) target, properties, imagesDir);
            } else if (target instanceof SVGTarget) {
                renderImageSVG((ImageItem)item, (SVGTarget) target, properties, imagesDir);
            } else if (target instanceof PDFTarget) {
                renderImagePDF((ImageItem)item, (PDFTarget) target, properties, imagesDir);
            }
        }
        
    }

    public void renderImageProcessing(ImageItem item, G2DTarget target, PreviewProperties properties, File directory) {
        //Graphics
        
        //PGraphics graphics = new PGraphics();//target.getGraphics();
        // PGraphics graphics = target.getGraphics();
        graphics.pushStyle();
        
        PImage image = item.renderProcessing(directory, target);
        
        if(image == null)
        {
            logger.log(Level.WARNING, "Unable to load image: {0}", item.getSource() );
            return;
        }
        
        //Params
        Float x = item.getData(NodeItem.X);
        Float y = item.getData(NodeItem.Y);
        Float size = item.getData(NodeItem.SIZE);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        //java.awt.Image img = toolkit.createImage("C:\\Users\\totetmatt\\Pictures\\max.png");
        
        
        
        int alpha = (int) ((properties.getFloatValue(IMAGE_OPACITY) / 100f) * 255f);
        if (alpha > 255) {
            alpha = 255;
        }
        
        //target.getGraphics().drawImage(img,new AffineTransform(), null);
        graphics.imageMode(PGraphics.CENTER);
        graphics.tint(255, alpha);
       
        
        graphics.image(image,x, y, size, size);
        
        
        graphics.tint(255, 255);
        graphics.popStyle();
    }

    public void renderImagePDF(ImageItem item, PDFTarget target, PreviewProperties properties, File directory) {
                      
        Image image = item.renderPDF(directory);
        
        if(image == null)
        {
            logger.log(Level.WARNING, "Unable to load image: {0}", item.getSource());
            return;
        }
        
        Float x = item.getData(NodeItem.X);
        Float y = item.getData(NodeItem.Y);
        Float size = item.getData(NodeItem.SIZE);


        float alpha = properties.getFloatValue(IMAGE_OPACITY) / 100f;

        PdfContentByte cb = target.getContentByte();

        
        if (alpha < 1f) {
            cb.saveState();
            PdfGState gState = new PdfGState();
            gState.setFillOpacity(alpha);
            gState.setStrokeOpacity(alpha);
            cb.setGState(gState);
        }
        
        image.setAbsolutePosition(x-size/2, -y-size/2);
        image.scaleToFit(size, size);
        try {
            cb.addImage(image);
        } catch (DocumentException ex) {
            logger.log(Level.SEVERE, "Unable to add image to document: "+item.getSource(), ex);
        }

        if (alpha < 1f) {
            cb.restoreState();
        }
    }

    public void renderImageSVG(ImageItem item, SVGTarget target, PreviewProperties properties, File directory) 
    {
        
        //Params
        Float x = item.getData(NodeItem.X);
        Float y = item.getData(NodeItem.Y);
        Float size = item.getData(NodeItem.SIZE);

        float alpha = properties.getFloatValue(IMAGE_OPACITY) / 100f;
        if (alpha > 1) {
            alpha = 1;
        }

        Element nodeElem = target.createElement("image");
        nodeElem.setAttribute("class", "node" );
        nodeElem.setAttribute("xlink:href",(String)item.renderSVG(directory));
        nodeElem.setAttribute("x", ""+(x-size/2));
        nodeElem.setAttribute("y", ""+(y-size/2));
        
        nodeElem.setAttribute("width", size.toString());
        nodeElem.setAttribute("height", size.toString());
        nodeElem.setAttribute("style", "opacity: "+alpha);

        target.getTopElement(SVGTarget.TOP_NODES).appendChild(nodeElem);
    }
    
    @Override
    public void preProcess(PreviewModel previewModel) {
    }
    
    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[]{
            PreviewProperty.createProperty(this, "ImageNodes.property.enable", Boolean.class,
               NbBundle.getMessage(ImageNodes.class, "ImageNodes.property.enable.name"),
               NbBundle.getMessage(ImageNodes.class, "ImageNodes.property.enable.description"),
              CATEGORY_NODE_IMAGE).setValue(false),
            /*PreviewProperty.createProperty(this, IMAGE_DESCRIPTION, String.class,
                NbBundle.getMessage(ImageNodes.class, IMAGE_DESCRIPTION+".name"),
                NbBundle.getMessage(ImageNodes.class, IMAGE_DESCRIPTION+".description"),
                PreviewProperty.CATEGORY_NODES,"ImageNodes.property.enable").setValue("image"),*/
            PreviewProperty.createProperty(this, IMAGE_DIRECTORY, String.class,
                NbBundle.getMessage(ImageNodes.class, IMAGE_DIRECTORY+".name"),
                NbBundle.getMessage(ImageNodes.class, IMAGE_DIRECTORY+".description"),
                CATEGORY_NODE_IMAGE,"ImageNodes.property.enable").setValue(new File(".").getAbsolutePath()),
            PreviewProperty.createProperty(this, IMAGE_OPACITY, Float.class,
                NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.opacity.displayName"),
                NbBundle.getMessage(NodeRenderer.class, "NodeRenderer.property.opacity.description"),
                CATEGORY_NODE_IMAGE,"ImageNodes.property.enable").setValue(100f)};

    }  
    
    private boolean showNodes(PreviewProperties properties){
        return properties.getFloatValue(IMAGE_OPACITY) > 0 && properties.getBooleanValue("ImageNodes.property.enable");
    }
     
    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {

            if(!(item instanceof ImageItem))
                return false;

            return showNodes(properties) && item.getSource() != null && item.getSource() instanceof String;

    }
        
    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return itemBuilder instanceof NodeImageItemBuilder && showNodes(properties);
    }

    public CanvasSize getCanvasSize(Item item, PreviewProperties pp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
