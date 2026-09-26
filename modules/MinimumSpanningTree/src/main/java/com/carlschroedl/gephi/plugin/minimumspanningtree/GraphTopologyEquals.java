package com.carlschroedl.gephi.plugin.minimumspanningtree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.openide.util.Pair;

/**
 * This class determines whether two graphs have the same nodes and edges. Nodes
 * from different graphs are considered equal if and only if their node ids are 
 * equal. Edges from different graphs are considered equal if and only if their 
 * source node ids, target node ids, and weights are equal. Two graphs are equal
 * if and only if all of their nodes and edges are equal, and they have the same
 * number of edges and nodes.
 * All other node and edge attributes are ignored in this comparison.
  */
public class GraphTopologyEquals {

    private static Pair getSourceTargetPair(Edge e) {
        Object aSourceId = e.getSource().getId();
        Object aTargetId = e.getTarget().getId();
        Pair<Object, Object> pair = Pair.of(aSourceId, aTargetId);
        return pair;

    }
    /**
     * Compares edge collections based on source node id , target node id, and edge weight.
     * @param aEdgeCollection
     * @param bEdgeCollection
     * @return true if all edges in both collections match, otherwise false.
     */
    public static boolean edgeCollectionsAreEqual(Collection<Edge> aEdgeCollection, Collection<Edge> bEdgeCollection) {
        boolean equal = true;
        if (aEdgeCollection.size() == bEdgeCollection.size()) {
            Map<Pair<Object, Object>, Double> aEdgeMap = new HashMap<Pair<Object, Object>, Double>();
            for (Edge aEdge : aEdgeCollection) {
                double weight = aEdge.getWeight();
                aEdgeMap.put(getSourceTargetPair(aEdge), weight);
            }

            Map<Pair<Object, Object>, Double> bEdgeMap = new HashMap<Pair<Object, Object>, Double>();
            for (Edge bEdge : bEdgeCollection) {
                double weight = bEdge.getWeight();
                bEdgeMap.put(getSourceTargetPair(bEdge), weight);
            }

            equal = MapDeepEquals.mapDeepEquals(aEdgeMap, bEdgeMap);
        } else {
            equal = false;
        }
        return equal;
    }

    public static boolean graphsHaveSameTopology(Graph a, Graph b) {
        boolean equal = true;
        a.writeLock();
        b.writeLock();
        try {
            if (a.getNodeCount() == b.getNodeCount() && a.getEdgeCount() == b.getEdgeCount()) {
                for (Node aNode : a.getNodes()) {
                    Node bNode = b.getNode(aNode.getId());
                    if (null == bNode) {
                        equal = false;
                        break;
                    } else {
                        Collection<Edge> aEdges = a.getEdges(aNode).toCollection();
                        Collection<Edge> bEdges = b.getEdges(bNode).toCollection();
                        if (!edgeCollectionsAreEqual(aEdges, bEdges)) {
                            equal = false;
                            break;
                        }
                    }
                }
            } else {
                equal = false;
            }
        } finally {
            a.writeUnlock();
            b.writeUnlock();
        }
        return equal;
    }

}
