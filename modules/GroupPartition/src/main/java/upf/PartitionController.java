/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package upf;

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
        
        GraphModel gmodel = originWorkspace.getLookup().lookup(GraphModel.class);
        Graph graph = gmodel.getGraph();
        graph.readLock();
        GraphPartitionFactory gp_factory = new GraphPartitionFactory(graph);
        //Should be a parameter to choose a algorithm manually or auto.
        IPartitionGraph gAlgorithm = gp_factory.getGraphPartition();
        Graph graphAlgorithm = gAlgorithm.partition();
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
        
        Map<String, Node> GhostToNew = new HashMap<String,Node>();
        
        Random random = new Random(232323);
        
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
           newGraph.addEdge(nEdge);
        }
               
        //Phantom Graph example
        /*
        GraphModel model = GraphModel.Factory.newInstance();  
        Graph g = model.getDirectedGraph();
        GraphFactory fact = model.factory();
        Node node = fact.newNode("x0");
        node.setLabel("X 0 First");
        Random random = new Random(232323);
        node.setX(random.nextInt(2000) - 1000);
        node.setY(random.nextInt(2000) - 1000);
        node.setSize(10.0f);
        g.addNode(node);
        
        for (Node a : g.getNodes().toArray()){
            System.out.println(node.toString());
            System.out.println(node.getLabel());
        }
        */
        //End Phantom Graph
        //How to create a node in current workspace
        /*
        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel model = gc.getGraphModel();
        Graph g = model.getDirectedGraph();
        GraphFactory fact = model.factory();
        Node node = fact.newNode("x0");
        node.setLabel("X 0 First");
        Random random = new Random(232323);
        node.setX(random.nextInt(2000) - 1000);
        node.setY(random.nextInt(2000) - 1000);
        node.setSize(10.0f);
        g.addNode(node);
        */
        //End create node
        
        
        
        //newWorkspace.
        //newWorkspace.add(model);
        //newWorkspace.add(g);
        //End delete
    }
}
