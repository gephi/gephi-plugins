/*
 * Copyright 2008-2012 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 *
 * This file is part of Gephi.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Gephi Consortium. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 3 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://gephi.org/about/legal/license-notice/
 * or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License files at
 * /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 3, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 3] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 3 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 3 code and therefore, elected the GPL
 * Version 3 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Gephi Consortium.
 */
package AverageGlobalConnectionEfficiency;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 * http://www.w3.org/People/Massimo/papers/2001/efficiency_prl_01.pdf
 *
 * @author Cezary Bartosiak
 */
public class AverageGlobalConnectionEfficiency implements Statistics, LongTask {
    private boolean cancel = false;
    private ProgressTicket progressTicket;
    private double avggce = 0.0;

    // General
    private boolean isDirected = false;

    // Samples count
    private int samplesCount = 10;

    // Removal strategy options
    private int k = 0;
    private boolean exactlyK = true;
    private MsType mstype = MsType.Random; // or "RandomRandom"

    @Override
    public void execute(GraphModel graphModel) {
        Graph graph;
        if (isDirected)
            graph = graphModel.getDirectedGraph();
        else {
            graph = graphModel.getUndirectedGraph();
        }
        execute(graph);

    }

    public void execute(Graph graph){
        cancel = false;

        avggce = 0.0;

        graph.readLock();

        Progress.start(progressTicket, samplesCount);

        double sum = 0.0;
        for (int i = 0; i < samplesCount && !cancel; ++i) {
            GraphView sourceView  = graph.getView();
            graph.readUnlock();
            GraphView currentView = graph.getModel().copyView(sourceView);
            Graph g = graph.getModel().getGraph(currentView);
            removeNodes(g);
            graph.readLock();
            sum += getGCE(g);
            Progress.progress(progressTicket);
        }
        avggce = sum / samplesCount;

        graph.readUnlock();
    }

    public void removeNodes(Graph graph) {
        Random random = new Random();

        Node[] nodes = graph.getNodes().toArray();
        List<Node> modNodes = new ArrayList<Node>();
        if (mstype.equals(MsType.Random)){
            List<Node> rNodes = new LinkedList<Node>(Arrays.asList(nodes));
            for (int i = 0; i < k; ++i)
                if (exactlyK)
                    modNodes.add(rNodes.remove(random.nextInt(rNodes.size())));
                else {
                    Node node = rNodes.get(random.nextInt(rNodes.size()));
                    if (!modNodes.contains(node))
                        modNodes.add(node);
                }
        }
        else if (mstype.equals(MsType.RandomRandom)) {
            List<Node> rNodes = new LinkedList<Node>(Arrays.asList(nodes));
            for (int i = 0; i < k; ++i) {
                Node rNode;
                if (exactlyK)
                    rNode = rNodes.remove(random.nextInt(rNodes.size()));
                else rNode = rNodes.get(random.nextInt(rNodes.size()));
                Node[] neighbors = graph.getNeighbors(rNode).toArray();
                Node node = neighbors[random.nextInt(neighbors.length)];
                if (!modNodes.contains(node))
                    modNodes.add(node);
            }
        }
        else modNodes = Arrays.asList(nodes);

        for (Node node : modNodes)
            graph.removeNode(node);
    }

    private double getGCE(Graph graph) {
        int n = graph.getNodeCount();

        // FloydWarshall algorithm
        Node[] nodes = graph.getNodes().toArray();
        double[][] d = new double[n][n];
        for (int i = 0; i < n && !cancel; ++i)
            for (int j = 0; j < n && !cancel; ++j)
                if (i == j)
                    d[i][j] = 0.0;
                else if (graph.isAdjacent(nodes[i], nodes[j]))
                    d[i][j] = 1.0;
                else d[i][j] = Double.POSITIVE_INFINITY;
        for (int k = 0; k < n && !cancel; ++k)
            for (int i = 0; i < n && !cancel; ++i)
                for (int j = 0; j < n && !cancel; ++j)
                    d[i][j] = Math.min(d[i][j], d[i][k] + d[k][j]);

        double sum = 0.0;
        for (int i = 0; i < n && !cancel; ++i)
            for (int j = 0; j < n && !cancel; ++j)
                if (i != j)
                    sum += 1.0 / d[i][j];

        if (n <= 1)
            return 0.0;
        return sum / (double)(n * (n - 1));
    }

    public double getAvgGCE() {
        return avggce;
    }

    public void setDirected(boolean isDirected) {
        this.isDirected = isDirected;
    }

    public void setSamplesCount(int samplesCount) {
        this.samplesCount = samplesCount;
    }

    public void setK(int k) {
        this.k = k;
    }

    public void setExactlyK(boolean exactlyK) {
        this.exactlyK = exactlyK;
    }

    public void setMstype(MsType mstype) {
        this.mstype = mstype;
    }

    public boolean isDirected() {
        return isDirected;
    }

    public int getSamplesCount() {
        return samplesCount;
    }

    public MsType getMstype() {
        return mstype;
    }

    public int getK() {
        return k;
    }

    public boolean isExactlyK() {
        return exactlyK;
    }



    public String getReport() {
        NumberFormat f = new DecimalFormat("#0.0000");

        String report = "<html><body><h1>Average Global Connection Efficiency Report</h1>"
                + "<hr>"
                + "<br>"
                + "<br><h2>Results:</h2>"
                + "Samples Count: " + samplesCount + "<br>"
                + "Average Global Connection Efficiency: " + f.format(avggce)
                + "</body></html>";

        return report;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
