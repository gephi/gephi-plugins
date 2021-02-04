/*
 * Created by JFormDesigner on Sun Apr 22 16:40:35 CEST 2018
 */

package pl.edu.wat.wcy.gephi.plugin.dbscan.view;

import pl.edu.wat.wcy.gephi.plugin.dbscan.core.Labels;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.PlainDocument;

/**
 * @author Hanss Solo
 */
public class SettingsPanel extends JPanel {

    private JLabel radiusLabel;
    private JTextField radius;
    private JLabel numberOfNeighborsLabel;
    private JTextField neighbors;

    public SettingsPanel() {
        initComponents();
    }

    private void initComponents() {

        radiusLabel = new JLabel();
        radius = new JTextField(2);
        ((PlainDocument)radius.getDocument()).setDocumentFilter(new IntegersDocumentFilter());
        numberOfNeighborsLabel = new JLabel();
        neighbors = new JTextField(2);
        ((PlainDocument)neighbors.getDocument()).setDocumentFilter(new IntegersDocumentFilter());

        setLayout(new MigLayout(
                "fillx,hidemode 3,align left top",
                // columns
                "[fill]" +
                        "[fill]",
                // rows
                "[]" +
                        "[]"));

        radiusLabel.setText(Labels.NEIGHBORHOOD_RADIUS);
        add(radiusLabel, "cell 0 0,align left top,grow 0 0");
        add(radius, "cell 1 0");

        numberOfNeighborsLabel.setText(Labels.MINIMAL_NEIGHBORS_NUMBER);
        add(numberOfNeighborsLabel, "cell 0 1,align left top,grow 0 0");
        add(neighbors, "cell 1 1");
    }

    public int getRadius() {
        return Integer.valueOf(radius.getText());
    }

    public int getNeighbors() {
        return Integer.valueOf(neighbors.getText());
    }

    public void setRadius(int radius) {
        this.radius.setText(String.valueOf(radius));
    }

    public void setNeighbors(int neighbors) {
        this.neighbors.setText(String.valueOf(neighbors));
    }
}
