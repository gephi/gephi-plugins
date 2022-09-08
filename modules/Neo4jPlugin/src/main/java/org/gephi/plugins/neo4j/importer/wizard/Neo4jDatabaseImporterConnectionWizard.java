package org.gephi.plugins.neo4j.importer.wizard;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

import javax.swing.event.ChangeListener;
import java.awt.*;

public class Neo4jDatabaseImporterConnectionWizard implements WizardDescriptor.ValidatingPanel {

    private final Neo4jDatabaseImporterConnectionPanel component = new Neo4jDatabaseImporterConnectionPanel();

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
        this.component.getDbUrl().setText(Utils.isEmptyOrNull(Neo4jImporterWizardData.dbUrl) ? "neo4j://localhost" : Neo4jImporterWizardData.dbUrl);
        this.component.getDbName().setText(Neo4jImporterWizardData.dbName != null ? Neo4jImporterWizardData.dbName : "");
        this.component.getDbAuthType().setSelectedIndex(Neo4jImporterWizardData.dbAuthType != null ? Neo4jImporterWizardData.dbAuthType : 0);
        this.component.getDbUsername().setText(Utils.isEmptyOrNull(Neo4jImporterWizardData.dbUsername) ? "neo4j" : Neo4jImporterWizardData.dbUsername);
        this.component.getDbPassword().setText(Neo4jImporterWizardData.dbPassword != null ? Neo4jImporterWizardData.dbPassword : "");
    }

    @Override
    public void storeSettings(Object settings) {
        Neo4jImporterWizardData.dbUrl = this.component.getDbUrl().getText();
        Neo4jImporterWizardData.dbName = Utils.isEmptyOrNull(this.component.getDbName().getText()) ? null : this.component.getDbName().getText();
        Neo4jImporterWizardData.dbAuthType = this.component.getDbAuthType().getSelectedIndex();
        Neo4jImporterWizardData.dbUsername = Utils.isEmptyOrNull(this.component.getDbUsername().getText()) ? "neo4j" : this.component.getDbUsername().getText();
        Neo4jImporterWizardData.dbPassword = this.component.getDbPassword().getText();
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
