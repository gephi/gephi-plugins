package net.clementlevallois.parsers;

import net.clementlevallois.controller.MyFileImporter;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.clementlevallois.utils.ExcelCellTypesSolver;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.gephi.io.importer.api.ContainerLoader;

/*
 Copyright 2008-2013 Clement Levallois
 Authors : Clement Levallois <clementlevallois@gmail.com>
 Website : http://www.clementlevallois.net


 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Clement Levallois. All rights reserved.

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

 Contributor(s): Clement Levallois

 */
public class ExcelParser {

    String fileName;
    String sheetName;
    boolean headersPresent;
    ContainerLoader container;

    private Map<String, Map<String, Multiset<String>>> datastruct = new HashMap();
    private Map<Integer, String> mapColNumToHeader = new HashMap();

    private static final String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public ExcelParser(String fileName, String sheetName) {
        this.fileName = fileName;
        this.sheetName = sheetName;
    }

    public ExcelParser(String fileName) {
        this.fileName = fileName;
    }

    public ExcelParser() {

    }

    public Map<String, Map<String, Multiset<String>>> parse() throws FileNotFoundException, IOException, InvalidFormatException {

        InputStream inp;
        inp = new FileInputStream(fileName);
        Workbook wb = WorkbookFactory.create(inp);

        Row row;

        Sheet sheet;
        if (sheetName == null) {
            sheet = wb.getSheetAt(0);
        } else {
            sheet = wb.getSheet(sheetName);
        }

        //mapping the column numbers to the headers titles
        if (MyFileImporter.headersPresent) {
            row = sheet.getRow(0);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                Cell cell = row.getCell(j);
                mapColNumToHeader.put(j, cell.getStringCellValue());
            }
        } else {
            row = sheet.getRow(0);
            for (int j = 0; j < row.getLastCellNum(); j++) {
                mapColNumToHeader.put(j, alphabet[j]);
            }

        }

        //data structure to host all attributes for a node, and their weights / intensity
        String nodeName;
        Map<String, Multiset<String>> attributes;

        //scanning the rows of the Excel file and filling in the datastruct
        int startingRow = 1;
        boolean breakNow = false;
        for (int i = startingRow; i <= sheet.getLastRowNum(); i++) {
            if (breakNow) {
                break;
            }
            row = sheet.getRow(i);
            if (row == null) {
                break;
            }

            // name of the node is the first cell of the row
            nodeName = row.getCell(0).getStringCellValue();

            if (nodeName == null || nodeName.isEmpty()) {
                break;
            }

            //looping through the columns of this row, intializing the map of attributes and their values
            attributes = new HashMap();

            //a boolean to keep track of which column is a value / an attribute
            boolean previousColIsAttribute = false;

            //the name of the current attribute
            String attributeName;

            String prevCellValue = null;

            for (int j = 1; j < row.getLastCellNum(); j++) {

                //value of the preceding cell. Useful for attributes which are weighted. See CASE 1 below.
                if (ExcelCellTypesSolver.anyCellToString(row.getCell(j)) == null || ExcelCellTypesSolver.anyCellToString(row.getCell(j)).isEmpty()) {
                    datastruct.put(nodeName, attributes);
                    breakNow = true;
                    break;
                } else {
                    String cellContent = ExcelCellTypesSolver.anyCellToString(row.getCell(j));
                    //getting the header's name, which is the attributes name
                    attributeName = mapColNumToHeader.get(j);

                    // 1. CASE OF weighted values. One every two columns is an attribute, the other is a value for this attribute. Starting at column 1.
                    if (MyFileImporter.isWeightedAttributes()) {
                        if (previousColIsAttribute) {
                            attributeName = mapColNumToHeader.get(j - 1);
                            float weight = Float.valueOf(cellContent);
                            Multiset<String> values = HashMultiset.create();
                            values.add(prevCellValue, Math.round(weight));
                            attributes.put(attributeName, values);
                            previousColIsAttribute = false;
                        } else {
                            prevCellValue = cellContent;
                            previousColIsAttribute = true;
                        }
                    } // 2. CASE OF non weighted values. Every column is an attribute with a weight of 1.
                    else {
                        Multiset<String> values = HashMultiset.create();
                        values.add(cellContent, 1);
                        attributes.put(attributeName, values);
                    }

                }
            }
            datastruct.put(nodeName, attributes);

        }

        inp.close();

        return datastruct;
    }

    public String[] getHeaders() throws FileNotFoundException, IOException, InvalidFormatException {
        InputStream inp;
        inp = new FileInputStream(fileName);
        Workbook wb = WorkbookFactory.create(inp);
        List<String> listHeaders = new ArrayList();

        Row row;
        Sheet sheet = wb.getSheet(sheetName);
        row = sheet.getRow(0);
        for (int j = 0; j < row.getLastCellNum(); j++) {
            if (row.getCell(j).getStringCellValue().isEmpty() || row.getCell(j).getStringCellValue() == null) {
                break;
            }
            listHeaders.add(row.getCell(j).getStringCellValue());
        }
        inp.close();
        return listHeaders.toArray(new String[listHeaders.size()]);
    }

    public String[] getSheetsNames() throws FileNotFoundException, IOException, InvalidFormatException {
        InputStream inp;
        inp = new FileInputStream(fileName);
        Workbook wb = WorkbookFactory.create(inp);
        List<String> listSheetsNames = new ArrayList();

        for (int j = 0; j < wb.getNumberOfSheets(); j++) {
            listSheetsNames.add(wb.getSheetName(j));
        }
        inp.close();
        return listSheetsNames.toArray(new String[listSheetsNames.size()]);
    }

    public Integer getColumnCount() throws FileNotFoundException, IOException, InvalidFormatException {
        InputStream inp;
        inp = new FileInputStream(fileName);
        Workbook wb = WorkbookFactory.create(inp);
        Sheet sheet = wb.getSheet(sheetName);
        Row row = sheet.getRow(0);
        int numColumns = row.getLastCellNum();
        inp.close();
        return numColumns;
    }

    public void testDynamics() {

    }
}
