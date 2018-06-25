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

import java.util.logging.Level;
import java.util.logging.Logger;
import net.objecthunter.exp4j.*;


/**
 *
 * @author Ivan Andrada
 */
public class ColumnCalculatorParser {

    private static final String SYMBOL = "$";
    
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
        Double formulaResult = null;
	String var = "";
        Integer valuesOfColumnsLength = valuesOfColumns.length;
	for ( int i = 0; i < valuesOfColumnsLength; ++i ){
            var = getVariable(i);
            formula = formula.replace( var, valuesOfColumns[i].toString() );
	}
        
        //Comprobacion de variables incorrectas
        if ( existIncorrectVariable( formula, valuesOfColumnsLength) ){
            return null;
        }

	try{
            Expression e = new ExpressionBuilder(formula).build();
            formulaResult = e.evaluate();
	} catch(Exception e){
            Logger.getLogger("").log( Level.WARNING, null, e );
	} finally {
            //Aqui se hacen las gestiones finales si procede
	}
        return formulaResult;
    
    }
    
    private static Boolean existIncorrectVariable(String formula, Integer maxNumForVariables){
        for ( int i = 0; i< maxNumForVariables; ++i ){
            formula = formula.replace( getVariable(i), "" );
        }
        
        return formula.contains(SYMBOL);
    }
    
    private static String getVariable(Integer variable){
        return SYMBOL + variable.toString();
    }
}
