package org.gephi.plugins.neo4j.importer.ui;

import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.plugins.neo4j.importer.Neo4jDatabaseImporter;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ImporterWizardUI.class)
public class Neo4jDatabaseImporterWizard implements ImporterWizardUI {

    private Neo4jDatabaseImporter importer = null;
    private Panel[] panels = null;

    @Override
    public String getDisplayName() {
        return "Import networks from a Neo4j database";
    }

    @Override
    public String getCategory() {
        return "importer";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Panel[] getPanels() {
        if (panels == null) {
            panels = new Panel[3];
            panels[0] = new Neo4jDatabaseImporterConnectionWizard();
            panels[1] = new Neo4jDatabaseImporterTypesWizard();
            panels[2] = new Neo4jDatabaseImporterQueriesWizard();
        }
        return panels;
    }

    @Override
    public void setup(Panel panel) {
        this.importer = new Neo4jDatabaseImporter();
    }

    @Override
    public void unsetup(WizardImporter importer, Panel panel) {
        this.panels = null;
        this.importer = null;
    }

    @Override
    public boolean isUIForImporter(Importer importer) {
        return importer instanceof Neo4jDatabaseImporter;
    }
}
