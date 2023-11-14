package pl.edu.wat.student.rzepinski.jakub.util;

import org.gephi.io.importer.api.NodeDraft;

public class Utils {
    private static final int LAYOUT_SPACING = 50;

    public static boolean isPositive(int number) {
        return number > 0;
    }

    public static String getNodeId(int x, int y) {
        return String.format("(%d, %d)", x, y);
    }

    public static void setNodePosition(NodeDraft node, int x, int y) {
        node.setX(x * LAYOUT_SPACING);
        node.setY(y * LAYOUT_SPACING);
    }
}
