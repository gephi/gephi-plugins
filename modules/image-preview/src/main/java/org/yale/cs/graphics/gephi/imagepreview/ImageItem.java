/*
Copyright 2012 Yale Computer Graphics Group
Authors : Yitzchak Lockerman
Website : http://graphics.cs.yale.edu/

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2012 Yale Computer Graphics Group. All rights reserved.

The contents of this file are subject to the terms of the GNU
General Public License Version 3 only ("GPL" or "License"). 
You may not use this file except in compliance with the
License. You can obtain a copy of the License at /gpl-3.0.txt.
See the License for the specific language governing permissions and limitations 
under the License.  When distributing the software, include this License Header
Notice in each file and include the License file at /gpl-3.0.txt. 
If applicable, add the following below the License Header, with the fields
enclosed by brackets [] replaced by your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s): Totetmatt (0.9.X Transition)

This file is based on, and meant to be used with, Gephi. (http://gephi.org/)
 */
package org.yale.cs.graphics.gephi.imagepreview;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.gephi.preview.plugin.items.AbstractItem;
import org.gephi.preview.plugin.items.NodeItem;

/**
 * A Item that represents a image within the file system.
 * <p>
 * Each instance of <code>ImageItem</code> represents a single image file. When the file is loaded (using one of the methods) the image data is cached in memory, allowing for faster rendering in the
 * future.
 * <p>
 * In addition to the cached images, the item contains all the properties of {@link NodeItem}.
 *
 * @author Yitzchak Lockerman
 */
public class ImageItem extends AbstractItem {

    /**
     * The identifier of this item.
     */
    public static final String IMAGE = "Image";

    /**
     * The data key for accessing the image file within the cache.
     */
    public static final String IMAGE_DATA = "Image_Data";

    /**
     * The data key for accessing the iText PDF image file within the cache.
     */
    public static final String PDF_DATA = "PDF_Data";

    private static final Logger LOG = Logger.getLogger(ImageItem.class.getName());

    /**
     *
     * @param source The image filename
     */
    public ImageItem(String source) {
        super(source, IMAGE);
    }

    /**
     * Prepares this ImageItem to be rendered by the grapchis target. Loading from the cache, if available. If the method is forced to load a image from the hard drive, it store it in the cache.
     *
     * @param directory The name of the folder to load images from.
     * @return A image corresponding to this item.
     */
    public BufferedImage loadImage(File directory) {
        BufferedImage image = (BufferedImage) data.get(IMAGE_DATA);
        if (image == null) {
            if (source instanceof String) {
                final File file = new File(directory, (String) source);

                try {
                    image = ImageIO.read(file);
                } catch (java.io.IOException e) {
                    LOG.log(Level.SEVERE, "Unable to load image: " + file, e);
                }
            }

            //If we can't render the image
            if (image == null) {
                LOG.log(Level.WARNING, "Unable to load image: {0}", source);
                return null;
            }

            data.put(IMAGE_DATA, image);
        }

        return image;
    }

    /**
     * @param directory The name of the folder to load images from.
     * @return The attribute to be added to the SVG file to represent this image.
     */
    public String renderSVG(File directory) {
        return new File(directory, (String) source).getAbsolutePath();
    }

    /**
     * Prepares this ImageItem to be rendered by the PDF target. Loading from the cache, if available. If the method is forced to load a image from the hard drive, it stores it in the cache.
     *
     * @param directory The name of the folder to load images from.
     * @return A iText PDF Image corresponding to this item.
     */
    public com.itextpdf.text.Image renderPDF(File directory) {
        com.itextpdf.text.Image image = (com.itextpdf.text.Image) data.get(PDF_DATA);
        if (image == null) {
            if (source instanceof String) {
                final File file = new File(directory, (String) source);

                try {
                    image = com.itextpdf.text.Image.getInstance(file.getCanonicalPath());
                } catch (BadElementException ex) {
                    LOG.log(Level.SEVERE, "Unable to load image: " + file, ex);
                } catch (MalformedURLException ex) {
                    LOG.log(Level.SEVERE, "Unable to load image: " + file, ex);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, "Unable to load image: " + file, ex);
                }

            }
            //If we can't render the image, fallback to the defult render
            if (image == null) {
                LOG.log(Level.WARNING, "Unable to load image: {0}", source);
                return null;
            }

            data.put(PDF_DATA, image);
        }

        return image;
    }
}