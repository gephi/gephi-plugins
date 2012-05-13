/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package edu.uic.cs.compbio.DyNSPK;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeOrigin;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.DynamicInteger;
import org.gephi.data.attributes.type.Interval;
import org.gephi.dynamic.api.DynamicController;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.*;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.statistics.spi.DynamicStatistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Lookup;


/**
 *
 * @author Mathieu Bastian
 */
public class DynamicEntropy implements DynamicStatistics, LongTask {


    //Data
    private GraphModel graphModel;
    private DynamicModel dynamicModel;
    private double window;
    private double tick;
    private Interval bounds;
    private boolean isDirected;
    private boolean averageOnly;
    private boolean cancel = false;
    //Result
    //private List<Interval<Double>> averages;
    private Map<Double, Double> degreeTs;
    private int first;
    private Node[] previousnodes;
    private Edge[] previousedges;
    private Map<Double, Double> nodedata;
    private Map<Double, Double> edgedata;
    private Map<Double, Double> sumdata;

    public DynamicEntropy() {
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        if (graphController != null && graphController.getModel() != null) {
            isDirected = graphController.getModel().isDirected();
        }
    }

    public void execute(GraphModel graphModel, AttributeModel attributeModel) {
        this.graphModel = graphModel;
        //this.averages = new ArrayList<Interval<Double>>();
        this.degreeTs = new HashMap<Double, Double>();
        this.nodedata = new HashMap<Double, Double>();
        this.edgedata = new HashMap<Double, Double>();
        this.sumdata = new HashMap<Double, Double>();
        this.isDirected = graphModel.isDirected();
        this.dynamicModel = Lookup.getDefault().lookup(DynamicController.class).getModel(graphModel.getWorkspace());

     
    }
    public String makeChart(Map<Double, Double> data, String filename){
         //Time series
        XYSeries dSeries = ChartUtils.createXYSeries(data, "Node Time Series");

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Degree Time Series",
                "Count",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);

        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, false);
        String degreeImageFile = ChartUtils.renderChart(chart,  filename);
        return degreeImageFile;
    }
    public String getReport() {
        NumberFormat f = new DecimalFormat("#0.000000");


        String report = "<HTML> <BODY> <h1>Dynamic Degree Report </h1> "
                + "<hr>"
                + "<br> Bounds: from " + f.format(bounds.getLow()) + " to " + f.format(bounds.getHigh())
                + "<br> Window: " + window
                + "<br> Tick: " + tick
                + "<br><br><h2> Average degrees over time: </h2>"
                + "<br /><br />" + makeChart(this.nodedata, "nodets.png") + makeChart(this.edgedata, "edgets.png") + makeChart(this.sumdata, "sumts.png") ;
/*
 *    private Map<Double, Double> nodedata;
    private Map<Double, Double> edgedata;
    private Map<Double, Double> sumdata;
 */
        /*for (Interval<Double> average : averages) {
        report += average.toString(dynamicModel.getTimeFormat().equals(DynamicModel.TimeFormat.DOUBLE)) + "<br />";
        }*/
        report += "<br /><br /></BODY></HTML>";
        return report;
    }

    public void loop(GraphView window, Interval interval) {
        HierarchicalGraph graph = graphModel.getHierarchicalGraph(window);
        HierarchicalDirectedGraph directedGraph = null;
        if (isDirected) {
            directedGraph = graphModel.getHierarchicalDirectedGraph(window);
               if(this.first == 0){
            //Initialization. Since this is executed first we allow for the previous window to be accessed 
            first++;
            previousedges = directedGraph.getEdges().toArray();
                    
            previousnodes = directedGraph.getNodes().toArray();
            edgedata.put(interval.getHigh(), 0.0);
            nodedata.put(interval.getHigh(), 0.0);
        }
        else if(this.first == 1){
            
           //Here we count the changes 
           double edgedelta = 0.0;
         
           for (Edge prevedge : previousedges) {
             if(directedGraph.contains(prevedge)){
                 edgedelta++;
                }
             
           }
           edgedata.put(interval.getHigh(), edgedelta);
           double nodedelta = 0;
            for (Node prevnode : previousnodes) {
             if(directedGraph.contains(prevnode)){
                 nodedelta++;
                }
             
           }
           
           nodedata.put(interval.getHigh(), nodedelta);
           Double sumdelta = nodedelta + edgedelta;
           sumdata.put(interval.getHigh(), sumdelta);
           //Now we mutate previous edge and node so we can compare it in the next window
            previousedges = directedGraph.getEdges().toArray();
            previousnodes = directedGraph.getNodes().toArray();
            }
        }
        else {
        if(this.first == 0){
            //Initialization. Since this is executed first we allow for the previous window to be accessed 
            first++;
            previousedges = graph.getEdges().toArray();
                    
            previousnodes = graph.getNodes().toArray();
            edgedata.put(interval.getHigh(), 0.0);
            nodedata.put(interval.getHigh(), 0.0);
        }
        else if(this.first == 1){
            
           //Here we count the changes 
           double edgedelta = 0.0;
         
           for (Edge prevedge : previousedges) {
             if(graph.contains(prevedge)){
                 edgedelta++;
                }
             
           }
           edgedata.put(interval.getHigh(), edgedelta);
           double nodedelta = 0;
            for (Node prevnode : previousnodes) {
             if(graph.contains(prevnode)){
                 nodedelta++;
                }
             
           }
           
           nodedata.put(interval.getHigh(), nodedelta);
           Double sumdelta = nodedelta + edgedelta;
           sumdata.put(interval.getHigh(), sumdelta);
           //Now we mutate previous edge and node so we can compare it in the next window
            previousedges = graph.getEdges().toArray();
            previousnodes = graph.getNodes().toArray();
            }
        }

        long sum = 0;
        

       
    }
    public void end() {
    }

    public void setBounds(Interval bounds) {
        this.bounds = bounds;
    }

    public void setWindow(double window) {
        this.window = window;
    }

    public void setTick(double tick) {
        this.tick = tick;
    }

    public double getWindow() {
        return window;
    }

    public double getTick() {
        return tick;
    }

    public Interval getBounds() {
        return bounds;
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public void setAverageOnly(boolean averageOnly) {
        this.averageOnly = averageOnly;
    }

    public boolean isAverageOnly() {
        return averageOnly;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
    }
}
