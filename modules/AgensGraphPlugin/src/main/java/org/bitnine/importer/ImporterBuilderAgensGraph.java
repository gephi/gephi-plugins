/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.bitnine.importer;

import org.gephi.io.importer.spi.DatabaseImporter;
import org.gephi.io.importer.spi.DatabaseImporterBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dehowefeng
 */
@ServiceProvider(service = DatabaseImporterBuilder.class)
public class ImporterBuilderAgensGraph implements DatabaseImporterBuilder{
    
        public static final String IDENTIFER = "agensgraph";

    @Override
    public DatabaseImporter buildImporter() {
        return new ImporterAgensGraph();
    }

    @Override
    public String getName() {
        return IDENTIFER;
    }
    
}
