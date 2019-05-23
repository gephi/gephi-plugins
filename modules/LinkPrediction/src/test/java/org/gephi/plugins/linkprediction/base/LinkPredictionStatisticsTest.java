package org.gephi.plugins.linkprediction.base;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Table;
import org.gephi.plugins.linkprediction.base.LinkPredictionStatistics;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatistics;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.jupiter.api.BeforeEach;
import org.openide.util.Lookup;

import static org.gephi.plugins.linkprediction.base.LinkPredictionStatistics.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkPredictionStatisticsTest {
    GraphModel graphModel;

    @BeforeEach void setUp() {
        //Init project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();

        //Get the default graph model
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
    }

    @org.junit.jupiter.api.Test void testInitializeColumns_Successfully() {
        LinkPredictionStatistics statistic = new CommonNeighboursStatistics();

        Table edgeTable = graphModel.getEdgeTable();
        statistic.initializeColumns(edgeTable);

        Column colLP = edgeTable.getColumn(LP_ALGORITHM);
        assertTrue(colLP != null);

        Column colAddinRun = edgeTable.getColumn(ADDED_IN_RUN);
        assertTrue(colAddinRun != null);

        Column colLastValue = edgeTable.getColumn(LAST_VALUE);
        assertTrue(colLastValue != null);
    }

}
