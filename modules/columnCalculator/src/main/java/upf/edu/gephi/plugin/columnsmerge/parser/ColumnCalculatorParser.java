package upf.edu.gephi.plugin.columnsmerge.parser;

import net.objecthunter.exp4j.*;

/**
 *
 * @author Ivan Andrada
 */
public class ColumnCalculatorParser {

    private static final String SYMBOL = "$";

    /**
     * Se calcula la fórmula para una array de valores y una formula. La formula se parsea con ex4j
     *
     * @param valuesOfColumns valores de las columnas, para una row concreta
     * @param formula Formula sin parsear, facilitada por el usuario en la interfaz
     * @return BigDecimal con el resultado de aplicar a formula
     */
    public static Double getFormulaResult(Number[] valuesOfColumns, String formula) {
        Double formulaResult = null;
        String var;
        Integer valuesOfColumnsLength = valuesOfColumns.length;
        for (int i = valuesOfColumnsLength - 1; i >= 0; --i) {
            var = getVariable(i);
            formula = formula.replace(var, valuesOfColumns[i].toString());
        }

        //Comprobacion de variables incorrectas
        if (existIncorrectVariable(formula, valuesOfColumnsLength)) {
            return null;
        }

        try {
            Expression e = new ExpressionBuilder(formula).build();
            formulaResult = e.evaluate();
        } catch (Throwable e) {
            //Bad expression, such as division by 0
        }
        return formulaResult;

    }

    private static Boolean existIncorrectVariable(String formula, Integer maxNumForVariables) {
        for (int i = maxNumForVariables - 1; i >= 0; --i) {
            formula = formula.replace(getVariable(i), "");
        }

        return formula.contains(SYMBOL);
    }

    private static String getVariable(Integer variable) {
        return SYMBOL + variable.toString();
    }
}
