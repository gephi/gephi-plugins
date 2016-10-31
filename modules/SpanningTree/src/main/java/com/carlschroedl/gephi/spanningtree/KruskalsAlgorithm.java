package com.carlschroedl.gephi.spanningtree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 *
 * @author Carl Schroedl <carlschroedl@gmail.com>
 */
public class KruskalsAlgorithm extends SpanningTreeAlgorithm {

    private static final String name = "Kruskal's Algorithm";
    private ProgressTicket progressTicket;
    private boolean cancel;
    private HashMap<Integer, KNode> kNodes;
    private double STweight;    //spanning tree weight
    private int edgesInST;      //number of edges in spanning tree
    private static final String ST_COL_ID = "spanningtree";     //not displayed to user
    private static final String ST_COL_NAME = "Spanning Tree";  //displayed to user

    @Override
    public void execute(Graph graph, AttributeModel attributeModel) {
        //See http://wiki.gephi.org/index.php/HowTo_write_a_metric#Implementation_help
        graph.writeLock();
        kNodes = new HashMap<Integer, KNode>();
        PriorityQueue<KEdge> edgeQ = new PriorityQueue<KEdge>();
        this.STweight = 0;
        this.edgesInST = 0;

        //<new attributes for the spanning tree>

        AttributeTable edgeTable = attributeModel.getEdgeTable();
        AttributeColumn stEdgeCol = edgeTable.getColumn(ST_COL_ID);

        if (stEdgeCol == null) {
            stEdgeCol = edgeTable.addColumn(ST_COL_ID, ST_COL_NAME, AttributeType.INT, AttributeOrigin.COMPUTED, 0);
        }

        //</new attributes for the spanning tree>

        try {
            /*
             * The upper bound for number of edges in the spanning tree is the
             * number of edges in the graph. Accordingly, initialize the 
             * progressTicket with this upper bound. Update the progressTicket
             * with the running total of of edges that have been added to the 
             * spanning tree. In this way the algorithm will always terminate at
             * or before 100%.
             */
            Progress.start(progressTicket, graph.getEdgeCount());

            //Convert all edges to the Comparable KEdge, then add to PriorityQueue
            for (Edge e : graph.getEdges()) {
                edgeQ.add(new KEdge(e));
            }//endforeach

            //By definition, a tree of n node must have n-1 edges
            int finishSize = graph.getNodeCount() - 1;

            KEdge tempKEdge; //KEdge popped off the PriorityQueue

            while (edgesInST < finishSize) {
                tempKEdge = edgeQ.remove();
                if (tempKEdge.getSource().getDjsPointer().find()
                        != tempKEdge.getTarget().getDjsPointer().find()) {
                    //and yes, it IS ok to use != instead of !blah.equals()

                    //change edge's Spanning Tree Attribute to non-default value
                    tempKEdge.edge.getAttributes().setValue(ST_COL_ID, 1);

                    ++edgesInST;
                    this.STweight += tempKEdge.weight;


                    Unionizer unionizer = new Unionizer();
                    unionizer.union(
                            tempKEdge.getSource().getDjsPointer(),
                            tempKEdge.getTarget().getDjsPointer());
                }

                Progress.progress(progressTicket, edgesInST);
                if (cancel) {
                    //remove the spanning tree column
                    attributeModel.getEdgeTable().removeColumn(stEdgeCol);
                    break;
                }
            }//end while

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Unlock graph
            graph.writeUnlock();
        }
    }

    @Override
    public JPanel getOptions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean cancel() {
        this.cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    @Override
    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        throw new UnsupportedOperationException("Not supported yet, "
                + "needs a Graph, not a GraphModel");
    }

    @Override
    public String getReport() {
        return "Number of edges in spanning tree: "
                + this.edgesInST
                + "<br/> Weight of spanning tree: "
                + this.STweight
                + "<br/> Every graph member in the spanning tree now has an attribute \"Spanning Tree\" set equal to \"1\"."
                + "<br/> The spanning tree can be visualized by creating a Filter on the Attribute \"Spanning Tree\" with value = \"1\"";
    }

    // <editor-fold defaultstate="collapsed" desc="Inner Helper Classes">
    private class DisjointSet {

        private int rank;
        private DisjointSet parent;
        private Object value;           //the value of a particular set member
        private String label = "";          //used primarily for debug output
        private static final String ARROW = " ==> ";    //also for debug output
        private static final boolean DEBUG = false;     //prints if true

        /*
         * label used primarily for testing
         */
        DisjointSet(String label) {
            this.label = label;
            this.rank = 0;
            this.parent = this;     //must maintain self reference 
        }

        DisjointSet() {
            this.rank = 0;
            this.parent = this;
        }

        private DisjointSet find() {

            DisjointSet x = this;

            String dbg = label + " : " + x.rank + ARROW;

            while (x != x.parent) {
                x = x.parent;
                dbg += x.label + " : " + x.rank + ARROW;
            }
            if (DEBUG) {
                //cut off last arrow
                dbg = dbg.substring(0, dbg.length() - ARROW.length());
                System.out.println(dbg);
            }
            return x;
        }

        /*
         * <ACCESSORS AND MUTATORS>
         * <editor-fold defaultstate="collapsed" desc="ACCESSORS AND MUTATORS">
         */
        public void setRank(int rank) {
            this.rank = rank;
        }

        public int getRank() {
            return rank;
        }

        public void setParent(DisjointSet parent) {
            this.parent = parent;
        }

        public DisjointSet getParent() {
            return parent;
        }
        /*
         * <ACCESSORS AND MUTATORS>
         */

        public void setLabel(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return value;
        }
        //</editor-fold>
    }//end private class

    /*
     * Simply unionizes two disjoint sets. This would be a static method were it
     * not an inner class
     */
    private class Unionizer {

        public void union(DisjointSet x, DisjointSet y) {
            DisjointSet rootX = x.find();
            DisjointSet rootY = y.find();

            if (rootX == rootY) {
                //System.out.println("Warning: the two sets already have the same root");
                return;
            } else if (rootX.rank > rootY.rank) {
                rootY.parent = rootX;
            } else {
                rootX.parent = rootY;
                if (rootX.rank == rootY.rank) {
                    ++rootY.rank;
                }
            }

        }//end method
    }

    /*
     * KNodes associate Nodes with a disjoint set, which is fundamental
     * to fast union-finding for Kruskal's Algorithm.
     */
    private class KNode {

        private ArrayList<KEdge> edges = new ArrayList<KEdge>();
        private String label = "";      //used only for testing
        private DisjointSet djsPointer; //the associated disjoint set
        private Node node;              //the original Node

        KNode(Node n) {
            this.node = n;            //retain copy of original edge
            this.djsPointer = new DisjointSet();
        }

        KNode() {
            this("", null);
        }

        KNode(String label, DisjointSet djsPointer) {
            this.label = label;
            this.djsPointer = djsPointer;
        }

        public void addKEdge(KEdge e) {
            edges.add(e);
        }

        @Override
        public String toString() {
            return this.label;
        }

        public void setDjsPointer(DisjointSet djsPointer) {
            this.djsPointer = djsPointer;
        }

        public DisjointSet getDjsPointer() {
            return djsPointer;
        }

        /*
         * returns the original Node
         */
        public Node getNode() {
            return this.node;
        }
    }

    private class KEdge implements Comparable<KEdge> {
        /*
         * This class is necessary because there needs to be a representation
         * of edges with KNode source and target nodes. See KNodes for why KNodes
         * are necessary
         */

        /* 
         * This class needs access to a global, persistent 
         * Hashmap<Integer, Knode> knodes
         * 
         * Comparable is implemented so that KEdge's can be sorted via PriorityQ
         * 
         */
        private KNode source;
        private KNode target;
        private double weight;  //create local copy of edge's weight
        private Edge edge;      //maintain pointer to original edge

        KEdge(Edge e) {
            this.edge = e;
            int srcID = e.getSource().getId();
            if (kNodes.containsKey(srcID)) {
                this.source = kNodes.get(srcID);
            } else {
                this.source = new KNode(e.getSource());
                kNodes.put(srcID, this.source);
            }

            int targetID = e.getTarget().getId();
            if (kNodes.containsKey(targetID)) {
                this.target = kNodes.get(targetID);
            } else {
                this.target = new KNode(e.getTarget());
                kNodes.put(targetID, this.target);
            }

            this.weight = e.getWeight();
        }

        KEdge(KNode source, KNode target, double weight) {
            this.source = source;
            this.target = target;
            this.weight = weight;
        }

        KEdge(KNode source, KNode target) {
            this(source, target, 1);
        }

        public String toString() {
            return "[" + this.source.toString()
                    + "]<====" + this.weight
                    + "====>[" + this.target.toString()
                    + "]";

        }
        /*
         * <ACCESSORS AND MUTATORS>
         */

        public void setSource(KNode source) {
            this.source = source;
        }

        public KNode getSource() {
            return source;
        }

        public void setTarget(KNode target) {
            this.target = target;
        }

        public KNode getTarget() {
            return target;
        }

        public double getWeight() {
            return this.weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;

        }

        public Edge getEdge() {
            return this.edge;
        }
        /*
         * </ACCESSORS AND MUTATORS>
         */

        @Override
        public int compareTo(KEdge otherKEdge) {
            double difference = this.weight - otherKEdge.weight;
                /*
                 * the method does not simply return 'difference' because the 
                 * interface's specified return type is int. Casting from double
                 * to int could result in precision loss and incorrect comparisons
                 */
                if (0 < difference) {
                    return 1;
                } else if (0 > difference) {
                    return -1;
                } else {
                    return 0;
                }
        }
    }//end inner class
// </editor-fold>
}//outer class
