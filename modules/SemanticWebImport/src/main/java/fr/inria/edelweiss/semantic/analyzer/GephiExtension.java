/*
 * Copyright (c) 2011, INRIA All rights reserved.
 */
package fr.inria.edelweiss.semantic.analyzer;

import fr.inria.edelweiss.sparql.GephiUtils;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.html.StyleSheet;
import org.gephi.graph.api.AttributeUtils;

/**
 *
 * @author edemairy
 */
public class GephiExtension {

    public static final String GEPHI_PREFIX = "http://gephi.org/";
    public static final String GEPHI_SET_LABEL = GEPHI_PREFIX + "label";
    public static final String GEPHI_SET_SIZE = GEPHI_PREFIX + "size";
    public static final String GEPHI_SET_COLOR_RGB = GEPHI_PREFIX + "colorRGB";
    public static final String GEPHI_SET_COLOR = GEPHI_PREFIX + "color";
    public static final String GEPHI_SET_COLOR_R = GEPHI_PREFIX + "color_r";
    public static final String GEPHI_SET_COLOR_G = GEPHI_PREFIX + "color_g";
    public static final String GEPHI_SET_COLOR_B = GEPHI_PREFIX + "color_b";
    public static final String GEPHI_SET_SHAPE = GEPHI_PREFIX + "sharp";

    enum C {

        red(Color.RED), blue(Color.BLUE), green(Color.GREEN), pink(Color.PINK), white(Color.WHITE), black(Color.BLACK),
        gray(Color.GRAY), yellow(Color.YELLOW), cyan(Color.CYAN), orange(Color.ORANGE), magenta(Color.MAGENTA), darkGray(Color.DARK_GRAY),
        lightGray(Color.LIGHT_GRAY);
        private final Color color;

        C(Color color) {
            this.color = color;
        }

        public static Color get(String name) {
            for (C a : C.values()) {
                if (a.toString().equals(name)) {
                    return a.color;
                }
            }
            return null;
        }
    }

    enum Shape {

        SPHERE("sphere"), DISK("disk"), RECTANGLE("rectangle"), CIRCLE("circle"), DOT("dot"), SQUARE("square");
        private final String shapeName;

        private Shape(final String shapeName) {
            this.shapeName = shapeName;
        }

        public static String getShape(String name) {
            for (Shape s : Shape.values()) {
                if (s.toString().equals(name)) {
                    return s.shapeName;
                }
            }
            return null;
        }
    }
    private static final Logger logger = Logger.getLogger(GephiExtension.class.getName());

    public static boolean isGephiExtension(final String name) {
        return name.startsWith(GEPHI_PREFIX);
    }

    public static void processGephiExtension(final String sourceLabel, final String edgeLabel, final String targetLabel, final GephiUtils gephiUtil) {
        if (gephiUtil.isEdge(sourceLabel)) {
            logger.log(Level.SEVERE, "Setting values on edges is not supported.\n {0} -- {1} --> {2} failed.", new Object[]{sourceLabel, edgeLabel, targetLabel});
            return;
        }
        if (!gephiUtil.nodeExist(sourceLabel)) {
            gephiUtil.addNode(sourceLabel);
        }
        if (edgeLabel.equals(GEPHI_SET_LABEL)) {
            gephiUtil.setNodeLabel(sourceLabel, targetLabel);
        }

        if (edgeLabel.equals(GEPHI_SET_SIZE)) {
            float targetSize = GephiUtils.convertFloat(targetLabel);
            gephiUtil.setNodeSize(sourceLabel, targetSize);
        }

        if (edgeLabel.equals(GEPHI_SET_COLOR_RGB)) {
            String[] colorRGB = GephiUtils.stringSplit(targetLabel);
            float color_r = GephiUtils.convertFloatColor(colorRGB[0]);
            float color_g = GephiUtils.convertFloatColor(colorRGB[1]);
            float color_b = GephiUtils.convertFloatColor(colorRGB[2]);
            gephiUtil.setNodeColor(sourceLabel, color_r, color_g, color_b);
        }

        if (edgeLabel.equals(GEPHI_SET_COLOR)) {
            Color c;
            StyleSheet style = new StyleSheet();
            c = style.stringToColor(targetLabel.toLowerCase());
            if (c == null) {
                logger.log(Level.WARNING, "Color \"{0}\" not found. Setting the color to black instead.", targetLabel);
                c = Color.BLACK;
            }

            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();
            String rValue = Integer.toString(r);
            String gValue = Integer.toString(g);
            String bValue = Integer.toString(b);
            float color_r = GephiUtils.convertFloatColor(rValue);
            float color_g = GephiUtils.convertFloatColor(gValue);
            float color_b = GephiUtils.convertFloatColor(bValue);
            gephiUtil.setNodeColor(sourceLabel, color_r, color_g, color_b);
        }

        if (edgeLabel.equals(GEPHI_SET_COLOR_R)) {
            float color_r = GephiUtils.convertFloatColor(targetLabel);
            gephiUtil.setNodeColor_R(sourceLabel, color_r);
        }

        if (edgeLabel.equals(GEPHI_SET_COLOR_G)) {
            float color_g = GephiUtils.convertFloatColor(targetLabel);
            gephiUtil.setNodeColor_G(sourceLabel, color_g);
        }

        if (edgeLabel.equals(GEPHI_SET_COLOR_B)) {
            float color_b = GephiUtils.convertFloatColor(targetLabel);
            gephiUtil.setNodeColor_B(sourceLabel, color_b);
        }

        if (edgeLabel.equals(GEPHI_SET_SHAPE)) {
            String shape;
            shape = Shape.getShape(targetLabel.toLowerCase());
            gephiUtil.setNodeShape(sourceLabel, shape);
        } else {
            String nameNewAttribute = edgeLabel.replaceFirst(GEPHI_PREFIX, "");
            if (gephiUtil.isEdge(sourceLabel)) {
                gephiUtil.addAttributeToEdges(nameNewAttribute, String.class);
		gephiUtil.setEdgeAttr(sourceLabel, nameNewAttribute, targetLabel);
            } else {
                gephiUtil.addAttributeToNodes(nameNewAttribute, String.class);
		gephiUtil.setNodeAttr(sourceLabel, nameNewAttribute, targetLabel);
            }
        }
    }
}
