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
import javax.swing.Icon;
import javax.swing.JOptionPane;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.project.api.ProjectController;
import org.gephi.visualization.spi.GraphContextMenuItem;
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
        final Column[] availableColumns = table.toArray();
        Column initialSelection;
        LastColumnUsed lc = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getLookup().lookup(LastColumnUsed.class);
        if (lc != null) {
            initialSelection = table.getColumn(lc.column);
        } else {
            initialSelection = null;
        }

        Column selection = (Column) JOptionPane.showInputDialog(null, NbBundle.getMessage(CopyDataToClipboard.class, "CopyDataToClipboard.message"), getName(), JOptionPane.QUESTION_MESSAGE, null, availableColumns, initialSelection);
        if (selection != null) {
            final StringBuilder sb = new StringBuilder();
            for (Node node : nodes) {
                sb.append(node.getAttribute(selection));
                sb.append('\n');
            }
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection ss = new StringSelection(sb.toString());
            clipboard.setContents(ss, ss);

            if (lc == null) {
                Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().add(new LastColumnUsed(selection.getTitle()));
            } else {
                lc.column = selection.getTitle();
            }
        }
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
