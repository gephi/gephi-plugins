/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.statistics;

import fr.com.hp.hpl.jena.rdf.arp.MalformedURIException;
import fr.com.hp.hpl.jena.rdf.arp.URI;
import fr.inria.edelweiss.semantic.SemanticWebImportMainWindowTopComponent;
import fr.inria.edelweiss.semantic.statistics.gui.JTreeTable;
import fr.inria.edelweiss.semantic.statistics.gui.TypeTreeModel;
import fr.inria.edelweiss.semantic.statistics.gui.TypeTreeNode;
import fr.inria.edelweiss.sparql.SparqlRequester;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreeNode;
import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.io.IOException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author edemairy
 */
public class SemanticWebStatistics implements Statistics {

    public static final String GET_SUBCLASSES = "select ?x ?t where { ?x rdfs:subClassOf ?t } group by ?x";
    public static final String COUNT_TYPES = "select ?t (count(?x) as ?total) where { ?x rdf:type ?t } group by ?t";
    public static final String DRIVER_TYPE = "fr.inria.edelweiss.sparql.corese.CoreseDriver";
    private static final Logger logger = Logger.getLogger(SemanticWebStatistics.class.getName());
    private final StringBuilder summary = new StringBuilder();
    private final StringBuilder report = new StringBuilder();

    /*
     * Turn the exception mechanism in a boolean function.
     */
    static protected boolean isURILegal(final String uri) {
        try {
            URI newUri = new URI(uri);
        } catch (NullPointerException e) {
            return false;
        } catch (MalformedURIException e) {
            return false;
        }
        return true;
    }

    /**
     * Computes the statistics. The resulting report can be obtained from the {@link Statistics#getReport() getReport} method.
     *
     * @param gm The input.
     */
    @Override
    public void execute(GraphModel gm) {
        try {
            SparqlRequester sparqlRequester = SemanticWebImportMainWindowTopComponent.getSparqlRequester();
            Map<String, Integer> countTypes = countTypes(sparqlRequester.selectOnGraph(COUNT_TYPES));
            TypeTreeModel typeModel = buildTreeModel(sparqlRequester.selectOnGraph(GET_SUBCLASSES));
            updateModelCount(typeModel, countTypes);
            displayTypeTree(typeModel);
            summary.append(countTypes.size()).append(" types found");
            report.append("<p>").append(countTypes.size()).append(" types were found :</p>");
            for (String key : countTypes.keySet()) {
                report.append("<p>").append(key).append(": found ").append(countTypes.get(key)).append(" times</p>");
            }
        } catch (HeadlessException ex) {
            report.append("An exception occurred: ").append(ex.getMessage());
        }
    }

    @Override
    public String getReport() {
        return report.toString();
    }

    public String getSummary() {
        return summary.toString();
    }

    /**
     * Display the model in a JTreeTable.
     *
     * @param typeModel to display.
     */
    protected void displayTypeTree(TypeTreeModel typeModel) {
        JPanel frame = new JPanel(new BorderLayout());
        JTreeTable treeTable = new JTreeTable(typeModel);
        frame.add(new JScrollPane(treeTable), BorderLayout.CENTER);
        try {
            SemanticWebImportMainWindowTopComponent.getDefault().addTab("Tree Type", frame);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "An exception occurred when attempting to display the type tree: ", ex.getMessage());
        }
    }

    /**
     * Convert the result returned by a select counting the types into a
     * map.
     *
     * @param selectOnGraph Result obtained with a select request. The first
     *                      column must contain the name of the type, the second column must
     *                      contain the number of occurrences found for this type.
     * @return The counted types.
     */
    protected Map<String, Integer> countTypes(String[][] selectOnGraph) {
        HashMap<String, Integer> result = new HashMap<String, Integer>();
        for (int i = 0; i < selectOnGraph.length; ++i) {
            String name = selectOnGraph[i][0];
            Integer count = Integer.parseInt(selectOnGraph[i][1]);
            result.put(name, count);
        }
        return result;
    }

    /**
     * Register for each node who are its fathers and children. Note that a
     * node can have several children and/or father.
     *
     * @param selectOnGraph
     * @return
     */
    protected TypeTreeModel buildTreeModel(String[][] selectOnGraph) {
        TypeTreeNode root = new TypeTreeNode("Root", 0, 0);
        TypeTreeModel result = new TypeTreeModel(root);
        HashMap<String, TypeTreeNode> builtNodes = new HashMap<String, TypeTreeNode>();

        for (int i = 0; i < selectOnGraph.length; ++i) {
            String childTypeName = selectOnGraph[i][0];
            String fatherTypeName = selectOnGraph[i][1];
            TypeTreeNode fatherNode;
            if (builtNodes.containsKey(fatherTypeName)) {
                fatherNode = builtNodes.get(fatherTypeName);
            } else {
                fatherNode = new TypeTreeNode(fatherTypeName, 0, 0);
                builtNodes.put(fatherTypeName, fatherNode);
            }
            TypeTreeNode childNode;
            if (builtNodes.containsKey(childTypeName)) {
                childNode = builtNodes.get(childTypeName);
            } else {
                childNode = new TypeTreeNode(childTypeName, 0, 0);
                builtNodes.put(childTypeName, childNode);
            }
            fatherNode.add(childNode);
        }
        for (String nodeName : builtNodes.keySet()) {
            TypeTreeNode currentNode = builtNodes.get(nodeName);
            if (!currentNode.hasFather()) {
                result.getRoot().add(currentNode);
            }
        }
        return result;
    }

    /**
     * Add the count for each node of typeModel, and add nodes if they are
     * present in countTypes, but not in typeModel. It can happen that a
     * type is known with rdf:type (used to obtain countTypes), but that
     * there is no known rdfs:subClassOf relationship with it (letting the
     * typeModel unaware of this type).
     *
     * @param typeModel
     * @param countTypes
     */
    protected void updateModelCount(TypeTreeModel typeModel, final Map<String, Integer> countTypes) {
        var seenTypes = new HashSet<String>();
        int total = 0;
        for (Integer count : countTypes.values()) {
            total += count;
        }
        if (total == 0) {
            throw new IllegalArgumentException("total count must not be 0.");
        }

        TypeTreeNode node = typeModel.getRoot();
        var queue = new LinkedList<TypeTreeNode>();
        queue.add(node);
        while (!queue.isEmpty()) {
            TypeTreeNode currentNode = queue.pollLast();
            String nodeName = currentNode.getName();
            int countType = (countTypes.get(nodeName) != null) ? countTypes.get(nodeName) : 0;
            currentNode.setNumber(countType);
            currentNode.setPercentage(100.0 * countType / total);
            seenTypes.add(nodeName);

            Enumeration<TreeNode> childs = currentNode.children();
            while (childs.hasMoreElements()) {
                queue.addFirst((TypeTreeNode) childs.nextElement());
            }
        }
        Set<String> unseenTypes = countTypes.keySet();
        unseenTypes.removeAll(seenTypes);
        for (String unseenType : unseenTypes) {
            typeModel.getRoot().add(new TypeTreeNode(unseenType, countTypes.get(unseenType), 100.0 * countTypes.get(unseenType) / total));
        }
    }
}

/**
 * Compares by ascending count of successors, then by alphabetical order if
 * there is counts are equal.
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
class SuccessorComparator implements Comparator<String> {

    private Map<String, Set<String>> successors;

    public SuccessorComparator(Map<String, Set<String>> successors) {
        this.successors = successors;
    }

    @Override
    public int compare(String o1, String o2) {

        if (successors.get(o1).size() < successors.get(o2).size()) {
            return -1;
        } else if (successors.get(o1).size() > successors.get(o2).size()) {
            return 1;
        } else {
            return o1.compareTo(o2);
        }
    }
}
