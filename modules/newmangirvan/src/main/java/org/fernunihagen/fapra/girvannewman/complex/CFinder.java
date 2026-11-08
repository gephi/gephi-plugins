package org.fernunihagen.fapra.girvannewman.complex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Klasse zur Bestimmung der Komponenten (Communities) des Graphen.
 * @author Andrej Sibirski
 */
class CFinder {

    /**
     * Adjazenzlisten des Graphen der sich in der Verarbeitung befindet. Auf diesen Adjazenzlisten finden Loeschung der Ablaufsteuerung {@link GNComplex} statt.
     */
    private LinkedList<Integer>[] workingAdjList;
    
    /**
     * Adjazenzlisten des einfachen, ungerichteten zugrunde liegenden Graphen. Sie werden als Hilfsmittel benoetigt.
     */
    private LinkedList<Integer>[] undirSimpleAdjList;
    
    /**
     * Anzahl der Knoten des Graphen
     */
    private int nodeCount;
    
    /**
     * Array fuer die Zuordnung von Knoten zu ihrer Community. Die i-te Position im Array gibt die Community an der der Knoten i angehoert.
     */
    private int[] commMap;
    
    /**
     * Map fuer die Zuordnung der Knotenmenge einer Community zu der entsprechenden Community.
     */
    private Map<Integer, List<Integer>> commNodes;
    
    /**
     * Ganze Zahl fuer das Zaehlen der Communities.
     */
    private int commCount;
    
    /**
     * Liste, die als Queue fungiert.
     */
    private LinkedList<Integer> queue;
    
    /**
     * Flag, das angibt, ob sich die Communities veraendert haben.
     */
    private boolean commChanged;

    /**
     * Konstruktor fuer die Initialiserung des Objekts.
     * @param workingAdjList Array mit Adjazenzlisten des sich in Verarbeitung befindlichen Graphen. 
     *                       Auf diesen Adjazenzlisten arbeitet die Ablaufsteuerung {@link GNComplex}.
     * @param undirSimpleAdjList Array mit Adjazenzlisten des zugrunde liegenden einfachen, ungerichteten Graphen. 
     *                           Diese Adjazenzlisten werden als Hilfsmittel benoetigt.
     */
    CFinder(LinkedList<Integer>[] workingAdjList, LinkedList<Integer>[] undirSimpleAdjList) {
        /*
        Notwendige Initialisierungen
        */
        this.workingAdjList = workingAdjList;
        this.undirSimpleAdjList = undirSimpleAdjList;
        this.nodeCount = workingAdjList.length;
        this.commMap = new int[nodeCount];
        this.commCount = 0;
        this.queue = new LinkedList<>();
        this.commChanged = false;
        this.commNodes = new HashMap<>();
    }

    /**
     * Bestimmt die Komponenten (Communities) fuer den gesamten Graphen.
     */
    void calculate() {
        commCount = 0;
        Arrays.fill(commMap, -1);
        /*
        Fuehre an jedem Knoten des Graphen eine Breitensuche durch.
        */
        for (int i = 0; i < nodeCount; i++) {
            if (commMap[i] == -1) {
                /*
                Knoten wurde noch keine Community-Nummer zugewiesen. Neue Community (Komponente) gefunden.
                */
                queue.clear();
                queue.addLast(i);
                HashSet<Integer> set = new HashSet<>();
                /*
                Beginne mit der Breitensuche
                */
                while (!queue.isEmpty()) {
                    int j = queue.removeFirst();
                    set.add(j);
                    commMap[j] = commCount;
                    for (int k : undirSimpleAdjList[j]) {
                        if (commMap[k] == -1) {
                            queue.addLast(k);
                        }
                    }
                }
                /*
                Breitensuche abgeschlossen. Ordne die Knotenmenge der Community zu und erhoehe den Zaehler fuer Communities um 1.
                */
                commNodes.put(commCount, new LinkedList<>(set));
                commCount++;
            }
        }
    }

    /**
     * Bestimmt die Komponenten (Communities) nachdem eine Kante geloescht wurde. 
     * Die urspruengliche Komponente, in der die Kante lag, koennte in zwei Komponenten zerfallen sein.
     * @param delSrc ganze Zahl mit der Knotennummer des Quellknotens der geloeschten Kante.
     * @param delTrgt ganze Zahl mit der Knotennummer des Zielknotens der geloeschten Kante.
     */
    void calculate(int delSrc, int delTrgt) {
        /*
        Pruefe ob eine Kante zwischen den Knoten in den Adjazenzlisten der Ablaufsteuerung vorhanden ist.
        */
        if (workingAdjList[delSrc].contains(delTrgt) || workingAdjList[delTrgt].contains(delSrc)) {
            /*
            Eine Kante ist vorhanden. Terminiere die Berechnung, da ein Zerfall ausgeschlossen ist
            */
            return;
        } else {
            /*
            Kante ist nicht vorhanden, passe die einfachen, ungerichteten Adjazenzlisten an.
            */
            undirSimpleAdjList[delSrc].remove((Integer) delTrgt);
            undirSimpleAdjList[delTrgt].remove((Integer) delSrc);
        }
        /*
        Nummer der ggf. zerfallen Komponente
        */
        int commLabel = commMap[delSrc];
        /* Hilfsmenge, um sich die bereits besuchten Knoten zu merken.
        */
        Set<Integer> checked = new HashSet<>();
        /*
        Liste mit Quell und Zielknoten der geloeschten Kante. Im schlimmsten Fall muss an beiden eine Breitensuche durchgefuehrt werden.
        */
        LinkedList<Integer> workingSet = new LinkedList<>();
        workingSet.add(delSrc);
        workingSet.add(delTrgt);
        /*
        Beginne mit der Breitensuche am Quellknoten.
        */
        for (int i : workingSet) {
            queue.clear();
            queue.addLast(i);
            HashSet<Integer> set = new HashSet<>();
            while (!queue.isEmpty()) {
                int j = queue.removeFirst();
                set.add(j);
                commMap[j] = commLabel;
                checked.add(j);
                for (int k : undirSimpleAdjList[j]) {
                    if (!checked.contains(k)) {
                        queue.add(k);
                    }
                    if (k == delTrgt && i != delTrgt) {
                        /*
                        Waehrend der Breitensuche wurde erneut der Zielknoten gefunden, d.h. es gibt einen Pfad zwischen den beiden. 
                        Ein Zerfall ist ausgeschlossen, terminiere deshalb die Berechnung.
                        */
                        commChanged = false;
                        return;
                    }
                }
            }
            /*
            Die Breitensuche ist komplett durchgelaufen ohne vorzeitig terminiert worden zu sein. 
            Aktualisiere die Knotenmenge der Community.
            */
            commNodes.put(commLabel, new LinkedList<>(set));
            commLabel = commCount;
        }
        /*
        Eroehe den Community-Zaehler um 1. Wenn die Ausfuerhung sich an diesem Punkt befindet, bedeutet dies, dass die urspr. Komponente zerfallen ist.
        Setze das Flag, dass sich die Communities veraendert haben auf true.
        */
        commCount++;
        commChanged = true;
    }

    /**
     * Gibt die Knoten einer Community zurueck, die in der gleichen Community liegen wie der als Parameter uebergebene Knoten.
     * @param node ganze Zahl, die die Knotennummer darstellt anhand der die Community ermittelt wird, fuer die die Knotenmenge zurueckgegeben wird.
     * @return Liste mit Knotennummern der Knoten, die der selben Community angehoeren wie der als Parameter spezifizierte Knoten.
     */
    List<Integer> getCommNodesByNode(int node) {
        int comm = commMap[node];
        LinkedList<Integer> l = new LinkedList<>(commNodes.get(comm));
        return l;
    }

    /**
     * Gibt einen Wahrheitswert zurueck, der angibt, ob sich die Communities nach Kantenloeschung veraendert haben. 
     * Hierfuer muss zuvor die Methode {@link #calculate(int, int)} ausgefuehrt worden sein.
     * 
     * @return 
     */
    boolean didCommChanged() {
        return commChanged;
    }

    /**
     * Gibt die Anzahl der Communities zurueck.
     * @return ganze Zahl, die die Anzahl der Communities darstellt.
     */
    int getCommCount() {
        return commCount;
    }

    /**
     * Gibt ein zweidimensionales Array zurueck, dass eine Zuordnung von Knoten zu ihrer Community enthaelt.
     * Die i-te Position des Arrays gibt die Community an, der der Knoten i angehoert.
     * @return 
     */
    int[] getCommMap() {
        return commMap;
    }

}
