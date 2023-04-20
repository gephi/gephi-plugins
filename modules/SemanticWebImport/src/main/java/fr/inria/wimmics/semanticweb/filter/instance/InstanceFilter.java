/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */

package fr.inria.wimmics.semanticweb.filter.instance;

import fr.inria.edelweiss.semantic.SemanticWebImportMainWindowTopComponent;
import java.util.Formatter;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.filters.spi.ComplexFilter;
import org.gephi.filters.spi.FilterProperty;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class InstanceFilter implements ComplexFilter {

    private static final Logger logger = Logger.getLogger(InstanceFilter.class.getName());
    private String type;

    public InstanceFilter() {
    }

    @Override
    public String getName() {
        return "Model Types Filter";
    }

    @Override
    public FilterProperty[] getProperties() {
        FilterProperty[] result = new FilterProperty[0];
        try {
            result = new FilterProperty[]{FilterProperty.createProperty(this, String.class, "type", "getType", "setType")};
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(InstanceFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String getType() {
        return type;
    }

    public void setType(String newType) {
        type = newType;
    }
    static public String GET_ALLTYPES_OF =
            "SELECT ?t WHERE { "
            + "{ ?x rdf:type ?t } Union "
            + "{ ?x rdfs:subClassOf ?t } Union "
            + "{ ?x rdfs:subPropertyOf ?t}"
            + "} "
            + "group by ?t";

    @Override
    public Graph filter(Graph graph) {
        try (Formatter getSubtypesOfrequest = new Formatter().format(GET_ALLTYPES_OF, type)) {
            String[][] nodesToKeepList = SemanticWebImportMainWindowTopComponent.getSparqlRequester().selectOnGraph(getSubtypesOfrequest.toString());
            HashSet<String> nodesToKeepSet = convertStringArrayToSet(nodesToKeepList);

            Node[] nodes = graph.getNodes().toArray();
            for (Node node : nodes) {
                if (!nodesToKeepSet.contains((String) node.getId())) {
                    graph.removeNode(node);
                }
            }
        }
        return graph;
    }

    private HashSet<String> convertStringArrayToSet(String[][] listNodes) {
        HashSet<String> result = new HashSet<String>();
        for (int i = 0; i < listNodes.length; ++i) {
            result.add(listNodes[i][0]);
        }
        return result;
    }
}
