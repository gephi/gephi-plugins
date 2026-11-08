package org.fernunihagen.fapra.girvannewman;

import java.util.List;
import java.util.Map;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.utils.progress.ProgressTicket;

/**
 * Schnittstelle, die von allen Realisierungen des Girvan-Newman Algorithmus implementiert werden muss.
 * @author Andrej Sibirski
 */
public interface IGirvanNewman {
    /**
     * Starten der Berechnung. 
     */
    public void calculate();
    
    /**
     * Gibt die Zuordnung der Knoten zu den Communities als Map zurueck. 
     * @return Map mit der Zuordnung der Knoten zu Communities
     */
    public Map<Node, Integer> getCommunities();
    
    /**
     * Gibt die Anzahl der gefundenen Communities als ganze Zahl zurueck.
     * @return Rueckgabe einer ganzen Zahl, die die Anzahl der gefundenen Communities darstellt.
     */
    public int getCommunitiesCount();
    
    /**
     * Gibt die Berechnungdauer in Sekunden als Fliesskommazahl zurueck.
     * @return Rueckgabe einer Fliesskommazahl, die die Berechnungsdauer darstellt.
     */
    public float getComputationTimeAsSeconds();
    
    /**
     * Gibt die hoechste gefundene Modularitaet als Fliesskommazahl zurueck.
     * @return Rueckgabe einer Fliesskommazahl, die die hoechste gefundene Modularitaet darstellt.
     */
    public float getMaxFoundModularity();
    
    /**
     * Gibt eine Liste mit dem Verlauf der Modularitaet bei schrittweiser Kantenloeschung zurueck. 
     * @return Rueckgabe einer Liste mit dem Verlauf der Modualiraet bei schrittweiser Kantenloeschung. Das i-te Element
     * der Liste gibt die Modularitaet nach der Loeschung der i-ten Kante an.
     */
    public List<Float> getModularityRunAsList();
    
    /**
     * Gibt die Anzahl der Knoten im verarbeiteten Graphen als ganze Zahl zurueck.
     * @return Rueckgabe einer ganzen Zahl, die die Anzahl der Knoten im verarbeiteten Graphen darstellt.
     */
    public int getNodeCount();
    
    /**
     * Gibt die Anzahl der verarbeiteten Kanten als ganze Zahl zuruecl.
     * @return  Rueckgabe einer ganzen Zahl, die die Anzahl der verarbeiteten Kanten darstellt.
     */
    public int getProcessedEdgeCount();
    
    /**
     * Setzt ein Flag, dass einen Abbruchwunsch der Berechnung signalisiert.
     */
    public void setCancel();
    
    /**
     * Setzt den Graph, in welchem Communities gesucht werden sollen.
     * @param graph Graph, in welchem Communities gesucht werden sollen.
     */
    public void setGraph(Graph graph);
    
    /**
     * Setzt das ProgressTicket, welches fuer die Fortschrittsanzeige benutzt werden soll.
     * @param progressTicket ProgressTicket, welches fuer die Fortschrittsanzeige benutzt werden soll.
     */
    public void setProgressTicket(ProgressTicket progressTicket);
    
    /**
     * Setzt das Flag, das die Beruecksichtigung des Kantentyps bei der Berechnung der Shortest-Path Betweeness signalisiert auf den Wert von respETypeSPB. 
     * @param respETypeSPB Wahrheitswert fuer das Flag fuer die Beruecksichtigung des Kantentyps bei der Berechnung der Shortest-Path Betweeness. 
     * Ist der Wahrheitswert true, dann werden Kantentypen beruecksichtigt.
     */
    public void setRespETypeSPB(boolean respETypeSPB);
    
    /**
     * Setzt das Flag, dass die Beruecksichtigung von parallelen Kanten bei der Berechnung der Modularitaet signalisiert auf den Wert von respMultiEModul.
     * @param respMultiEModul Wahrheitswert fuer das Flag fuer die Beruecksichtigung von paralleleln Kanten bei der Berechnung der Modularitaet. 
     * Ist der Wahrheitswert true, dann werden parallele Kanten bei der Berechnung der Modularitaet beruecksichtigt.
     */
    public void setRespMultiEModul(boolean respMultiEModul);
    
    /**
     * Setzt das Flag, dass die Beruecksichtigung von parallelen Kanten bei der Berechnung der Shortest-Path Betweeness signalisiert auf den Wert von respMultiESPB.
     * @param respMultiESPB Wahrheitswert fuer das Flag fuer die Beruecksichtigung von paralleleln Kanten bei der Berechnung der Shortest-Path Betweeness. 
     * Ist der Wahrheitswert true, dann werden parallele Kanten bei der Berechnung der Shortest-Path Betweeness beruecksichtigt.
     */
    public void setRespMultiESPB(boolean respMultiESPB);
    
    /**
     * Setzt das Flag, dass die Beruecksichtigung des Kantentyps bei der Berechnung der Modularitaet signalisiert auf den Wert von respMultiESPB.
     * @param respETypeModul Wahrheitswert fuer das Flag fuer die Beruecksichtigung des Kantentyps bei der Berechnung der Modularitaet. 
     * Ist der Wahrheitswert true, dann wird der Kantentyp bei der Berechnung der Modularitaet beruecksichtigt.
     */
    public void setRespETypeModul(boolean respETypeModul);  
}
