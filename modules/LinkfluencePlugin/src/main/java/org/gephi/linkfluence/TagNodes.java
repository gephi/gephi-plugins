/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.linkfluence;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.Icon;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.api.datatables.DataTablesController;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.linkfluence.ui.GeneralColumnAndValueChooser;
import org.gephi.linkfluence.ui.GeneralColumnAndValueChooserUI;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that fills the given column of multiple nodes with a value.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class TagNodes implements NodesManipulator, GeneralColumnAndValueChooser, GraphContextMenuItem{

    private Node[] nodes;
    private Column column;
    private Table table;
    private Column[] availableColumns;
    private String value;

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
        prepareData();
    }

    public void setup(Graph graph, Node[] nodes) {
        this.nodes = nodes;
        prepareData();
    }

    private void prepareData(){
        table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable();
        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
        ArrayList<Column> availableColumnsList = new ArrayList<Column>();
        for (Column c : table) {
            if (ac.canChangeColumnData(c)) {
                availableColumnsList.add(c);
            }
        }
        availableColumns = availableColumnsList.toArray(new Column[0]);
    }

    public void execute() {
        if (column != null) {
            AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);
            ac.fillNodesColumnWithValue(nodes, column, value);
            Lookup.getDefault().lookup(DataTablesController.class).refreshCurrentTable();
        }
    }

    public String getName() {
        if (nodes.length > 1) {
            return NbBundle.getMessage(TagNodes.class, "TagNodes.name.multiple");
        } else {
            return NbBundle.getMessage(TagNodes.class, "TagNodes.name.single");
        }
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        return nodes.length > 0;
    }

    public ManipulatorUI getUI() {
        return new GeneralColumnAndValueChooserUI();
    }

    public int getType() {
        return 200;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/linkfluence/resources/tag-label.png", true);
    }

    public Integer getMnemonicKey() {
        return KeyEvent.VK_T;
    }

    public boolean isAvailable() {
        return true;
    }

    public ContextMenuItemManipulator[] getSubItems() {
        return null;
    }

    public Column[] getColumns() {
        return availableColumns;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Table getTable() {
        return table;
    }
}
