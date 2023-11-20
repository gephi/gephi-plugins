/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */

package fr.inria.wimmics.semanticweb.filter.sparql;

import fr.inria.edelweiss.semantic.SemanticWebImportMainWindowTopComponent;
import fr.inria.edelweiss.sparql.GephiUtils;

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
public class SparqlFilter implements ComplexFilter {

    private final Logger logger = Logger.getLogger(SparqlFilter.class.getName());
    private String selectRequest;

    public SparqlFilter() {
    }

    @Override
    public String getName() {
        return "SPARQL Filter";
    }

    @Override
    public FilterProperty[] getProperties() {
        FilterProperty[] result = new FilterProperty[0];
        try {
            result = new FilterProperty[]{FilterProperty.createProperty(this, String.class, "selectRequest", "getSelectRequest", "setSelectRequest")};
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(SparqlFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    public String getSelectRequest() {
        return selectRequest;
    }

    public void setSelectRequest(String newType) {
        selectRequest = newType;
    }

    @Override
    public Graph filter(Graph graph) {
        String[][] nodesToKeepList = SemanticWebImportMainWindowTopComponent.getSparqlRequester().selectOnGraph(selectRequest);
        HashSet<String> nodesToKeepSet = convertStringArrayToSet(nodesToKeepList);

        Node[] nodes = graph.getNodes().toArray();
        forNodes:
        for (Node node : nodes) {
            if (nodesToKeepSet.contains(GephiUtils.getSparqlId(node))) {
                continue forNodes;
            } else {
                graph.removeNode(node);
            }
        }
        return graph;
    }

    private HashSet<String> convertStringArrayToSet(String[][] listNodes) {
        HashSet<String> result = new HashSet<String>();
        for (int i = 0; i < listNodes.length; ++i) {
            for (int j = 0; j < listNodes[0].length; ++j) {
                result.add(listNodes[i][j]);
            }
        }
        return result;
    }
}
