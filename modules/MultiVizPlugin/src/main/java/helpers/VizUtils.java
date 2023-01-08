/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package helpers;

import java.util.Comparator;
import javax.swing.JOptionPane;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;

/**
 *
 * @author J
 */
public class VizUtils {

    public static String getLabel(Node node, Column column) {
        return node.getAttribute(column).toString();
    }

    public static Column getLabel(Table table, String column) {
        Column label = table.getColumn(column);
        if (label == null) {
            label = table.getColumn(column.replace(" ", "_"));
        }
        return label;
    }

    public static String getLabel(Edge edge, Column column) {
        return edge.getAttribute(column).toString();
    }

    public static void nodesRandom(Node[] nodes) {
        for (Node node : nodes) {
            float random = (float) ((0.01 + Math.random()) * 1000) - 500;
            node.setX(random);
            node.setY((float) ((0.01 + Math.random()) * 1000) - 500);
        }
    }

    public static void alert(String title, String message){
      JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);            
    }

}
