package upf.edu.gephi.plugin.columnsmerge;

import javax.swing.Icon;
import upf.edu.gephi.plugin.columnsmerge.parser.ColumnCalculatorParser;
import org.gephi.datalab.api.AttributeColumnsController;
import upf.edu.gephi.plugin.columnsmerge.ui.ColumnCalculatorUI;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.columns.merge.AttributeColumnsMergeStrategy;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Table;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.gephi.graph.api.AttributeUtils;

/**
 * @author Javier Gonzalez
 */
public class ColumnCalculator implements AttributeColumnsMergeStrategy {

    public static final String COLUMN_TITLE_SAVED_PREFERENCES = "";
    public static final String CUSTOM_FORMULA_SAVED_PREFERENCES = "";
    private Table table;
    private Column[] columns;
    private String columnTitle;
    private String customFormula;

    @Override
    public void setup(Table table, Column[] columns) {
        this.table = table;
        this.columns = columns;
        columnTitle = NbPreferences.forModule(ColumnCalculator.class).get(COLUMN_TITLE_SAVED_PREFERENCES, "");
        customFormula = NbPreferences.forModule(ColumnCalculator.class).get(COLUMN_TITLE_SAVED_PREFERENCES, "");
    }

    @Override
    public void execute() {
        // Pasamos las variables obtenidas en el UI
        NbPreferences.forModule(ColumnCalculator.class).put(COLUMN_TITLE_SAVED_PREFERENCES, columnTitle);
        NbPreferences.forModule(ColumnCalculator.class).put(CUSTOM_FORMULA_SAVED_PREFERENCES, customFormula);
        
        if (table == null || columns == null) {
            throw new IllegalArgumentException("table, columns or operations can't be null and operations length must be columns length -1");
        }

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);

        Column newColumn = ac.addAttributeColumn(table, columnTitle, Double.class);
        
        Number[] rowNumbers;
        Element[] rows = ac.getTableAttributeRows(table);
        for (int i=0; i< rows.length;++i) {
            rowNumbers = ac.getRowNumbers(rows[i], columns);
            Double formulaResult = ColumnCalculatorParser.getFormulaResult(rowNumbers, customFormula);
            rows[i].setAttribute(newColumn, formulaResult);
        }

    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ColumnCalculator.class, "ColumnCalculator.name");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(ColumnCalculator.class, "ColumnCalculator.description");
    }

    @Override
    public boolean canExecute() {
         // Check if the input column is a NumberType column
        for(Column column : columns){
            if(!AttributeUtils.isNumberType(column.getTypeClass())){
               return false; 
            }
        }
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return new ColumnCalculatorUI();
    }

    @Override
    public int getType() {
        return 200;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
    
    public String getColumnTitle() {
        return columnTitle;
    }

    public void setColumnTitle(String columnTitle) {
        this.columnTitle = columnTitle;
    }
    
    
    public String getCustomFormula() {
        return customFormula;
    }

    public void setCustomFormula(String customFormula) {
        this.customFormula = customFormula;
    }
    
    public Table getTable(){
        return this.table;
    }
    
    public Column[] getColumns(){
        return this.columns;
    }
}
