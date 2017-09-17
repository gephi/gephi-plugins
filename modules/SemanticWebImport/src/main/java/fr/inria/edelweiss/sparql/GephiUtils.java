/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Edge;
import org.gephi.project.api.ProjectController;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import java.awt.Color;
import org.gephi.graph.api.Table;

/**
 *
 * @author edemairy
 *
 * Wrapper to help to build a graph in Gephi.
 */
public class GephiUtils {
	
	public final static String GEPHI_PREFIX = "http://gephi.org";
	public final static String SPARQLID = "sparqlid";
	
	public static Graph getCurrentGraph() {
		GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
		GraphModel model = graphController.getGraphModel();
		Graph graph = model.getGraph();
		return graph;
	}
	
	static void createProject() {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
	}
	private Graph graph;
	private GraphModel model;
	
	public GephiUtils(GraphModel model) {
		this.model = model;
		graph = model.getDirectedGraph();
	}
	
	public static void createProjectIfEmpty() {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		if (pc.getCurrentProject() == null) {
			pc.newProject();
		}
	}
	
	public static void createWorkspaceIfEmpty() {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		if (pc.getCurrentWorkspace() == null) {
			pc.newWorkspace(pc.getCurrentProject());
		}
	}
	
	public static void renameCurrentWorkspace(String newName) {
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.renameWorkspace(pc.getCurrentWorkspace(), newName);
	}
	
	public static boolean isBlankNode(String id) {
		Pattern junk = Pattern.compile("_.*");
		Matcher matcherJunk = junk.matcher(id);
		return matcherJunk.matches();
	}
	
	public static String decodeString(final String id) {
		String decodedName = "";
		try {
			decodedName = URLDecoder.decode(id, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			Exceptions.printStackTrace(ex);
		}
		return decodedName;
	}
	
	public static float convertFloat(final String id) {
		float decodedNum = 0;
		try {
			decodedNum = Float.parseFloat(id.replaceAll("\"", "\\\""));
		} catch (NumberFormatException ex) {
			Exceptions.printStackTrace(ex);
		}
		return decodedNum;
	}

	// Get values of color size RGB (0->1)
	public static float convertFloatColor(final String id) {
		float decodedNum = 0, temp;
		try {
			temp = Float.parseFloat(id.replaceAll("\"", "\\\""));
			decodedNum = (temp % 256) / 255;
		} catch (NumberFormatException ex) {
			Exceptions.printStackTrace(ex);
		}
		return decodedNum;
	}

	//Split color values RGB (0,0,0)
	public static String[] stringSplit(final String id) {
		String delimiter = "\\,";
		String[] stringValues = null;
		String temp;
		try {
			temp = id.replaceAll("\"", "\\\"");
			//temp= id.substring(1,id.length()-1);
			stringValues = temp.split(delimiter);
		} catch (Exception ex) {
			Exceptions.printStackTrace(ex);
		}
		return stringValues;
	}
	
	public void addNode(String id) {
		if (!nodeExist(id)) {
			Node newNode = model.factory().newNode();
			String decodedName = decodeString(id);
			SplittedName splittedName = splitName(decodedName);
			
			newNode.setLabel(decodedName);
			setSparqlId(newNode, decodedName);
			newNode.setAttribute("namespace", splittedName.getNamespace());
			
			Random rand = new Random();
			
			int idNum = (Integer.parseInt((String)newNode.getId()));
			
			newNode.setX((float) (100 * (idNum / 360.0) * Math.cos(Math.PI * (idNum / 36.0))));
			newNode.setY((float) (100 * (idNum / 360.0) * Math.sin(Math.PI * (idNum / 36.0))));
			newNode.setZ(0);
			newNode.setR((float) ((idNum % 16) / 15.0));
			newNode.setG((float) (((idNum / 256) >> 4) / 15.0));
			newNode.setB((float) (((idNum / 1024) >> 8) / 15.0));
			newNode.setSize(1);
			
			graph.addNode(newNode);
		}
	}
	
	public boolean nodeExist(final String s) {
		return (findNode(s) != null);
	}

	/**
	 *
	 * @param s
	 * @return
	 */
	public Node findNode(final String s) {
		String decodedString = decodeString(s);
		for (Node n : graph.getNodes().toArray()) {
			if (getSparqlId(n).equals(decodedString)) {
				return n;
			}
		}
		return null;
	}
	
	public void addEdge(String s0, String edge, String s1) {
		// Build the vertices between nodes

		Edge newEdge = model.factory().newEdge(findNode(s0), findNode(s1), 1, true);
		newEdge.setLabel(edge);
		graph.addEdge(newEdge);
	}
	
	public void setNodeLabel(final String sourceNodeName, final String argumentName) {
		findNode(sourceNodeName).setLabel(argumentName);
	}
	
	public void setNodeSize(final String sourceNodeName, final float argumentValue) {
		findNode(sourceNodeName).setSize(argumentValue);
	}
	
	public void setNodeColor(final String sourceNodeName, final float r, final float g, final float b) {
		findNode(sourceNodeName).setColor(new Color(r, g, b));
	}
	
	public void setNodeColor_R(final String sourceNodeName, final float r) {
		findNode(sourceNodeName).setR(r);
	}
	
	public void setNodeColor_G(final String sourceNodeName, final float g) {
		findNode(sourceNodeName).setG(g);
	}
	
	public void setNodeColor_B(final String sourceNodeName, final float b) {
		findNode(sourceNodeName).setB(b);
	}
	
	public void setNodeShape(final String sourceNodeName, final String shapeName) {
		//findNode (sourceNodeName).setNodeData().setShape(shapeName);
	}
	
	public void setFollowYourNoseDepth(final int depth) {
		RdfParser graphDepth = null;
		graphDepth.setDepth(depth);
	}
	
	public Object[] getNodeAttributes(final String sourceNodeName) {
		Node node = findNode(sourceNodeName);
		if (node == null) {
			addNode(sourceNodeName);
		}
		Object[] attributes = node.getAttributes();
		return attributes;
	}
	
	public static void addAttributeToNodes(final String columnName, Class klass) {
		Table nodeTable = getCurrentGraph().getModel().getNodeTable();
		if (!nodeTable.hasColumn(columnName)) {
			nodeTable.addColumn(columnName, klass);
		}
	}
	
	public boolean nodeNotAlreadyInserted(final Graph graph, final String nodeName) {
		return (findNode(nodeName) == null);
	}
	
	static protected SplittedName splitName(final String fullName) {
		String namespace;
		if (fullName.contains("#")) {
			namespace = fullName.substring(0, fullName.indexOf('#'));
		} else if (fullName.contains("/")) {
			// Namespace = until the last '/', Id = right part of '/'.
			namespace = fullName.substring(0, fullName.lastIndexOf('/'));
		} else {
			namespace = "";
		}
		
		SplittedName result = new SplittedName(namespace, shortenName(fullName));
		
		return result;
	}
	
	static protected String shortenName(final String fullName) {
		String result;
		
		if (fullName.isEmpty()) {
			result = "";
		} else {
			result = removePrefix(fullName);
			result = cleanSuffix(result);
		}
		
		return result;
	}
	
	public void removeNode(String id) {
		if (nodeExist(id)) {
			Node node = findNode(id);
			model.getGraph().removeNode(node);
		}
	}
	
	public void removeEdge(String id) {
		if (graph.getEdge(id) != null) {
			Edge edge = graph.getEdge(id);
			model.getGraph().removeEdge(edge);
		}
	}
	
	public boolean isEdge(String id) {
		return (graph.getEdge(id) != null);
	}
	
	public static void addAttributeToEdges(String columnName, Class klass) {
		Table edgeTable = getCurrentGraph().getModel().getEdgeTable();
		if (!edgeTable.hasColumn(columnName)) {
			edgeTable.addColumn(columnName, klass);
		}
	}
	
	public Object[] getEdgeAttributes(String id) {
		Edge edge = model.getGraph().getEdge(id);
		Object[] attributes = edge.getAttributes();
		return attributes;
		
	}
	
	public void setEdgeLabel(String sourceLabel, String targetLabel) {
		model.getGraph().getEdge(sourceLabel).setLabel(targetLabel);
	}
	
	static public void setSparqlId(Node node, String decodedName) {
		node.setAttribute(SPARQLID, decodedName);
	}
	
	static public String getSparqlId(Node node) {
		return node.getAttribute(SPARQLID).toString();
	}
	
	public void setNodeAttr(String sourceLabel, String nameNewAttribute, String targetLabel) {
		Node n = findNode(sourceLabel);
		n.setAttribute(nameNewAttribute, targetLabel);
	}
	
	public void setEdgeAttr(String sourceLabel, String nameNewAttribute, String targetLabel) {
		Edge e = graph.getEdge(sourceLabel);
		e.setAttribute(nameNewAttribute, targetLabel);
	}
	
	static public class SplittedName {
		
		private final String id;
		private final String namespace;
		
		public SplittedName(final String namespace, String id) {
			this.namespace = namespace;
			this.id = id;
		}
		
		public String getNamespace() {
			return this.namespace;
		}
		
		public String getId() {
			return this.id;
		}
	};
	
	static public String removePrefix(final String name) {
		String result = name;
		
		if (name.contains("#")) {
			result = name.replaceAll(".*#", "");
		}
		
		return result;
	}
	
	static public String cleanSuffix(final String name) {
		String result = name;
		
		if (name.endsWith("/")) {
			result = result.substring(0, result.length() - 1);
		}
		
		final int lastPos = result.lastIndexOf("/", result.length());
		
		if (lastPos != -1) {
			result = result.substring(lastPos + 1, result.length());
		}
		
		return result;
	}
}
