package net.clementlevallois.controller;

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
import net.clementlevallois.parsers.CsvParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.DefaultListModel;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.io.importer.api.Report;
import org.gephi.io.importer.spi.WizardImporter;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;

public class MyFileImporter implements WizardImporter, LongTask {

    public static ContainerLoader container;
    private static Report report;
    private ProgressTicket progressTicket;
    private boolean cancel = false;
    private static String[] headers;
    private static String filePathAndName;
    private static String fileName;
    private static boolean weightedAttributes;
    
    private static DefaultListModel listModelHeaders = new DefaultListModel();
    private static String textDelimiter = "\"";
    public static String sheetName;
    private static String fieldDelimiter = ",";
    public static Boolean headersPresent = true;

    @Override
    public boolean execute(ContainerLoader loader) {
        container = loader;
        report = new Report();

        Controller controller = new Controller();
        try {
            controller.run();
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvalidFormatException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
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

    public static String getFilePathAndName() {
        return filePathAndName;
    }

    public static String getFileName() {
        return fileName;
    }

    public static void setFilePathAndName(String filePath) {
        MyFileImporter.filePathAndName = filePath;
    }

    public static void setFileName(String fileName) {
        MyFileImporter.fileName = fileName;
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

    public static boolean isWeightedAttributes() {
        return weightedAttributes;
    }

    public static void setWeightedAttributes(boolean weightedAttributes) {
        MyFileImporter.weightedAttributes = weightedAttributes;
    }
    
    
}
