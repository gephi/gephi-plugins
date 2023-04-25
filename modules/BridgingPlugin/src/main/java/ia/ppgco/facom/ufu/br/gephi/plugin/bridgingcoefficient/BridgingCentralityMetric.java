package ia.ppgco.facom.ufu.br.gephi.plugin.bridgingcoefficient;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.statistics.plugin.GraphDistance;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.gephi.graph.api.*;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.NbBundle;
import java.math.BigDecimal;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;

/**
 *
 * @author getulio
 */
public class BridgingCentralityMetric extends GraphDistance implements Statistics {

    private static final Logger LOG = Logger.getLogger(BridgingCentralityMetric.class.getName());

    public static final String BETWEENNESS_CENTRALITY = "betweennesscentrality2";
    public static final String BRIDGING_CENTRALITY = "bridgingcentrality";
    public static final String BRIDGING_COEFFICIENT = "bridgingcoefficient";

    // relatorio de execucao
    private String report = "";

    // controle de execucao, com possibilidade de interrupcao pelo usuario
    private boolean cancelled = false;
    private ProgressTicket progressTicket;

    // parametros de entrada do usuario
    private boolean directed;
    private boolean normalized;

    // tomada de tempo de execucao
    private LocalDateTime start = null;
    private LocalDateTime stop_betweeness = null;
    private LocalDateTime stop_bridging = null;

    // estatisticas calculadas pelo algoritmo
    private double[] mybetweenness;
    private double[] bridging_cent;
    private double bridging_coef;

    // variaveis globais
    private int Number_of_Nodes;

    /**
     * Bridging Centrality algorithm ....
     *
     * @param graphModel
     */
    @Override
    public void execute(GraphModel graphModel) {
        directed = graphModel.getGraphVisible().isDirected();

        final Graph graph;
        if (directed) {
            graph = graphModel.getDirectedGraphVisible();
        } else {
            graph = graphModel.getUndirectedGraphVisible();
        }

        execute(graph);
        LOG.log(java.util.logging.Level.INFO, "Success Bridging Centrality calculations.");
    }

    @Override
    public void execute(Graph graph) {

        graph.readLock();

        try {

            initializeAttributeColunms(graph.getModel());
            //number of nodes
            Number_of_Nodes = graph.getNodeCount();

            //creates one array for each coeficient to be calculated
            mybetweenness = new double[Number_of_Nodes];
            bridging_cent = new double[Number_of_Nodes];

            //a humble index strategy, a sequential index
            HashMap<Node, Integer> indicies = createIndiciesMap(graph);
            //marks the start of the process
            start = LocalDateTime.now();
            //calculates mybetweenness
            mybetweenness = calculateBetweeness(graph, indicies, directed, normalized);
            //mark the end of the process
            stop_betweeness = LocalDateTime.now();
            // calculates  bridging coefficient and bridging centrality
            saveCalculatedValues(graph, indicies, mybetweenness);
            //mark the end of the process of the bridging metrics            
            stop_bridging = LocalDateTime.now();
            // -----------
            if (cancelled) {
                String msg = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.alert.message");
                String title = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.alert.title");
                JOptionPane.showMessageDialog(null, msg, title, JOptionPane.WARNING_MESSAGE);
                LOG.log(java.util.logging.Level.WARNING, msg);
            } else {
                //Print results
                Duration diff_1 = Duration.between(start, stop_betweeness);
                Duration diff_2 = Duration.between(stop_betweeness, stop_bridging);
                String duration = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.duration");

                report += "<b>" + duration + " </b> : " + diff_1.getSeconds() + " seconds (Betweenness) <br/>";
                report += "<b>" + duration + " </b> : " + diff_2.getSeconds() + " seconds (Bridging) <br/>";
                report += "<b>" + duration + " </b> : " + (diff_1.getSeconds() + diff_2.getSeconds()) + " seconds (Total) <br/>";

                report += "<br/>";
            }
            // -----------
        } finally {
            graph.readUnlock();
        }
    }

    /**
     * Inicializa a tabela de nós
     *
     * @param graphModel
     */
    private void initializeAttributeColunms(GraphModel graphModel) {

        Table nodeTable = graphModel.getNodeTable();

        if (!nodeTable.hasColumn(BETWEENNESS_CENTRALITY)) {
            nodeTable.addColumn(BETWEENNESS_CENTRALITY, "Betweenness Centrality", BigDecimal.class, new BigDecimal("0"));
        }

        if (!nodeTable.hasColumn(BRIDGING_COEFFICIENT)) {
            nodeTable.addColumn(BRIDGING_COEFFICIENT, "Bridging Coefficient", BigDecimal.class, new BigDecimal("0"));
        }

        if (!nodeTable.hasColumn(BRIDGING_CENTRALITY)) {
            nodeTable.addColumn(BRIDGING_CENTRALITY, "Bridging Centrality", BigDecimal.class, new BigDecimal("0"));
        }
    }

    @Override
    public HashMap<Node, Integer> createIndiciesMap(Graph graph) {

        HashMap<Node, Integer> indicies = new HashMap<Node, Integer>();

        int index = 0;
        for (Node n : graph.getNodes()) {
            indicies.put(n, index);
            index++;
        }

        return indicies;
    }

    /**
     *
     * @param graph
     * @param indicies
     * @param directed
     * @param normalized
     *
     * @return array with betweeness statistics
     */
    private double[] calculateBetweeness(Graph graph, HashMap<Node, Integer> indicies, boolean directed, boolean normalized) {

        double[] nodeBetweenness = new double[Number_of_Nodes];

        int count = 0;

        Progress.start(progressTicket, Number_of_Nodes);
        //Bread First Search (BFS) to calculate all the shortest paths from node s to t
        for (Node s : graph.getNodes()) {
            //The below structures are candidates to performance improvments: 
            //Creates a stack for each node, ...
            Stack<Node> S = new Stack<Node>();
            //... a linked listed for each node, ...
            LinkedList<Node>[] Pred = new LinkedList[Number_of_Nodes];
            // ... define sigma(s, t) as the number of shortest paths between nodes s and t ...
            double[] sigma = new double[Number_of_Nodes];
            //...and also a distance for each node.
            int[] dist = new int[Number_of_Nodes];

            int s_index = indicies.get(s);
            //all parameters are set empty 
            BFS_Initialization(Pred, sigma, dist, s_index, Number_of_Nodes);

            LinkedList<Node> Q = new LinkedList<Node>();
            //All nodes are added to the end of the Q list
            //Starting node: 1. Choose the starting node s and put it on the queue Q.
            Q.addLast(s);
            while (!Q.isEmpty()) {
                //The calculation always starts from first node. 1. dequeue v from Q
                Node v = Q.removeFirst();
                //Despite of being removed of the list the node is stored in a stack S
                //This stack will be poped up to simulate the backtracking of the shortest paths
                //from all nodes s and t; and allows us to calculate the mybetweenness in the process
                S.push(v);
                //Recovery of the node index.
                //Using this index we will directely access the 'dist' and 'sigma' attributes of the node
                int v_index = indicies.get(v);
                //Read all edges conected to the node
                EdgeIterable edgeIter = getEdgeIter(graph, v, directed);
                //Iterate for all edges to access their nodes in the opposite side
                for (Edge edge : edgeIter) {
                    //Obtain a new node in the opposite edge of the node
                    Node reachable = graph.getOpposite(v, edge);
                    //recover the index of the reachable node
                    //Using this index we will directely access the 'dist' and 'sigma' attributes of the reachable node
                    int r_index = indicies.get(reachable);
                    //if the reachable node where not investigated yet (dist<0) ...
                    if (dist[r_index] < 0) {
                        //... add the reachable node to the list of further nodes to be analysed ...
                        Q.addLast(reachable);
                        //... and increment the 'dist' of the reachable node to the value of the initial node plus zero
                        dist[r_index] = dist[v_index] + 1;
                        //It meaks sense because the distance from the reachable node to the current node is exactly 1.
                    }
                    //If the reachable node was just incorporated to the list of further analyses then the next condition
                    //will be always true, even because v_index no longer changes. However, the current node here
                    //could be a reachable node from another one causing the increment of the distantce 'dist'. Once the
                    //distance is augmented by one the node no longer will be analysed (no loops).  Still, the
                    //next condition certificates the 'sigma' sum only in care of difference of exacltly one edge between
                    //the current node and the reachable node, no matter the value of 'dist'.
                    if (dist[r_index] == (dist[v_index] + 1)) {
                        sigma[r_index] = sigma[r_index] + sigma[v_index];
                        //The current node v will be added to the list of nodes to visit of the reachable node
                        Pred[r_index].addLast(v);
                    }
                }
            }

            // -------------
            //All nodes and its connections had its paths length computed. 
            //Now is possible to calculate the mybetweenness poping up the stack created with the BFS
            //1. set delta(v) to zero for all nodes v in V.
            double[] delta = new double[Number_of_Nodes];
            //3. while S is not empty, ...
            while (!S.empty()) {
                //... pop w off S
                Node w = S.pop();

                int w_index = indicies.get(w);

                ListIterator<Node> it = Pred[w_index].listIterator();
                while (it.hasNext()) {
                    Node u = it.next();
                    int u_index = indicies.get(u);
                    //for all nodes u in Pred(w) set delta(u) to delta(u) + MAGIC(delta(w)).
                    delta[u_index] += (sigma[u_index] / sigma[w_index]) * (1 + delta[w_index]);
                    //MAGIC(delta(w)) is an incremental formula based on the recursive definition
                    //of the shortests paths from s to w. It means that a proportion of shortests
                    //paths from the w predecessors incorporates the u (or v) node. This proportion
                    //is the division operation. This proportion multiplies at least one path plus
                    //the remaining shortest paths to w. The incremental sum of all these shortests
                    //paths proportions (deltas) raises the integral mybetweenness metric.
                }

                if (w != s) {
                    nodeBetweenness[w_index] += delta[w_index];
                }
            }

            count++;

            if (cancelled) {
                return nodeBetweenness;
            }

            Progress.progress(progressTicket, count);
        }

        // corrige e normaliza o resultado da mybetweenness
        calculateCorrection(graph, indicies, nodeBetweenness, directed, normalized);

        return nodeBetweenness;
    }

    /**
     * Salva as estatisticas calculadas como atributos de cada nó
     *
     * @param graph
     * @param indicies
     * @param nodeBetweenness
     */
    private void saveCalculatedValues(Graph graph, HashMap<Node, Integer> indicies, double[] nodeBetweenness) {

        for (Node s : graph.getNodes()) {

            int s_index = indicies.get(s);

            bridging_coef = bridging_coeficient(graph, s);
            //The bridging centrality is just a multiplication of two other metrics
            bridging_cent[s_index] = bridging_coef * mybetweenness[s_index];

            s.setAttribute(BETWEENNESS_CENTRALITY, new BigDecimal(nodeBetweenness[s_index]));
            s.setAttribute(BRIDGING_COEFFICIENT, new BigDecimal(bridging_coef));
            s.setAttribute(BRIDGING_CENTRALITY, new BigDecimal(bridging_cent[s_index]));
        }
    }

    /**
     * Aplica as correcoes/normalizacoes nos nós, para a betweeness
     *
     * @param graph
     * @param indicies
     * @param nodeBetweenness
     * @param directed
     * @param normalized
     */
    private void calculateCorrection(Graph graph, HashMap<Node, Integer> indicies, double[] nodeBetweenness, boolean directed, boolean normalized) {
        int s_index;
        for (Node s : graph.getNodes()) {

            s_index = indicies.get(s);

            // corrige o resultado, para o caso de rede nao dirigida
            if (!directed) {
                nodeBetweenness[s_index] /= 2;
            }

            // normaliza o resultado
            if (normalized) {
                nodeBetweenness[s_index] /= directed ? (Number_of_Nodes - 1) * (Number_of_Nodes - 2) : (Number_of_Nodes - 1) * (Number_of_Nodes - 2) / 2;
            }
        }
    }

    private void BFS_Initialization(LinkedList<Node>[] Pred, double[] sigma, int[] dist, int index, int n) {
        //There is a potential problem in this function: the Node s is passed but never used.
        //For what node these parameteres are being set?
        for (int w = 0; w < n; w++) {
            //1. Mark w as unvisited by setting dist[w] (the distance between s and the node w) to infinity.
            dist[w] = -1;
            //2. Set Pred[w] (nodes that immediately precede w on a shortest path from s) to the empty list.
            Pred[w] = new LinkedList<Node>();

            sigma[w] = 0;
        }
        //The number of shortest paths start with the value 1 since sigma(v,v)=1
        sigma[index] = 1;
        //Starting node: 2. Set dist[s] to 0.
        dist[index] = 0;
    }

    private EdgeIterable getEdgeIter(Graph graph, Node v, boolean directed) {

        if (directed) {
            return ((DirectedGraph) graph).getOutEdges(v);
        }

        return graph.getEdges(v);
    }

    /**
     * Should return plain text or HTML text that describe the algorithm execution.
     *
     * @return
     */
    @Override
    public String getReport() {

        String htmlIMG1 = "";
        String htmlIMG2 = "";
        String htmlIMG3 = "";

        try {
            TempDir tempDir = TempDirUtils.createTempDir();
            htmlIMG1 = createImageFile(tempDir, bridging_cent, "Bridging Centrality Distribution", "Value", "Count");
            //            htmlIMG2 = createImageFile(tempDir, bridging_coef, "Bridging Coefficient Distribution", "Value", "Count");
            htmlIMG3 = createImageFile(tempDir, mybetweenness, "Betweenness Centrality Distribution", "Value", "Count");
        } catch (IOException ex) {

            String msg = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.error.message");
            String title = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.error.title");
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);

            LOG.log(java.util.logging.Level.SEVERE, msg, ex);
        }

        String report_title = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.title");
        String params = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params");
        String param_1 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_1");
        String value_1_1 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_1.value_1");
        String value_1_2 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_1.value_2");
        String value_2_1 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_2.value_1");
        String value_2_2 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_2.value_2");
        String results = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.results");

        String param_2 = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.params.par_2");

        String rep = "<HTML> <BODY> <h1>" + report_title + "</h1> "
                + "<hr>"
                + "<br>"
                + "<h2> " + params + " : </h2>"
                + param_1 + " : " + (directed ? value_1_1 : value_1_2) + "<br />"
                + param_2 + " : " + (normalized ? value_2_1 : value_2_2) + "<br />"
                + "<br /> <h2> " + results + ": </h2>";

        rep += report;

        String algorithms = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.algorithm");
        String credits = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.credits");
        String contact = NbBundle.getMessage(BridgingCentralityMetric.class, "BridgingCentralityMetric.report.contact");

        rep += "<br /><br />"
                + "<br>Please, check the Data Laboratory Tab for the full listing of Bridging Centrality for the graph.</br>"
                + "<br /><br />"
                + htmlIMG1 + "<br /><br />"
                + htmlIMG2 + "<br /><br />"
                + htmlIMG3 + "<br /><br />"
                + "<br /><br />"
                + "<h2>" + algorithms + ": </h2>"
                + "<ul>"
                + "<li>Ulrik Brandes, <b>A Faster Algorithm for Betweenness Centrality</b>, in Journal of Mathematical Sociology 25(2):163-177, (2001)</li>"
                + "<li>Hwang, W., Cho, Y. R., Zhang, A., & Ramanathan, M. (2006, March). <b>Bridging centrality: identifying bridging nodes in scale-free networks</b>. In Proceedings of the 12th ACM SIGKDD international conference on Knowledge discovery and data mining (pp. 20-23).</li>"
                + "</ul>"
                + "<b>" + credits + ": </b>"
                + "<ul>"
                + "<li>Get&uacute;lio de Morais Pereira</li>"
                + "<li>Anderson Rodrigues dos Santos</li>"
                + "<li>Luis Felipe Nunes Reis</li>"
                + "<ul>"
                + "<li>" + contact + " : santosardr@ufu.br </li>"
                + "</ul>"
                + "<ul>"
                + "</BODY> </HTML>";

        return rep;
    }

    private String createImageFile(TempDir tempDir, double[] pVals, String pName, String pX, String pY) {

        //distribution of nodeIDs
        Map<Double, Integer> dist = new HashMap<Double, Integer>();

        for (int i = 0; i < Number_of_Nodes; i++) {
            Double d = pVals[i];
            if (dist.containsKey(d)) {
                Integer v = dist.get(d);
                dist.put(d, v + 1);
            } else {
                dist.put(d, 1);
            }
        }

        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(dist, pName);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);
        dataset.setAutoWidth(true);

        JFreeChart chart = ChartFactory.createXYLineChart(
                pName,
                pX,
                pY,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        XYPlot xyPlot = (XYPlot) chart.getPlot();
        NumberAxis domain = (NumberAxis) xyPlot.getDomainAxis();
        domain.setRange(0.00, domain.getUpperBound());
        domain.setTickUnit(new NumberTickUnit(domain.getUpperBound() / 10));

        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        // ChartUtils.scaleChart(chart, dSeries, normalized);

        return ChartUtils.renderChart(chart, pName + ".png");
    }

    /**
     * Calcula o bridging coeficient de um dado vertice
     *
     * @param graph
     * @param node
     *
     * @return bridging coefficient value
     */
    private static double bridging_coeficient(Graph graph, Node node) {
        if(isIsolatedNode(graph,node)){
            return 0;
        }
        //The inverse degree of a node divided by the inverse degree of all their imediate neighbors
        double n = 1.0 / graph.getDegree(node);

        double d = 0.0;
        Iterator<Node> it = graph.getNeighbors(node).iterator();
        while (it.hasNext()) {
            Node t = (Node) it.next();
            d += 1.0 / graph.getDegree(t);
        }

        return n / d;
    }

    /**
     * cancelar a execucao do algoritmo
     */
    @Override
    public boolean cancel() {
        this.cancelled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    @Override
    public boolean isDirected() {
        return directed;
    }

    @Override
    public void setDirected(boolean directed) {
        this.directed = directed;
    }

    @Override
    public boolean isNormalized() {
        return normalized;
    }

    @Override
    public void setNormalized(boolean normalized) {
        this.normalized = normalized;
    }
    
    public static boolean isIsolatedNode(Graph graph, Node node) {
        
        NodeIterable iterable = graph.getNodes();
        
        for (Node s : iterable) {
            if (graph.isAdjacent(node, s)) {
                iterable.doBreak();
                return false;
            }
        }
        // If loop ends without return false, it is an isolated node
        return true;
    }

}
