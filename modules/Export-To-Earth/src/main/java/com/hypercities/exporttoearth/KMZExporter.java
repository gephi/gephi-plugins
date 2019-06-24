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

import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Icon;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Exports Gephi graphs to KMZ files.
 *
 * @author Dave Shepard
 */
public class KMZExporter implements GraphExporter, ByteExporter, LongTask {

    private Logger logger = Logger.getLogger("");
    private boolean exportVisible;
    private Workspace workspace;
    private boolean cancelled;
    private ProgressTicket ticket;
    private OutputStream outputStream;

    private Column longitudeColumn;
    private Column latitudeColumn;
    private Column[] columnsToExport;

    private int maxEdgeWidth  = ColumnSelectionPanel.DEFAULT_EDGE_WIDTH;
    private int maxNodeRadius = ColumnSelectionPanel.DEFAULT_NODE_RADIUS;

    private static Column[] getColumns(Table table) {
        Column[] columns = new Column[table.countColumns()];

        for (int i = 0; i < columns.length; i++) {
            columns[i] = table.getColumn(i);
        }

        return columns;
    }
    

    private String getMessage(String resourceName) {
        return NbBundle.getMessage(KMZExporter.class, resourceName);
    }

    @Override
    public void setExportVisible(boolean bln) {
        exportVisible = bln;
    }

    @Override
    public boolean isExportVisible() {
        return exportVisible;
    }

    public void setColumnsToUse(Column longitudeColumn, Column latitudeColumn, Column[] columnsToExport) {
        logger.log(Level.INFO, "Long column: {0}", longitudeColumn);
        logger.log(Level.INFO, "Lat column: {0}", latitudeColumn);
        this.longitudeColumn = longitudeColumn;
        this.latitudeColumn = latitudeColumn;
        this.columnsToExport = columnsToExport;
    }

    void setEdgeAndNodeDimensions(int width, int radius) {
        maxEdgeWidth = width;
        maxNodeRadius = radius;
    }

    private boolean checkNumericColumn(Column col) {
        return AttributeUtils.isNumberType(col.getTypeClass())
                && !AttributeUtils.isArrayType(col.getTypeClass())
                && !AttributeUtils.isDynamicType(col.getTypeClass());
    }

    @Override
    public boolean execute() {
        ticket.setDisplayName(getMessage("EvaluatingGraph"));
        Progress.start(ticket);
        
        // 1. Validate -- do we have lat/lon columns?
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel model = graphController.getGraphModel(workspace);
        Graph graph;
        if (exportVisible) {
            graph = model.getGraphVisible();
        } else {
            graph = model.getGraph();
        }


        ArrayList<Node> validNodes = new ArrayList<Node>();
        float maxSize = 0.0f;
        double invalidNodeCount = 0,
                totalNodes = graph.getNodeCount()
        ;
        if (longitudeColumn == null || latitudeColumn == null) {
            GeoAttributeFinder gaf = new GeoAttributeFinder();
            gaf.findGeoFields(getColumns(model.getNodeTable()));
            setColumnsToUse(gaf.getLongitudeColumn(), gaf.getLatitudeColumn(), getColumns(model.getNodeTable()));
        }
        
        if (longitudeColumn == null || latitudeColumn == null) {
            JOptionPane.showMessageDialog(null, 
                    getMessage("MissingGeocoordinatesFoundWarningMessage"), 
                    getMessage("MissingGeocoordinatesFoundWarningTitle"), 
                    JOptionPane.ERROR_MESSAGE
            );
            
            Progress.finish(ticket);
            return false;
        }
        
        if(!checkNumericColumn(longitudeColumn) || !checkNumericColumn(latitudeColumn)){
            JOptionPane.showMessageDialog(null, 
                    getMessage("NotNumericGeocoordinatesFoundWarningMessage"), 
                    getMessage("NotNumericGeocoordinatesFoundWarningTitle"), 
                    JOptionPane.ERROR_MESSAGE
            );
            
            Progress.finish(ticket);
            return false;
        }

        for (Node node : graph.getNodes()) {
            float size = node.size();
            if (size > maxSize) {
                maxSize = size;
            }

            boolean hasLat = false,
                    hasLon = false
            ;

            if (node.getAttribute(latitudeColumn) != null) {
                hasLat = true;
            }
            if (node.getAttribute(longitudeColumn) != null) {
                hasLon = true;
            }

            if (hasLat && hasLon) {
                validNodes.add(node);
            } else {
                invalidNodeCount++;
            }
        }

        double maxWeight = 0, 
              minWeight = 0
        ;

        for (Edge edge : graph.getEdges()) {
            double weight = edge.getWeight();
            if (weight > maxWeight) {
                maxWeight = weight;
            }

            if (minWeight == 0 || weight < minWeight) {
                minWeight = weight;
            }
        }

        if (invalidNodeCount == totalNodes) {
            // no valid nodes: exit
            JOptionPane.showMessageDialog(null, 
                    getMessage("NoGeocoordinatesFoundWarningMessage"), 
                    getMessage("NoGeoCoordinatesFoundWarningTitle"), 
                    JOptionPane.ERROR_MESSAGE
            );
            
            Progress.finish(ticket);
            return false;
        } else if (invalidNodeCount > validNodes.size()) {
            int nodesWithoutCoordinates = (int)(invalidNodeCount / totalNodes * 100);
            JOptionPane.showMessageDialog(null, 
                    String.format(getMessage("FewGeocoordinatesFoundWarningMessage"), 
                            nodesWithoutCoordinates),
                    getMessage("FewGeocoordinatesFoundWarningTItle"),
                    JOptionPane.ERROR_MESSAGE
            );
        }

        // 2. Produce export
        final Kml kml = new Kml();
        final Folder folder = kml.createAndSetFolder();
        // 2a. produce nodes
        int styleCounter = 0;

        Progress.setDisplayName(ticket, getMessage("ExportingNodes"));

        HashMap<Object, Color> nodeColors = new HashMap<Object, Color>();
        IconRenderer renderer = new IconRenderer(maxNodeRadius);

        double maxScale = 2.0;
        for (Node n : validNodes) {
            renderer.render(n);
            String iconFilename = renderer.getLastFilename();
            float weight = n.size();

            StringBuilder sb = new StringBuilder();
            for (Column ac : columnsToExport) {
                sb.append(ac.getTitle()).append(": ").append(n.getAttribute(ac)).append("\n");
            }
            
            String description = sb.toString();
            nodeColors.put(n.getId(), (Color) n.getColor());
            Placemark placemark = folder.createAndAddPlacemark().withName(n.getLabel()).withDescription(description);

            Style style = folder.createAndAddStyle().withId("style_" + styleCounter);
            style.createAndSetIconStyle().withScale((weight / maxSize) * maxScale).withIcon(new Icon().withHref(iconFilename));

            placemark.setStyleUrl("#style_" + styleCounter);
            
            double longValue = ((Number) n.getAttribute(longitudeColumn)).doubleValue();
            double latValue = ((Number) n.getAttribute(latitudeColumn)).doubleValue();
            placemark.createAndSetPoint().addToCoordinates(
                    longValue,
                    latValue
            );
            styleCounter++;

            if (cancelled) {
                return false;
            }
        }


        Progress.setDisplayName(ticket, getMessage("ExportingEdges"));
        // 2b. produce edges
        for (Edge edge : graph.getEdges()) {
            Node source = edge.getSource();
            Node target = edge.getTarget();

            if (source == null || target == null) {
                continue;
            }

            if (source.getAttribute(latitudeColumn) == null 
                    || source.getAttribute(longitudeColumn) == null
                    || target.getAttribute(latitudeColumn) == null 
                    || target.getAttribute(longitudeColumn) == null) {
                continue;
            }

            double weight = edge.getWeight();
            String title = edge.getLabel();

            if (title == null) {
                title = source.getLabel() + " " + getMessage("and") + " " + target.getLabel();
            }

            StringBuilder sb = new StringBuilder();
            for (Column column : edge.getAttributeColumns()) {
                if (column == latitudeColumn || column == longitudeColumn) {
                    continue;
                }

                if (edge.getAttribute(column) != null) {
                    sb.append(column.getTitle()).append(": ").append(edge.getAttribute(column)).append("\n");
                }
            }
            String description = sb.toString();

            String colorCode = "#33ffffff";
            Color color = edge.getColor();
            if (color != null && edge.alpha() > 0) {//0 alpha means the edge has no specific color
                colorCode = String.format(
                        "#%02x%02x%02x%02x",
                        color.getAlpha(),
                        color.getRed(), 
                        color.getGreen(), 
                        color.getBlue()
                );
            }

            Placemark placemark = folder.createAndAddPlacemark().withDescription(description).withName(title);

            Style style = folder.createAndAddStyle().withId("style_" + styleCounter);
            
            double edgeWidth = 0;
            if (minWeight == maxWeight) {
                edgeWidth = maxEdgeWidth;
            } else {
                edgeWidth = (weight / maxWeight) * maxEdgeWidth;
            }
            style.createAndSetLineStyle().withWidth(edgeWidth).withColorMode(ColorMode.NORMAL).withColor(colorCode);
            placemark.setStyleUrl("#style_" + styleCounter);

            double longValueSource = ((Number) source.getAttribute(longitudeColumn)).doubleValue();
            double latValueSource = ((Number) source.getAttribute(latitudeColumn)).doubleValue();
            double longValueTarget = ((Number) target.getAttribute(longitudeColumn)).doubleValue();
            double latValueTarget = ((Number) target.getAttribute(latitudeColumn)).doubleValue();
            placemark.createAndSetLineString()
                    .addToCoordinates(longValueSource, latValueSource, 0)
                    .addToCoordinates(longValueTarget, latValueTarget, 0)
                    .withTessellate(Boolean.TRUE).withExtrude(Boolean.TRUE);

            styleCounter++;

            if (cancelled) {
                return false;
            }
        }


        Progress.setDisplayName(ticket, getMessage("WritingKMZFile"));
        try {
            writeKMZ(kml, renderer);
            JOptionPane.showMessageDialog(null, getMessage("ExportCompleteMessage"),
                getMessage("ExportCompleteTitle"), JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IOException ex) {
            Logger.getLogger(KMZExporter.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, getMessage("ExportSaveErrorMessage"),
                    getMessage("ExportSaveErrorTitle"), JOptionPane.ERROR_MESSAGE
            );
        } finally {
            Progress.finish(ticket);
        }
        
        return true;
    }

    private void writeKMZ(Kml kml, IconRenderer icons) throws IOException {
        ZipOutputStream out = new ZipOutputStream(outputStream);
        ZipEntry entry = new ZipEntry("doc.kml");
        out.putNextEntry(entry);
        kml.marshal(out);

        icons.renderToKMZ(out);
        out.close();
    }

    @Override
    public void setWorkspace(Workspace wrkspc) {
        workspace = wrkspc;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public boolean cancel() {
        return cancelled = true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        ticket = pt;
    }

    @Override
    public void setOutputStream(OutputStream out) {
        outputStream = out;
    }
}
