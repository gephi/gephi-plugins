/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Controller;

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
import Parsers.CsvParser;
import Parsers.ExcelParser;
import Wizard.Panel1;
import Wizard.Panel2;
import Wizard.Panel4;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.DefaultListModel;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.SpigotImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;

/**
 * File importer example which can import the Matrix Market file format. This
 * format is a text-based representation of a matrix and can be tested with
 * <a
 * href="http://www2.research.att.com/~yifanhu/GALLERY/GRAPHS/index.html">Yifan
 * Hu's matrix gallery</a>.
 * <p>
 * The example show how graph data should be set in the {@link ContainerLoader}
 * instance. It shows how {@link NodeDraft} and {@link EdgeDraft} are created
 * from the factory. It also append logs in the {@link Report} class, which is
 * the standard way to report messages and issues.
 *
 * @author Mathieu Bastian
 */
public class MyFileImporter implements SpigotImporter, LongTask {

    public static ContainerLoader container;
    private static Report report;
    private static CsvParser csvParser;
    public static boolean innerLinksIncluded;
    public static boolean removeDuplicates;
    public static boolean removeSelfLoops;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    private static String[] headers;
    private static String filePath;
    private static DefaultListModel listModelHeaders = new DefaultListModel();
    private static String textDelimiter = "\"";
    public static String sheetName;
    private static String fieldDelimiter = ",";
    private static String firstConnectedAgent;
    private static String secondConnectedAgent;
    public static Integer firstConnectedAgentIndex;
    public static Integer secondConnectedAgentIndex;
    public static String firstConnectorDelimiter;
    public static String secondConnectorDelimiter;
    public static Boolean headersPresent;
    public static String timeField;
    public static int timeFieldIndex;
    private static final String[] alphabet = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};

    @Override
    public boolean execute(ContainerLoader loader) {
        container = loader;
        report = new Report();
        
        if (filePath.endsWith(".xlsx")) {
            try {
                ExcelParser excelParser = new ExcelParser(filePath, sheetName);
                excelParser.convertToNetwork();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvalidFormatException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            try {
                csvParser.convertToNetwork();
                csvParser.csvReader.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return !cancel;
    }

    public static String[] getHeaders() {
        return headers;
    }

    public static void setHeaders(String[] headers) {
        MyFileImporter.headers = headers;
    }

    public static DefaultListModel getListModelHeaders() {
        return listModelHeaders;
    }

    public static void setListModelHeaders(DefaultListModel listModelHeaders) {
        MyFileImporter.listModelHeaders = listModelHeaders;
    }

    public static String getTextDelimiter() {
        return textDelimiter;
    }

    public static void setTextDelimiter(String textDelimiter) {
        MyFileImporter.textDelimiter = textDelimiter;
    }

    public static String getFieldDelimiter() {
        return fieldDelimiter;
    }

    public static void setFieldDelimiter(String fieldDelimiter) {
        MyFileImporter.fieldDelimiter = fieldDelimiter;
    }

    public static String getFilePath() {
        return filePath;
    }

    public static void setFilePath(String filePath) {
        MyFileImporter.filePath = filePath;
    }

    public static String getFirstConnectedAgent() {
        return firstConnectedAgent;
    }

    public static void setFirstConnectedAgent(String firstConnectedAgent) {
        MyFileImporter.firstConnectedAgent = firstConnectedAgent;
    }

    public static String getSecondConnectedAgent() {
        return secondConnectedAgent;
    }

    public static void setSecondConnectedAgent(String secondConnectedAgent) {
        MyFileImporter.secondConnectedAgent = secondConnectedAgent;
    }

    public static void parseCsv() {
        try {
            fieldDelimiter = Panel1.selectedFileDelimiter;
            textDelimiter = Panel1.jTextFieldTextDelimiter.getText();
            headersPresent = Panel1.jCheckBoxHeadersIncluded.isSelected();

            csvParser = new CsvParser(MyFileImporter.getFilePath(), textDelimiter, fieldDelimiter);
            if (headersPresent) {
                headers = csvParser.getHeaders();
            } else {

                headers = new String[csvParser.csvReader.getColumnCount()];
                for (int i = 0; i < csvParser.csvReader.getColumnCount(); i++) {
                    if (i > alphabet.length - 1) {
                        Integer firstLetter = (i / 26);
                        headers[i] = alphabet[firstLetter] + alphabet[i % 26];
                    } else {
                        headers[i] = alphabet[i];
                    }
                }
            }
            MyFileImporter.setHeaders(headers);

            DefaultListModel listModel = new DefaultListModel();
            for (String string : headers) {
                listModel.addElement(string);
            }
            MyFileImporter.setListModelHeaders(listModel);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void parseExcel() throws FileNotFoundException, InvalidFormatException {
        try {
            fieldDelimiter = Panel1.selectedFileDelimiter;
            textDelimiter = Panel1.jTextFieldTextDelimiter.getText();
            headersPresent = Panel1.jCheckBoxHeadersIncluded.isSelected();
            sheetName = Panel1.selectedSheet;

            ExcelParser excelParser = new ExcelParser(MyFileImporter.getFilePath(), sheetName);
            if (headersPresent) {
                headers = excelParser.getHeaders();
            } else {
                Integer columnCount = excelParser.getColumnCount();
                headers = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    if (i > alphabet.length - 1) {
                        Integer firstLetter = (i / 26);
                        headers[i] = alphabet[firstLetter] + alphabet[i % 26];
                    } else {
                        headers[i] = alphabet[i];
                    }
                }
            }
            MyFileImporter.setHeaders(headers);

            DefaultListModel listModel = new DefaultListModel();
            for (String string : headers) {
                listModel.addElement(string);
            }
            MyFileImporter.setListModelHeaders(listModel);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void getConnectedAgents() {
        firstConnectedAgent = Panel2.firstConnector;
        secondConnectedAgent = Panel2.secondConnector;
        List<String> headersList = new ArrayList();
        headersList.addAll(Arrays.asList(headers));
        firstConnectedAgentIndex = headersList.indexOf(firstConnectedAgent);
        secondConnectedAgentIndex = headersList.indexOf(secondConnectedAgent);
    }

    public static void getTimeField() {
        timeField = Panel4.fieldTime;
        List<String> headersList = new ArrayList();
        headersList.addAll(Arrays.asList(headers));
        timeFieldIndex = headersList.indexOf(timeField);
    }

    @Override
    public ContainerLoader getContainer() {
        return container;
    }

    @Override
    public Report getReport() {
        return report;
    }

    public static Report getStaticReport() {
        return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return cancel;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}