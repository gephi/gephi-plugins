/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.wimmics.semanticweb.filter.type;

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
public class TypeFilterBuilder implements FilterBuilder {

    @Override
    public Category getCategory() {
        return new Category("SemanticWeb", getIcon());

//        return FilterLibrary.ATTRIBUTES;
    }

    @Override
    public String getName() {
        return "Node Type Filter";
    }

    @Override
    public Icon getIcon() {
        return SemanticWebImportMainWindowTopComponent.loadIcon();
    }

    @Override
    public String getDescription() {
        return "Returns the nodes having a type matching, as a regexp, the string given as argument.";
    }

    @Override
    public Filter getFilter(Workspace workspace) {
        return new TypeFilter();
    }

    @Override
    public JPanel getPanel(Filter filter) {
        TypeFilter semanticWebFilter = (TypeFilter) filter;
        return new TypeFilterPanel(semanticWebFilter);
    }

    public void destroy(Filter filter) {
    }
}
