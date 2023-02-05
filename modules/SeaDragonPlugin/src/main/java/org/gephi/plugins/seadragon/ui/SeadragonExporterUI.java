/*
 * Gephi Seadragon Plugin
 *
 * Copyright 2010-2011 Gephi
 * Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 * Licensed under Apache 2 License (http://www.apache.org/licenses/LICENSE-2.0)
 */

package org.gephi.plugins.seadragon.ui;

import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.gephi.desktop.io.export.spi.ExporterClassUI;
import org.gephi.lib.validation.DialogDescriptorWithValidation;
import org.gephi.plugins.seadragon.SeadragonExporter;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.api.LongTaskErrorHandler;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.netbeans.validation.api.ui.swing.ValidationPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * @author Mathieu Bastian
 */
@ServiceProvider(service = ExporterClassUI.class)
public class SeadragonExporterUI implements ExporterClassUI {

    private final ExporterSettings settings = new ExporterSettings();
    private final LongTaskListener longTaskListener;
    private final LongTaskErrorHandler errorHandler;
    private boolean cancelled = true;
    private String filePath;

    public SeadragonExporterUI() {
        longTaskListener = new LongTaskListener() {

            @Override
            public void taskFinished(LongTask task) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        if (!cancelled) {
                            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

                            if (desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
                                Object[] options = {"Open in browser", "OK"};
                                int n = JOptionPane.showOptionDialog(WindowManager.getDefault().getMainWindow(),
                                    "Seadragon export finished.",
                                    "Export finished",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.INFORMATION_MESSAGE,
                                    null, //do not use a custom Icon
                                    options, //the titles of buttons
                                    options[0]); //default button title
                                if (n == 0) {
                                    try {
                                        File f = new File(filePath + File.separator + "seadragon.html");
                                        if (f.exists()) {
                                            desktop.browse(f.toURI());
                                        }
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            } else {
                                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                    "Seadragon export finished.", "Export finished", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    }
                });
            }
        };
        errorHandler = new LongTaskErrorHandler() {

            @Override
            public void fatalError(Throwable t) {
                cancelled = true;
                String message = t.getCause().getMessage();
                if (message == null || message.isEmpty()) {
                    message = t.getMessage();
                }
                NotifyDescriptor.Message msg = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(msg);
            }
        };
    }

    @Override
    public String getName() {
        return "Seadragon Web...";
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public void action() {

        final SeadragonExporter exporter = new SeadragonExporter();
        settings.load(exporter);

        SeadragonSettingsPanel panel = new SeadragonSettingsPanel();
        panel.setup(exporter);
        ValidationPanel validationPanel = (ValidationPanel) SeadragonSettingsPanel.createValidationPanel(panel);

        DialogDescriptor dd = DialogDescriptorWithValidation.dialog(validationPanel, "Seadragon Web Export");
        Object result = DialogDisplayer.getDefault().notify(dd);
        if (result == NotifyDescriptor.OK_OPTION) {
            panel.unsetup(true);
            settings.save(exporter);
            filePath = exporter.getPath().getAbsolutePath();

            LongTaskExecutor executor = new LongTaskExecutor(true, "Seadragon");
            executor.setLongTaskListener(longTaskListener);
            executor.setDefaultErrorHandler(errorHandler);
            executor.execute(exporter, new Runnable() {

                @Override
                public void run() {
                    Workspace currentWorkspace =
                        Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace();
                    exporter.setWorkspace(currentWorkspace);
                    cancelled = !exporter.execute();
                }
            });
        } else {
            panel.unsetup(false);
        }
    }

    private static class ExporterSettings {

        final String LAST_WIDTH = "SeadragonExporterUI_Last_Width";
        final String LAST_HEIGHT = "SeadragonExporterUI_Last_Height";
        final String LAST_MARGIN = "SeadragonExporterUI_Last_Margin";
        final String LAST_TILESIZE = "SeadragonExporterUI_Last_TileSize";
        private int overlap = 1;
        private int width = 8192;
        private int height = 6144;
        private int tileSize = 256;
        private int margin = 4;

        public void save(SeadragonExporter exporter) {
            this.overlap = exporter.getOverlap();
            this.width = (int) exporter.getWidth();
            this.height = (int) exporter.getHeight();
            this.tileSize = exporter.getTileSize();
            this.margin = exporter.getMargin();

            NbPreferences.forModule(SeadragonExporterUI.class).putInt(LAST_WIDTH, width);
            NbPreferences.forModule(SeadragonExporterUI.class).putInt(LAST_HEIGHT, height);
            NbPreferences.forModule(SeadragonExporterUI.class).putFloat(LAST_MARGIN, margin);
            NbPreferences.forModule(SeadragonExporterUI.class).putInt(LAST_TILESIZE, tileSize);
        }

        public void load(SeadragonExporter exporter) {
            width = NbPreferences.forModule(SeadragonExporterUI.class).getInt(LAST_WIDTH, width);
            height = NbPreferences.forModule(SeadragonExporterUI.class).getInt(LAST_HEIGHT, height);
            margin = NbPreferences.forModule(SeadragonExporterUI.class).getInt(LAST_MARGIN, margin);
            tileSize = NbPreferences.forModule(SeadragonExporterUI.class).getInt(LAST_TILESIZE, tileSize);

            exporter.setOverlap(overlap);
            exporter.setWidth(width);
            exporter.setHeight(height);
            exporter.setTileSize(tileSize);
            exporter.setMargin(margin);
        }
    }
}
