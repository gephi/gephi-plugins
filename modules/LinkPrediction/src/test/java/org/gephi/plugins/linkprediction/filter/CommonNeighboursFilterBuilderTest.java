package org.gephi.plugins.linkprediction.filter;

import org.gephi.filters.spi.Category;
import org.gephi.plugins.linkprediction.base.LinkPredictionFilterPanel;
import org.gephi.plugins.linkprediction.statistics.CommonNeighboursStatisticsBuilder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.openide.util.Lookup;

import static org.gephi.plugins.linkprediction.base.LinkPredictionFilterBuilder.LINK_PREDICTION_CATEGORY;
import static org.junit.jupiter.api.Assertions.*;

class CommonNeighboursFilterBuilderTest {

    @Test void getName() {
        CommonNeighboursFilterBuilder builder = new CommonNeighboursFilterBuilder();
        assertEquals(CommonNeighboursStatisticsBuilder.COMMON_NEIGHBOURS_NAME, builder.getName());
    }

    @Test void getDescription() {
        CommonNeighboursFilterBuilder builder = new CommonNeighboursFilterBuilder();
        assertEquals(CommonNeighboursFilterBuilder.COMMON_NEIGHBOURS_DESC, builder.getDescription());
    }

    @Test void getFilter() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        CommonNeighboursFilterBuilder builder = new CommonNeighboursFilterBuilder();
        assertEquals(CommonNeighboursFilter.class, builder.getFilter(workspace).getClass());
    }

    @Test void getCategory() {
        CommonNeighboursFilterBuilder builder = new CommonNeighboursFilterBuilder();
        Category category  = builder.getCategory();

        assertEquals(LINK_PREDICTION_CATEGORY, category.getName());
    }

    @Ignore
    @Test void getIcon() {
    }

    @Test void getPanel() {
        CommonNeighboursFilterBuilder builder = new CommonNeighboursFilterBuilder();
        CommonNeighboursFilter filter = new CommonNeighboursFilter();
        assertEquals(LinkPredictionFilterPanel.class, builder.getPanel(filter).getClass());
    }

    @Ignore
    @Test void destroy() {
    }
}
