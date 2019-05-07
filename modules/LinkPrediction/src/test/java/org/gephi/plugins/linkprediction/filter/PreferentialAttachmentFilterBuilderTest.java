package org.gephi.plugins.linkprediction.filter;

import org.gephi.filters.spi.Category;
import org.gephi.plugins.linkprediction.base.LinkPredictionFilterPanel;
import org.gephi.plugins.linkprediction.statistics.PreferentialAttachmentStatisticsBuilder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.openide.util.Lookup;

import static org.gephi.plugins.linkprediction.base.LinkPredictionFilterBuilder.LINK_PREDICTION_CATEGORY;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PreferentialAttachmentFilterBuilderTest {

    @Test void getName() {
        PreferentialAttachmentFilterBuilder builder = new PreferentialAttachmentFilterBuilder();
        assertEquals(PreferentialAttachmentStatisticsBuilder.PREFERENTIAL_ATTACHMENT_NAME, builder.getName());
    }

    @Test void getDescription() {
        PreferentialAttachmentFilterBuilder builder = new PreferentialAttachmentFilterBuilder();
        assertEquals(PreferentialAttachmentFilterBuilder.PREFERENTIAL_ATTACHMENT_DESC, builder.getDescription());
    }

    @Test void getFilter() {
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        PreferentialAttachmentFilterBuilder builder = new PreferentialAttachmentFilterBuilder();
        assertEquals(PreferentialAttachmentFilter.class, builder.getFilter(workspace).getClass());
    }

    @Test void getCategory() {
        PreferentialAttachmentFilterBuilder builder = new PreferentialAttachmentFilterBuilder();
        Category category  = builder.getCategory();

        assertEquals(LINK_PREDICTION_CATEGORY, category.getName());
    }

    @Ignore
    @Test void getIcon() {
    }

    @Test void getPanel() {
        PreferentialAttachmentFilterBuilder builder = new PreferentialAttachmentFilterBuilder();
        PreferentialAttachmentFilter filter = new PreferentialAttachmentFilter();
        assertEquals(LinkPredictionFilterPanel.class, builder.getPanel(filter).getClass());
    }

    @Ignore
    @Test void destroy() {
    }
}
