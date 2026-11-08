/*
 * Copyright 2026 Matt Artz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gephi.plugins.shapebypartition;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.text.PDFTextStripper;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.SVGTarget;
import org.gephi.preview.types.DependantColor;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Draws a legend with shape and colour sections into each export target the
 * way Gephi does, and reads back what landed on the page.
 */
public class ShapeLegendRendererTest {

    private static final Color RED = new Color(0xE1, 0x57, 0x59);
    private static final Color BLUE = new Color(0x2A, 0x78, 0xD6);
    private static final double[] BOUNDS = {0, 0, 1000, 800};

    @Test
    public void pngPixelsShowTheColourSwatchesInTheirGroupColours() {
        ShapeLegend legend = legend();
        double[] box = legend.bounds(ShapeLegendRenderer.font(properties()));
        BufferedImage image = new BufferedImage((int) (box[2] - box[0]) + 20, (int) (box[3] - box[1]) + 20,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.translate(10 - box[0], 10 - box[1]);
        G2DTarget target = (G2DTarget) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{G2DTarget.class}, (proxy, method, args) -> {
                if ("getGraphics".equals(method.getName())) {
                    return graphics;
                }
                throw new UnsupportedOperationException(method.getName());
            });

        new ShapeLegendRenderer().render(item(legend), target, properties());

        List<ShapeLegend.Symbol> symbols = legend.symbols(ShapeLegendRenderer.font(properties()));
        ShapeLegend.Symbol swatch = symbols.get(symbols.size() - 2);
        int px = (int) Math.round(swatch.x + 10 - box[0]);
        int py = (int) Math.round(swatch.y + 10 - box[1]);
        assertEquals("the first colour row is the largest group's colour",
            BLUE.getRGB() & 0xFFFFFF, image.getRGB(px, py) & 0xFFFFFF);
    }

    @Test
    public void svgHoldsBothSectionsInOneGroup() throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        Element root = document.createElement("svg");
        document.appendChild(root);
        SVGTarget target = (SVGTarget) Proxy.newProxyInstance(getClass().getClassLoader(),
            new Class<?>[]{SVGTarget.class}, (proxy, method, args) -> {
                switch (method.getName()) {
                    case "getTopElement":
                        Element group = document.createElement("g");
                        group.setAttribute("id", (String) args[0]);
                        root.appendChild(group);
                        return group;
                    case "createElement":
                        return document.createElement((String) args[0]);
                    case "createTextNode":
                        return document.createTextNode((String) args[0]);
                    case "toHexString":
                        return String.format("#%06x", ((Color) args[0]).getRGB() & 0xFFFFFF);
                    case "getScaleRatio":
                        return 1f;
                    default:
                        throw new UnsupportedOperationException(method.getName());
                }
            });

        new ShapeLegendRenderer().render(item(legend()), target, properties());

        Element group = (Element) root.getElementsByTagName("g").item(0);
        assertEquals(ShapeLegendRenderer.SVG_GROUP, group.getAttribute("id"));
        List<String> texts = new ArrayList<>();
        NodeList textNodes = group.getElementsByTagName("text");
        for (int i = 0; i < textNodes.getLength(); i++) {
            texts.add(textNodes.item(i).getTextContent());
        }
        assertEquals(Arrays.asList("team", "a", "b", "project", "q", "p"), texts);
        List<String> fills = new ArrayList<>();
        NodeList paths = group.getElementsByTagName("path");
        for (int i = 0; i < paths.getLength(); i++) {
            fills.add(((Element) paths.item(i)).getAttribute("fill"));
        }
        assertEquals(4, fills.size());
        assertEquals("#2a78d6", fills.get(2));
        assertEquals("#e15759", fills.get(3));
    }

    @Test
    public void pdfCarriesTheLegendText() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                // Gephi's PDF space is flipped and offset; centre the legend on the page.
                stream.transform(new org.apache.pdfbox.util.Matrix(0.4f, 0, 0, 0.4f, -360, 300));
                PDFTarget target = (PDFTarget) Proxy.newProxyInstance(getClass().getClassLoader(),
                    new Class<?>[]{PDFTarget.class}, (proxy, method, args) -> {
                        switch (method.getName()) {
                            case "getContentStream":
                                return stream;
                            case "getPDFont":
                                return null;
                            default:
                                throw new UnsupportedOperationException(method.getName());
                        }
                    });
                new ShapeLegendRenderer().render(item(legend()), target, properties());
            }
            document.save(bytes);
        }
        try (PDDocument reread = Loader.loadPDF(bytes.toByteArray())) {
            String text = new PDFTextStripper().getText(reread);
            for (String expected : Arrays.asList("team", "project", "q", "p")) {
                assertTrue("PDF text lacks '" + expected + "': " + text, text.contains(expected));
            }
        }
    }

    private static ShapeLegend legend() {
        List<Object> values = Arrays.asList("a", "a", "b");
        ShapeLegend shapes = ShapeLegend.build("team", PartitionShapes.assign(values), values,
            Arrays.asList(RED, RED, RED), Arrays.asList(20f, 20f, 20f), BOUNDS);
        return shapes.withColourKey(ColourKey.read("project", Arrays.asList("q", "q", "p"),
            Arrays.asList(BLUE, BLUE, RED)));
    }

    private static Item item(ShapeLegend legend) {
        Item item = new LegendItemBuilder().getItems(null)[0];
        item.setData(LegendItemBuilder.LEGEND, legend);
        return item;
    }

    private static PreviewProperties properties() {
        PreviewProperties properties = new PreviewProperties();
        properties.putValue(ShapeLegendRenderer.SHOW, true);
        properties.putValue(ShapeLegendRenderer.TITLE, "");
        properties.putValue(ShapeLegendRenderer.COLOUR_TITLE, "");
        properties.putValue(PreviewProperty.NODE_LABEL_FONT, new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        properties.putValue(PreviewProperty.BACKGROUND_COLOR, Color.WHITE);
        properties.putValue(PreviewProperty.NODE_BORDER_FIXED, false);
        properties.putValue(PreviewProperty.NODE_BORDER_WIDTH, 1f);
        properties.putValue(PreviewProperty.NODE_BORDER_COLOR, new DependantColor(DependantColor.Mode.DARKER));
        return properties;
    }
}
