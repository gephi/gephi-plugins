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
package org.gephi.neo4j.plugin.api;

import java.awt.Color;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Path;

/**
 *
 * @author Martin Škurla
 */
public final class MutableNeo4jDelegateNodeDebugger implements Neo4jDelegateNodeDebugger {

    private final Neo4jDelegateNodeDebugger neo4jDebugger;
    private boolean showNodes;
    private boolean showRelationships;
    private Color nodesColor;
    private Color relationshipsColor;
    private DebugTarget debugTarget;

    public MutableNeo4jDelegateNodeDebugger(Neo4jDelegateNodeDebugger sourceNeo4jDebugger) {
        this.neo4jDebugger = sourceNeo4jDebugger;

        this.showNodes = sourceNeo4jDebugger.isShowNodes();
        this.showRelationships = sourceNeo4jDebugger.isShowRelationships();

        this.nodesColor = sourceNeo4jDebugger.getNodesColor();
        this.relationshipsColor = sourceNeo4jDebugger.getRelationshipsColor();

        this.debugTarget = sourceNeo4jDebugger.getDebugTarget();
    }

    @Override
    public Iterable<Path> paths(GraphDatabaseService graphDB) {
        return neo4jDebugger.paths(graphDB);
    }

// <editor-fold defaultstate="collapsed" desc="Getters & setters">
    @Override
    public boolean isShowNodes() {
        return showNodes;
    }

    public void setShowNodes(boolean showNodes) {
        this.showNodes = showNodes;
    }

    @Override
    public boolean isShowRelationships() {
        return showRelationships;
    }

    public void setShowRelationships(boolean showRelationships) {
        this.showRelationships = showRelationships;
    }

    @Override
    public Color getNodesColor() {
        return nodesColor;
    }

    public void setNodesColor(Color nodesColor) {
        this.nodesColor = nodesColor;
    }

    @Override
    public Color getRelationshipsColor() {
        return relationshipsColor;
    }

    public void setRelationshipsColor(Color relationshipsColor) {
        this.relationshipsColor = relationshipsColor;
    }

    @Override
    public DebugTarget getDebugTarget() {
        return debugTarget;
    }

    public void setDebugTarget(DebugTarget debugTarget) {
        this.debugTarget = debugTarget;
    }
// </editor-fold>
}
