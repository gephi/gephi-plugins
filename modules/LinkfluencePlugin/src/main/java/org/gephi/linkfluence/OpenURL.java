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

import javax.swing.Icon;
import org.gephi.datalab.spi.ContextMenuItemManipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.nodes.NodesManipulator;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.visualization.spi.GraphContextMenuItem;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = GraphContextMenuItem.class)
public class OpenURL implements GraphContextMenuItem, NodesManipulator{

    private Node node;

    public void setup(Node[] nodes, Node clickedNode) {
        setup(null, nodes);
    }

    @Override
    public void setup(Graph graph, Node[] nodes) {
        if (nodes.length == 1) {
            node = nodes[0];
        }else{
            node=null;
        }
    }

    public void execute() {
    }

    @Override
    public ContextMenuItemManipulator[] getSubItems() {
        GraphContextMenuItem[] subItems=new GraphContextMenuItem[2];

        //Always provide subitems so their shortcuts are available:
        subItems[0]=new OpenURLLastItem();
        subItems[1]=new OpenURLSubItem();
        return subItems;
    }

    public String getName() {
        return NbBundle.getMessage(OpenURL.class, "GraphContextMenu_OpenURL");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(OpenURL.class, "GraphContextMenu_OpenURL.description");
    }

    @Override
    public boolean isAvailable() {
        return node != null;
    }

    public boolean canExecute() {
        return true;
    }

    public int getType() {
        return 400;
    }

    public int getPosition() {
        return 300;
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/linkfluence/resources/globe-network.png", false);
    }

    public Integer getMnemonicKey() {
        return null;
    }

    public ManipulatorUI getUI() {
        return null;
    }
}
