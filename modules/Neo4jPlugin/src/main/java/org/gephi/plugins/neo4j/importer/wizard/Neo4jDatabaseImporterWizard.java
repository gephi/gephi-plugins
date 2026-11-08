package org.gephi.plugins.neo4j.importer.wizard;

import org.gephi.io.importer.spi.Importer;
import org.gephi.io.importer.spi.ImporterWizardUI;
import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.plugins.neo4j.importer.Neo4jDatabaseImporter;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ImporterWizardUI.class)
public class Neo4jDatabaseImporterWizard implements ImporterWizardUI {

    private Panel[] panels = null;

    @Override
    public String getDisplayName() {
        return "Neo4j - Import networks from a Neo4j database";
    }

    @Override
    public String getCategory() {
        return "import";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public Panel[] getPanels() {
        if (panels == null) {
            panels = new Panel[2];
            panels[0] = new Neo4jDatabaseImporterConnectionWizard();
            panels[1] = new Neo4jDatabaseImporterImportWizard();
        }
        return panels;
    }

    @Override
    public void setup(Panel panel) {
        System.out.println(panel);

        /*this.importer = new Neo4jDatabaseImporter();
        i
        */
    }

    @Override
    public void unsetup(WizardImporter importer, Panel panel) {
        this.panels = null;
        if (this.isUIForImporter(importer)) {
            Neo4jDatabaseImporter neoImporter = (Neo4jDatabaseImporter) importer;
            neoImporter.setUrl(Neo4jImporterWizardData.dbUrl);
            neoImporter.setDBName(Utils.isEmptyOrNull(Neo4jImporterWizardData.dbName) ? null : Neo4jImporterWizardData.dbName);
            if (Neo4jImporterWizardData.dbAuthType == 0) {
                neoImporter.setUsername(Neo4jImporterWizardData.dbUsername);
                neoImporter.setPasswd(Neo4jImporterWizardData.dbPassword);
            } else {
                neoImporter.setUsername(null);
                neoImporter.setPasswd(null);
            }

            if (Neo4jImporterWizardData.importMode == 0) {
                neoImporter.setLabels(Neo4jImporterWizardData.labels.size() == 0 ? null : Neo4jImporterWizardData.labels);
                neoImporter.setRelationshipTypes(Neo4jImporterWizardData.relationshipTypes.size() == 0 ? null : Neo4jImporterWizardData.relationshipTypes);
                neoImporter.setNodeQuery(null);
                neoImporter.setEdgeQuery(null);
            } else {
                neoImporter.setLabels(null);
                neoImporter.setRelationshipTypes(null);
                neoImporter.setNodeQuery(Neo4jImporterWizardData.nodeQuery);
                neoImporter.setEdgeQuery(Neo4jImporterWizardData.edgeQuery);
            }
        }
    }

    @Override
    public boolean isUIForImporter(Importer importer) {
        return importer instanceof Neo4jDatabaseImporter;
    }
}
