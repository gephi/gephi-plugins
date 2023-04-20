/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.wimmics.semanticweb.filter.sparql;

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
public class SparqlFilterBuilder implements FilterBuilder {
    @Override
    public Category getCategory() {
        return new Category("SemanticWeb", getIcon());
    }

    @Override
    public String getName() {
        return "SPARQL Filter";
    }

    @Override
    public Icon getIcon() {
        return SemanticWebImportMainWindowTopComponent.loadIcon();
    }

    @Override
    public String getDescription() {
        return "Keep only the nodes returned by the SELECT expression provided.";
    }

    @Override
    public Filter getFilter(Workspace workspace) {
        return new SparqlFilter();
    }

    @Override
    public JPanel getPanel(Filter filter) {
        SparqlFilter semanticWebFilter = (SparqlFilter) filter;
        return new SparqlFilterPanel(semanticWebFilter);
    }

    @Override
    public void destroy(Filter filter) {
    }
}
