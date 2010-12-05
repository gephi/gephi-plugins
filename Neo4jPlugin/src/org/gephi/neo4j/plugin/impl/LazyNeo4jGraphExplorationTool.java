/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
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
package org.gephi.neo4j.plugin.impl;


import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.Node;
import org.gephi.neo4j.plugin.api.Neo4jImporter;
import org.gephi.neo4j.plugin.api.TraversalOrder;
import org.gephi.neo4j.plugin.impl.GraphModelImportConverter;
import org.gephi.neo4j.plugin.impl.GraphModelImportConverter.Neo4jGraphModel;
import org.gephi.tools.spi.NodeClickEventListener;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.spi.LongTask;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Martin Škurla
 */
@ServiceProvider(service=Tool.class)
public class LazyNeo4jGraphExplorationTool implements Tool {
    private ToolEventListener[] toolListeners;
    private LazyNeo4jGraphExplorationToolPanel toolPanel;


    @Override
    public void select() {
        toolListeners = null;
        toolPanel = null;
    }

    @Override
    public void unselect() {}

    @Override
    public ToolEventListener[] getListeners() {
        toolListeners = new ToolEventListener[] { new NodeClickEventListener() {
            @Override
            public void clickNodes(Node[] nodes) {
                final Neo4jGraphModel neo4jGraphModel = GraphModelImportConverter.getNeo4jModelForCurrentWorkspace();

                final long startNeo4jNodeId = neo4jGraphModel.getGephiToNeo4jNodeMap().get(nodes[0].getId());

                clearGraphAndNeo4jGraphModel(neo4jGraphModel);

                final Neo4jImporter neo4jImporter = Lookup.getDefault().lookup(Neo4jImporter.class);

                LongTaskExecutor executor = new LongTaskExecutor(true);
                executor.execute((LongTask) neo4jImporter, new Runnable() {

                    @Override
                    public void run() {
                        neo4jImporter.importDatabase(neo4jGraphModel.getGraphDB(),
                                                     startNeo4jNodeId,
                                                     TraversalOrder.DEPTH_FIRST,
                                                     toolPanel.getDepth());
                    }
                });               
            }

            private void clearGraphAndNeo4jGraphModel(Neo4jGraphModel neo4jGraphModel) {
                Lookup.getDefault().lookup(GraphController.class).getModel().getGraph().clear();

                neo4jGraphModel.getGephiToNeo4jNodeMap().clear();
                neo4jGraphModel.getNeo4jToGephiNodeMap().clear();
            }
        }};

        return toolListeners;
    }

    @Override
    public ToolUI getUI() {
        return new ToolUI() {

            @Override
            public JPanel getPropertiesBar() {
                toolPanel = new LazyNeo4jGraphExplorationToolPanel();

                return toolPanel;
            }

            @Override
            public Icon getIcon() {
                return new ImageIcon(getClass().getResource("/org/gephi/neo4j/plugin/impl/resources/Neo4j-logo.png"));
            }

            @Override
            public String getName() {
                return NbBundle.getMessage(LazyNeo4jGraphExplorationTool.class, "LazyExplorationTool.name");
            }

            @Override
            public String getDescription() {
                return NbBundle.getMessage(LazyNeo4jGraphExplorationTool.class, "LazyExplorationTool.description");
            }

            @Override
            public int getPosition() {
                return 210;
            }
        };
    }

    @Override
    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.SELECTION;
    }
}
