package org.gephi.plugins.jpegexporter;

import javax.swing.JPanel;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ExporterUI.class)
public class UIExporterJPEG implements ExporterUI {

    private static final String PREF_WIDTH = "JPEG_width";
    private static final String PREF_HEIGHT = "JPEG_height";
    private static final String PREF_DPI = "JPEG_dpi";
    private static final JPEGExporter DEFAULTS = new JPEGExporter();

    private UIExporterJPEGPanel panel;
    private JPEGExporter exporter;

    @Override
    public JPanel getPanel() {
        panel = new UIExporterJPEGPanel();
        return panel;
    }

    @Override
    public void setup(Exporter exporter) {
        this.exporter = (JPEGExporter) exporter;
        this.exporter
            .setWidthCm(NbPreferences.forModule(UIExporterJPEG.class).getFloat(PREF_WIDTH, DEFAULTS.getWidthCm()));
        this.exporter
            .setHeightCm(NbPreferences.forModule(UIExporterJPEG.class).getFloat(PREF_HEIGHT, DEFAULTS.getHeightCm()));
        this.exporter.setDpi(NbPreferences.forModule(UIExporterJPEG.class).getInt(PREF_DPI, DEFAULTS.getDpi()));
        if (panel != null) {
            panel.setup(this.exporter);
        }
    }

    @Override
    public void unsetup(boolean update) {
        if (update && panel != null && exporter != null) {
            panel.unsetup(exporter);
            NbPreferences.forModule(UIExporterJPEG.class).putFloat(PREF_WIDTH, exporter.getWidthCm());
            NbPreferences.forModule(UIExporterJPEG.class).putFloat(PREF_HEIGHT, exporter.getHeightCm());
            NbPreferences.forModule(UIExporterJPEG.class).putInt(PREF_DPI, exporter.getDpi());
        }
        panel = null;
        exporter = null;
    }

    @Override
    public boolean isUIForExporter(Exporter exporter) {
        return exporter instanceof JPEGExporter;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(UIExporterJPEG.class, "UIExporterJPEG.name");
    }
}
