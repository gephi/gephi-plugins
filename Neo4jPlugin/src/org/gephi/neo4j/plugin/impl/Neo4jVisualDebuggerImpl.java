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

import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.NodeData;
import org.gephi.graph.api.Renderable;
import org.gephi.neo4j.plugin.api.Neo4jDelegateNodeDebugger;
import org.gephi.neo4j.plugin.api.Neo4jVisualDebugger;
import org.gephi.neo4j.plugin.api.NoMoreElementsException;
import org.gephi.neo4j.plugin.impl.GraphModelImportConverter.Neo4jGraphModel;
import org.neo4j.graphdb.Path;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin Škurla
 */
@ServiceProvider(service = Neo4jVisualDebugger.class)
public class Neo4jVisualDebuggerImpl implements Neo4jVisualDebugger {

    private static final Color NOT_CUSTOM_COLOR = new Color(-1);
    private final List<org.gephi.graph.api.Node> previousNodes = new LinkedList<org.gephi.graph.api.Node>();
    private final List<Edge> previousEdges = new LinkedList<Edge>();
    private final List<Color> previousNodeColors = new LinkedList<Color>();
    private final List<Color> previousEdgeColors = new LinkedList<Color>();
    private Graph graph;
    private Iterator<Path> paths;
    private Neo4jGraphModel neo4jModel;
    private Path currentPath;
    private boolean finishedTraversal = false;

    @Override
    public void initialize() {
        this.neo4jModel = GraphModelImportConverter.getNeo4jModelForCurrentWorkspace();
        this.graph = Lookup.getDefault().lookup(GraphController.class).getModel().getGraphVisible();
    }

    @Override
    public void restore() {
        restorePreviousEdgesColors();
        restorePreviousNodesColors();

        finishedTraversal = true;
    }

    @Override
    public void nextStep(Neo4jDelegateNodeDebugger neo4jDelegateNodeDebugger) throws NoMoreElementsException {
        if (paths == null || finishedTraversal == true) {
            this.paths = neo4jDelegateNodeDebugger.paths(GraphModelImportConverter.getGraphDBForCurrentWorkspace()).iterator();
            this.finishedTraversal = false;
            this.currentPath = null;
        }

        if (paths.hasNext()) {
            Path path = paths.next();

            try {
                update(neo4jDelegateNodeDebugger, path);
            } catch (NoMoreElementsRuntimeException nmere) {
                restore();

                throw new NoMoreElementsException();
            }
        } else {
            restore();

            throw new NoMoreElementsException();
        }
    }

    @Override
    public void update(Neo4jDelegateNodeDebugger neo4jDelegateNodeDebugger) {
        if (paths == null || finishedTraversal == true) {
            this.paths = neo4jDelegateNodeDebugger.paths(GraphModelImportConverter.getGraphDBForCurrentWorkspace()).iterator();
            this.finishedTraversal = false;
            this.currentPath = null;
        }

        try {
            update(neo4jDelegateNodeDebugger, currentPath);
        } catch (NoMoreElementsRuntimeException nmere) {
            throw new AssertionError();
        }
    }

    private void update(Neo4jDelegateNodeDebugger neo4jDebugger, Path path) {
        restorePreviousNodesColors();
        restorePreviousEdgesColors();

        currentPath = path;

        applyCurrentNodesColors(neo4jDebugger);
        applyCurrentEdgesColors(neo4jDebugger);
    }

    private void restorePreviousNodesColors() {
        if (!previousNodes.isEmpty()) {
            for (int index = 0; index < previousNodes.size(); index++) {
                setColor(previousNodes.get(index).getNodeData(), previousNodeColors.get(index));
            }

            previousNodes.clear();
            previousNodeColors.clear();
        }
    }

    private void restorePreviousEdgesColors() {
        if (!previousEdges.isEmpty()) {
            for (int index = 0; index < previousEdges.size(); index++) {
                setColor(previousEdges.get(index).getEdgeData(), previousEdgeColors.get(index));
            }

            previousEdges.clear();
            previousEdgeColors.clear();
        }
    }

    private void applyCurrentNodesColors(Neo4jDelegateNodeDebugger neo4jDebugger) {
        if (neo4jDebugger.isShowNodes()) {
            if (currentPath != null) {
                for (org.neo4j.graphdb.Node neoNode : currentPath.nodes()) {
                    int nodeId = neo4jModel.getNeo4jToGephiNodeMap().get(neoNode.getId());
                    org.gephi.graph.api.Node currentGephiNode = graph.getNode(nodeId);

                    if (currentGephiNode == null) {
                        try {
                            nextStep(neo4jDebugger);
                        } catch (NoMoreElementsException nmee) {
                            throw new NoMoreElementsRuntimeException();
                        }
                    }

                    previousNodes.add(currentGephiNode);

                    NodeData currentNodeData = currentGephiNode.getNodeData();
                    previousNodeColors.add(new Color(currentNodeData.r(),
                            currentNodeData.g(),
                            currentNodeData.b()));

                    setColor(currentNodeData, neo4jDebugger.getNodesColor());
                }
            }
        }
    }

    private void applyCurrentEdgesColors(Neo4jDelegateNodeDebugger neo4jDebugger) {
        if (neo4jDebugger.isShowRelationships()) {
            int nodeCount = previousNodes.size();

            if (nodeCount > 1) {
                for (int index = 0; index < nodeCount - 1; index++) {
                    org.gephi.graph.api.Node startNode = previousNodes.get(index);
                    org.gephi.graph.api.Node endNode = previousNodes.get(index + 1);

                    Edge edge1 = graph.getEdge(startNode, endNode);
                    Edge edge2 = graph.getEdge(endNode, startNode);

                    Edge currentEdge = (edge1 != null) ? edge1
                            : edge2;

                    previousEdges.add(currentEdge);

                    EdgeData currentEdgeData = currentEdge.getEdgeData();

                    // special feature in Gephi when R part of the RGB has value -1, it
                    // means Edge does not have custom color
                    if (currentEdgeData.r() != -1) {
                        previousEdgeColors.add(new Color(currentEdgeData.r(),
                                currentEdgeData.g(),
                                currentEdgeData.b()));
                    } else {
                        previousEdgeColors.add(NOT_CUSTOM_COLOR);
                    }

                    setColor(currentEdgeData, neo4jDebugger.getRelationshipsColor());
                }
            }
        }
    }

    private void setColor(Renderable renderable, Color color) {
        if (color != NOT_CUSTOM_COLOR) {
            float[] rgbColorComponents = color.getRGBColorComponents(null);

            renderable.setR(rgbColorComponents[0]);
            renderable.setG(rgbColorComponents[1]);
            renderable.setB(rgbColorComponents[2]);
        } else {
            renderable.setR(-1);
            renderable.setG(0);
            renderable.setB(0);
        }
    }

    private class NoMoreElementsRuntimeException extends RuntimeException {
    }
}
