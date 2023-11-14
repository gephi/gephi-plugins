package org.gephi.plugins.neo4j.importer;

import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.io.importer.spi.WizardImporterBuilder;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = WizardImporterBuilder.class)
public class Neo4jDatabaseImpoterBuilder implements WizardImporterBuilder {

    @Override
    public WizardImporter buildImporter() {
        return new Neo4jDatabaseImporter();
    }

    @Override
    public String getName() {
        return "Neo4j";
    }
}
