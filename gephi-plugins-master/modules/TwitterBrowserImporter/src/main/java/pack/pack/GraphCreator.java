package pack.pack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


import it.uniroma1.dis.wsngroup.gexf4j.core.EdgeType;
import it.uniroma1.dis.wsngroup.gexf4j.core.Gexf;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Mode;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeClass;
import it.uniroma1.dis.wsngroup.gexf4j.core.data.AttributeList;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.GexfImpl;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.StaxGraphWriter;
import it.uniroma1.dis.wsngroup.gexf4j.core.impl.data.AttributeListImpl;

public class GraphCreator {

	List<Vertex> nodes;
	String fileName;
	String path; 
	String graphType; 
	Graph graph;
	Gexf gexf;
	HashMap<String, Node> nodesAdd; 
		
	GraphCreator(String fn, String pt, String ty){
		fileName = fn;
		path = pt; 
		graphType = ty; 
		nodes = new ArrayList<Vertex>(); 
		nodesAdd = new  HashMap<String, Node>(); 
	}
	
	
	void addVertex(Vertex a) {
		nodes.add(a);
	}
	
	
	
	void initGraph(String creator, String des) {
	
    	gexf = new GexfImpl();
		Calendar date = Calendar.getInstance();
		
		gexf.getMetadata()
			.setLastModified(date.getTime())
			.setCreator(creator)
			.setDescription(des);
		gexf.setVisualization(true);
		
		graph = gexf.getGraph();
		graph.setDefaultEdgeType(EdgeType.UNDIRECTED).setMode(Mode.STATIC);
		
		AttributeList attrList = new AttributeListImpl(AttributeClass.NODE);
		graph.getAttributeLists().add(attrList);
		
	}
	
	
	void createGraph() {

		for(Vertex it : nodes) {
			
			Node root; 
			
			if(!nodesAdd.containsValue(it.value)) {
				root = graph.createNode(it.value).setLabel(it.value);
				nodesAdd.put(it.value, root);
			}else {
				root = nodesAdd.get(it.value); 
			}
				
				for(String child : it.connections) {
					
					Node ch; 
					
					if(!nodesAdd.containsValue(child)) {
						ch = graph.createNode(child).setLabel(child);
						nodesAdd.put(child, ch);
					}else {
						ch = nodesAdd.get(child); 
					}
					
					if(!root.hasEdgeTo(ch.getId()))root.connectTo(ch).setLabel(graphType); 
				}			
		}			
	}
	
	
	
	String savefile() {
		//creates new file
		StaxGraphWriter graphWriter = new StaxGraphWriter();
		File f = new File(fileName+".gexf");
		Writer out;
		try {
			out =  new FileWriter(f, false);
			graphWriter.writeToStream(gexf, out, "UTF-8");
			System.out.println(f.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
		}
                
                return f.getAbsolutePath();
	}

}