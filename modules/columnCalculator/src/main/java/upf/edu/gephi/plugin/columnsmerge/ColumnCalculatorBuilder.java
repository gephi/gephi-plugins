package upf.edu.gephi.plugin.columnsmerge;

import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategy;
import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategyBuilder;
import org.openide.util.lookup.ServiceProvider;

/**
 * Builder for ColumnCalculator
 * @author Javier Gonzalez
 */
@ServiceProvider(service=AttributeColumnsMergeStrategyBuilder.class)
public class ColumnCalculatorBuilder implements AttributeColumnsMergeStrategyBuilder{
    
    @Override
    public AttributeColumnsMergeStrategy getAttributeColumnsMergeStrategy() {
        return new ColumnCalculator();
    }
    
}
