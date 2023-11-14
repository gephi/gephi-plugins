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

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.types.IntervalMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimestampMap;
import org.gephi.graph.api.types.TimestampSet;
import org.gephi.project.api.ProjectController;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.joda.time.DateTimeZone;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Nodes manipulator that copies the given column data of multiple nodes to clipboard.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class CopyDataToClipboard implements NodesManipulator, GraphContextMenuItem {

    private Node[] nodes;

    public void setup(Node[] nodes, Node clickedNode) {
        this.nodes = nodes;
    }

    public void setup(Graph graph, Node[] nodes) {
        this.nodes = nodes;
    }

    public void execute() {
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Table table = graphModel.getNodeTable();
        
        List<String> availableColumns = new ArrayList<String>();
        for (Column column : table) {
            availableColumns.add(column.getTitle());
        }
        String initialSelection;
        LastColumnUsed lc = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getLookup().lookup(LastColumnUsed.class);
        if (lc != null) {
            initialSelection = lc.column;
        } else {
            initialSelection = null;
        }

        String columnId = (String) JOptionPane.showInputDialog(null, NbBundle.getMessage(CopyDataToClipboard.class, "CopyDataToClipboard.message"), getName(), JOptionPane.QUESTION_MESSAGE, null, availableColumns.toArray(), initialSelection);
        if (columnId != null) {
            Column column = table.getColumn(columnId);
            if(column != null ){
                boolean isArray = column.isArray();
                boolean isTimestampSet = TimestampSet.class.isAssignableFrom(column.getTypeClass());
                boolean isIntervalSet = IntervalSet.class.isAssignableFrom(column.getTypeClass());
                boolean isTimestampMap = TimestampMap.class.isAssignableFrom(column.getTypeClass());
                boolean isIntervalMap = TimestampMap.class.isAssignableFrom(column.getTypeClass());
                TimeFormat timeFormat = graphModel.getTimeFormat();
                DateTimeZone timeZone = graphModel.getTimeZone();



                final StringBuilder sb = new StringBuilder();
                for (Node node : nodes) {
                    Object value = node.getAttribute(column);
                    sb.append(printValue(value, isArray, isTimestampSet, isIntervalSet, isTimestampMap, isIntervalMap, timeFormat, timeZone));
                    sb.append('\n');
                }
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection ss = new StringSelection(sb.toString());
                clipboard.setContents(ss, ss);

                if (lc == null) {
                    Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().add(new LastColumnUsed(column.getTitle()));
                } else {
                    lc.column = column.getTitle();
                }
            }
        }
    }
    
    private String printValue(Object value, boolean isArray, boolean isTimestampSet, boolean isIntervalSet, boolean isTimestampMap, boolean isIntervalMap, TimeFormat timeFormat, DateTimeZone timeZone){
        if(value == null) {
            return null;
        }
        
        if(isArray){
            return AttributeUtils.printArray(value);
        } else if (isTimestampSet){
            return ((TimestampSet) value).toString(timeFormat, timeZone);
        } else if (isIntervalSet){
            return ((IntervalSet) value).toString(timeFormat, timeZone);
        } else if (isTimestampMap){
            return ((TimestampMap) value).toString(timeFormat, timeZone);
        } else if (isIntervalMap){
            return ((IntervalMap) value).toString(timeFormat, timeZone);
        }
        
        return value.toString();
    }

    public String getName() {
        return NbBundle.getMessage(CopyDataToClipboard.class, "CopyDataToClipboard.name");
    }

    public String getDescription() {
        return "";
    }

    public boolean canExecute() {
        return nodes.length > 0;
    }

    public ManipulatorUI getUI() {
        return null;
    }

    public int getType() {
        return 200;
    }

    public int getPosition() {
        return 300;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/linkfluence/resources/clipboard.png", true);
    }

    @Override
    public Integer getMnemonicKey() {
        return KeyEvent.VK_R;
    }

    public ContextMenuItemManipulator[] getSubItems() {
        return null;
    }

    public boolean isAvailable() {
        return true;
    }
}
