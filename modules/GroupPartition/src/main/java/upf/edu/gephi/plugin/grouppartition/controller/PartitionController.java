package upf.edu.gephi.plugin.grouppartition.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;
import upf.edu.gephi.plugin.grouppartition.algorithm.GraphPartitionAlgorithmFactory;
import upf.edu.gephi.plugin.grouppartition.algorithm.IGraphPartitionAlgorithm;

/**
 *
 * @author puig
 */
public class PartitionController {

    ProjectController pc;
    Workspace originWorkspace;
    
    public PartitionController() {
        pc = Lookup.getDefault().lookup(ProjectController.class);
        //Should be a parameter to select the workspace
        originWorkspace = pc.getCurrentWorkspace();
    }
    
    void generatePartition(boolean createNewWorkspace){
        
        //Reads graph from actual workspace model. TODO Choose different workspace to load from.
        GraphModel gmodel = originWorkspace.getLookup().lookup(GraphModel.class);
        Graph graph = gmodel.getGraph();
        graph.readLock();
        //Factory decides which algorithm to load.
        //TODO Should be a parameter to choose a algorithm manually or auto.
        //TODO More parameters like a comparison class (to not only be able to do partitions on colors alone)
        GraphPartitionAlgorithmFactory gp_factory = new GraphPartitionAlgorithmFactory(graph);
        IGraphPartitionAlgorithm gAlgorithm = gp_factory.getGraphPartition();
        Graph graphAlgorithm = gAlgorithm.doPartition();
        graph.readUnlock();
        
        if (createNewWorkspace) {
            Workspace workspace = pc.newWorkspace(pc.getCurrentProject());
            pc.openWorkspace(workspace);
        }
        else {
            graph.clear();
        }
        
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel model = gc.getGraphModel();
        Graph newGraph = model.getDirectedGraph();
        GraphFactory fact = model.factory();
        
        Table nodeTable = model.getNodeTable();
        nodeTable.addColumn("Size", int.class);
        
        Table edgeTable = model.getEdgeTable();
        edgeTable.addColumn("Size", int.class);
        
        //We recreate the graph in a workspace because we could not found a way to add the graph directly to the worksapce
        //Mapping of old id node and new node
        Map<String, Node> GhostToNew = new HashMap<String,Node>();
        
        //TODO Use a position algorithm
        Random random = new Random(232323); //Just to position the nodes
       
        for(Node node : graphAlgorithm.getNodes().toArray()){
           Node nNode = fact.newNode();
           nNode.setLabel(node.getLabel());
           Integer qty = Integer.valueOf(node.getAttribute("Size").toString());
           nNode.setAttribute("Size", node.getAttribute("Size"));
           nNode.setSize(qty);
           nNode.setColor(node.getColor());
           
           nNode.setX(random.nextInt(2000) - 1000);
           nNode.setY(random.nextInt(2000) - 1000);
           newGraph.addNode(nNode); 
           GhostToNew.put(node.getId().toString(), nNode);
        }
        for(Edge edge : graphAlgorithm.getEdges().toArray()){
           Node node1 = GhostToNew.get(edge.getSource().getId().toString());
           Node node2 = GhostToNew.get(edge.getTarget().getId().toString());
           Edge nEdge = fact.newEdge(node1, node2, graphAlgorithm.isDirected());
           nEdge.setAttribute("Size", Integer.valueOf(edge.getAttribute("Size").toString()));
           nEdge.setWeight(edge.getWeight());
           newGraph.addEdge(nEdge);
        }
    }
}
