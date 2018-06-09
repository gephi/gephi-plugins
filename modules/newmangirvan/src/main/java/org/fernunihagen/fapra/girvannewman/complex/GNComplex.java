package org.fernunihagen.fapra.girvannewman.complex;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.fernunihagen.fapra.girvannewman.IGirvanNewman;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 * Klasse, die fuer die Realisierung des Algorithmus von Girvan-Newman zustaendig ist. 
 * Diese Klasse enthaelt die gesamte Ablaufsteuerung.
 * @author Andrej Sibirski
 */
public class GNComplex implements IGirvanNewman{

    /**
     * Graph, in dem Communities gesucht werden. 
     */
    private Graph graph;
    
    /**
     * ProgressTicket zur Darstellung des Fortschritts.
     */
    private ProgressTicket progressTicket;
    
    /**
     * Flag zur Signalisierung eines Abbruchwunsches.
     */
    private boolean isCancelled;
    
    /**
     * Map fuer die Zuordnung der Knoten zu Nummern. Die Map wird
     * fuer die Nummerierung der Knoten genutzt.
     */
    private Map<Node, Integer> nodeIndex;
    
    /**
     * Anzahl der Knoten im Graphen.
     */
    private int nodeCount;
    
    /**
     * Anzahl der Kanten, die vom Algorithmus verarbeitet wurden.
     */
    private int processedEdgeCount;

    /**
     * Flag zur Beruecksichtigung des Kantentyps bei der Berechnung 
     * der Shortest-Path Betweeness.
     */
    private boolean respETypeSPB;
    
    /**
     * Flag zur Beruecksichtigung paralleler Kanten bei der Berechnung
     * der Shortest-Path Betweeness.
     */
    private boolean respMultiESPB;
    
    /**
     * Flag zur Beruecksichtigung des Kantentyps bei der Berechnung der Modularitaet.
     */
    private boolean respETypeModul;
    
    /**
     * Flag zur Beruecksichtigung paralleler Kanten bei der Berechnung der Modularitaet.
     */
    private boolean respMultiEModul;
 
    /**
     * Array, dass die Zuordnung der Knoten zu ihrer Community angibt. Die i-te Position 
     * gibt die Nummer der Community von Knoten i an.
     */
    private int[] commDevision;
    
    /**
     * Anzahl der Communities.
     */
    private int commCount;
    
    /**
     * Groessste gefundene Modularitaet.
     */
    private float maxMod;
    
    /**
     * Liste mit dem Verlauf der Modularitaet bei sukzessiver Kantenloeschung.
     */
    private LinkedList<Float> modRun;

    /**
     * Berechnungsdauer.
     */
    private long computationTime;

    @Override
    public void setGraph(Graph graph) {
        this.graph = graph; 
    }
    
    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket; 
    }
    
    @Override
    public void setRespETypeSPB(boolean respETypeSPB) {
        this.respETypeSPB = respETypeSPB; 
    }
    
    @Override
    public void setRespMultiESPB(boolean respMultiESPB) {
        this.respMultiESPB = respMultiESPB; 
    }
    
    @Override
    public void setRespETypeModul(boolean respETypeModul) {
        this.respETypeModul = respETypeModul; 
    }
    
    @Override
    public void setRespMultiEModul (boolean respMultiEModul) {
        this.respMultiEModul = respMultiEModul; 
    }

    @Override
    public void setCancel() {
        this.isCancelled = true;
    }

    /**
     * Gibt ein Listen-Array mit den Adjazenzlisten fuer den Graphen zurueck. Die i-te Position im Array
     * referenziert die Adjazenzliste fuer Knoten i. 
     * @param respEType Wahrheitswert, der angibt ob Kantentypen beruecksichtigt werden sollen.
     * @param respMultiEdges Wahrhitswert, der angibt ob parallele Kanten beruecksichtigt werden sollen.
     * @return Listen-Array mit den Adjazenzlisten des Graphen.
     */
    private LinkedList<Integer>[] getAdjList(boolean respEType, boolean respMultiEdges) {
        /*
         * Erzeuge das Array mit leeren Adjazenzlisten
         */
        LinkedList<Integer>[] l = new LinkedList[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            l[i] = new LinkedList<>();
        }
        /*
         * Befuelle die Adjazenzlisten, indem jede Kante des Graphen betrachtet wird.
         */
        for (Edge e : graph.getEdges()) {
            Node src = e.getSource();
            Node trgt = e.getTarget();
            int srcIndex = nodeIndex.get(src); // Nummer Quellknoten
            int trgtIndex = nodeIndex.get(trgt); // Nummer Zielknoten
            /*
             Wenn parallele Kanten beruecksichtigt werden sollen, wird ggf. die Nummer 
             des Zielknoten mehrmals zu der Adjanzenzliste des Quellknotens hinzugefuegt. 
             */
            if (respMultiEdges) {
                l[srcIndex].add(trgtIndex); 
            } else {
                if (!l[srcIndex].contains(trgtIndex)) {
                    l[srcIndex].add(trgtIndex);
                }
            }
            
            /*
             Wenn der Kantentyp nicht beruecksichtigt werden soll, so muss auch die Nummer des Quellknotens
             zu der Adjazenzliste des Zielknotens hinzugefuegt werden. Abhaengig davon, ob parallele Kanten 
             beruecksichtigt werden sollen, muss die Nummer des Quellknotens ggf. mehrmals hinzugefuegt werden.
             */
            if (!respEType) {
                if (respMultiEdges) {
                    l[trgtIndex].add(srcIndex);
                } else {
                    if (!l[trgtIndex].contains(srcIndex)) {
                        l[trgtIndex].add(srcIndex);
                    }
                }
            } else {
                /*
                Der Kantentyp soll beruecksichtigt werden. Abhaengig davon, ob es sich um eine gerichtete
                oder ungerichtete Kante handelt und ob parallele Kanten beruecksichtigt werden sollen, 
                muss auch hier ggf. die Nummer des Quellknotens mehrmals zur Adjazenzliste des Zielknotens
                hinzugefuegt werden.
                */
                if (!e.isDirected()) {
                    if (respMultiEdges) {
                        l[trgtIndex].add(srcIndex);
                    } else {
                        if (!l[trgtIndex].contains(srcIndex)) {
                            l[trgtIndex].add(srcIndex);
                        }
                    }
                }
            }
        }
        return l;
    }

    /**
     * Gibt ein zweidimensionales Array zurueck, das die Anzahl ungerichteter Kanten zwischen zwei Knoten angibt. Die Position (i,j)
     * gibt die Anzahl ungerichteter Kanten, die zwischen Knoten i und Knoten j verlaufen. Diese Anzahl ist fuer die
     * Positionen (i,j) und (j,i) gleich. 
     * @param respEType Wahrheitswert, der angibt ob Kantentypen beruecksichtigt werden sollen. 
     * @param respMultiEdges Wahrheitswert, der angibt ob parallele Kanten beruecksichtigt werden sollen.
     * @return zweidimensionales Array mit der Anzahl ungerichteter Kanten zwischen zwei Knoten.
     */
    private int[][] getUndirConnCount(boolean respEType, boolean respMultiEdges) {
        /*
        Erzeuge das zweidimensionale Array. Defaultwerte sind 0. 
        */
        int[][] count = new int[nodeCount][nodeCount];
        /*
        Durchlaufe jede Kante im Graphen
        */
        for (Edge e : graph.getEdges()) {
            Node src = e.getSource();
            Node trgt = e.getTarget();
            int srcIndex = nodeIndex.get(src);
            int trgtIndex = nodeIndex.get(trgt);
            /*
            Wenn der Kantentyp nicht beruecksichtigt werden soll, dann wird jede Kante als ungerichtet angesehen. 
            */
            if (!e.isDirected() || !respEType) {
                /*
                Wenn parallele Kanten beruecksichtigt werden sollen, dann erhoehe die Anzahl jedes Mal um 1. 
                */
                if (respMultiEdges) {
                    count[srcIndex][trgtIndex]++;
                    count[trgtIndex][srcIndex]++;
                } else {
                    /*
                    Parallele Kanten sollen nicht beruecksichtigt werden. Die Anzahl kann deshalb nur 1 sein. 
                    */
                    count[srcIndex][trgtIndex] = 1;
                    count[trgtIndex][srcIndex] = 1;
                }
            }
        }
        return count;
    }
    
    /**
     * Nummeriert jeden Knoten im Graphen und legt die Zuordnung in einer Map ab.
     */
    private void enumerateNodes() {
        Map<Node, Integer> index = new HashMap<>();
        int i = 0;
        for (Node n : graph.getNodes()) {
            index.put(n, i);
            i++;
        }
        this.nodeIndex = index;
    }

    @Override
    public void calculate() {
        long startTime = System.currentTimeMillis();
        this.nodeCount = graph.getNodeCount();
        this.modRun = new LinkedList<>();
        /*
        Nummeriere alle Knoten durch und erzeuge die Adjazenzlisten aus dem Graphen. 
        Bei der Erzeugung der Adjazenzlisten beruecksichtige die Benutzereinstellungen 
        ob Kantentypen und/oder parallele Kanten beruecksichtigt werden sollen.
        */
        enumerateNodes();
        LinkedList<Integer>[] adjList = getAdjList(respETypeSPB, respMultiESPB);
        /*
        Erzeuge ein zweidimensioanels Array mit der Anzahl ungerichteter Kanten zwischen zwei Knoten.
        */
        int[][] undirConnCount = getUndirConnCount(respETypeSPB, respMultiESPB);       
        /*
        Erzeuge die Objekte, die fuer die Berechnung der Teilprobleme gebraucht werden:
        EFinder -> Kante mit groesstem Betweeness Wert
        CFinder -> Finden der Komponenten und somit Communities
        MFinder -> Berechnung der Modularitaet
        */ 
        EFinder eFinder = new EFinder(adjList); 
        /*
        Das Finden der Komponenten benoetigt eine separate Adjazenzliste, die Kantentypen und 
        parallele Kanten nicht beruecksichtigt, da zwei Knoten zur gleichen Komponente gehoeren sollen, 
        unabhaengig davon welcher Kantentyp sie verbindet.
        */
        CFinder cFinder = new CFinder(adjList, getAdjList(false, false));
        MFinder mFinder = new MFinder(getAdjList(respETypeModul, respMultiEModul));
        /*
        Berechne die Anzahl der zu verarbeitenden Kanten
        */
        int units = 0;
        for (LinkedList<Integer> l : adjList) {
            units += l.size();
        }
        Progress.start(progressTicket, units);
        /*
        Da ungerichtete Kanten doppelt gezaehlt wurden, muss der Wert von getUndirConnSum() durch 2 geteilt werden.
        */
        processedEdgeCount = units - (getUndirConnSum(undirConnCount) / 2);
        cFinder.calculate(); // Berechnung der anfaenglich vorhandenen Communities (Komponenten)
        commDevision = cFinder.getCommMap(); // Afaenglich vorhandene Communities (Komponenten)
        commCount = cFinder.getCommCount(); // Anfaengliche Modularitaet.
        maxMod = mFinder.getModularity(commDevision, commCount);
        /*
        Berechne die Shortest-Path Betweeness für den kompletten Graphen und frage die Nummer des Quell- und Zielknotens ab zwischen denen 
        eine Kante entfernt werden muss. 
        */
        eFinder.calculate();
        int srcIndex = eFinder.getSrcIndex();
        int trgtIndex = eFinder.getTrgtIndex();
        float lastModularityValue = maxMod;
        modRun.addLast(lastModularityValue);
        /*
        Solange es eine Kante zum Loeschen gibt oder kein Abbruchwunsch vorliegt
        */
        while (srcIndex != -1 && trgtIndex != -1 && !isCancelled) {
            /*
            Die Loeschung der Kante kann bewirken, dass die Komponente in der sie liegt
            zerfaellt. Da die Shortest-Path Betweeness nicht fuer den ganzen Graphen neu 
            berechnet werden soll, werden die Knoten der ggf. zerfallenen Komponente festgestellt. 
            Sie bilden die neuen Startknoten fuer die Berechnung der Betweeness Beitraege.
            */
            List<Integer> newStartNodes = cFinder.getCommNodesByNode(srcIndex);
            /*
            Entferne die Kante
            */
            adjList[srcIndex].remove((Integer) trgtIndex);
            Progress.progress(progressTicket);
            if (!respETypeSPB) {
                /*
                Kantentyp soll nicht beruecksichtigt werden. Entferne daher
                auch die Nummer des Quellknotens aus der Adjazenzliste des Zielknotens.
                */
                adjList[trgtIndex].remove((Integer) srcIndex);
                Progress.progress(progressTicket);
            } else {
                /*
                Kantentypen sollen beruecksichtigt werden. Wenn es ungerichtete Kanten zwischen 
                Quell- und Zielknoten gibt, dann verringere deren Anzahl um 1 und entferne die
                die Nummer des Quellknotens aus der Adjazenzliste des Zielknotens.
                */
                if (undirConnCount[srcIndex][trgtIndex] > 0) {
                    undirConnCount[srcIndex][trgtIndex]--;
                    undirConnCount[trgtIndex][srcIndex]--;
                    adjList[trgtIndex].remove((Integer) srcIndex);
                    Progress.progress(progressTicket);
                }
            }
            /*
            Berechne die Aufteilung des Graphen in Communities (Komponenten) in Abhaengigkeit der geloeschten Kante. 
            */
            cFinder.calculate(srcIndex, trgtIndex);
            if (cFinder.didCommChanged()) {
                /*
                Die urspruengliche Community ist zerfallen. Berechne erneut die Modularitaet und speichere die Aufteilung, wenn 
                die Modularitaet groesser als das zuletzt gefundene Maximum ist.
                */
                int commCount = cFinder.getCommCount();
                int[] commMap = cFinder.getCommMap();
                float f = mFinder.getModularity(commMap, commCount);
                lastModularityValue = f;
                if (f > maxMod) {
                    maxMod = f;
                    commDevision = commMap.clone();
                    this.commCount = commCount;
                }
            }
            modRun.addLast(lastModularityValue);
            /*
            Berechne erneut die Shortest-Path Betweeness ausgehend von den Knoten der Community, 
            in der die zuvor geloeschte Kante fiel. Frage anschliessend die als naechstes zu loeschende Kante ab.
            */
            eFinder.calculate(newStartNodes);
            srcIndex = eFinder.getSrcIndex();
            trgtIndex = eFinder.getTrgtIndex();
        }
        /*
        Setzte die Fortschrittsanzeige auf 100 Prozent, speichere die Berechnungsdauer fuer den gesamten Algorithmus.
        */
        Progress.finish(progressTicket);
        long endTime = System.currentTimeMillis();
        computationTime = endTime - startTime;
    }

    @Override
    public int getCommunitiesCount() {
        return commCount;
    }

    @Override
    public Map<Node, Integer> getCommunities() {
        HashMap<Node, Integer> map = new HashMap<>();
        for (Node n : graph.getNodes()) {
            int nI = nodeIndex.get(n);
            map.put(n, commDevision[nI]);
        }
        return map;
    }

    @Override
    public int getProcessedEdgeCount() {
        return processedEdgeCount;
    }

    @Override
    public int getNodeCount() {
        return nodeCount;
    }

    @Override
    public float getMaxFoundModularity() {
        return maxMod;
    }

    @Override
    public float getComputationTimeAsSeconds() {
        return (float) computationTime / 1000f;
    }

    @Override
    public List<Float> getModularityRunAsList() {
        return modRun;
    }

    /**
     * Summiert alle Werte des zweidimensionalen Arrays mit der Anzahl ungerichteter Kante zwischen zwei Knoten auf und gibt sie als ganze Zahl zurueck. 
     * @param undirConnCount zweidimensionales Array mit der Anzahl ungerichteter Kanten zwischen zwei Knoten.
     * @return ganze Zahl mit der Summe der Werte des zweidimensionalen Arrays mit der Anzahl ungerichteter Kanten. 
     */
    private int getUndirConnSum(int[][] undirConnCount) {
        int i = 0;
        /*
        Durchlaufe das Array und summiere auf.
        */
        for (int[] j : undirConnCount) {
            for (int k : j) {
                i += k;
            }
        }
        return i;
    }
}
