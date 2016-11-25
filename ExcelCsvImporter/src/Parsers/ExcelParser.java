/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Parsers;

import Controller.MyFileImporter;
import Utils.Utils;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.dynamic.api.DynamicModel.TimeFormat;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.Issue;
import org.gephi.io.importer.api.NodeDraft;
import org.joda.time.LocalDate;

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
    Integer columnCount;
    Multiset<String> nodes = HashMultiset.create();
    Multiset<String> nodesFirst = HashMultiset.create();
    Multiset<String> nodesSecond = HashMultiset.create();
    Multiset<String> edges = HashMultiset.create();
    ContainerLoader container;
    public static int nbColumnFirstAgent;
    public static int nbColumnSecondAgent;
    Map<String, Set<String>> nodeAndIntervals = new HashMap();
    Map<String, Set<String>> edgeAndIntervals = new HashMap();
    Set<String> nodesCurrentLine;

    public ExcelParser(String fileName, String sheetName) {
        this.fileName = fileName;
        this.sheetName = sheetName;
    }

    public ExcelParser(String fileName) {
        this.fileName = fileName;
    }

    public ExcelParser() {
    }

    public void parse() throws FileNotFoundException, IOException, InvalidFormatException {

        InputStream inp;
        inp = new FileInputStream(fileName);
        Workbook wb = WorkbookFactory.create(inp);

        Row row;
        Sheet sheet = wb.getSheet(sheetName);
        int startingRow = 0;
        boolean breakNow = false;
        for (int i = startingRow; i <= sheet.getLastRowNum(); i++) {
            if (breakNow) {
                break;
            }
            row = sheet.getRow(i);
            if (row == null) {
                break;
            }

            for (int j = 0; j < row.getLastCellNum(); j++) {
                if (row.getCell(j).getStringCellValue().isEmpty() || row.getCell(j).getStringCellValue() == null) {
                    breakNow = true;
                    break;
                }
//                category.setCategoryName(row.getCell(j).getStringCellValue());
            }
        }

        inp.close();
    }

    public void convertToNetwork() throws IOException, InvalidFormatException {

        container = MyFileImporter.container;
        container.setEdgeDefault(EdgeDefault.UNDIRECTED);

        //dealing with the case of dynamic networks!!
        if (MyFileImporter.timeField != null) {
            container.setTimeFormat(TimeFormat.DATETIME);
        }

        AttributeTable atNodes = container.getAttributeModel().getNodeTable();
//        AttributeColumn acTest = atNodes.addColumn("type", AttributeType.DYNAMIC_BOOLEAN);
//        NodeDraft nodeDraft = container.factory().newNodeDraft();
//        nodeDraft.addTimeInterval("2009-03-01", "2009-03-010");
//        nodeDraft.setId("0");
//        nodeDraft.setLabel("test");
//        nodeDraft.addAttributeValue(acTest, true, "2009-03-02", "2009-03-03", true, true);
//        nodeDraft.addAttributeValue(acTest, true, "2009-03-04", "2009-03-06", true, true);
//        container.addNode(nodeDraft);

        String firstDelimiter;
        String secondDelimiter;
        firstDelimiter = Utils.getCharacter(MyFileImporter.firstConnectorDelimiter);
        secondDelimiter = Utils.getCharacter(MyFileImporter.secondConnectorDelimiter);
        boolean oneTypeOfAgent = MyFileImporter.getFirstConnectedAgent().equals(MyFileImporter.getSecondConnectedAgent());

        nbColumnFirstAgent = MyFileImporter.firstConnectedAgentIndex;
        nbColumnSecondAgent = MyFileImporter.secondConnectedAgentIndex;

        Integer lineCounter = 0;

        InputStream inp;
        inp = new FileInputStream(fileName);
        Workbook wb = WorkbookFactory.create(inp);

        Row row;
        Sheet sheet = wb.getSheet(sheetName);
        int startingRow;
        if (MyFileImporter.headersPresent) {
            startingRow = 1;
        } else {
            startingRow = 0;
        }
        Set<String> linesFirstAgent = new HashSet();
        Set<String> linesSecondAgent = new HashSet();

//        Double earliestTime = 1000000000000d;
//        Double latestTime = 0d;
        String interval;
        for (int i = startingRow; i <= sheet.getLastRowNum(); i++) {
            interval = null;
            row = sheet.getRow(i);
            if (row == null) {
                break;
            }

            Cell cell = row.getCell(nbColumnFirstAgent);
            if (cell == null) {
                Issue issue = new Issue("problem with line " + lineCounter + " (empty column " + MyFileImporter.getFirstConnectedAgent() + "). It was skipped in the conversion", Issue.Level.WARNING);
                MyFileImporter.getStaticReport().logIssue(issue);
                continue;
            }

            String firstAgent = null;
            switch (cell.getCellType()) {
                case Cell.CELL_TYPE_BOOLEAN:
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    firstAgent = String.valueOf(cell.getNumericCellValue());
                    break;
                case Cell.CELL_TYPE_STRING:
                    firstAgent = cell.getStringCellValue();
                    break;
                case Cell.CELL_TYPE_BLANK:
                    break;
                case Cell.CELL_TYPE_ERROR:
                    break;
                // CELL_TYPE_FORMULA will never occur
                case Cell.CELL_TYPE_FORMULA:
                    break;
            }

            if (firstAgent == null || firstAgent.isEmpty()) {
                Issue issue = new Issue("problem with line " + lineCounter + " (empty column " + MyFileImporter.getFirstConnectedAgent() + "). It was skipped in the conversion", Issue.Level.WARNING);
                MyFileImporter.getStaticReport().logIssue(issue);
                continue;
            }

            if (MyFileImporter.removeDuplicates) {
                boolean newLine = linesFirstAgent.add(firstAgent);
                if (!newLine) {
                    continue;
                }
            }

            String secondAgent = null;

            if (!oneTypeOfAgent) {
                cell = row.getCell(nbColumnSecondAgent);
                if (cell == null) {
                    Issue issue = new Issue("problem with line " + lineCounter + " (empty column " + MyFileImporter.getFirstConnectedAgent() + "). It was skipped in the conversion", Issue.Level.WARNING);
                    MyFileImporter.getStaticReport().logIssue(issue);
                    continue;
                }
                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_BOOLEAN:
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        secondAgent = String.valueOf(cell.getNumericCellValue());
                        break;
                    case Cell.CELL_TYPE_STRING:
                        secondAgent = cell.getStringCellValue();
                        break;
                    case Cell.CELL_TYPE_BLANK:
                        break;
                    case Cell.CELL_TYPE_ERROR:
                        break;
                    // CELL_TYPE_FORMULA will never occur
                    case Cell.CELL_TYPE_FORMULA:
                        break;
                }
                if (secondAgent == null || secondAgent.isEmpty()) {
                    Issue issue = new Issue("problem with line " + lineCounter + " (empty column " + MyFileImporter.getSecondConnectedAgent() + "). It was skipped in the conversion", Issue.Level.WARNING);
                    MyFileImporter.getStaticReport().logIssue(issue);
                    continue;
                }
                if (MyFileImporter.removeDuplicates) {
                    boolean newLine = linesSecondAgent.add(secondAgent);
                    if (!newLine) {
                        continue;
                    }
                }

            }
            lineCounter++;

            String[] firstAgentSplit;
            String[] secondAgentSplit;
            nodesCurrentLine = new HashSet();

            if (firstDelimiter != null) {
                firstAgentSplit = firstAgent.trim().split(firstDelimiter);
            } else {
                firstAgentSplit = new String[1];
                firstAgentSplit[0] = firstAgent;
            }
            for (String node : firstAgentSplit) {
                node = node.trim();
                if (!node.isEmpty()) {
                    nodesFirst.add(node);
                    nodes.add(node);
                    nodesCurrentLine.add(node);
                }
            }

            if (!oneTypeOfAgent) {

                if (secondDelimiter != null) {
                    secondAgentSplit = secondAgent.trim().split(secondDelimiter);
                } else {
                    secondAgentSplit = new String[1];
                    secondAgentSplit[0] = secondAgent;
                }
                for (String node : secondAgentSplit) {
                    if (!node.isEmpty()) {
                        node = node.trim();
                        nodesSecond.add(node);
                        nodes.add(node);
                        nodesCurrentLine.add(node);
                    }
                }
            } else {
                secondAgentSplit = null;
            }

            //detecting the value of the time field
            if (MyFileImporter.timeField != null) {
                cell = row.getCell(MyFileImporter.timeFieldIndex);
                if (cell == null) {
                    Issue issue = new Issue("problem with line " + lineCounter + " (empty column " + MyFileImporter.timeField + "). It was skipped in the conversion", Issue.Level.WARNING);
                    MyFileImporter.getStaticReport().logIssue(issue);
                    continue;
                }
                String timeField = null;

                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_BOOLEAN:
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        timeField = String.valueOf(cell.getNumericCellValue()).split("[.,]")[0];
                        break;
                    case Cell.CELL_TYPE_STRING:
                        timeField = cell.getStringCellValue();
                        break;
                    case Cell.CELL_TYPE_BLANK:
                        break;
                    case Cell.CELL_TYPE_ERROR:
                        break;
                    // CELL_TYPE_FORMULA will never occur
                    case Cell.CELL_TYPE_FORMULA:
                        break;
                }

                if (timeField == null || timeField.isEmpty()) {
                    Issue issue = new Issue("problem with line " + lineCounter + " (empty column " + MyFileImporter.timeField + "). It was skipped in the conversion", Issue.Level.WARNING);
                    MyFileImporter.getStaticReport().logIssue(issue);
                    continue;
                }

                //dealing with the case there is a duration in the time field. Duration: two time stamps separated by a comma
                //getting min and max times
                timeField = timeField.trim();
                timeField = timeField.replaceAll(" ", "");
                Long start = null;
                Long end = null;
                Long time = null;
                LocalDate date;
                try {
                    if (timeField.contains(",")) {
                        if (timeField.split(",")[0].split("-").length < 3) {
//                            start = DynamicUtilities.getDoubleFromXMLDateString(timeField.split(",")[0] + "-01-01");
                            date = new LocalDate(Integer.valueOf(timeField.split(",")[0]), 01, 01);
                            start = date.toDateTimeAtStartOfDay().getMillis();

                        } else {
                            date = new LocalDate(Integer.valueOf(timeField.split(",")[0].split("-")[0]), Integer.valueOf(timeField.split(",")[0].split("-")[1]), Integer.valueOf(timeField.split(",")[0].split("-")[2]));
                            start = date.toDateTimeAtStartOfDay().getMillis();
                        }
//                        if (start < earliestTime) {
//                            earliestTime = start;
//                        }
                        if (timeField.split(",")[1].split("-").length < 3) {
//                            end = DynamicUtilities.getDoubleFromXMLDateString(timeField.split(",")[1] + "-01-01");
                            date = new LocalDate(Integer.valueOf(timeField.split(",")[1]), 01, 01);
                            end = date.toDateTimeAtStartOfDay().getMillis();
                        } else {
                            date = new LocalDate(Integer.valueOf(timeField.split(",")[1].split("-")[0]), Integer.valueOf(timeField.split(",")[1].split("-")[1]), Integer.valueOf(timeField.split(",")[1].split("-")[2]));
                            end = date.toDateTimeAtStartOfDay().getMillis();
                        }
//                        if (end > latestTime) {
//                            latestTime = end;
//                        }
                    } else {
                        if (timeField.split("-").length < 3) {
//                            time = DynamicUtilities.getDoubleFromXMLDateString(timeField + "-01-01");
                            date = new LocalDate(Integer.valueOf(timeField), 01, 01);
                            time = date.toDateTimeAtStartOfDay().getMillis();
                        } else {
                            date = new LocalDate(Integer.valueOf(timeField.split("-")[0]), Integer.valueOf(timeField.split("-")[1]), Integer.valueOf(timeField.split("-")[2]));
                            time = date.toDateTimeAtStartOfDay().getMillis();
                        }
//                        if (time < earliestTime) {
//                            earliestTime = time;
//                        }
//                        if (time > latestTime) {
//                            latestTime = time;
//                        }

                        if (start != null && end != null && end < start) {
                            Issue issue = new Issue("problem with line " + lineCounter + ": end time can not be earlier than start time. Line was skipped in the conversion.", Issue.Level.WARNING);
                            MyFileImporter.getStaticReport().logIssue(issue);
                            continue;

                        }

                    }
                } catch (NumberFormatException e) {
                    Issue issue = new Issue("problem with line " + lineCounter + ": time not formatted correctly. It was skipped in the conversion", Issue.Level.WARNING);
                    MyFileImporter.getStaticReport().logIssue(issue);
                    continue;
                }

                if (time == null) {
                    interval = BigDecimal.valueOf(start).toPlainString() + "," + BigDecimal.valueOf(end).toPlainString();
                } else {
                    interval = BigDecimal.valueOf(time).toPlainString() + "," + BigDecimal.valueOf(time).toPlainString();
                }

                for (String n : nodesCurrentLine) {

                    Set<String> intervals = nodeAndIntervals.get(n);
                    if (intervals == null) {
                        intervals = new TreeSet();
                    }
                    intervals.add(interval);
                    nodeAndIntervals.put(n, intervals);
                }
            }

            //let's find all connections between all the agents in this row
            Utils usefulTools = new Utils();
            String edge;

            if (!MyFileImporter.innerLinksIncluded) {
                for (String x : firstAgentSplit) {
                    for (String xx : secondAgentSplit) {
                        if (!(MyFileImporter.removeSelfLoops & x.equals(xx))) {
                            if (!x.trim().isEmpty() & !xx.trim().isEmpty()) {
                                edge = x.trim() + "|" + xx.trim();
                                edges.add(edge);
                                if (interval != null) {
                                    Set<String> intervals = edgeAndIntervals.get(edge);
                                    if (intervals == null) {
                                        intervals = new TreeSet();
                                    }
                                    intervals.add(interval);
                                    edgeAndIntervals.put(edge, intervals);
                                }
                            }
                        }
                    }
                }
            } else {
                List<String> connections;
                String[] both = ArrayUtils.addAll(firstAgentSplit, secondAgentSplit);
                connections = usefulTools.getListOfLinks(both, MyFileImporter.removeSelfLoops);
                for (String e : connections) {
                    edges.add(e);
                    if (interval != null) {
                        Set<String> intervals = edgeAndIntervals.get(e);
                        if (intervals == null) {
                            intervals = new TreeSet();
                        }
                        intervals.add(interval);
                        edgeAndIntervals.put(e, intervals);
                    }

                }

            }
        }
        NodeDraft node;
        AttributeColumn acFrequency;
        AttributeColumn acType;

        if (atNodes.getColumn("frequency") == null) {
            acFrequency = atNodes.addColumn("frequency", AttributeType.INT);
        } else {
            acFrequency = atNodes.getColumn("frequency");
        }
        if (atNodes.getColumn("type") == null) {
            acType = atNodes.addColumn("type", AttributeType.INT);
        } else {
            acType = atNodes.getColumn("type");
        }

        StringBuilder type;
        boolean atLeastOneType = false;

        for (String n : nodes.elementSet()) {
            type = new StringBuilder();
            node = container.factory().newNodeDraft();
            node.setId(n);
            node.setLabel(n);
            node.addAttributeValue(acFrequency, nodes.count(n));
            if (nodesFirst.contains(n)) {
                type.append(MyFileImporter.getFirstConnectedAgent());
                atLeastOneType = true;
            }
            if (nodesSecond.contains(n)) {
                if (atLeastOneType) {
                    type.append("; ");
                }
                type.append(MyFileImporter.getSecondConnectedAgent());
            }
            node.addAttributeValue(acType, type);

            if (MyFileImporter.timeField != null) {
                if (nodeAndIntervals.get(n) != null) {
                    for (String inter : nodeAndIntervals.get(n)) {
                        LocalDate start = new LocalDate(Long.parseLong(inter.split(",")[0]));
                        LocalDate end = new LocalDate(Long.parseLong(inter.split(",")[1]));
                        node.addTimeInterval(start.toString("yyyy-MM-dd"), end.toString("yyyy-MM-dd"));
                    }
                }
            }
            container.addNode(node);
        }

        //loop for edges
        Integer idEdge = 0;
        EdgeDraft edge;
        for (String e : edges.elementSet()) {
//            System.out.println("edge: " + e);

            String sourceNode = e.split("\\|")[0];
            String targetNode = e.split("\\|")[1];
            edge = container.factory().newEdgeDraft();
            idEdge = idEdge + 1;
            while (container.edgeExists(String.valueOf(idEdge))) {
                idEdge++;
            }
            edge.setSource(container.getNode(sourceNode));
            edge.setTarget(container.getNode(targetNode));
            edge.setWeight((float) edges.count(e));
            edge.setId(String.valueOf(idEdge));
            edge.setType(EdgeDraft.EdgeType.UNDIRECTED);
            if (MyFileImporter.timeField != null) {
                if (edgeAndIntervals.get(e) != null) {
                    for (String inter : edgeAndIntervals.get(e)) {
                        LocalDate start = new LocalDate(Long.parseLong(inter.split(",")[0]));
                        LocalDate end = new LocalDate(Long.parseLong(inter.split(",")[1]));
                        edge.addTimeInterval(start.toString("yyyy-MM-dd"), end.toString("yyyy-MM-dd"));
                    }
                }
            }

            container.addEdge(edge);
        }
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
