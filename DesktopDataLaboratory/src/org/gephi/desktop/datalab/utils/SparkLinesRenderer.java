/*
Copyright 2008-2010 Gephi
Authors : Eduardo Ramos
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
package org.gephi.desktop.datalab.utils;

import java.awt.Component;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import org.gephi.data.attributes.type.DynamicType;
import org.gephi.data.attributes.type.NumberList;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.utils.sparklines.SparklineGraph;
import org.gephi.utils.sparklines.SparklineParameters;

/**
 * TableCellRenderer for drawing sparklines from cells that have a NumberList or DynamicNumber as their value.
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class SparkLinesRenderer extends DefaultTableCellRenderer {

    private static final Color SELECTED_BACKGROUND = new Color(225, 255, 255);
    private static final Color UNSELECTED_BACKGROUND = Color.white;
    private TimeFormat timeFormat = TimeFormat.DOUBLE;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (value == null) {
            //Render empty string when null
            return super.getTableCellRendererComponent(table, null, isSelected, hasFocus, row, column);
        }

        String stringRepresentation = null;
        Number[] numbers = null;
        if (value instanceof NumberList) {
            numbers = getNumberListNumbers((NumberList) value);
            stringRepresentation = value.toString();
        } else if (value instanceof DynamicType) {
            numbers = getDynamicNumberNumbers((DynamicType) value);
            stringRepresentation = ((DynamicType) value).toString(timeFormat == TimeFormat.DOUBLE);
        } else {
            throw new IllegalArgumentException("Only number lists and dynamic numbers are supported for sparklines rendering");
        }

        //If there is less than 3 elements, show as a String.
        if (numbers.length < 3) {
            return super.getTableCellRendererComponent(table, stringRepresentation, isSelected, hasFocus, row, column);
        }

        JLabel label = new JLabel();
        Color background;
        if (isSelected) {
            background = SELECTED_BACKGROUND;
        } else {
            background = UNSELECTED_BACKGROUND;
        }

        //Note: Can't use interactive SparklineComponent because TableCellEditors don't receive mouse events.

        final SparklineParameters sparklineParameters = new SparklineParameters(table.getColumnModel().getColumn(column).getWidth() - 1, table.getRowHeight(row) - 1, Color.BLUE, background, Color.RED, Color.GREEN, null);
        final BufferedImage i = SparklineGraph.draw(numbers, sparklineParameters);
        label.setIcon(new ImageIcon(i));
        label.setToolTipText(stringRepresentation);//String representation as tooltip

        return label;
    }

    private Number[] getNumberListNumbers(NumberList numberList) {
        ArrayList<Number> numbers = new ArrayList<Number>();
        Number n;
        for (int i = 0; i < numberList.size(); i++) {
            n = (Number) numberList.getItem(i);
            if (n != null) {
                numbers.add(n);
            }
        }
        return numbers.toArray(new Number[0]);
    }

    private Number[] getDynamicNumberNumbers(DynamicType dynamicNumber) {
        ArrayList<Number> numbers = new ArrayList<Number>();
        if (dynamicNumber == null) {
            return new Number[0];
        }
        Number[] dynamicNumbers;
        dynamicNumbers = (Number[]) dynamicNumber.getValues().toArray(new Number[0]);
        Number n;
        for (int i = 0; i < dynamicNumbers.length; i++) {
            n = (Number) dynamicNumbers[i];
            if (n != null) {
                numbers.add((Number) n);
            }
        }
        return numbers.toArray(new Number[0]);
    }

    public TimeFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(TimeFormat timeFormat) {
        this.timeFormat = timeFormat;
    }
}
