package net.clementlevallois.parsers;

import net.clementlevallois.utils.Utils;
import com.csvreader.CsvReader;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.Report;
import org.openide.util.Exceptions;

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
public class CsvParser {

    private final String filePath;
    private CsvReader csvReader;
    private final String textDelimiter;
    private final String fieldDelimiter;
    private final boolean headersPresent;
    private final boolean weightedAttributes;
    
    private BufferedReader br;
    
    private final Map<String, Map<String, Multiset<String>>> datastruct = new HashMap();
    private final Map<Integer, String> mapColNumToHeader = new HashMap<>();

    private final Report report = new Report();
    
    private static final String[] ALPHABET = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    public CsvParser(String filePath, String textDelimiter, String fieldDelimiter, boolean headersPresent, boolean weightedAttributes) {
        this.filePath = filePath;
        this.fieldDelimiter = fieldDelimiter;
        this.textDelimiter = textDelimiter;
        this.headersPresent = headersPresent;
        this.weightedAttributes = weightedAttributes;
        this.init();
    }

    private void init() {
        try {
            br = new BufferedReader(new FileReader(filePath));

            String textDelimiterAsAString = Utils.getCharacter(textDelimiter);
            String fieldDelimiterAsAString = Utils.getCharacter(fieldDelimiter);

            char textDelimiterAsACharacter = textDelimiterAsAString.charAt(0);
            char fieldDelimiterAsACharacter = fieldDelimiterAsAString.charAt(0);

            csvReader = new CsvReader(br, fieldDelimiterAsACharacter);
//            csvReader.setEscapeMode(CsvReader.ESCAPE_MODE_BACKSLASH);
            csvReader.setDelimiter(fieldDelimiterAsACharacter);
            csvReader.setTextQualifier(textDelimiterAsACharacter);
            csvReader.setUseTextQualifier(true);

        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public Map<String, Map<String, Multiset<String>>> parse() throws IOException {
        //data structure to host all attributes for a node, and their weights / intensity
        String nodeName;
        Map<String, Multiset<String>> attributes;

        int columnCount = 0;

        if (headersPresent) {
            csvReader.readHeaders();
            for (int j = 0; j < csvReader.getHeaderCount(); j++) {
                mapColNumToHeader.put(j, csvReader.getHeader(j));
            }
            columnCount = csvReader.getHeaderCount();
        }
        boolean breakNow = false;

        while (csvReader.readRecord()) {
            if (breakNow) {
                break;
            }

            if (!headersPresent && csvReader.getCurrentRecord() == 1) {
                for (int j = 0; j < csvReader.getValues().length; j++) {
                    mapColNumToHeader.put(j, ALPHABET[j]);
                }
                columnCount = csvReader.getValues().length;
            }

            nodeName = csvReader.get(0);

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

            for (int j = 1; j < columnCount; j++) {

                //getting the header's name, which is the attributes name
                attributeName = mapColNumToHeader.get(j);

                //checking if the field is null or empty. If it is, it should be ignored for similarity computations. We do that by replacing the null value by a random string, to make sure this cell is unique -> dissimilar to any other.
                if (csvReader.get(j) == null || csvReader.get(j).isEmpty()) {
                    Multiset<String> values = HashMultiset.create();
                    values.add(UUID.randomUUID().toString(), 1);
                    attributes.put(attributeName, values);
                }
 
                //if field not empty / blank / null
                 
                else {
                    String cellContent = csvReader.get(j);

                    // 1. CASE OF weighted values. One every two columns is an attribute, the other is a value for this attribute. Starting at column 1.
                    if (weightedAttributes) {
                        if (previousColIsAttribute) {
                            attributeName = mapColNumToHeader.get(j - 1);
                            float weight = 0;
                            try {
                                weight = Float.valueOf(cellContent);
                            } catch (NumberFormatException ex) {
                                report.logIssue(new Issue("Expected a number at attribute " + attributeName + " but found value: " + cellContent, Issue.Level.SEVERE));
                            }
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
        csvReader.close();
        br.close();
        return datastruct;

    }

    public Report getReport() {
        return report;
    }
}
