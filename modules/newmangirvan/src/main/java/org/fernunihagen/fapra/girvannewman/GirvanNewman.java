package org.fernunihagen.fapra.girvannewman;

import java.util.HashMap;
import java.util.Map;
import org.fernunihagen.fapra.girvannewman.complex.GNComplex;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Graph;
import org.gephi.statistics.spi.Statistics;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Table;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class GirvanNewman implements Statistics, LongTask {

    private boolean respETypeSPB;
    private boolean respMultiESPB;
    private boolean respETypeModul;
    private boolean respMultiEModul;

    private ProgressTicket progressTicket;

    private IGirvanNewman proxy;

    @Override
    public void execute(GraphModel gm) {
        Graph graph = gm.getGraph();
        proxy = new GNComplex(); 
        proxy.setGraph(graph);
        proxy.setProgressTicket(progressTicket);
        proxy.setRespETypeSPB(respETypeSPB);
        proxy.setRespMultiESPB(respMultiESPB);
        proxy.setRespETypeModul(respETypeModul); 
        proxy.setRespMultiEModul(respMultiEModul);
        

        proxy.calculate();
        Map<Node, Integer> commMap = proxy.getCommunities();

        Table nodeTable = gm.getNodeTable();
        String colName = "Cluter-ID";
        Column column = nodeTable.getColumn(colName);
        if (column == null) {
            column = nodeTable.addColumn(colName, Integer.class);
        }
        for (Node n : graph.getNodes()) {
            int comm = commMap.get(n);
            n.setAttribute(column, comm);
        }
    }

    @Override
    public String getReport() {
        int i = 0; 
        Map<Integer, Float> modRun = new HashMap<>();
        for (float f : proxy.getModularityRunAsList()) {
            modRun.put(i, f);
            i++;
        }
        XYSeries dSeries = ChartUtils.createXYSeries(modRun, "Modularity Run");
        XYSeriesCollection dataset1 = new XYSeriesCollection();
        dataset1.addSeries(dSeries);
        JFreeChart chart = ChartFactory.createXYLineChart(null, "Deleted Edge", "Modularity", dataset1);
        chart.removeLegend();
               
        ChartUtils.decorateChart(chart);
        String imageFile = ChartUtils.renderChart(chart, "modrun.png");
        
        String s = "<html> <body><h1>Girvan-Newman Report</h1>"
                + "<hr><h2>Parameters:</h2>"
                + "<table><tr><td>Respect edge type for shortest path betweeness:</td>"
                + "<td>" + (respETypeSPB ? "yes" : "no") + "</td></tr>"
                + "<tr><td>Respect parallel edges for shortest path betweeness:</td>"
                + "<td>" + (respMultiESPB ? "yes" : "no") + "</td></tr>"
                + "<tr><td colspan = 2></td></tr>"
                + "<tr><td>Respect edge type for modularity computation:</td>"
                + "<td>" + (respETypeModul ? "yes" : "no") + "</td></tr>"
                + "<tr><td>Respect parallel edges for modularity computation:</td>"
                + "<td>" + (respMultiEModul ? "yes" : "no") + "</td></tr></table>"
                + "<br /><br />"
                + "<h2>Processed Graph Data</h2>"
                + "<table><tr><td>Nodes:</td><td>" + proxy.getNodeCount() + "</td></tr>"
                + "<tr><td>Edges</td><td>" + proxy.getProcessedEdgeCount() + "</td></tr></table>"
                + "<br />"
                + "<table><tr><td>Processing time: " + proxy.getComputationTimeAsSeconds() + " sec.</td></tr></table>"
                + "<br /><br />"
                + "<h2>Communities</h2>"
                + "<table><tr><td>Number of communities:</td><td>" + (proxy.getNodeCount() > 0 ?  proxy.getCommunitiesCount() : "--")+ "</td></tr>"
                + "<tr><td>Maximum found modularity:</td><td>" + (proxy.getProcessedEdgeCount() > 0 ? proxy.getMaxFoundModularity() : "--") + "</td></tr></table>"
                + "<br /><br />"
                + (proxy.getProcessedEdgeCount() > 0 ? imageFile : "")
                + "<br /><br />"
                + ""
   
                + "</body></html>";

        return s;

    }

    @Override
    public boolean cancel() {
        proxy.setCancel();
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        progressTicket = pt;
    }

    public void setRespETypeSPB(boolean respETypeSPB) {
        this.respETypeSPB = respETypeSPB;
    }

    public void setRespMultiESPB(boolean respMultiESPB) {
        this.respMultiESPB = respMultiESPB;
    }

    public void setRespETypeModul(boolean respETypeModul) {
        this.respETypeModul = respETypeModul;
    }

    public void setRespMultiEModul(boolean respMultiEModul) {
        this.respMultiEModul = respMultiEModul;
    }

}
