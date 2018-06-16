/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos <eduramiba@gmail.com>
Website : http://www.gephi.org
This file is part of Gephi.
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
Copyright 2011 Gephi Consortium. All rights reserved.
The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
Contributor(s):
Portions Copyrighted 2011 Gephi Consortium.
 */
package org.columns.merge.parser;

import java.math.BigDecimal;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Element;
import org.gephi.graph.api.Table;
import org.openide.util.Lookup;

/**
 *
 * @author Ivan Andrada
 */
public class ColumnCalculatorParser {
    
    /**
     * MergeStrategy Custom Formula. (Fill description)
     *
     *
     * @param table 
     * @param columnsToMerge
     * @param newColumnTitle
     * @return Column with result of the formula
     */
    public static Column applyCustomFormula(Table table, Column[] columnsToMerge, String newColumnTitle, String customFormula) {
        if (table == null || columnsToMerge == null) {
            throw new IllegalArgumentException("table, columns or operations can't be null and operations length must be columns length -1");
        }

        AttributeColumnsController ac = Lookup.getDefault().lookup(AttributeColumnsController.class);

        Column newColumn;
        newColumn = ac.addAttributeColumn(table, newColumnTitle, BigDecimal.class);//Create as BIGDECIMAL column by default. Then it can be duplicated to other type.
        if (newColumn == null) {
            return null;
        }

        Number[] rowNumbers;

        for (Element row : ac.getTableAttributeRows(table)) {
                rowNumbers = ac.getRowNumbers(row, columnsToMerge);
                BigDecimal formulaResult = getFormulaResult(rowNumbers, customFormula);
                row.setAttribute(newColumn, formulaResult);
        }

        return newColumn;
    }
    
    /**
     * Se calcula la fórmula para una array de valores y una formula. 
     * La formula se parsea con ex4j
     * 
     * @param valuesOfColumns valores de las columnas, para una row concreta
     * @param formula Formula sin parsear, facilitada por el usuario en la interfaz
     * @return BigDecimal con el resultado de aplicar a formula 
     */
    private static BigDecimal getFormulaResult(Number[] valuesOfColumns, String formula){
        BigDecimal formulaResult = new BigDecimal(3); //valor por defecto hasta que se desarrolle el método
        
        //TODO @IvanAndrada CALCULAR FORMULA
        
        
        return formulaResult;
    }
    
}
