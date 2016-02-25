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
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.spi.ByteExporter;
import org.gephi.io.exporter.spi.GraphExporter;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.plugin.items.EdgeItem;
import org.gephi.preview.plugin.items.NodeItem;
import org.gephi.preview.types.EdgeColor;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

/**
 * Exports Gephi graphs to KMZ files.
 *
 * @author Dave Shepard
 */
public class KMZExporter implements GraphExporter, ByteExporter, LongTask {

    private boolean exportVisible;
    private Workspace workspace;
    private boolean cancelled;
    private ProgressTicket ticket;
    private File rootDir;
    private OutputStream outputStream;

    private Column longitudeColumn;
    private Column latitudeColumn;
    private Column[] columnsToExport;

    private int maxEdgeWidth  = 0;
    private int maxNodeRadius = 0;

    @Override
    public void setExportVisible(boolean bln) {
        exportVisible = bln;
    }

    @Override
    public boolean isExportVisible() {
        return exportVisible;
    }

    public void setColumnsToUse(Column longitudeColumn, Column latitudeColumn, Column[] columnsToExport) {
        this.longitudeColumn = longitudeColumn;
        this.latitudeColumn = latitudeColumn;
        this.columnsToExport = columnsToExport;
    }

    void setEdgeAndNodeDimensions(int width, int radius) {
        maxEdgeWidth = width;
        maxNodeRadius = radius;
    }

    @Override
    public boolean execute() {

        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        PreviewProperties props = controller.getModel(workspace).getProperties();
        PreviewModel previewModel = workspace.getLookup().lookup(PreviewModel.class);
        int width = 50;
        int height = 50;
        props.putValue("width", width);
        props.putValue("height", height);

        // 1. Validate -- do we have lat/lon columns?
        ticket.start();
        Progress.start(ticket);


        int renderablesCount = 0;
        ArrayList<NodeItem> validNodes = new ArrayList<NodeItem>();
        double invalidNodeCount = 0,
                totalNodes = 0;

        GraphModel model = Lookup.getDefault().lookup(GraphModel.class);
//        AttributeModel model = Lookup.getDefault().lookup(AttributeController.class).getModel();
        Float maxSize = new Float(0.0);

        if (longitudeColumn == null || latitudeColumn == null) {
            GeoAttributeFinder gaf = new GeoAttributeFinder();
            gaf.findGeoFields(ColumnUtils.getColumns(model.getNodeTable()));
            setColumnsToUse(gaf.getLongitudeColumn(), gaf.getLatitudeColumn(), ColumnUtils.getColumns(model.getNodeTable()));
        }

        for (Item ni : previewModel.getItems(Item.NODE)) {
            Node n = (Node) ni.getSource();
            Float size = (Float) ni.getData(NodeItem.SIZE);

            if (size > maxSize) {
                maxSize = size;
            }

            boolean hasLat = false,
                    hasLon = false;

            if (n.getAttribute(latitudeColumn) != null) {
                hasLat = true;
            }
            if (n.getAttribute(longitudeColumn) != null) {
                hasLon = true;
            }

            if (hasLat && hasLon) {
                validNodes.add((NodeItem) ni);
                renderablesCount++;
            } else {
                invalidNodeCount++;
            }
            totalNodes++;
        }

        float maxWeight = 0, 
                minWeight = 0;
        for (Item i : previewModel.getItems(Item.EDGE)) {
            Float weight = (Float) i.getData(EdgeItem.WEIGHT);
            if (weight > maxWeight) {
                maxWeight = weight;
                renderablesCount++;
            }
            if (minWeight == 0 || weight < minWeight) {
                minWeight = weight;
            }
        }

        if (invalidNodeCount == totalNodes) {
            // no valid nodes: exit
            String message = "Although columns with geocoordinates were found,"
                    + " all of the values in them were null."
                    + " This may have happened because the Preview needs to be "
                    + " refreshed. Please click the Refresh button and try again."
            ;

            JOptionPane.showMessageDialog(null, message, "Geocoordinates Not Found", JOptionPane.ERROR_MESSAGE);
            return false;
        } else if (invalidNodeCount > validNodes.size()) {
            double nodesWithoutCoordinates = invalidNodeCount / totalNodes;
            String message = "Warning: " + (int) (nodesWithoutCoordinates * 100)
                    + "% of the nodes in this graph have no geocoordinates.\n"
                    + " A KMZ will still be produced, but it may not have very many"
                    + " nodes or edges.";
            JOptionPane.showMessageDialog(null, message, "Few geocoordinates found.", JOptionPane.ERROR_MESSAGE);
        }

        // 2. Produce export
        final Kml kml = new Kml();
        final Folder folder = kml.createAndSetFolder();
        // 2a. produce nodes
        int styleCounter = 0;

        ticket.setDisplayName("Finding nodes");
        ticket.start(validNodes.size());

        HashMap<Integer, Color> modularityClassColors = new HashMap<Integer, Color>();
        HashMap<Object, Color> nodeColors = new HashMap<Object, Color>();
        IconRenderer renderer = new IconRenderer(maxNodeRadius);

        double maxScale = 2.0;
        for (NodeItem ni : validNodes) {
            Node n = (Node) ni.getSource();
            renderer.render(ni, props);
            String iconFilename = renderer.getLastFilename();
            Float weight = (Float) ni.getData(NodeItem.SIZE);

            String description = "";
            for (Column ac : columnsToExport) {
                description += ac.getTitle() + ": " + n.getAttribute(ac) + "\n";
            }
            
            nodeColors.put(n.getId(), (Color) ni.getData(NodeItem.COLOR));
            Placemark placemark = folder.createAndAddPlacemark().withName((String) n.getAttribute("Label")).withDescription(description);

            Style style = folder.createAndAddStyle().withId("style_" + styleCounter);
            style.createAndSetIconStyle().withScale((weight / maxSize) * maxScale).withIcon(new Icon().withHref(iconFilename));

            placemark.setStyleUrl("#style_" + styleCounter);
            placemark.createAndSetPoint().addToCoordinates((Double) n.getAttribute(longitudeColumn),
                    (Double) n.getAttribute(latitudeColumn));
            styleCounter++;

            if (cancelled) {
                return false;
            }
        }

        if (styleCounter == 0) {
            JOptionPane.showMessageDialog(null, "Sorry, the Preview has not been rendered correctly.\n"
                    + " Please try switching to Preview mode and running the plugin again.");
            return false;
        }

        ticket.setDisplayName("Exporting edges");
        // 2b. produce edges
        EdgeColor ec = previewModel.getProperties().getValue(PreviewProperty.EDGE_COLOR);
        for (Item i : previewModel.getItems(Item.EDGE)) {
            Edge e = (Edge) i.getSource();
            Node source =  e.getSource();
            Node targe =  e.getTarget();
            // It's possible for an edge to have a source or target that doesn't
            // have geocoordinates, so we skip those.
            if (source == null || targe == null) {
                continue;
            }
            if (source.getAttribute(latitudeColumn) == null 
                    || source.getAttribute(longitudeColumn) == null
                    || targe.getAttribute(latitudeColumn) == null 
                    || targe.getAttribute(longitudeColumn) == null) {
                continue;
            }
            float weight = (Float) i.getData(EdgeItem.WEIGHT);

            String title = i.getData(EdgeItem.EDGE_LABEL);
            if (title == null) {
                title = source.getAttribute("Label") + " and " + targe.getAttribute("Label");
            }
            String description = "";
            for (Column ac : ColumnUtils.getColumns(model.getEdgeTable())) {
                if ((ac.getTitle() == null ? latitudeColumn != null
                        : !ac.getTitle().equals(latitudeColumn.getTitle()))
                        && (ac.getTitle() == null ? longitudeColumn != null
                        : !ac.getTitle().equals(longitudeColumn.getTitle()))) {

                    // Filter labels with null attributes
                    if (e.getAttribute(ac) != null) {
                        description += ac.getTitle() + ": " + e.getAttribute(ac) + "\n";
                    }
                }
            }

            // Default is whitish
            
            
            Color color = i.getData(EdgeItem.COLOR);
            switch(ec.getMode()) {
                case SOURCE:
                    color = nodeColors.get(e.getSource().getId());
                    break;
                case TARGET:
                    color = nodeColors.get(e.getTarget().getId());
                    break;
                case MIXED:
                    EdgeColor tempEdgeColor = new EdgeColor(ec.getMode());
                    color = tempEdgeColor.getColor(null, 
                            nodeColors.get(e.getSource().getId()), 
                            nodeColors.get(e.getTarget().getId())
                    );
                    break;
                default:
                    color = (Color) i.getData(EdgeItem.COLOR);
            }
            
            String colorCode = "#33ffffff";
            if (color != null) {
                colorCode = "#" + Integer.toHexString(color.getAlpha()) + ""
                        + Integer.toHexString(color.getRed())
                        + Integer.toHexString(color.getGreen())
                        + Integer.toHexString(color.getBlue());
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

            placemark.createAndSetLineString().addToCoordinates((Double) source.getAttribute(longitudeColumn),
                    (Double) source.getAttribute(latitudeColumn), 0).addToCoordinates((Double) targe.getAttribute(longitudeColumn),
                    (Double) targe.getAttribute(latitudeColumn), 0).withTessellate(Boolean.TRUE).withExtrude(Boolean.TRUE);

            styleCounter++;


            if (cancelled) {
                return false;
            }
        }
        try {
            writeKMZ(kml, renderer);
            JOptionPane.showMessageDialog(null, "Export complete",
                    "KML Export complete.", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(KMZExporter.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Error saving.",
                    "Sorry, could not write the file.", JOptionPane.ERROR_MESSAGE);
        } finally {
            ticket.finish();
            return true;
        }
    }

    private synchronized void writeKMZ(Kml kml, IconRenderer icons) throws IOException {
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
