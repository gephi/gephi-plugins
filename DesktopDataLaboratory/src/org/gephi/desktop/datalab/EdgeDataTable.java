/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Mathieu Jacomy, Julian Bilcke, Eduardo Ramos
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.desktop.datalab;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import javax.swing.DefaultCellEditor;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.AbstractTableModel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeRow;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.type.DynamicBigDecimal;
import org.gephi.data.attributes.type.DynamicBigInteger;
import org.gephi.data.attributes.type.DynamicByte;
import org.gephi.data.attributes.type.DynamicDouble;
import org.gephi.data.attributes.type.DynamicFloat;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.DynamicLong;
import org.gephi.data.attributes.type.DynamicShort;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.NumberList;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.DataLaboratoryHelper;
import org.gephi.datalab.spi.edges.EdgesManipulator;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.tools.api.EditWindowController;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jdesktop.swingx.table.TableColumnModelExt;
import org.openide.awt.MouseUtils;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.gephi.desktop.datalab.utils.PopupMenuUtils;
import org.gephi.desktop.datalab.utils.SparkLinesRenderer;
import org.gephi.desktop.datalab.utils.TimeIntervalCellEditor;
import org.gephi.desktop.datalab.utils.TimeIntervalsRenderer;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;

/**
 *
 * @author Mathieu Bastian
 */
public class EdgeDataTable {

    private boolean useSparklines = false;
    private boolean timeIntervalGraphics = false;
    private boolean showEdgesNodesLabels = false;
    private JXTable table;
    private PropertyEdgeDataColumn[] propertiesColumns;
    private RowFilter rowFilter;
    private Edge[] selectedEdges;
    private AttributeUtils attributeUtils;
    private AttributeColumnsController attributeColumnsController;
    private boolean refreshingTable = false;
    private AttributeColumn[] showingColumns = null;
    private static final int FAKE_COLUMNS_COUNT = 3;
    private EdgeDataTableModel model;
    private TimeIntervalsRenderer timeIntervalsRenderer;
    private TimeIntervalCellEditor timeIntervalCellEditor;
    private TimeFormat currentTimeFormat;
    private SparkLinesRenderer sparkLinesRenderer;

    public EdgeDataTable() {
        attributeUtils = AttributeUtils.getDefault();
        attributeColumnsController = Lookup.getDefault().lookup(AttributeColumnsController.class);

        table = new JXTable();
        prepareRenderers();
        table.setHighlighters(HighlighterFactory.createAlternateStriping());
        table.setColumnControlVisible(true);
        table.setSortable(true);
        table.setRowFilter(rowFilter);

        propertiesColumns = new PropertyEdgeDataColumn[FAKE_COLUMNS_COUNT];

        propertiesColumns[0] = new PropertyEdgeDataColumn(NbBundle.getMessage(EdgeDataTable.class, "EdgeDataTable.source.column.text")) {

            @Override
            public Class getColumnClass() {
                return String.class;
            }

            @Override
            public Object getValueFor(Edge edge) {
                if (showEdgesNodesLabels) {
                    return edge.getSource().getNodeData().getId() + " - " + edge.getSource().getNodeData().getLabel();
                } else {
                    return edge.getSource().getNodeData().getId();
                }
            }
        };

        propertiesColumns[1] = new PropertyEdgeDataColumn(NbBundle.getMessage(EdgeDataTable.class, "EdgeDataTable.target.column.text")) {

            @Override
            public Class getColumnClass() {
                return String.class;
            }

            @Override
            public Object getValueFor(Edge edge) {
                if (showEdgesNodesLabels) {
                    return edge.getTarget().getNodeData().getId() + " - " + edge.getTarget().getNodeData().getLabel();
                } else {
                    return edge.getTarget().getNodeData().getId();
                }
            }
        };
        propertiesColumns[2] = new PropertyEdgeDataColumn(NbBundle.getMessage(EdgeDataTable.class, "EdgeDataTable.type.column.text")) {

            @Override
            public Class getColumnClass() {
                return String.class;
            }

            @Override
            public Object getValueFor(Edge edge) {
                if (edge.isDirected()) {
                    return NbBundle.getMessage(EdgeDataTable.class, "EdgeDataTable.type.column.directed");
                } else {
                    return NbBundle.getMessage(EdgeDataTable.class, "EdgeDataTable.type.column.undirected");
                }
            }
        };
        //Add listener of table selection to refresh edit window when the selection changes (and if the table is not being refreshed):
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                if (!refreshingTable) {
                    EditWindowController edc = Lookup.getDefault().lookup(EditWindowController.class);
                    if (edc.isOpen()) {
                        if (table.getSelectedRow() != -1) {
                            edc.editEdges(getEdgesFromSelectedRows());
                        } else {
                            edc.disableEdit();
                        }
                    }
                }
            }
        });
        table.addMouseListener(new PopupAdapter());
        table.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
                    Edge[] selectedEdges = getEdgesFromSelectedRows();
                    if (selectedEdges.length > 0) {
                        EdgesManipulator del = dlh.getEdgesManipulatorByName("DeleteEdges");
                        if (del != null) {
                            del.setup(selectedEdges, null);
                            if (del.canExecute()) {
                                dlh.executeManipulator(del);
                            }
                        }
                    }
                }
            }
        });
    }

    private void prepareRenderers() {
        DynamicModel dm = Lookup.getDefault().lookup(DynamicController.class).getModel();
        table.setDefaultRenderer(NumberList.class, sparkLinesRenderer = new SparkLinesRenderer());
        table.setDefaultRenderer(DynamicBigDecimal.class, new SparkLinesRenderer());
        table.setDefaultRenderer(DynamicBigInteger.class, new SparkLinesRenderer());
        table.setDefaultRenderer(DynamicByte.class, new SparkLinesRenderer());
        table.setDefaultRenderer(DynamicDouble.class, new SparkLinesRenderer());
        table.setDefaultRenderer(DynamicFloat.class, new SparkLinesRenderer());
        table.setDefaultRenderer(DynamicInteger.class, new SparkLinesRenderer());
        table.setDefaultRenderer(DynamicLong.class, new SparkLinesRenderer());
        table.setDefaultRenderer(DynamicShort.class, new SparkLinesRenderer());
        double min, max;
        if (dm != null) {
            min = dm.getMin();
            max = dm.getMax();
        } else {
            min = Double.NEGATIVE_INFINITY;
            max = Double.POSITIVE_INFINITY;
        }
        table.setDefaultRenderer(TimeInterval.class, timeIntervalsRenderer = new TimeIntervalsRenderer(min, max, timeIntervalGraphics));

        //Use default string editor for them:
        table.setDefaultEditor(String.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultEditor(NumberList.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultEditor(DynamicBigDecimal.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultEditor(DynamicBigInteger.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultEditor(DynamicByte.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultEditor(DynamicDouble.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultEditor(DynamicFloat.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultEditor(DynamicInteger.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultEditor(DynamicLong.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultEditor(DynamicShort.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultEditor(TimeInterval.class, new DefaultCellEditor(new JTextField()));
        table.setDefaultEditor(TimeInterval.class, timeIntervalCellEditor=new TimeIntervalCellEditor(new JTextField()));
    }

    public JXTable getTable() {
        return table;
    }

    public boolean setPattern(String regularExpr, int column) {
        try {
            if (!regularExpr.startsWith("(?i)")) {   //CASE_INSENSITIVE
                regularExpr = "(?i)" + regularExpr;
            }
            rowFilter = RowFilter.regexFilter(regularExpr, column);
            table.setRowFilter(rowFilter);
        } catch (PatternSyntaxException e) {
            return false;
        }
        return true;
    }

    public void refreshModel(HierarchicalGraph graph, AttributeColumn[] cols, DataTablesModel dataTablesModel) {
        showingColumns = cols;
        DynamicModel dm = Lookup.getDefault().lookup(DynamicController.class).getModel();
        if (dm != null) {
            timeIntervalsRenderer.setMinMax(dm.getMin(), dm.getMax());
            currentTimeFormat = dm.getTimeFormat();
            timeIntervalsRenderer.setTimeFormat(currentTimeFormat);
            timeIntervalCellEditor.setTimeFormat(currentTimeFormat);
            sparkLinesRenderer.setTimeFormat(currentTimeFormat);
        }
        timeIntervalsRenderer.setDrawGraphics(timeIntervalGraphics);
        refreshingTable = true;
        if (selectedEdges == null) {
            selectedEdges = getEdgesFromSelectedRows();
        }
        ArrayList<EdgeDataColumn> columns = new ArrayList<EdgeDataColumn>();
        columns.addAll(Arrays.asList(propertiesColumns));

        for (AttributeColumn c : cols) {
            columns.add(new AttributeEdgeDataColumn(c));
        }

        if (model == null) {
            model = new EdgeDataTableModel(graph.getEdgesAndMetaEdges().toArray(), columns.toArray(new EdgeDataColumn[0]));
            table.setModel(model);
        } else {
            model.setEdges(graph.getEdgesAndMetaEdges().toArray());
            model.setColumns(columns.toArray(new EdgeDataColumn[0]));
        }

        setEdgesSelection(selectedEdges);//Keep row selection before refreshing.
        selectedEdges = null;
        refreshingTable = false;
    }

    public void setEdgesSelection(Edge[] edges) {
        this.selectedEdges = edges;//Keep this selection request to be able to do it if the table is first refreshed later.
        HashSet<Edge> edgesSet = new HashSet<Edge>();
        edgesSet.addAll(Arrays.asList(edges));
        table.clearSelection();
        for (int i = 0; i < table.getRowCount(); i++) {
            if (edgesSet.contains(getEdgeFromRow(i))) {
                table.addRowSelectionInterval(i, i);
            }
        }
    }

    public void scrollToFirstEdgeSelected() {
        int row = table.getSelectedRow();
        if (row != -1) {
            Rectangle rect = table.getCellRect(row, 0, true);
            table.scrollRectToVisible(rect);
        }
    }

    public boolean hasData() {
        return table.getRowCount() > 0;
    }

    public boolean isUseSparklines() {
        return useSparklines;
    }

    public void setUseSparklines(boolean useSparklines) {
        this.useSparklines = useSparklines;
    }

    public boolean isTimeIntervalGraphics() {
        return timeIntervalGraphics;
    }

    public void setTimeIntervalGraphics(boolean timeIntervalGraphics) {
        this.timeIntervalGraphics = timeIntervalGraphics;
    }

    public boolean isShowEdgesNodesLabels() {
        return showEdgesNodesLabels;
    }

    public void setShowEdgesNodesLabels(boolean showEdgesNodesLabels) {
        this.showEdgesNodesLabels = showEdgesNodesLabels;
    }

    private String[] getHiddenColumns() {
        List<String> hiddenCols = new ArrayList<String>();
        TableColumnModelExt columnModel = (TableColumnModelExt) table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumnExt col = columnModel.getColumnExt(i);
            if (!col.isVisible()) {
                hiddenCols.add((String) col.getHeaderValue());
            }
        }
        return hiddenCols.toArray(new String[0]);
    }

    private void setHiddenColumns(String[] columns) {
        TableColumnModelExt columnModel = (TableColumnModelExt) table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumnExt col = columnModel.getColumnExt(i);
            for (int j = 0; j < columns.length; j++) {
                if (columns[j].equals(col.getHeaderValue())) {
                    col.setVisible(false);
                }
            }
        }
    }

    private class EdgeDataTableModel extends AbstractTableModel {

        private Edge[] edges;
        private EdgeDataColumn[] columns;

        public EdgeDataTableModel(Edge[] edges, EdgeDataColumn[] cols) {
            this.edges = edges;
            this.columns = cols;
        }

        public int getRowCount() {
            return edges.length;
        }

        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columns[columnIndex].getColumnName();
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columns[columnIndex].getColumnClass();
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columns[columnIndex].isEditable();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            return columns[columnIndex].getValueFor(edges[rowIndex]);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            columns[columnIndex].setValueFor(edges[rowIndex], aValue);
        }

        public Edge getEdgeAtRow(int row) {
            return edges[row];
        }

        public EdgeDataColumn[] getColumns() {
            return columns;
        }

        public void setColumns(EdgeDataColumn[] columns) {
            boolean columnsChanged = columns.length != this.columns.length;
            this.columns = columns;
            if (columnsChanged) {
                fireTableStructureChanged();
            }
        }

        public Edge[] getEdges() {
            return edges;
        }

        public void setEdges(Edge[] edges) {
            this.edges = edges;
            fireTableDataChanged();
        }
    }

    private interface EdgeDataColumn {

        public Class getColumnClass();

        public String getColumnName();

        public Object getValueFor(Edge edge);

        public void setValueFor(Edge edge, Object value);

        public boolean isEditable();
    }

    private class AttributeEdgeDataColumn implements EdgeDataTable.EdgeDataColumn {

        private AttributeColumn column;

        public AttributeEdgeDataColumn(AttributeColumn column) {
            this.column = column;
        }

        public Class getColumnClass() {
            if (useSparklines && attributeUtils.isNumberListColumn(column)) {
                return NumberList.class;
            } else if (useSparklines && attributeUtils.isDynamicNumberColumn(column)) {
                return column.getType().getType();
            } else if (column.getType() == AttributeType.TIME_INTERVAL) {
                return TimeInterval.class;
            } else if (attributeUtils.isNumberColumn(column)) {
                return column.getType().getType();//Number columns should not be treated as Strings because the sorting would be alphabetic instead of numeric
            } else if (column.getType() == AttributeType.BOOLEAN) {
                return Boolean.class;
            } else {
                return String.class;//Treat all columns as Strings. Also fix the fact that the table implementation does not allow to edit Character cells.
            }
        }

        public String getColumnName() {
            return column.getTitle();
        }

        public Object getValueFor(Edge edge) {
            Object value = edge.getEdgeData().getAttributes().getValue(column.getIndex());
            if (useSparklines && (attributeUtils.isNumberListColumn(column) || attributeUtils.isDynamicNumberColumn(column))) {
                return value;
            } else if (column.getType() == AttributeType.TIME_INTERVAL) {
                return value;
            } else if (attributeUtils.isNumberColumn(column)) {
                return value;
            } else if (column.getType() == AttributeType.BOOLEAN) {
                return value;
            } else {
                //Show values as Strings like in Edit window and other parts of the program to be consistent
                if (value != null) {
                    if (value instanceof DynamicType) {//When type is dynamic, take care to show proper time format
                        return ((DynamicType) value).toString(currentTimeFormat == TimeFormat.DOUBLE);
                    } else {
                        return value.toString();
                    }
                } else {
                    return null;
                }
            }
        }

        public void setValueFor(Edge edge, Object value) {
            attributeColumnsController.setAttributeValue(value, edge.getEdgeData().getAttributes(), column);
        }

        public boolean isEditable() {
            return attributeColumnsController.canChangeColumnData(column);
        }
    }

    private abstract class PropertyEdgeDataColumn implements EdgeDataTable.EdgeDataColumn {

        private String name;

        public PropertyEdgeDataColumn(String name) {
            this.name = name;
        }

        public abstract Class getColumnClass();

        public String getColumnName() {
            return name;
        }

        public abstract Object getValueFor(Edge edge);

        public void setValueFor(Edge edge, Object value) {
        }

        public boolean isEditable() {
            return false;
        }
    }

    private class PopupAdapter extends MouseUtils.PopupMouseAdapter {

        PopupAdapter() {
        }

        protected void showPopup(final MouseEvent e) {
            int selRow = table.rowAtPoint(e.getPoint());

            if (selRow != -1) {
                if (!table.getSelectionModel().isSelectedIndex(selRow)) {
                    table.getSelectionModel().clearSelection();
                    table.getSelectionModel().setSelectionInterval(selRow, selRow);
                }
                final Point p = e.getPoint();
                new Thread(new Runnable() {

                    public void run() {
                        final JPopupMenu pop = createPopup(p);
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                showPopup(p.x, p.y, pop);
                            }
                        });
                    }
                }).start();
            } else {
                table.getSelectionModel().clearSelection();
            }
            e.consume();

        }

        private void showPopup(int xpos, int ypos, final JPopupMenu popup) {
            if ((popup != null) && (popup.getSubElements().length > 0)) {
                final PopupMenuListener p = new PopupMenuListener() {

                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    }

                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                        popup.removePopupMenuListener(this);
                        table.requestFocus();
                    }

                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                };
                popup.addPopupMenuListener(p);
                popup.show(table, xpos, ypos);
            }
        }

        private JPopupMenu createPopup(Point p) {
            final Edge[] selectedEdges = getEdgesFromSelectedRows();
            final Edge clickedEdge = getEdgeFromRow(table.rowAtPoint(p));
            JPopupMenu contextMenu = new JPopupMenu();

            //First add edges manipulators items:
            DataLaboratoryHelper dlh = DataLaboratoryHelper.getDefault();
            Integer lastManipulatorType = null;
            for (EdgesManipulator em : dlh.getEdgesManipulators()) {
                em.setup(selectedEdges, clickedEdge);
                if (lastManipulatorType == null) {
                    lastManipulatorType = em.getType();
                }
                if (lastManipulatorType != em.getType()) {
                    contextMenu.addSeparator();
                }
                lastManipulatorType = em.getType();
                if (em.isAvailable()) {
                    contextMenu.add(PopupMenuUtils.createMenuItemFromEdgesManipulator(em, clickedEdge, selectedEdges));
                }
            }

            //Add AttributeValues manipulators submenu:
            AttributeRow row = (AttributeRow) clickedEdge.getEdgeData().getAttributes();
            int realColumnIndex = table.convertColumnIndexToModel(table.columnAtPoint(p)) - FAKE_COLUMNS_COUNT;//Get real attribute column index not counting fake columns.
            if (realColumnIndex >= 0) {
                AttributeColumn column = showingColumns[realColumnIndex];
                if (column != null) {
                    contextMenu.add(PopupMenuUtils.createSubMenuFromRowColumn(row, column));
                }
            }
            return contextMenu;
        }
    }

    private Edge getEdgeFromRow(int row) {
        return ((EdgeDataTableModel) table.getModel()).getEdgeAtRow(table.convertRowIndexToModel(row));
    }

    public Edge[] getEdgesFromSelectedRows() {
        int[] selectedRows = table.getSelectedRows();
        Edge[] edges = new Edge[selectedRows.length];
        for (int i = 0; i < edges.length; i++) {
            edges[i] = getEdgeFromRow(selectedRows[i]);
        }
        return edges;
    }
}
