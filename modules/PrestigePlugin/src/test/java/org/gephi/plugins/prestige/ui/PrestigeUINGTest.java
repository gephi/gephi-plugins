package org.gephi.plugins.prestige.ui;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.plugins.prestige.PrestigeStatistics;
import org.gephi.plugins.prestige.calculation.DomainCalculator;
import org.gephi.plugins.prestige.calculation.IndegreeCalculator;
import org.gephi.plugins.prestige.calculation.ProximityCalculator;
import org.gephi.project.api.ProjectController;
import org.openide.util.Lookup;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Gephi's Statistics panel calls {@link PrestigeUI#unsetup()} when the settings dialog is confirmed, and the
 * statistics controller calls it again once the statistics has finished, by which time the settings panel has
 * been released. A caller running the statistics without the dialog never builds a panel at all.
 * <p>
 * Which metrics were calculated is read from the node columns they add to the graph.
 */
public class PrestigeUINGTest {

    private GraphModel graphModel;

    @BeforeMethod
    public void openProjectWithDirectedPath() {
        Lookup.getDefault().lookup(ProjectController.class).newProject();
        graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();

        Graph graph = graphModel.getGraph();
        Node a = graphModel.factory().newNode("a");
        Node b = graphModel.factory().newNode("b");
        Node c = graphModel.factory().newNode("c");
        graph.addNode(a);
        graph.addNode(b);
        graph.addNode(c);
        graph.addEdge(graphModel.factory().newEdge(a, b, true));
        graph.addEdge(graphModel.factory().newEdge(b, c, true));
    }

    @AfterMethod
    public void closeProject() {
        Lookup.getDefault().lookup(ProjectController.class).closeCurrentProject();
    }

    @Test
    public void unsetup_calledAgainAfterTheRun_doesNotThrow() {
        PrestigeUI ui = new PrestigeUI();
        PrestigeStatistics statistics = new PrestigeStatistics();

        // Settings dialog confirmed
        ui.getSettingsPanel();
        ui.setup(statistics);
        ui.unsetup();

        // Statistics executed, then released again
        ui.setup(statistics);
        statistics.execute(graphModel);
        ui.unsetup();

        // The metrics ticked in the panel were calculated, the statistics has none selected by default
        assertTrue(hasColumn(IndegreeCalculator.INDEGREE_KEY));
        assertTrue(hasColumn(DomainCalculator.DOMAIN_KEY));
        assertTrue(hasColumn(ProximityCalculator.PROXIMITY_KEY));
    }

    @Test
    public void unsetup_withoutSettingsPanel_keepsTheConfiguredSettings() {
        PrestigeUI ui = new PrestigeUI();
        PrestigeStatistics statistics = new PrestigeStatistics();
        statistics.setCalculateDomain(true);

        ui.setup(statistics);
        statistics.execute(graphModel);
        ui.unsetup();

        assertTrue(hasColumn(DomainCalculator.DOMAIN_KEY));
        assertFalse(hasColumn(IndegreeCalculator.INDEGREE_KEY));
        assertFalse(hasColumn(ProximityCalculator.PROXIMITY_KEY));
    }

    @Test
    public void unsetup_afterAnEarlierRun_doesNotApplyTheClosedPanelAgain() {
        // PrestigeUI is a singleton, shared by every run
        PrestigeUI ui = new PrestigeUI();
        ui.getSettingsPanel();
        PrestigeStatistics dialogRun = new PrestigeStatistics();
        ui.setup(dialogRun);
        ui.unsetup();

        PrestigeStatistics laterRun = new PrestigeStatistics();
        laterRun.setCalculateDomain(true);
        ui.setup(laterRun);
        laterRun.execute(graphModel);
        ui.unsetup();

        assertTrue(hasColumn(DomainCalculator.DOMAIN_KEY));
        assertFalse(hasColumn(IndegreeCalculator.INDEGREE_KEY));
        assertFalse(hasColumn(ProximityCalculator.PROXIMITY_KEY));
    }

    @Test
    public void unsetup_beforeAnythingElse_doesNotThrow() {
        new PrestigeUI().unsetup();
    }

    @Test
    public void unsetup_withSettingsPanelButWithoutStatistics_doesNotThrow() {
        PrestigeUI ui = new PrestigeUI();
        ui.getSettingsPanel();
        ui.unsetup();
    }

    private boolean hasColumn(String key) {
        return graphModel.getNodeTable().hasColumn(key);
    }
}
