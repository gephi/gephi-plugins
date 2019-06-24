/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>, Eduardo Ramos <eduramiba@gmail.com>
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
import java.net.URL;
import javax.swing.Icon;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.project.api.ProjectController;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.awt.HtmlBrowser.URLDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class OpenURLLastItem implements GraphContextMenuItem, NodesManipulator{

    private Node node;
    private String columnId, url;

    public void setup(Node[] nodes, Node clickedNode) {
        setup(null, nodes);
    }

    public void setup(Graph graph, Node[] nodes) {
        url = null;
        columnId = null;
        if (nodes.length == 1) {
            node = nodes[0];
            LastColumnUsed lc = Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().getLookup().lookup(LastColumnUsed.class);
            if (lc != null && lc.column != null) {
                columnId = lc.column;
                Table table = Lookup.getDefault().lookup(GraphController.class).getGraphModel().getNodeTable();
                Column column = table.getColumn(columnId);
                if (column != null) {
                    Element row = node;
                    Object value;
                    if ((value = row.getAttribute(column)) != null) {
                        url = value.toString();

                        if (!url.matches("(https?|ftp):(//?|\\\\?)?.*")) {
                            //Does not look like an URL, try http:
                            url = "http://" + url;
                        }
                    }
                }else{
                    columnId=null;
                     Lookup.getDefault().lookup(ProjectController.class).getCurrentWorkspace().remove(lc);
                }
            }
        } else {
            node = null;
        }
    }

    public void execute() {
        if (url != null) {
            try {
                URLDisplayer.getDefault().showURLExternal(new URL(url));
            } catch (Exception ex) {
            }
        }
    }

    public String getName() {
        return NbBundle.getMessage(OpenURLLastItem.class, "GraphContextMenu_OpenURLLastItem", columnId != null ? columnId : "--");
    }

    public boolean canExecute() {
        return url != null;
    }

    public int getType() {
        return 0;
    }

    public int getPosition() {
        return 0;
    }

    public Icon getIcon() {
        return null;
    }

    @Override
    public Integer getMnemonicKey() {
        return KeyEvent.VK_P;
    }

    public ContextMenuItemManipulator[] getSubItems() {
        return null;
    }

    public boolean isAvailable() {
        return true;
    }

    public String getDescription() {
        return null;
    }

    public ManipulatorUI getUI() {
        return null;
    }
}
