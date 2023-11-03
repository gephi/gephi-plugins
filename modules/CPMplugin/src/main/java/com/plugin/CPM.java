package com.plugin;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.gephi.graph.api.*;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;

import java.util.*;

/**
 * @author Account
 */
public class CPM implements org.gephi.statistics.spi.Statistics, LongTask {

    private String report = "";
    private boolean cancel = false;

    private ProgressTicket progressTicket;
    private int k = 0;
    private Set<Set<Node>> Cliques = new HashSet<Set<Node>>();
    GenQueue<TreeSet<Node>> Bk = new GenQueue<TreeSet<Node>>();

    public class SortByID implements Comparator<Node> {

        public int compare(Node n1, Node n2) {
            if (n1.getStoreId() > n2.getStoreId()) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Queue Implementation">
    public Object getLastElement(final Collection c) {
        /*
         final Iterator itr = c.iterator();
         Object lastElement = itr.next();
         while (itr.hasNext()) {
         lastElement = itr.next();
         }
         return lastElement;
         */
        return null;
    }

    class GenQueue<E> {

        private LinkedList<E> list = new LinkedList<E>();

        public void enqueue(E item) {
            list.addLast(item);
        }

        public E dequeue() {
            return list.pollFirst();
        }

        public boolean hasItems() {
            return !list.isEmpty();
        }

        public int size() {
            return list.size();
        }

        public void addItems(GenQueue<? extends E> q) {
            while (q.hasItems()) {
                list.addLast(q.dequeue());
            }
        }
    }
    //</editor-fold>

    private Vector<Node> getLargerIndexNodes(Graph g, Node vi) {
        Vector<Node> output = new Vector<Node>();
        for (Node n : g.getNodes()) {

            boolean b1 = n.getStoreId() > vi.getStoreId(),
                    b2 = g.getEdge(n, vi) != null,
                    b3 = g.getEdge(vi, n) != null;

            if (b1 && (b2 || b3)) {
                //TODO check degree of n and vi
                output.addElement(n);
            }
        }

        return output;
    }

    private boolean checkBk1IsClique(Graph g, TreeSet<Node> Bk1) {
        for (Node firstNode : Bk1) {
            for (Node secondNode : Bk1) {
                if (firstNode == secondNode) {
                    continue;
                }
                if (g.getEdge(firstNode, secondNode) == null && g.getEdge(secondNode, firstNode) == null) { //One edge is missing in the Bk+1 clique
                    return false;
                }
            }
        }

        return true;
    }

    Random r = new Random();

    @Override
    public void execute(GraphModel gm) {
        /*for (int i = 0; i < 2; i++) {
            Graph graph = gm.getGraphVisible();
            Node node = gm.factory().newNode();

            node.setLabel(String.valueOf(r.nextInt()));
            node.setX(r.nextInt(500));
            node.setY(r.nextInt(500));
            node.setSize(10f);
            graph.addNode(node);
        }*/


        Graph g = gm.getGraphVisible();

        g.readLock();

        //Firstly add each node as an item in Bk
        int count = 0;
        TreeSet<Node> tmp;

        for (Node n : g.getNodes()) {
            count++;
            //Trick: if the node's degree is less than k-1, it can not involve in k-clique
            if (g.getDegree(n) >= k - 1) {
                tmp = new TreeSet<Node>(new SortByID());
                tmp.add(n);
                Bk.enqueue(tmp); //Add the B1 (node itself) to the queue
            }
        }

        //Now start the iterative process for finding cliques
        tmp = Bk.dequeue();

        while (tmp != null) {
            if (cancel) {
                //Empty variables
                Bk.list.clear();
                tmp.clear();
                Cliques.clear();
                return;
            }

            //Search for Bk+1
            Node vi = tmp.last(); //(Node) getLastElement(tmp);
            Vector<Node> largerIndexes = getLargerIndexNodes(g, vi);

            for (Node vj : largerIndexes) {
                TreeSet<Node> Bk1 = new TreeSet<Node>(new SortByID());
                Bk1.addAll(tmp); //Clone current Bk into Bk+1
                Bk1.add(vj);
                if (Bk1.size() <= getK() && checkBk1IsClique(g, Bk1)) {

                    if (Bk1.size() == getK()) { //A clique of size k found. Finish expanding this Bk+1 here.
                        Cliques.add(Bk1);
                    } else if (Bk1.size() < getK()) {
                        Bk.enqueue(Bk1); //k should be checked for finding cliques of size k.
                    } else { //Clique with larger size will be omitted.
                        report += "<br>Larger Clique Found. It should not be here<br>";
                    }
                }
            }

            tmp = Bk.dequeue(); //Check next item
        }
        g.readUnlock();

        //Algorithm finished.
        //Write the output
        report += "Clique Detection started. Nodes with <b>" + (k - 1) + "</b> edges will not be included.";
        report += "<br><br>";
        report += "Found Cliques of size " + getK() + ".<br>Now making new graph ...<br>Clearing old graph ...";

        //edit the graph
        g.clear();
        report += " [+]<br>Creating new nodes ...";

        gm = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        int nID = 0;
        Set<Node> nodes = new HashSet<Node>();

        for (Set<Node> firstClique : Cliques) { //Create the nodes
            Node firstNode = gm.factory().newNode(String.valueOf(nID++));
            firstNode.setX(-500 + r.nextInt(1000));
            firstNode.setY(-500 + r.nextInt(1000));
            firstNode.setSize(8f);

            String nodeLabel = "";

            for (Node n : firstClique) {
                nodeLabel += n.getLabel() + ",";
            }

            nodeLabel = nodeLabel.substring(0, nodeLabel.length() - 1); //remove last ,
            firstNode.setLabel(nodeLabel);

            nodes.add(firstNode);
        }

        report += "[+]<br>Detecting and creating the edges ...";
        HashSet<Edge> edges = new HashSet<Edge>();

        for (Node vi : nodes) {
            for (Node vj : nodes) {
                if ((vi != vj) && (getSharedNodes(vi, vj) == k - 1)) {
                    if (g.isDirected()) {
                        edges.add(gm.factory().newEdge(vi, vj,true));
                    } else {
                        edges.add(gm.factory().newEdge(vi, vj, false));
                    }
                }
            }
        }

        report += "[+]<br>Redrawing new graph ...";
        for (Node n : nodes) {
            g.addNode(n);
        }


        for (Edge e : edges) {
            g.addEdge(e);
        }

        report += "[+]<br>Done!<br><br><br>Palla, Gergely, Imre Derényi, Illés Farkas, and Tamás Vicsek. \"Uncovering the overlapping community structure of complex networks in nature and society.\" Nature 435, no. 7043 (2005): 814-818";

    }

    private int getSharedNodes(Node vi, Node vj) {
        String[] firstCliqueNodes = vi.getLabel().split(",");
        String[] secondCliqueNodes = vj.getLabel().split(",");

        int sharedNodes = 0;

        for (String n1 : firstCliqueNodes) {
            for (String n2 : secondCliqueNodes) {
                if (n1.equals(n2)) {
                    sharedNodes++;
                }
            }
        }

        return sharedNodes;
    }

    @Override
    public String getReport() {
        return report;
    }

    @Override
    public boolean cancel() {
        cancel = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this.progressTicket = pt;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }
}
