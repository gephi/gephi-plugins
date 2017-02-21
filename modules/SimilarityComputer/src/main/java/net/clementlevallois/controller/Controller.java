package net.clementlevallois.controller;

import net.clementlevallois.controller.MyFileImporter;
import net.clementlevallois.utils.Utils;
import Wizard.Panel1;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import net.clementlevallois.parsers.CsvParser;
import net.clementlevallois.parsers.ExcelParser;
import net.clementlevallois.utils.Pair;
import net.clementlevallois.utils.PairWithWeight;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;

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
    public static String filePath;
    ContainerLoader container;

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

    public Controller(String filePath) {
        this.filePath = filePath;
    }

    public void run() throws FileNotFoundException, InvalidFormatException, ExecutionException, IOException, InterruptedException {

        if (MyFileImporter.getFileName().endsWith("xls") | MyFileImporter.getFileName().endsWith("xlsx")) {
            ExcelParser excelParser = new ExcelParser(MyFileImporter.getFilePathAndName());
            datastruct = excelParser.parse();
        } else {
            CsvParser csvParser = new CsvParser(MyFileImporter.getFilePathAndName(), MyFileImporter.getTextDelimiter(), MyFileImporter.getFieldDelimiter() );
            datastruct = csvParser.parse();
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

        container = MyFileImporter.container;
        container.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);

        NodeDraft node;

        //add nodes
        for (String nodeName : datastruct.keySet()) {
            node = container.factory().newNodeDraft(nodeName);
            node.setLabel(nodeName);
            container.addNode(node);
        }

        //add edges
        Integer idEdge = 0;
        EdgeDraft edge;
        Iterator<MatrixEntry> itSM;

        List<PairWithWeight> edges = new ArrayList();

        for (FlexCompColMatrix simMatrix : similarityMatrices) {

            itSM = simMatrix.iterator();

            while (itSM.hasNext()) {

                MatrixEntry currElement = itSM.next();
                double csCoeff = currElement.get();
                if (csCoeff <= 0) {
                    continue;
                }
                if (currElement.column() == currElement.row()) {
                    continue;
                }

                String source = container.getNode(VectorsBuilder.mapNodes.inverse().get((int) currElement.column())).getId();
                String target = container.getNode(VectorsBuilder.mapNodes.inverse().get((int) currElement.row())).getId();

                
                //complex stuff just to add the weights of multiple edges between nodes (because they are similar on multiple attributes)
                Pair pair;
                if (source.compareTo(target) < 0) {
                    pair = new Pair(source, target);
                } else {
                    pair = new Pair(target, source);
                }
                PairWithWeight pww = new PairWithWeight();
                pww.setPair(pair);
                pww.setWeight(csCoeff);
                if (edges.contains(pww)){
                    PairWithWeight get = edges.remove(edges.indexOf(pww));
                    get.setWeight(get.getWeight()+csCoeff);
                    edges.add(get);
                }
                else{
                    edges.add(pww);                    
                }

            }
        }

            //now we can add all these edges
            for (PairWithWeight pww : edges) {
                
                edge = container.factory().newEdgeDraft(String.valueOf(idEdge));
                idEdge = idEdge + 1;
                edge.setSource(container.getNode((String)pww.getPair().getLeft()));
                edge.setTarget(container.getNode((String)pww.getPair().getRight()));
                edge.setWeight((float) pww.getWeight());
                edge.setDirection(EdgeDirection.UNDIRECTED);
                container.addEdge(edge);
            }

    }
}
