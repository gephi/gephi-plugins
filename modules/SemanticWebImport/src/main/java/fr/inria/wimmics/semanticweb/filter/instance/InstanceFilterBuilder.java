/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.wimmics.semanticweb.filter.instance;

import fr.inria.edelweiss.semantic.SemanticWebImportMainWindowTopComponent;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.project.api.Workspace;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class responsible for building a new semanticweb filter.
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
@ServiceProvider(service = FilterBuilder.class)
public class InstanceFilterBuilder implements FilterBuilder {
    @Override
    public Category getCategory() {
        return new Category("SemanticWeb", getIcon());
//        return FilterLibrary.ATTRIBUTES;
    }

    @Override
    public String getName() {
        return "Model Types Filter";
    }

    @Override
    public Icon getIcon() {
        return SemanticWebImportMainWindowTopComponent.loadIcon();
    }

    @Override
    public String getDescription() {
        return "Remove all the nodes that are types, so that only instances are kept.";
    }

    @Override
    public Filter getFilter(Workspace workspace) {
        return new InstanceFilter();
    }

    @Override
    public JPanel getPanel(Filter filter) {
        InstanceFilter semanticWebFilter = (InstanceFilter) filter;
        return new InstanceFilterPanel(semanticWebFilter);
    }

    @Override
    public void destroy(Filter filter) {
    }
}
