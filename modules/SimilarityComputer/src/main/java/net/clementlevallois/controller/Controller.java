package net.clementlevallois.controller;

import com.google.common.collect.Multiset;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.clementlevallois.computer.CosineCalculation;
import net.clementlevallois.computer.VectorsBuilder;
import net.clementlevallois.graphgenerator.GraphOperations;
import net.clementlevallois.parsers.CsvParser;
import net.clementlevallois.parsers.ExcelParser;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.Report;

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
public class Controller {

    //
    // ##### parameters
    //
    //
    private final ContainerLoader container;

    //
    // ##### objects and variables
    //
    //
    public static FlexCompColMatrix similarityMatrix;
    static public int countFinishedThreads = 0;
    static BufferedWriter bw;
    static String currLine;
    public static int countCalculus = 0;

    private Map<String, Map<String, Multiset<String>>> datastruct = new HashMap();

    public Controller(ContainerLoader container) {
        this.container = container;
    }

    public Report run() throws FileNotFoundException, InvalidFormatException, ExecutionException, IOException, InterruptedException {
        Report report;
        
        if (MyFileImporter.getFileName().endsWith("xls") | MyFileImporter.getFileName().endsWith("xlsx")) {
            ExcelParser excelParser = new ExcelParser(MyFileImporter.getFilePathAndName(), MyFileImporter.sheetName);
            datastruct = excelParser.parse();
            report = excelParser.getReport();
        } else {
            CsvParser csvParser = new CsvParser(MyFileImporter.getFilePathAndName(), MyFileImporter.getTextDelimiter(), MyFileImporter.getFieldDelimiter());
            datastruct = csvParser.parse();
            report = csvParser.getReport();
        }

        VectorsBuilder vectorsBuilder = new VectorsBuilder();
        Map<String, SparseVector[]> attributesToVectorsArrays = vectorsBuilder.sparseVectorArrayBuilder(datastruct);

        ExecutorService executorService = Executors.newScheduledThreadPool(5);

        Set<Callable<FlexCompColMatrix>> callables = new HashSet();

        for (String attribute : attributesToVectorsArrays.keySet()) {
            callables.add(new CosineCalculation(attributesToVectorsArrays.get(attribute)));
        }

        List<Future<FlexCompColMatrix>> futures = executorService.invokeAll(callables);

        List<FlexCompColMatrix> similarityMatrices = new ArrayList();
        for (Future<FlexCompColMatrix> future : futures) {
            similarityMatrices.add(future.get());
        }

        executorService.shutdown();

        Logger.getLogger("").log(Level.INFO, "Cosine calculated!");

        container.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);

        GraphOperations graphOperations = new GraphOperations();
        graphOperations.createGraph(container, datastruct, similarityMatrices);

        Logger.getLogger("").log(Level.INFO, "Graph created!");
        
        return report;
    }
}
