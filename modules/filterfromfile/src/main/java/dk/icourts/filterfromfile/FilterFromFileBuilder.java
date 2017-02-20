package dk.icourts.filterfromfile;

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.filters.api.FilterLibrary;
import org.gephi.filters.spi.Category;
import org.gephi.filters.spi.Filter;
import org.openide.util.lookup.ServiceProvider;
import org.gephi.filters.spi.FilterBuilder;
import org.gephi.project.api.Workspace;

/**
 * Builder for the {@link FilterFromFile} filter
 *
 * @author Yannis Panagis
 */
@ServiceProvider(service = FilterBuilder.class)
public class FilterFromFileBuilder implements FilterBuilder {

    @Override
    public Category getCategory() {
        return FilterLibrary.ATTRIBUTES;
    }

    @Override
    public String getName() {
        return "Filter by label list";
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public String getDescription() {
        return "Filter nodes of the graph based on the node labels contained in a text file";
    }

    @Override
    public Filter getFilter(Workspace wrkspc) {
        return new FilterFromFile();
    }

    @Override
    public JPanel getPanel(Filter filter) {
        FilterFrmFilePanel panel = new FilterFrmFilePanel((FilterFromFile) filter);
        return panel;
    }

    @Override
    public void destroy(Filter filter) {
        //Do nothing
    }
}
