package org.gephi.plugins.jpegexporter;

import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.VectorExporter;
import org.gephi.io.exporter.spi.VectorFileExporterBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = VectorFileExporterBuilder.class)
public class ExporterBuilderJPEG implements VectorFileExporterBuilder {

    @Override
    public VectorExporter buildExporter() {
        return new JPEGExporter();
    }

    @Override
    public FileType[] getFileTypes() {
        return new FileType[]{
            new FileType(".jpg", NbBundle.getMessage(ExporterBuilderJPEG.class, "fileType_JPG_Name")),
            new FileType(".jpeg", NbBundle.getMessage(ExporterBuilderJPEG.class, "fileType_JPEG_Name"))
        };
    }

    @Override
    public String getName() {
        return "jpeg";
    }
}
