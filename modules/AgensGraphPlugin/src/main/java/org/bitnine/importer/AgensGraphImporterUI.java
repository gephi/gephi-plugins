/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bitnine.importer;

import javax.swing.JPanel;
import org.gephi.io.database.drivers.SQLDriver;
import org.gephi.io.importer.api.Database;
import org.gephi.io.importer.plugin.database.ImporterBuilderEdgeList;
import org.gephi.io.importer.plugin.database.ImporterEdgeList;
import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dehowefeng
 */


@ServiceProvider(service=ImporterUI.class)
public class AgensGraphImporterUI implements ImporterUI {

    private AgensGraphImportPanel panel;
    private DatabaseImporter[] importers;

    @Override
    public void setup(Importer[] importers) {
        this.importers = (DatabaseImporter[]) importers;
        if (panel == null) {
            panel = new AgensGraphImportPanel();
        }

        //Driver Combo
        SQLDriver[] driverArray = new SQLDriver[0];
        driverArray = Lookup.getDefault().lookupAll(SQLDriver.class).toArray(driverArray);

        panel.setup();
    }

    @Override
    public JPanel getPanel() {
        if (panel == null) {
            panel = new AgensGraphImportPanel();
        }
        return AgensGraphImportPanel.createValidationPanel(panel);
    }

    @Override
    public void unsetup(boolean update) {
        if (update) {
            Database database = panel.getSelectedDatabase();
            for (DatabaseImporter importer : importers) {
                importer.setDatabase(database);
            }
        }
        panel = null;
        importers = null;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "AgensGraphBuilder.displayName");
    }

    public String getIdentifier() {
        return ImporterBuilderEdgeList.IDENTIFER;
    }

    @Override
    public boolean isUIForImporter(Importer importer) {
        return importer instanceof ImporterEdgeList;
    }
}