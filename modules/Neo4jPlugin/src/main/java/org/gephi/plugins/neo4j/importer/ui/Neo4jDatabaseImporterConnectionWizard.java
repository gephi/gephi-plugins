package org.gephi.plugins.neo4j.importer.ui;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import javax.swing.event.ChangeListener;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class Neo4jDatabaseImporterConnectionWizard implements WizardDescriptor.Panel<WizardDescriptor>, PropertyChangeListener {

    private Neo4jDatabaseImporterConnectionPanel component = new Neo4jDatabaseImporterConnectionPanel();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    public Component getComponent() {
        return this.component;
    }

    @Override
    public HelpCtx getHelp() {
        return null;
    }

    @Override
    public void readSettings(WizardDescriptor settings) {

    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    public void addChangeListener(ChangeListener l) {

    }

    @Override
    public void removeChangeListener(ChangeListener l) {

    }
}
