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

import java.util.Collection;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.neo4j.plugin.api.Neo4jExporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.remote.RemoteGraphDatabase;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin Škurla
 */
@ServiceProvider(service = Neo4jExporter.class)
public class Neo4jExporterImpl implements Neo4jExporter, LongTask {

    private GraphDatabaseService graphDB;
    private GraphModelExportConverter graphModelExportConverter;
    private String fromColumn;
    private String defaultValue;
    private Collection<String> exportedEdgeColumnNames;
    private Collection<String> exportedNodeColumnNames;
    private ProgressTicket progressTicket;
    private boolean cancelExport;

    @Override
    public boolean cancel() {
        cancelExport = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        cancelExport = false;
        this.progressTicket = progressTicket;
    }

    @Override
    public void exportDatabase(GraphDatabaseService graphDB, String fromColumn, String defaultValue, Collection<String> exportedEdgeColumnNames, Collection<String> exportedNodeColumnNames) {
        this.graphDB = graphDB;
        this.fromColumn = fromColumn;
        this.defaultValue = defaultValue;
        this.exportedEdgeColumnNames = exportedEdgeColumnNames;
        this.exportedNodeColumnNames = exportedNodeColumnNames;

        String longTaskMessage = (graphDB instanceof RemoteGraphDatabase)
                ? NbBundle.getMessage(Neo4jExporterImpl.class, "CTL_Neo4j_RemoteExportMessage")
                : NbBundle.getMessage(Neo4jExporterImpl.class, "CTL_Neo4j_LocalExportMessage");

        Progress.setDisplayName(progressTicket, longTaskMessage);
        Progress.start(progressTicket);

        doExport();
    }

    private void doExport() {
        Transaction transaction = graphDB.beginTx();
        try {
            exportGraph();

            if (!cancelExport) {
                transaction.success();
            }
        } finally {
            transaction.finish();
        }

        graphDB.shutdown();
        Progress.finish(progressTicket);
    }

    private void exportGraph() {
        graphModelExportConverter = GraphModelExportConverter.getInstance(graphDB);
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();

        exportNodes(graph.getNodes());
        exportEdges(graph.getEdgesAndMetaEdges());
        graphModelExportConverter.reset();
    }

    private void exportNodes(Iterable<org.gephi.graph.api.Node> nodes) {
        for (org.gephi.graph.api.Node node : nodes) {
            if (cancelExport) {
                return;
            }

            processNode(node);
        }
    }

    private void processNode(org.gephi.graph.api.Node node) {
        graphModelExportConverter.createNeoNodeFromGephiNode(node, exportedNodeColumnNames);
    }

    private void exportEdges(Iterable<Edge> edges) {
        for (Edge edge : edges) {
            if (cancelExport) {
                return;
            }

            processEdge(edge);
        }
    }

    private void processEdge(Edge edge) {
        graphModelExportConverter.createNeoRelationship(edge, exportedEdgeColumnNames, fromColumn, defaultValue);
    }
}
