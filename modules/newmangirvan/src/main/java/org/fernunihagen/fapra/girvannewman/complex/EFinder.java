package org.fernunihagen.fapra.girvannewman.complex;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Klasse fuer die Berechnung der Shortest-Path Betweeness.
 *
 * @author Andrej Sibirski
 */
class EFinder {

    /**
     * Adjazenzlisten des verarbeiteten Graphen.
     */
    private LinkedList<Integer>[] adjList;

    /**
     * Anzahl der Knoten des Graphen.
     */
    private int nodeCount;

    /**
     * Quellknoten der zu loeschenden Kante.
     */
    private int srcIndex;

    /**
     * Zielknoten der zu loeschenden Kante.
     */
    private int trgtIndex;

    /**
     * Zweidimensionales Array fuer die Speicherung der Betweeness-Werte. Der Eintrag (i,j) gibt die Betweeness fuer Kante (i,j) an.
     */
    private double[][] betweeness;

    /**
     * Konstruktor fuer die Initialisierung des Objekts.
     *
     * @param adjList Array mit Adjazenzlisten des verarbeiteten Graphen
     */
    EFinder(LinkedList<Integer>[] adjList) {
        this.adjList = adjList;
        this.nodeCount = adjList.length;
    }


    public void calculate() {
        betweeness = new double[nodeCount][nodeCount];
        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (int i = 0; i < nodeCount; i++) {
            executor.execute(new ContributionCalculator(i));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            setMaxBetweeness();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex); 

        }
    }


    public void calculate(List<Integer> startNodes) {   
        for (int i : startNodes) {
            betweeness[i] = new double[nodeCount];
        }

        ExecutorService executor = Executors.newFixedThreadPool(4); 
        
        for (int i : startNodes) {
            executor.execute(new ContributionCalculator(i));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            setMaxBetweeness();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex); 

        }
    }

    /**
     * Findet eine Kante mit der groessten Betweeness und setzt die Werte fuer {@link #srcIndex} und {@link #trgtIndex}
     */
    private void setMaxBetweeness() {
        double currentMax = 0d;
        srcIndex = -1;
        trgtIndex = -1;
        /*
        Durchlaufe das zweidimensionale Array und suche nach dem groessten Betweeness-Wert.
         */
        for (int i = 0; i < betweeness.length; i++) {
            for (int j = 0; j < betweeness.length; j++) {
                if (betweeness[i][j] > currentMax) {
                    currentMax = betweeness[i][j];
                    srcIndex = i;
                    trgtIndex = j;
                }
            }
        }
    }

    /**
     * Erhoeht die Shortest-Path Betweeness fuer eine Kante um einen bestimmten Wert.
     *
     * @param srcNode ganze Zahl fuer die Quellnummer des Knotens der Kante.
     * @param trgtNode ganze Zahl fuer die Zielnummer des Knotens der Kante.
     * @param contribution Fliesskommazahl, die den Wert repraesentiert, um den die Shortest-Path Betweeness zu erhoehen ist.
     */
    synchronized private void incBetweeness(int srcNode, int trgtNode, double contribution) {
        betweeness[srcNode][trgtNode] += contribution;
    }

    /**
     * Gibt eine ganze Zahl zurueck, die den Zielknoten der Kante mit der groessten Shortest-Path Betweeness repraesentiert.
     *
     * @return ganze Zahl des Zielknotens der Kante mit der groessten Shortest-Path Betweeness.
     */
    int getTrgtIndex() {
        return trgtIndex;
    }

    /**
     * Gibt eine ganze Zahl zurueck, die den Quellknoten der Kante mit der groessten Shortest-Path Betweeness repraesentiert.
     *
     * @return ganze Zahl des Quellknotens der Kante mit der groessten Shortest-Path Betweeness.
     */
    int getSrcIndex() {
        return srcIndex;
    }

    private class ContributionCalculator implements Runnable {

        private int startNode;

        public ContributionCalculator(int startNode) {
            this.startNode = startNode;
        }

        @Override
        public void run() {
            int[] distances;
            int[] weights;
            LinkedList<Integer> nodeLvl;
            LinkedList<Integer>[] ancestors;
            distances = new int[nodeCount];
            Arrays.fill(distances, -1);
            weights = new int[nodeCount];
            ancestors = new LinkedList[nodeCount];
            nodeLvl = new LinkedList<>();
            LinkedList<Integer> queue = new LinkedList();
            ancestors[startNode] = new LinkedList<>();
            distances[startNode] = 0;
            weights[startNode] = 1;
            queue.addLast(startNode);
            while (!queue.isEmpty()) {
                int acNode = queue.removeFirst();
                nodeLvl.addFirst(acNode);
                for (int i : adjList[acNode]) {

                    if (distances[i] == -1) {
                        distances[i] = distances[acNode] + 1;
                        weights[i] = weights[acNode];
                        ancestors[i] = new LinkedList<>();
                        ancestors[i].add(acNode);
                        queue.addLast(i);
                    } else {
                        if (distances[i] > distances[acNode]) {
                            weights[i] += weights[acNode];
                            ancestors[i].add(acNode);
                        }
                    }
                }
            }
            double[] dep = new double[nodeCount];
            for (int i : nodeLvl) {
                for (int j : ancestors[i]) {
                    double frac = (double) weights[j] / (double) weights[i];
                    double contribution = frac * (1 + dep[i]);
                    incBetweeness(j, i, contribution);
                    dep[j] = dep[j] + frac * (1 + dep[i]);
                }
            }

        }

    }

}
