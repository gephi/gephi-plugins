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
    private boolean proportional = true;

    private boolean directed=false;
    private boolean considerDirected=false;
    private double threshold=0.0;
    
    
    private int EDGE_TYPE = 1;
    
    public LongTaskTransformation(Column attributeColumn,
            String inDimension, 
            String commonDimension, 
            String outDimension,
            double threshold, 
            boolean removeEdges, 
            boolean removeNodes,
            boolean proportional, 
            boolean considerDirected) {
        
        this.attributeColumn = attributeColumn;
        this.inDimension = inDimension;
        this.commonDimension = commonDimension;
        this.outDimension = outDimension;
        this.removeEdges = removeEdges;
        this.removeNodes = removeNodes;
        this.proportional = proportional;
        this.considerDirected=considerDirected;
        this.threshold=threshold;
    }
    
    public void run_offline(GraphModel graphModel ) {
           execute(graphModel, false) ;
    }

    @Override
    public void run() {
        // number of tickets
        Progress.start(progressTicket, 5);

        // graph
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getGraphModel();
        execute(graphModel, true) ;
    }   

        private  void execute(GraphModel graphModel,Boolean online) {
        // number of tickets
        if (online) Progress.start(progressTicket, 5);

        // graph
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class); 
        Graph graph = graphModel.getGraphVisible();
        // Need to do that now because removing all edges later will creates error
        // on undirected graph
        boolean isDirectedGraph = graphModel.isDirected();
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
        if (online) Progress.start(progressTicket, firstVertical.size()+6);
   
        if (cancelled) {
            return;
        }
        if (online) Progress.progress(progressTicket,1);

        // first matrix
        Matrix firstMatrix = new Matrix(firstVertical.size(), firstHorizontal.size());
        Matrix firstUnweightMatrix = new Matrix(firstVertical.size(), firstHorizontal.size());
        float [] firstWeights= new float[firstVertical.size()]; 
        float [] firstUnweightWeights= new float[firstVertical.size()];
        {
        Edge[] edges = graph.getEdges().toArray();
        
        // get the type of edges... this is not robust
         if (edges.length>0){
             EDGE_TYPE= edges[0].getType();
            logger.log(Level.SEVERE, null, "edge type"+  EDGE_TYPE);
        } else {
            logger.log(Level.SEVERE, null, "no edges in graph");
        } 
        }
        for (int i = 0; i < firstVertical.size(); i++) {
            Set<Node> intersection = new HashSet<Node>(Arrays.asList(graph.getNeighbors(firstVertical.get(i)).toArray()));
            if (intersection.size() > 0) {
                try {
                    intersection.retainAll(firstHorizontal);
                    for (Node neighbour : intersection) {
                     int j=firstHorizontal.indexOf(neighbour);
                        if (j > -1){
                            Edge edge = graph.getEdge(firstVertical.get(i), firstHorizontal.get(j),EDGE_TYPE);
                            if (edge!= null) {
                                double w=edge.getWeight();
                                firstWeights[i]+=w*w;
                                firstUnweightWeights[i]+=1;
                                firstMatrix.set(i, j, w);
                                firstUnweightMatrix.set(i, j, (float)1.0);
                        }
                        }
                    }
                } catch (UnsupportedOperationException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
        // second matrix
        Matrix secondMatrix = new Matrix(secondVertical.size(), secondHorizontal.size());
        Matrix secondUnweightMatrix = new Matrix(secondVertical.size(), secondHorizontal.size());
        float [] secondWeights= new float[secondHorizontal.size()];
        float [] secondUnweightWeights= new float[firstVertical.size()]; 
        for (int i = 0; i < secondVertical.size(); i++) {
            
            Set<Node> intersection = new HashSet<Node>(Arrays.asList(graph.getNeighbors(secondVertical.get(i)).toArray()));
            if (intersection.size() > 0) {
                try {
                    intersection.retainAll(secondHorizontal);
                         for (Node neighbour : intersection) {
                            int j=secondHorizontal.indexOf(neighbour);
                            if (j>-1){                    
                                Edge edge =graph.getEdge(secondVertical.get(i), secondHorizontal.get(j),EDGE_TYPE);
                                 if (edge!= null) {
                                    double w=edge.getWeight();
                                    secondWeights[j]+=w*w;
                                    secondUnweightWeights[j]+=1;
                                   secondMatrix.set(i, j, w);
                                    secondUnweightMatrix.set(i, j, (float)1.0);
                                }
                            }
                         }
                } catch (UnsupportedOperationException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
        if (cancelled) {
            return;
        }
        if (online) Progress.progress(progressTicket, "Multiplication",2);
        
        Matrix result = firstMatrix.timesParallelIndexed(secondMatrix);
        if (cancelled) {
            return;
        }
        if (online)  Progress.progress(progressTicket, "Unweighted Multiplication",3);
        
        Matrix resultUnw = firstUnweightMatrix.timesParallelIndexed(secondUnweightMatrix);
        if (cancelled) {
            return;
        }
        if (online) Progress.progress(progressTicket, "Removing nodes/edges",4);
        float minDim=(float)secondVertical.size();
          
        if (removeNodes) {
            Node[] nodesToRemove= firstHorizontal.toArray(new Node[firstHorizontal.size()]);
            firstHorizontal.clear();
            secondVertical.clear();
            for (Node n : nodesToRemove) {
                graph.removeNode(n);
             }
        } else {
            if (removeEdges) {
                for (int i = 0; i < firstMatrix.getM(); i++) {
                    for (int j = 0; j < firstMatrix.getN(); j++) {
                        if (graph.contains(firstVertical.get(i)) && graph.contains(firstHorizontal.get(j)) && graph.getEdge(firstVertical.get(i), firstHorizontal.get(j),EDGE_TYPE) != null && firstMatrix.get(i, j) > 0) {
                            Edge edgeToRemove= graph.getEdge(firstVertical.get(i), firstHorizontal.get(j),EDGE_TYPE);
                            graph.removeEdge(edgeToRemove);
                        }
                    }
                }
                
                for (int i = 0; i < secondMatrix.getM(); i++) {
                    for (int j = 0; j < secondMatrix.getN(); j++) {
                        if (graph.contains(secondVertical.get(i)) && graph.contains(secondHorizontal.get(j)) && graph.getEdge(secondVertical.get(i), secondHorizontal.get(j),EDGE_TYPE) != null && secondMatrix.get(i, j) > 0) {
                            Edge edgeToRemove= graph.getEdge(secondVertical.get(i), secondHorizontal.get(j),EDGE_TYPE);
                            graph.removeEdge(edgeToRemove);
                       }
                    }
                }
            }
        }
          
        if (cancelled) {
            return;
        }
        if (online) Progress.progress(progressTicket, "Creating new edges",5);
        /* AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
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
        
        Edge ee;

        if(!this.proportional){
            for (int i = 0; i < result.getM(); i++) {
                for (int j = 0; j < result.getN(); j++) {
                    if (graph.contains(firstVertical.get(i)) && graph.contains(secondHorizontal.get(j)) 
                            && graph.getEdge(firstVertical.get(i), secondHorizontal.get(j)) == null
                            && result.get(i, j) > threshold) {
                        Node node1 = firstVertical.get(i);
                        Node node2 = secondHorizontal.get(j);
                        if(node1 != node2){
                             ee = graph.getEdge(node1, node2, EDGE_TYPE);
                            if(ee == null){ //Add if not already existing
                                ee = graphModel.factory().newEdge(firstVertical.get(i), secondHorizontal.get(j), EDGE_TYPE, (float) result.get(i, j), isDirectedGraph);
                                graph.addEdge(ee);
                            }
                            ee.setWeight(result.get(i, j));
                            ee.setAttribute(MMNT, inDimension + "<--->" + outDimension);
                            ee.setLabel(inDimension + "-" + outDimension);
                        }
                    }
		    if (cancelled) {
		        return;
		    }           
		    if (online) Progress.progress (progressTicket,i+6);
                }
            }
            } else {
            /*
                AttributeColumn edgeStrengthCol = model.getEdgeTable().getColumn("MM-Strength");        
                if ( edgeStrengthCol == null) {
                     edgeStrengthCol = model.getEdgeTable().addColumn("MMStrength", AttributeType.FLOAT);
                }

                AttributeColumn nodeStrengthCol = model.getNodeTable().getColumn("MM-Connections");        
                if ( nodeStrengthCol == null) {
                     nodeStrengthCol = model.getNodeTable().addColumn("MM-Connections", AttributeType.FLOAT);
                }
               AttributeColumn nodeVolCol = model.getNodeTable().getColumn("MM-vol");        
                if ( nodeVolCol == null) {
                     nodeVolCol = model.getNodeTable().addColumn("MM-vol", AttributeType.FLOAT);
                }
            */ 
           Table nodeTable = graphController.getGraphModel().getNodeTable();
           Column edgeStrengthCol;
            if(!edgeTable.hasColumn("MM-Strength")){
                edgeStrengthCol = edgeTable.addColumn("MM-Strength", Float.class);
            } else {
                edgeStrengthCol = edgeTable.getColumn("MM-Strength");
            } 
 
            Column nodeStrengthCol;
            if(!nodeTable.hasColumn("MM-Connections")){
                nodeStrengthCol = nodeTable.addColumn("MM-Connections", Float.class);
            } else {
                nodeStrengthCol = nodeTable.getColumn("MM-Connections");
            } 
           Column nodeVolCol;
            if(!nodeTable.hasColumn("MM-vol")){
                nodeVolCol = nodeTable.addColumn("MM-vol", Float.class);
            } else {
                nodeVolCol = nodeTable.getColumn("MM-vol");
            } 
            
            for (int i = 0; i < result.getM(); i++) {
                // for each node include the number of connections it has
                // and the accumulated quadratic weights of them
                Node node1 = firstVertical.get(i);
                node1.setAttribute(nodeStrengthCol,new Float(100.0*firstUnweightWeights[i]/minDim));
                node1.setAttribute(nodeVolCol, new Float(Math.sqrt(firstWeights[i])));
                //firstVertical.get(i).getNodeData().getAttributes().setValue(nodeStrengthCol.getIndex(),(float)100.0*firstUnweightWeights[i]/minDim);
                // firstVertical.get(i).getNodeData().getAttributes().setValue(nodeVolCol.getIndex(),(float)Math.sqrt(firstWeights[i]));
                for (int j = 0; j < result.getN(); j++) {
                    //System.out.println("processing (" + firstVertical.get(i).getNodeData().getLabel() +"-"+
                    //        secondHorizontal.get(j).getNodeData().getLabel()+")");
                    Node node2 = secondHorizontal.get(j);
                    try {
                    if (graph.contains(node1) && graph.contains(node2) 
                            && graph.getEdge(node1, node2) == null ) {
                                float iniWeight = (float) result.get(i, j); 
                                //iniWeight= (float)(iniWeight/(Math.sqrt(secondWeights[j])*Math.sqrt(firstWeights[i]))) ; //that is the cosine distance
                                iniWeight= (float)(100.0-200.0*Math.acos(iniWeight/(Math.sqrt(secondWeights[j])*Math.sqrt(firstWeights[i])))/Math.PI) ; //that is the cosine distance
                                // now we weight it by the number of components
                                iniWeight=Math.round(iniWeight * 100) / 100;
                                float finalWeight= (float) 100.0* (float) Math.sqrt( resultUnw.get(i, j) / minDim) ; // how many components in common
                                    // System.out.println("going to create");
                                    if (iniWeight > threshold) {
                                     ee = graphModel.factory().newEdge(node1, node2, EDGE_TYPE, iniWeight,this.directed );   
                                     if (!ee.isSelfLoop()) {
                                        ee.setAttribute(edgeStrengthCol,new Float(finalWeight));
                                        ee.setAttribute(MMNT, inDimension + "<--->" + outDimension);
                                        ee.setLabel(inDimension + "-" + outDimension);
                                        graph.addEdge(ee);
                                     }
                                }
                                //    else {System.out.println("under threshold "+iniWeight);}
                      }
                    } catch (Exception e){
                        e.printStackTrace(System.out);
                        System.out.println(e.getCause());
                        break;
                    }
                      //else {System.out.println("not null?");}
                }
            if (cancelled) {
                    return;
             }
            if (online) Progress.progress (progressTicket,i+6);
            }
        }     
  
        if (online) Progress.finish(progressTicket);
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
