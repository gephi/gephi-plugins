package org.gephi.plugins.neo4j.importer.wizard;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

import javax.swing.event.ChangeListener;
import java.awt.*;

public class Neo4jDatabaseImporterImportWizard implements WizardDescriptor.ValidatingPanel {

    private final Neo4jDatabaseImporterImportPanel component = new Neo4jDatabaseImporterImportPanel();


    @Override
    public Component getComponent() {
        return this.component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void readSettings(Object settings) {
        this.component.getLabels().setListData(Utils.neo4jWizardGetLabels().toArray(new String[0]));
        this.component.getTypes().setListData(Utils.neo4jWizardGetRelationshipTypes().toArray(new String[0]));
        this.component.getMenu().setSelectedIndex(Neo4jImporterWizardData.importMode != null ? Neo4jImporterWizardData.importMode : 0);
        this.component.getNodeQuery().setText(Utils.isEmptyOrNull(Neo4jImporterWizardData.nodeQuery) ? "MATCH (n) RETURN id(n) AS id, labels(n) AS labels" : Neo4jImporterWizardData.nodeQuery);
        this.component.getEdgeQuery().setText(Utils.isEmptyOrNull(Neo4jImporterWizardData.edgeQuery) ? "MATCH (n)-[r]->(m) RETURN id(r) AS id, type(r) AS type, id(n) AS sourceId, id(m) AS targetId" : Neo4jImporterWizardData.edgeQuery);
    }

    @Override
    public void storeSettings(Object settings) {
        Neo4jImporterWizardData.importMode = this.component.getMenu().getSelectedIndex();
        Neo4jImporterWizardData.labels = this.component.getLabels().getSelectedValuesList();
        Neo4jImporterWizardData.relationshipTypes = this.component.getTypes().getSelectedValuesList();
        Neo4jImporterWizardData.nodeQuery = this.component.getNodeQuery().getText();
        Neo4jImporterWizardData.edgeQuery = this.component.getEdgeQuery().getText();
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void validate() throws WizardValidationException {
        try {
            this.component.checkValidity();
        } catch (Exception e) {
            throw new WizardValidationException(this.component, e.getMessage(), e.getLocalizedMessage());
        }
    }

    @Override
    public void addChangeListener(ChangeListener l) {

    }

    @Override
    public void removeChangeListener(ChangeListener l) {

    }
}
