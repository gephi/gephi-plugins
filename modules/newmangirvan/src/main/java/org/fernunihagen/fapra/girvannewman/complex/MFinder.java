package org.fernunihagen.fapra.girvannewman.complex;

import java.util.LinkedList;

/**
 * Klasse zur Berechnung der Modularitaet.
 * @author Andrej Sibirski
 */
class MFinder {
    /**
     * Adjazenzlisten des urspruenglichen Graphen. Ob bei der Berechnung der Modularitaet Kantentypen 
     * und/oder parallele Kanten beruecksichtigt werden sollen wird durch die Adjazenzlisten beruecksichtigt.
     */
    private LinkedList<Integer>[] adjList;
    
    /**
     * Anzahl der Kanten laut Adjazenzlisten. Urspruenglich ungerichteten Kanten werden doppelt gezaehlt. 
     */
    private int eCount;

    /**
     * Konstruktor der Klasse. 
     * @param adjList Array mit Adjazenzlisten des urspruenglichen Graphen
     */
    MFinder(LinkedList<Integer>[] adjList) {
        this.adjList = adjList;
        for (int i = 0; i < adjList.length; i++) {
            eCount += adjList[i].size();
        }
    }

    /**
     * Berechnet die Modularitaet und gibt sie als Fliesskommazahl zurueck.
     * @param commMap Array mit der Aufteilung des Graphen in Communities.
     * @param commCount ganze Zahl mit der Anzahl der vorhandenen Communities.
     * @return Fliesskommazahl, die die Modularitaet repraesentiert.
     */
    public float getModularity(int[] commMap, int commCount) {
        int[] innerConn = new int[commCount];
        int[] allConn = new int[commCount];
        /*
        Durchlaufe die Adjazenzlisten und erhoehe beide Zaehler entsprechend. Da auch hier ungerichtete Kanten doppelt gezaehlt
        werden, gleicht dies das urspr. doppelte Zaehlen wieder aus.
        */
        for (int i = 0; i < adjList.length; i++) {
            for (int j : adjList[i]) {
                if (commMap[i] == commMap[j]) {
                    innerConn[commMap[j]] += 1;
                    allConn[commMap[j]] += 1;
                } else {
                    allConn[commMap[j]] += 1;
                }
            }
        }
        float result = 0f;
        /*
        Durchlaufe die Communities und summiere die Teilergebnisse zur Modularitaet auf.
        */
        for (int i = 0; i < commCount; i++) {
            float e = (float) innerConn[i] / (float) eCount;
            float a = (float) allConn[i] / (float) eCount;
            result = result + e - (a * a);
        }
        /*
        In result befindet sich nun der Wert fuer die Modularitaet.
        */
        if (Float.isNaN(result)) {
            return 0.0f;
        }
        return result;
    }
}
