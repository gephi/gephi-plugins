package Gaze;

import Controller.MyFileImporter;
import Utils.Utils;
import Wizard.Panel1;
import com.google.common.collect.BiMap;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDirectionDefault;
//import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;

/**
 *
 * @author C. Levallois
 */
public class Controller{

    //
    // ##### parameters
    //
    //
    public static boolean directedNetwork = true;
    public static boolean weightedNetwork;
    private static double cosineThreshold = 0.01;
    public static int maxNbTargetsPerSourceConsidered4CosineCalc = 1000;
    public static int minNbofCitationsASourceShouldMake = 0;
    public static int minNbofTimesASourceShouldBeCited = 0;
    public static int nbThreads = 8;
    public static int batchSize = 1;
    public static int testruns = 25000;
    public static String filePath;
    ContainerLoader container;
    static private String fieldSeparator = ",";

    //
    // ##### objects and variables
    //
    //
    public static FlexCompColMatrix similarityMatrix;
    static public int countFinishedThreads = 0;
    static BufferedWriter bw;
    static String currLine;
    public static int countCalculus = 0;

    public Controller(String filePath) {
        this.filePath = filePath;
    }

    public void run() {
        try {

            //this line is for the users who did not change the default value of zero to 1 when the network is directed
            if (directedNetwork & minNbofCitationsASourceShouldMake == 0) {
                minNbofCitationsASourceShouldMake = 1;
            }

            String fieldDelimiter = Utils.getCharacter(Panel1.selectedFileDelimiter);
            ParserAndVectorsBuilder tr = new ParserAndVectorsBuilder(fieldDelimiter);
            SparseVector[] listVectors = tr.EdgeListToMatrix();

            Thread t = new Thread(new CosineCalculation(listVectors));
            t.start();
            t.join();
            System.out.println("Cosine calculated!");

            //        This invert operation symply inversts keys and values in the map for ease of retrieval - nothing more!
            BiMap<Integer, String> inverseMapSources = ParserAndVectorsBuilder.mapSources.inverse();
            BiMap<Integer, String> inverseMapNodes = ParserAndVectorsBuilder.mapNodes.inverse();
            Iterator<MatrixEntry> itSM;
            container = MyFileImporter.container;
            container.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);

            Iterator<Integer> ITMapNodes = ParserAndVectorsBuilder.mapUndirected.keySet().iterator();
            NodeDraft node;

            //add nodes
            while (ITMapNodes.hasNext()) {
                int currEntry = ITMapNodes.next();
                if (ParserAndVectorsBuilder.multisetTargets.count(ParserAndVectorsBuilder.mapTargets.get(ParserAndVectorsBuilder.mapNodes.inverse().get(currEntry))) < minNbofTimesASourceShouldBeCited | ParserAndVectorsBuilder.mapUndirected.keys().count(currEntry) < minNbofCitationsASourceShouldMake) {
                    continue;
                }

                String nodeLabel = ParserAndVectorsBuilder.mapNodes.inverse().get(currEntry);
                node = container.factory().newNodeDraft(nodeLabel);
                node.setLabel(nodeLabel);

                container.addNode(node);
            }

            //add edges
            Integer idEdge = 0;
            EdgeDraft edge;

            itSM = similarityMatrix.iterator();

            while (itSM.hasNext()) {

                MatrixEntry currElement = itSM.next();
                double csCoeff = currElement.get();
                if (currElement.column() == currElement.row()) {
                    continue;
                }

                int nbOccAsSourceColumn = ParserAndVectorsBuilder.map.get((int) currElement.column()).size();
                int nbOccAsSourceRow = ParserAndVectorsBuilder.map.get((int) currElement.row()).size();
                int nbOccAsTargetColumn = ParserAndVectorsBuilder.multisetTargets.count(ParserAndVectorsBuilder.mapTargets.get((inverseMapSources.get((int) currElement.column()))));
                int nbOccAsTargetRow = ParserAndVectorsBuilder.multisetTargets.count(ParserAndVectorsBuilder.mapTargets.get((inverseMapSources.get((int) currElement.row()))));

                if ((csCoeff > cosineThreshold)
                        & (nbOccAsSourceColumn >= minNbofCitationsASourceShouldMake) & (nbOccAsSourceRow >= minNbofCitationsASourceShouldMake)
                        & (nbOccAsTargetColumn >= minNbofTimesASourceShouldBeCited) & (nbOccAsTargetRow >= minNbofTimesASourceShouldBeCited)) {

                    edge = container.factory().newEdgeDraft(String.valueOf(idEdge));
                    idEdge = idEdge + 1;
                    edge.setSource(container.getNode(inverseMapSources.get((int) currElement.column())));
                    edge.setTarget(container.getNode(inverseMapSources.get((int) currElement.row())));
                    edge.setWeight((float) csCoeff);
                    edge.setDirection(EdgeDirection.UNDIRECTED);
                    container.addEdge(edge);
                }
            }

        } catch (InterruptedException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
