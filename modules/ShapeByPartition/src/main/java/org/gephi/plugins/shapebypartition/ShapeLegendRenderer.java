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
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.UncheckedIOException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.gephi.preview.api.CanvasSize;
import org.gephi.preview.api.G2DTarget;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PDFTarget;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.api.SVGTarget;
import org.gephi.preview.plugin.renderers.SizeUtils;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.gephi.preview.types.DependantColor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.w3c.dom.Element;

/**
 * Draws the key to the shape mapping beside the graph, in the preview and in
 * the PNG, SVG and PDF exports.
 * <p>
 * Gephi's exports carry no key, and tools that build one from Gephi's own
 * colour and size mappings cannot see shapes. Without this, every figure
 * using the plugin needed its key added by hand.
 * <p>
 * Registered after the node and label renderers so the key is drawn on top,
 * and it reports its own extent so exports make room for it.
 *
 * @author Matt Artz
 */
@ServiceProvider(service = Renderer.class, position = 450)
public class ShapeLegendRenderer implements Renderer {

    /** Preview property switching the legend on and off. */
    public static final String SHOW = "shapebypartition.legend.show";

    /** Preview property overriding the legend title; blank uses the column's title. */
    public static final String TITLE = "shapebypartition.legend.title";

    /**
     * Preview property naming the column the node colours encode. Blank draws no
     * colour key: Gephi does not record which column the colours came from, so the
     * plugin never guesses.
     */
    public static final String COLOUR_COLUMN = "shapebypartition.legend.colour.column";

    /** Preview property overriding the colour section's heading; blank uses the column's title. */
    public static final String COLOUR_TITLE = "shapebypartition.legend.colour.title";

    /** SVG group the legend is written to, so it can be edited or removed as one. */
    static final String SVG_GROUP = "shape-legend";

    private static final Color DARK_TEXT = new Color(0x22, 0x22, 0x22);
    private static final Color LIGHT_TEXT = new Color(0xEE, 0xEE, 0xEE);
    private static final Color NEUTRAL_ON_DARK = new Color(0xA8, 0xAE, 0xB8);

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ShapeLegendRenderer.class, "ShapeLegendRenderer.name");
    }

    @Override
    public void preProcess(PreviewModel previewModel) {
        // The node renderer builds the legend, from the shapes it resolves.
    }

    @Override
    public void postProcess(PreviewModel previewModel, RenderTarget target, PreviewProperties properties) {
        // Nothing to release; the legend lives on its item until the next refresh.
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return LegendItemBuilder.TYPE.equals(item.getType())
            && properties.getBooleanValue(SHOW)
            && item.getData(LegendItemBuilder.LEGEND) != null;
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return itemBuilder instanceof LegendItemBuilder && properties.getBooleanValue(SHOW);
    }

    @Override
    public CanvasSize getCanvasSize(Item item, PreviewProperties properties) {
        ShapeLegend legend = legendOf(item, properties);
        if (legend == null || !properties.getBooleanValue(SHOW)) {
            return new CanvasSize();
        }
        double[] box = legend.bounds(font(properties));
        return new CanvasSize((float) box[0], (float) box[1],
            (float) (box[2] - box[0]), (float) (box[3] - box[1]));
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        ShapeLegend legend = legendOf(item, properties);
        if (legend == null) {
            return;
        }
        Font font = font(properties);
        Color text = textColor(properties);
        boolean dark = text == LIGHT_TEXT;
        float size = (float) legend.symbol;
        float border = Math.min(SizeUtils.getBorderWidth(properties, size), size / 2f);
        DependantColor borderColor = (DependantColor) properties.getValue(PreviewProperty.NODE_BORDER_COLOR);

        if (target instanceof G2DTarget) {
            Graphics2D graphics = ((G2DTarget) target).getGraphics();
            for (ShapeLegend.Symbol symbol : legend.symbols(font)) {
                ShapePainter.g2d(graphics, symbol.shape.symbol(symbol.x, symbol.y, size - border),
                    fillOf(symbol, dark), borderOf(borderColor, fillOf(symbol, dark)), border, 1f);
            }
            Object hint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setColor(text);
            for (ShapeLegend.Text run : legend.texts(font)) {
                graphics.setFont(styled(font, run));
                graphics.drawString(run.text, (float) run.x, (float) run.baseline);
            }
            if (hint != null) {
                graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, hint);
            }
        } else if (target instanceof SVGTarget) {
            SVGTarget svg = (SVGTarget) target;
            Element group = svg.getTopElement(SVG_GROUP);
            for (ShapeLegend.Symbol symbol : legend.symbols(font)) {
                Element path = ShapePainter.svg(svg, symbol.shape.symbol(symbol.x, symbol.y, size - border),
                    fillOf(symbol, dark), borderOf(borderColor, fillOf(symbol, dark)), border, 1f);
                path.setAttribute("class", "legend-symbol");
                group.appendChild(path);
            }
            for (ShapeLegend.Text run : legend.texts(font)) {
                Element element = svg.createElement("text");
                element.setAttribute("x", ShapePainter.svgNumber(run.x));
                element.setAttribute("y", ShapePainter.svgNumber(run.baseline));
                element.setAttribute("font-family", cssFamily(font));
                element.setAttribute("font-size", ShapePainter.svgNumber(run.size));
                if (run.bold) {
                    element.setAttribute("font-weight", "bold");
                }
                element.setAttribute("fill", svg.toHexString(text));
                element.setAttribute("class", run.bold ? "legend-title" : "legend-label");
                element.appendChild(svg.createTextNode(run.text));
                group.appendChild(element);
            }
        } else if (target instanceof PDFTarget) {
            PDFTarget pdf = (PDFTarget) target;
            PDPageContentStream stream = pdf.getContentStream();
            for (ShapeLegend.Symbol symbol : legend.symbols(font)) {
                ShapePainter.pdf(stream, symbol.shape.symbol(symbol.x, symbol.y, size - border),
                    fillOf(symbol, dark), borderOf(borderColor, fillOf(symbol, dark)), border, 1f);
            }
            for (ShapeLegend.Text run : legend.texts(font)) {
                writePdfText(pdf, stream, styled(font, run), run, text);
            }
        }
    }

    private static void writePdfText(PDFTarget pdf, PDPageContentStream stream, Font font,
                                     ShapeLegend.Text run, Color color) {
        try {
            stream.beginText();
            stream.setFont(pdfFont(pdf, font, run), run.size);
            stream.setNonStrokingColor(color);
            // PDF space has y increasing upwards; mirror as the shapes are.
            stream.newLineAtOffset((float) run.x, (float) -run.baseline);
            try {
                stream.showText(run.text);
            } catch (IllegalArgumentException unencodable) {
                // The PDF font cannot encode a character; keep the rest legible.
                stream.showText(run.text.replaceAll("[^\\x20-\\x7E]", "?"));
            }
            stream.endText();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * The font as CSS understands it. Java's logical families are not CSS
     * names, and a browser meeting "SansSerif" falls back to a serif face.
     */
    static String cssFamily(Font font) {
        switch (font.getFamily()) {
            case Font.SANS_SERIF:
            case Font.DIALOG:
            case Font.DIALOG_INPUT:
                return "sans-serif";
            case Font.SERIF:
                return "serif";
            case Font.MONOSPACED:
                return "monospace";
            default:
                return "'" + font.getFamily().replace("'", "") + "', sans-serif";
        }
    }

    /**
     * Gephi's PDF font for the run, unless that would be bold for a plain
     * label. Gephi caches PDF fonts by PostScript name and maps Java's logical
     * fonts through a fallback that can answer Helvetica-Bold for every
     * weight, which would set the whole key in bold.
     */
    private static PDFont pdfFont(PDFTarget pdf, Font font, ShapeLegend.Text run) {
        PDFont chosen = pdf.getPDFont(font);
        String name = chosen == null ? null : chosen.getName();
        if (chosen == null || (!run.bold && name != null && name.contains("Bold"))) {
            return new PDType1Font(run.bold ? Standard14Fonts.FontName.HELVETICA_BOLD
                : Standard14Fonts.FontName.HELVETICA);
        }
        return chosen;
    }

    private static ShapeLegend legendOf(Item item, PreviewProperties properties) {
        ShapeLegend legend = item.getData(LegendItemBuilder.LEGEND);
        return legend == null ? null
            : legend.retitled(properties.getStringValue(TITLE)).withColourTitle(properties.getStringValue(COLOUR_TITLE));
    }

    private static Font styled(Font font, ShapeLegend.Text run) {
        return font.deriveFont(run.bold ? Font.BOLD : Font.PLAIN, run.size);
    }

    /** Neutral symbols are lightened on a dark background so they keep their contrast. */
    private static Color fillOf(ShapeLegend.Symbol symbol, boolean dark) {
        return dark && ShapeLegend.NEUTRAL.equals(symbol.fill) ? NEUTRAL_ON_DARK : symbol.fill;
    }

    private static Color borderOf(DependantColor border, Color fill) {
        return border == null ? fill.darker() : border.getColor(fill);
    }

    /** The node label font's family, so the key matches the labels around it. */
    static Font font(PreviewProperties properties) {
        Object value = properties.getValue(PreviewProperty.NODE_LABEL_FONT);
        Font base = value instanceof Font ? (Font) value : new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        return base.deriveFont(Font.PLAIN);
    }

    /** Dark text on a light background, light text on a dark one. */
    static Color textColor(PreviewProperties properties) {
        Object value = properties.getValue(PreviewProperty.BACKGROUND_COLOR);
        if (!(value instanceof Color)) {
            return DARK_TEXT;
        }
        Color background = (Color) value;
        double luminance = (0.2126 * background.getRed() + 0.7152 * background.getGreen()
            + 0.0722 * background.getBlue()) / 255d;
        return luminance < 0.5 ? LIGHT_TEXT : DARK_TEXT;
    }

    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[]{
            PreviewProperty.createProperty(this, SHOW, Boolean.class,
                NbBundle.getMessage(ShapeLegendRenderer.class, "ShapeLegendRenderer.show.name"),
                NbBundle.getMessage(ShapeLegendRenderer.class, "ShapeLegendRenderer.show.description"),
                PreviewProperty.CATEGORY_NODES).setValue(true),
            PreviewProperty.createProperty(this, TITLE, String.class,
                NbBundle.getMessage(ShapeLegendRenderer.class, "ShapeLegendRenderer.title.name"),
                NbBundle.getMessage(ShapeLegendRenderer.class, "ShapeLegendRenderer.title.description"),
                PreviewProperty.CATEGORY_NODES).setValue(""),
            PreviewProperty.createProperty(this, COLOUR_COLUMN, String.class,
                NbBundle.getMessage(ShapeLegendRenderer.class, "ShapeLegendRenderer.colourColumn.name"),
                NbBundle.getMessage(ShapeLegendRenderer.class, "ShapeLegendRenderer.colourColumn.description"),
                PreviewProperty.CATEGORY_NODES).setValue(""),
            PreviewProperty.createProperty(this, COLOUR_TITLE, String.class,
                NbBundle.getMessage(ShapeLegendRenderer.class, "ShapeLegendRenderer.colourTitle.name"),
                NbBundle.getMessage(ShapeLegendRenderer.class, "ShapeLegendRenderer.colourTitle.description"),
                PreviewProperty.CATEGORY_NODES).setValue("")
        };
    }
}
