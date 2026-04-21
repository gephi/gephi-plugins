package org.gephi.plugins.jpegexporter;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import org.openide.util.NbBundle;

public class UIExporterJPEGPanel extends JPanel {

    private final JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(16.0d, 0.1d, 1000.0d, 0.1d));
    private final JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(9.0d, 0.1d, 1000.0d, 0.1d));
    private final JSpinner dpiSpinner = new JSpinner(new SpinnerNumberModel(300, 1, 2400, 1));

    public UIExporterJPEGPanel() {
        initComponents();
        JFormattedTextField widthField = ((JSpinner.NumberEditor) widthSpinner.getEditor()).getTextField();
        widthField.setColumns(8);
        JFormattedTextField heightField = ((JSpinner.NumberEditor) heightSpinner.getEditor()).getTextField();
        heightField.setColumns(8);
    }

    public void setup(JPEGExporter exporter) {
        widthSpinner.setValue((double) exporter.getWidthCm());
        heightSpinner.setValue((double) exporter.getHeightCm());
        dpiSpinner.setValue(exporter.getDpi());
    }

    public void unsetup(JPEGExporter exporter) {
        exporter.setWidthCm(((Double) widthSpinner.getValue()).floatValue());
        exporter.setHeightCm(((Double) heightSpinner.getValue()).floatValue());
        exporter.setDpi((Integer) dpiSpinner.getValue());
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel(NbBundle.getMessage(UIExporterJPEGPanel.class, "UIExporterJPEGPanel.widthLabel.text")), gbc);

        gbc.gridx = 1;
        add(widthSpinner, gbc);

        gbc.gridx = 2;
        add(new JLabel("cm"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel(NbBundle.getMessage(UIExporterJPEGPanel.class, "UIExporterJPEGPanel.heightLabel.text")), gbc);

        gbc.gridx = 1;
        add(heightSpinner, gbc);

        gbc.gridx = 2;
        add(new JLabel("cm"), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel(NbBundle.getMessage(UIExporterJPEGPanel.class, "UIExporterJPEGPanel.dpiLabel.text")), gbc);

        gbc.gridx = 1;
        add(dpiSpinner, gbc);

        gbc.gridx = 2;
        add(new JLabel("dpi"), gbc);
    }
}
