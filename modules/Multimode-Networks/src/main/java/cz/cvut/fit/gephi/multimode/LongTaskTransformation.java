package cz.cvut.fit.gephi.multimode;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Column;

import org.gephi.graph.api.*;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

/**
 *
 * @author Jaroslav Kuchar
 */
public class LongTaskTransformation implements LongTask, Runnable {
    private Logger logger = Logger.getLogger(LongTaskTransformation.class.getName()); 
    private ProgressTicket progressTicket;
    private boolean cancelled = false;
    private Column attributeColumn = null;
    private String inDimension;
    private String commonDimension;
    private String outDimension;
    private boolean removeEdges = true;
    private boolean removeNodes = true;
    
    private static final int EDGE_TYPE = 1;
    
    public LongTaskTransformation(Column attributeColumn, String inDimension, String commonDimension, String outDimension, boolean removeEdges, boolean removeNodes) {
        this.attributeColumn = attributeColumn;
        this.inDimension = inDimension;
        this.commonDimension = commonDimension;
        this.outDimension = outDimension;
        this.removeEdges = removeEdges;
        this.removeNodes = removeNodes;
    }
    
    @Override
    public void run() {
        // number of tickets
        Progress.start(progressTicket, 5);

        // graph
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();
        Graph graph = graphModel.getGraphVisible();
        //Graph graph = graphModel.getUndirectedGraphVisible();
        Node[] nodes = graph.getNodes().toArray();

        // matrix axis
        List<Node> firstHorizontal = new ArrayList<Node>();
        List<Node> firstVertical = new ArrayList<Node>();
        List<Node> secondHorizontal = new ArrayList<Node>();
        List<Node> secondVertical = new ArrayList<Node>();
        for (Node n : nodes) {
            String nodeValue;
            Object val = n.getAttribute(attributeColumn);
            if (val != null) {
                nodeValue = val.toString();
            } else {
                nodeValue = "null";
            }
            // matrix axis
            if (nodeValue.equals(inDimension)) {
                firstVertical.add(n);
            }
            if (nodeValue.equals(commonDimension)) {
                firstHorizontal.add(n);
                secondVertical.add(n);
            }
            if (nodeValue.equals(outDimension)) {
                secondHorizontal.add(n);
            }
        }
        
        if (cancelled) {
            return;
        }
        Progress.progress(progressTicket);

        // first matrix
        Matrix firstMatrix = new Matrix(firstVertical.size(), firstHorizontal.size());
        for (int i = 0; i < firstVertical.size(); i++) {
            Set<Node> intersection = new HashSet<Node>(Arrays.asList(graph.getNeighbors(firstVertical.get(i)).toArray()));
            if (intersection.size() > 0) {
                try {
                    intersection.retainAll(firstHorizontal);
                    for (Node neighbour : intersection) {
                        firstMatrix.set(i, firstHorizontal.indexOf(neighbour), 1);
                    }
                } catch (UnsupportedOperationException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
        // second matrix
        Matrix secondMatrix = new Matrix(secondVertical.size(), secondHorizontal.size());
        for (int i = 0; i < secondVertical.size(); i++) {
            
            Set<Node> intersection = new HashSet<Node>(Arrays.asList(graph.getNeighbors(secondVertical.get(i)).toArray()));
            if (intersection.size() > 0) {
                try {
                    intersection.retainAll(secondHorizontal);
                    for (Node neighbour : intersection) {
                        secondMatrix.set(i, secondHorizontal.indexOf(neighbour), 1);
                    }
                } catch (UnsupportedOperationException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
        if (cancelled) {
            return;
        }
        Progress.progress(progressTicket, "Multiplication");
        
        Matrix result = firstMatrix.timesParallelIndexed(secondMatrix);
        if (cancelled) {
            return;
        }
        Progress.progress(progressTicket, "Removing nodes/edges");
        
        
        if (removeNodes) {
            for (Node n : firstHorizontal) {
                graph.removeNode(n);
            }
        } else {
            if (removeEdges) {
                for (int i = 0; i < firstMatrix.getM(); i++) {
                    for (int j = 0; j < firstMatrix.getN(); j++) {
                        if (graph.contains(firstVertical.get(i)) && graph.contains(firstHorizontal.get(j)) && graph.getEdge(firstVertical.get(i), firstHorizontal.get(j)) != null && firstMatrix.get(i, j) > 0) {
                            graph.removeEdge(graph.getEdge(firstVertical.get(i), firstHorizontal.get(j)));
                        }
                    }
                }
                
                for (int i = 0; i < secondMatrix.getM(); i++) {
                    for (int j = 0; j < secondMatrix.getN(); j++) {
                        if (graph.contains(secondVertical.get(i)) && graph.contains(secondHorizontal.get(j)) && graph.getEdge(secondVertical.get(i), secondHorizontal.get(j)) != null && secondMatrix.get(i, j) > 0) {
                            graph.removeEdge(graph.getEdge(secondVertical.get(i), secondHorizontal.get(j)));
                        }
                    }
                }
            }
        }
        
        if (cancelled) {
            return;
        }
        Progress.progress(progressTicket, "Creating new edges");
        /*AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        AttributeModel model = ac.getModel();        
        AttributeColumn edgeTypeCol = model.getEdgeTable().getColumn("MMNT-EdgeType");        
        if (edgeTypeCol == null) {
            edgeTypeCol = model.getEdgeTable().addColumn("MMNT-EdgeType", AttributeType.STRING);
        }*/
        
        Table edgeTable = graphController.getGraphModel().getEdgeTable();
        Column MMNT;
        if(!edgeTable.hasColumn("MMNT-EdgeType")){
            MMNT = edgeTable.addColumn("MMNT-EdgeType", String.class);
        } else {
            MMNT = edgeTable.getColumn("MMNT-EdgeType");
        }
        
        for (int i = 0; i < result.getM(); i++) {
            for (int j = 0; j < result.getN(); j++) {
                if (graph.contains(firstVertical.get(i)) && graph.contains(secondHorizontal.get(j)) && graph.getEdge(firstVertical.get(i), secondHorizontal.get(j)) == null && result.get(i, j) > 0) {
                    Node node1 = firstVertical.get(i);
                    Node node2 = secondHorizontal.get(j);
                    if(node1 != node2){
                        Edge ee = graph.getEdge(node1, node2, EDGE_TYPE);
                        if(ee == null){ //Add if not already existing
                            ee = graphModel.factory().newEdge(firstVertical.get(i), secondHorizontal.get(j), EDGE_TYPE, (float) result.get(i, j), graphModel.isDirected());
                            graph.addEdge(ee);
                        }
                        
                        ee.setWeight(result.get(i, j));
                        ee.setAttribute(MMNT, inDimension + "<--->" + outDimension);
                        ee.setLabel(inDimension + "-" + outDimension);
                    }
                }
            }
        }
        Progress.finish(progressTicket);
    }
    
    @Override
    public boolean cancel() {
        cancelled = true;
        return true;
    }
    
    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this.progressTicket = pt;
    }
}
