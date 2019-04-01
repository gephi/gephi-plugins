/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package competencerank;

import java.util.Arrays;
import java.util.List;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Table;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.gephi.graph.api.DirectedGraph;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

/**
 *
 * @author Laura Horst, Michael Spranger (AG FoSIL - University of Applied Sciences Mittweida, Germany )
 */
public class CompetenceRank implements Statistics, LongTask {

    //initialize global variables
    private String report;
    private boolean cancel = false;
    private ProgressTicket progressTicket;
    private double[] compr;
    private boolean isDirected;
    
    @Override
    public void execute(GraphModel gm) {
        
        report += "Calculation of CompetenceRank started";
        Graph graph = gm.getGraphVisible();
        isDirected = graph.isDirected();
        graph.readLock();

        //examine wether the graph has directed edges
        try {
            DirectedGraph directedGraph = null;

            if (isDirected) {
                directedGraph = (DirectedGraph) graph;
            }
            else 
            {
                throw new Exception("Graph is not directed.");
            }
            Progress.start(progressTicket);
            
            //initialize local variables
            List<Node> nodeList = Arrays.asList(graph.getNodes().toArray());
            int[][] transitionMatrix = new int[graph.getNodeCount()][graph.getNodeCount()];          
            compr = new double[graph.getNodeCount()];
            int[][] adjacencyMatrix = new int[graph.getNodeCount() + 1][graph.getNodeCount() + 1];
            double[][] weightedAdjacencyMatrix = new double[graph.getNodeCount() + 1][graph.getNodeCount() + 1];
            int[] rowsums = new int[adjacencyMatrix.length]; 
            
            //beginning of calculating the LeaderRank
            for (int i = 0; i < rowsums.length - 1; i++) {
                rowsums[i] = 1;
            }
            rowsums[rowsums.length - 1] = adjacencyMatrix.length - 1;
            for (Edge e : graph.getEdges()) {                
                int sourceIndex = nodeList.indexOf(e.getSource());
                int targetIndex = nodeList.indexOf(e.getTarget());
                transitionMatrix[sourceIndex][targetIndex]++;
                if (adjacencyMatrix[sourceIndex][targetIndex] == 0) {
                    if (sourceIndex != targetIndex) {
                        adjacencyMatrix[sourceIndex][targetIndex] = 1;
                        rowsums[sourceIndex]++;
                    }
                }

            }
            if (cancel) {
                return;
            }
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                int value = (graph.getNodeCount() == i) ? 0 : 1;
                adjacencyMatrix[graph.getNodeCount()][i] = value;
                adjacencyMatrix[i][graph.getNodeCount()] = value;
            }

            for (int i = 0; i < adjacencyMatrix.length; i++) {
                for (int j = 0; j < adjacencyMatrix.length; j++) {
                    weightedAdjacencyMatrix[j][i] = (double) adjacencyMatrix[i][j] / rowsums[i];                    
                }
            }

            double[] score_t = new double[adjacencyMatrix.length];
            for (int i = 0; i < adjacencyMatrix.length - 1; i++) {
                score_t[i] = 1;
            }
            if (cancel) {
                return;
            }

            score_t[score_t.length - 1] = 0;            
            
            double error_t = 0.00002;
            double error = 1;

            while (error > error_t) {
                double[] score_copy = Arrays.copyOf(score_t, score_t.length);

                for (int i = 0; i < adjacencyMatrix.length; i++) {
                    double score_i = 0;                    
                    for (int j = 0; j < adjacencyMatrix.length; j++) {
                        score_i += weightedAdjacencyMatrix[i][j] * score_copy[j];                        
                    }
                    score_t[i] = score_i;
                }
                
                error = 0;
                for (int i = 0; i < score_t.length; i++) {
                    error += Math.abs(score_copy[i] - score_t[i]);
                    
                }
                error = error / transitionMatrix.length;
                
                if (cancel) {
                    return;
                }
            }
            double score_gn = score_t[score_t.length - 1];
            

            double[] score_uncor = Arrays.copyOfRange(score_t, 0, score_t.length - 1);
            //ending of calculating the LeaderRank

            //add a Column for CompetenceRank to the table in Data Laboratory
            Table nodeTable = gm.getNodeTable();                       
            Column crCol = nodeTable.getColumn("CompetenceRank");
            if (crCol == null) {
                crCol = nodeTable.addColumn("CompetenceRank", Double.class, Origin.DATA);           
            }
            
            //add up all outging edges in the graph
            int ktotal = 0;            
            for (int i = 0; i<graph.getNodeCount();i++) {
                Node node=nodeList.get(i);
                int ki=directedGraph.getOutEdges(node).toArray().length;
                ktotal+=ki;
            }
            

            
            for (int i = 0; i < score_uncor.length; i++) {
                //calculate LeaderRank of node i
                double lr = score_uncor[i] + score_gn / transitionMatrix.length;
                //get node i
                Node node = nodeList.get(i);
                //get the number of outgoing edges of node i
                double ki=directedGraph.getOutEdges(node).toArray().length;
                //calculate CompetenceRank of node i
                double cr = (2*lr)/(1+ki/ktotal*graph.getNodeCount());
                //write value of CompetenceRank in table
                node.setAttribute(crCol, cr);
                //save value of CompetenceRank in list
                compr[i]=cr;
              
            }

            progressTicket.finish();
            graph.readUnlockAll();
        } catch (Exception e) {
            report += "\n**** Exception ****\n" + e.getMessage() + "\n*******************";
            graph.readUnlockAll();
        }
    }


    @Override
    public String getReport() {
        //initialize dataset
        HistogramDataset hd = new HistogramDataset();
        //set type for dataset
        hd.setType(HistogramType.FREQUENCY);
        //add values of CompetenceRank to dataset, label dataset and 
        //determine in how many parts the histogram should be subdevided        
        hd.addSeries("Competence Rank", compr, 40);
       
        //create histogram
        JFreeChart histogramChart = ChartFactory.createHistogram(
                "CompetenceRank Frequency",//title
                "Value", //title x-axis
                "Count", //title y-axis
                hd, //add dataset
                PlotOrientation.VERTICAL, //direction of the bars
                false, //legend
                false, //tooltips
                false); //urls
        
        //convert histogram to an image
        String imageFile = ChartUtils.renderChart(histogramChart, "competenceranks.png");
        
        //define report in HTML-language
        report= "<HTML> <BODY> <h1>CompetenceRank Report </h1> "
                + "<hr> <br />"                
                + "<br> <h2> Results: </h2>"
                + imageFile //add image of histogram
                + "<br /><br />" + "<h2> Algorithm: </h2>"
                + "Michael Spranger, Florian Heinke, Hanna Siewerts, Joshua Hampl, Dirk Labudde <i>Opinion Leader in Star-Like Social Networks: A Simple Case?</i>, in The Eighth International Conference on Advances in Information Mining and Management (IMMM 2018), 2018<br />"
                + "</BODY> </HTML>";
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

}
