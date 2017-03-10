package org.bitnine.MyImporter.plugin;


import org.gephi.io.importer.api.FileType;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.importer.spi.FileImporterBuilder;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author dehowefeng
 */
@ServiceProvider(service = FileImporterBuilder.class)
public class MyImporterBuilder implements FileImporterBuilder {

   public FileImporter buildImporter() {
        return new MyImporter();
    }


    public FileType[] getFileTypes() {
        return new FileType[]{new FileType(".foo", "Foo files")};
    }

    public boolean isMatchingImporter(FileObject fileObject) {
        return fileObject.getExt().equalsIgnoreCase("foo");
    }

    public String getName() {
        return "foo";
    }
}