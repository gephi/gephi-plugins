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

import java.awt.List;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import net.objecthunter.exp4j.*;
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
     * Se calcula la fórmula para una array de valores y una formula. La formula
     * se parsea con ex4j
     *
     * @param valuesOfColumns valores de las columnas, para una row concreta
     * @param formula Formula sin parsear, facilitada por el usuario en la
     * interfaz
     * @return BigDecimal con el resultado de aplicar a formula
     */
    public static Double getFormulaResult(Number[] valuesOfColumns, String formula) {
        Double formulaResult = 0.0; //valor por defecto hasta que se desarrolle el método
        ArrayList<Integer> columnIndexes = new ArrayList<Integer>();
        ArrayList<Double> vals = new ArrayList<Double>();
        String[] splitted = formula.split("(?=[-+*/()])|(?<=[^-+*/][-+*/])|(?<=[()])");
        ArrayList<String> operators = new ArrayList<String>();
        operators.add(""); // Si no lo ponemos, hay NullPointerException
        String formattedFormula = "";
        //TODO @IvanAndrada CALCULAR FORMULA
        for (int i = 0; i < splitted.length; i++) {
            
            splitted[i] = splitted[i].replace("$", "");
            //System.out.println(splitted[i]);
            if (!splitted[i].contains("+") && !splitted[i].contains("-") && !splitted[i].contains("*") && !splitted[i].contains("/") && !splitted[i].contains("%") && !splitted[i].contains("(")
                    && !splitted[i].contains(")") && !splitted[i].contains("[") && !splitted[i].contains("]")) {
                columnIndexes.add(Integer.parseInt(splitted[i]));
            }
            else
            {
                operators.add(splitted[i]);
            }
        }
        
        operators.add(""); //Por motivos de debugging, para que no haya NullPointerException
        int sizeQry = columnIndexes.size();
        int max = Collections.max(columnIndexes);
        
        if (max > valuesOfColumns.length-1)
        {
            throw new IllegalArgumentException("The formula " + formula + " is not correct. The number argument $" + max +" is illegal.");
        }
        
        for (int i = 0; i < sizeQry; i++)
        {
            vals.add(valuesOfColumns[columnIndexes.get(i)].doubleValue());
            formattedFormula += vals.get(i).toString() + operators.get(i+1); 
        }
        
        Expression e = new ExpressionBuilder(formattedFormula).build(); //Usar libreria exp4j para parsear formula
        formulaResult = e.evaluate(); //Evaluar formula

        return formulaResult;
    
    }
}
