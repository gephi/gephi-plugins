/**
 * Copyright (c) 2012, David Shepard All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.hypercities.exporttoearth;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;
import org.gephi.graph.api.Node;

/**
 * Creates icons for Google Earth.
 *
 * Caches icons to avoid having to regenerate them.
 *
 * @author Dave Shepard
 */
public class IconRenderer {

    private ArrayList<String> filenames = new ArrayList<String>();
    private String lastFilename;
    private HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
    private int maxRadius;

    public IconRenderer(int maxRadius) {
        this.maxRadius = maxRadius;
    }

    public void render(Node node) {
        Color color = node.getColor();
        Color borderColor = color;

        int size = maxRadius,
            borderSize = 2 * maxRadius / 20;

        lastFilename = "tiles-" + size + "-" + color.getRed() + "-"
                + color.getGreen() + "-" + color.getBlue() + ".png";
        // If the circle has already been generated, don't regenerate it.
        if (filenames.contains(lastFilename)) {
            return;
        }
        filenames.add(lastFilename);

        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = img.createGraphics();

        graphics.setColor(color);
        graphics.fillOval(borderSize, borderSize, size - borderSize, size - borderSize);

        if (borderSize > 0) {
            graphics.setColor(borderColor);
            graphics.drawOval(borderSize, borderSize, size - borderSize, size - borderSize);
        }

        images.put(lastFilename, img);
    }

    public void renderToKMZ(ZipOutputStream out) throws IOException {
        for (Map.Entry<String, BufferedImage> entry : images.entrySet()) {
            String filename = entry.getKey();
            BufferedImage image = entry.getValue();
            ZipEntry ze = new ZipEntry(filename);
            out.putNextEntry(ze);
            ImageIO.write(image, "png", out);
        }
    }

    public String getLastFilename() {
        return lastFilename;
    }
}
