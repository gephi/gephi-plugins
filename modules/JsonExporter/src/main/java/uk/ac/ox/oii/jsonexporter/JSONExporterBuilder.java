/*
Copyright (C) 2012  Scott A. Hale
Website: http://www.scotthale.net/

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/license
 */
package uk.ac.ox.oii.jsonexporter;

import org.gephi.io.exporter.api.FileType;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.io.exporter.spi.GraphFileExporterBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author shale
 */
@ServiceProvider(service = GraphFileExporterBuilder.class)
public class JSONExporterBuilder implements GraphFileExporterBuilder {

    public String getName() {
        return "json";
    }

    public FileType[] getFileTypes() {
        return new FileType[]{new FileType(".json", "JSON Graph")};
    }

    @Override
    public GraphExporter buildExporter() {
        return new JSONExporter();
    }
}
