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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.gephi.preview.api.SVGTarget;
import org.w3c.dom.Element;

/**
 * Draws a filled, bordered outline to each render target, so nodes and legend
 * symbols are painted by the same code and cannot drift apart.
 *
 * @author Matt Artz
 */
final class ShapePainter {

    private ShapePainter() {
    }

    static void g2d(Graphics2D graphics, Outline outline, Color fill, Color border,
                    float borderWidth, float alpha) {
        Path2D.Double path = new Path2D.Double();
        outline.trace(new PathSink() {
            @Override
            public void moveTo(double x, double y) {
                path.moveTo(x, y);
            }

            @Override
            public void lineTo(double x, double y) {
                path.lineTo(x, y);
            }

            @Override
            public void quadTo(double cx, double cy, double x, double y) {
                path.quadTo(cx, cy, x, y);
            }

            @Override
            public void close() {
                path.closePath();
            }
        });
        int alpha255 = Math.round(alpha * 255f);
        graphics.setColor(withAlpha(fill, alpha255));
        graphics.fill(path);
        if (borderWidth > 0) {
            graphics.setColor(withAlpha(border, alpha255));
            graphics.setStroke(new BasicStroke(borderWidth, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND));
            graphics.draw(path);
        }
    }

    /** Returns a {@code path} element; the caller adds a class and appends it. */
    static Element svg(SVGTarget target, Outline outline, Color fill, Color border,
                       float borderWidth, float alpha) {
        Element element = target.createElement("path");
        element.setAttribute("d", svgPath(outline));
        element.setAttribute("fill", target.toHexString(fill));
        element.setAttribute("fill-opacity", Float.toString(alpha));
        if (borderWidth > 0) {
            element.setAttribute("stroke", target.toHexString(border));
            element.setAttribute("stroke-width", Float.toString(borderWidth * target.getScaleRatio()));
            element.setAttribute("stroke-opacity", Float.toString(alpha));
            element.setAttribute("stroke-linejoin", "round");
        }
        return element;
    }

    static void pdf(PDPageContentStream stream, Outline outline, Color fill, Color border,
                    float borderWidth, float alpha) {
        try {
            stream.saveGraphicsState();
            stream.setNonStrokingColor(fill);
            stream.setStrokingColor(border);
            stream.setLineWidth(borderWidth);
            stream.setLineJoinStyle(1);
            if (alpha < 1f) {
                PDExtendedGraphicsState state = new PDExtendedGraphicsState();
                state.setStrokingAlphaConstant(alpha);
                state.setNonStrokingAlphaConstant(alpha);
                stream.setGraphicsStateParameters(state);
            }
            outline.trace(new PdfPath(stream));
            if (borderWidth > 0) {
                stream.fillAndStroke();
            } else {
                stream.fill();
            }
            stream.restoreGraphicsState();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    static String svgPath(Outline outline) {
        StringBuilder d = new StringBuilder();
        outline.trace(new PathSink() {
            @Override
            public void moveTo(double x, double y) {
                d.append('M').append(svgNumber(x)).append(',').append(svgNumber(y));
            }

            @Override
            public void lineTo(double x, double y) {
                d.append('L').append(svgNumber(x)).append(',').append(svgNumber(y));
            }

            @Override
            public void quadTo(double cx, double cy, double x, double y) {
                d.append('Q').append(svgNumber(cx)).append(',').append(svgNumber(cy))
                    .append(' ').append(svgNumber(x)).append(',').append(svgNumber(y));
            }

            @Override
            public void close() {
                d.append('Z');
            }
        });
        return d.toString();
    }

    /**
     * Writes an outline into a PDF content stream. PDF space has y increasing
     * upwards, so every point is mirrored, as the built-in renderer mirrors
     * its circles. PDF has only cubic curves; a quadratic converts exactly.
     */
    private static final class PdfPath implements PathSink {

        private final PDPageContentStream stream;
        private double lastX;
        private double lastY;

        PdfPath(PDPageContentStream stream) {
            this.stream = stream;
        }

        @Override
        public void moveTo(double x, double y) {
            call(() -> stream.moveTo((float) x, (float) -y));
            lastX = x;
            lastY = y;
        }

        @Override
        public void lineTo(double x, double y) {
            call(() -> stream.lineTo((float) x, (float) -y));
            lastX = x;
            lastY = y;
        }

        @Override
        public void quadTo(double cx, double cy, double x, double y) {
            double x1 = lastX + 2d / 3d * (cx - lastX);
            double y1 = lastY + 2d / 3d * (cy - lastY);
            double x2 = x + 2d / 3d * (cx - x);
            double y2 = y + 2d / 3d * (cy - y);
            call(() -> stream.curveTo((float) x1, (float) -y1, (float) x2, (float) -y2,
                (float) x, (float) -y));
            lastX = x;
            lastY = y;
        }

        @Override
        public void close() {
            call(stream::closePath);
        }

        private static void call(PdfCall call) {
            try {
                call.run();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        private interface PdfCall {
            void run() throws IOException;
        }
    }

    private static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /** Two decimals is a hundredth of a layout unit, finer than any export resolves. */
    static String svgNumber(double value) {
        String text = String.format(Locale.ROOT, "%.2f", value);
        int end = text.length();
        while (text.charAt(end - 1) == '0') {
            end--;
        }
        if (text.charAt(end - 1) == '.') {
            end--;
        }
        String trimmed = text.substring(0, end);
        return "-0".equals(trimmed) ? "0" : trimmed;
    }

}
