/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.uic.cs.PairImporter;

import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author joshua
 */
@ServiceProvider(service = FileImporterBuilder.class)
public class PairImporterBuilder implements FileImporterBuilder {
    public String getName() {
        return "pair";
    }
 
    public FileType[] getFileTypes() {
        return new FileType[]{new FileType(".pair", "Pair files")};
    }

    @Override
    public FileImporter buildImporter() {
        return new PairImporter();

    }

    @Override
    public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.getExt().equalsIgnoreCase("pair");
    }

    
}
