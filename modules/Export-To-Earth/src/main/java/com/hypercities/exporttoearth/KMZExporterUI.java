/**
 * Copyright (c) 2012, David Shepard All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */
package com.hypercities.exporttoearth;

import javax.swing.JPanel;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.io.exporter.spi.ExporterUI;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * UI for panel
 * 
 * @author Dave Shepard
 */
@ServiceProvider(service = ExporterUI.class)
public class KMZExporterUI implements ExporterUI {

    private AttributeColumnSelectionPanel panel;
    private KMZExporter exporter;

    private GraphModel model;
    private Column longitudeColumn;
    private Column latitudeColumn;

    @Override
    public JPanel getPanel() {
        // get all fields

        Table nodeTable = model.getNodeTable();
        Column[] columns = new Column[nodeTable.countColumns()];
        for (int i = 0; i < nodeTable.countColumns(); i++) {
            columns[i] = nodeTable.getColumn(i);
        }

        // get geocoordinate fields
        GeoAttributeFinder gaf = new GeoAttributeFinder();
        gaf.findGeoFields(columns);
        longitudeColumn = gaf.getLongitudeColumn();
        latitudeColumn = gaf.getLatitudeColumn();
        // for each column, create a new label, checkbox, lat radio button and lon radio button
        // checkboxes are stored in a hash of objects, item to column name
        panel = new AttributeColumnSelectionPanel(columns, longitudeColumn, latitudeColumn);
        return panel;
    }

    @Override
    public void setup(Exporter exprtr) {
        model = Lookup.getDefault().lookup(GraphModel.class);
        exporter = (KMZExporter)exprtr;
    }

    @Override
    public void unsetup(boolean update) {
        if (update) {
            // the user hit OK; save everything
            exporter.setColumnsToUse(panel.getLongitudeColumn(), 
                    panel.getLatitudeColumn(), 
                    panel.getColumnsToExport()
                );
            exporter.setEdgeAndNodeDimensions(
                panel.getMaxEdgeWidth(), 
                panel.getMaxNodeRadius()
            );
        } else {
            // cancel was hit
        }
        panel = null;
        exporter = null;
    }

    @Override
    public boolean isUIForExporter(Exporter exprtr) {
        return exprtr instanceof KMZExporter;
    }

    @Override
    public String getDisplayName() {
        return "Select latitude and longitude fields";
    }
    
}
