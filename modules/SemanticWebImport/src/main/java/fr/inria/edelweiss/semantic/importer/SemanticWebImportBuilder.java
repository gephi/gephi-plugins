/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.importer;

import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory class for building the importer instances.
 * @author edemairy
 */
@ServiceProvider(service = FileImporterBuilder.class)
public class SemanticWebImportBuilder implements FileImporterBuilder {

    private static String lastFileName = "";

    @Override
    public final FileImporter buildImporter() {
        return new SemanticWebImporter();
    }

    @Override
    public final FileType[] getFileTypes() {
        return new FileType[]{
                    new FileType(".rdf", "RDF files"),
                    new FileType(".rdfs", "RDFS files")
                };
    }

    @Override
    public final boolean isMatchingImporter(final org.openide.filesystems.FileObject fileObject) {
        boolean result = (fileObject.getExt().equalsIgnoreCase("rdf") || fileObject.getExt().equalsIgnoreCase("rdfs"));
        if (result) {
            setLastFileName(fileObject.getPath());
        }
        return result;
    }

    @Override
    public final String getName() {
        return "SemanticWebImportBuilder";
    }

    /**
     * @return the lastFileName
     */
    public static String getLastFileName() {
        return lastFileName;
    }

    /**
     * @param lastFileName the lastFileName to set
     */
    private static void setLastFileName(String lastFileName) {
        SemanticWebImportBuilder.lastFileName = lastFileName;
    }
}
