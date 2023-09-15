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
package GlobalConnectionEfficency;

import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * http://www.w3.org/People/Massimo/papers/2001/efficiency_prl_01.pdf
 *
 * @author Cezary Bartosiak
 */
public class GlobalConnectionEfficiency implements Statistics, LongTask {
	private boolean cancel = false;
	private ProgressTicket progressTicket;

	private double gce = 0.0;

	private boolean isDirected = false;

	public void execute(GraphModel graphModel) {
		Graph graph = null;
		if (isDirected)
			graph = graphModel.getDirectedGraph();
		else graph = graphModel.getUndirectedGraph();
		execute(graph);
	}

	public void execute(Graph graph) {
		cancel = false;

		gce = 0.0;

		graph.readLock();

		int n = graph.getNodeCount();
		Progress.start(progressTicket, n * n + n * n + n * n);

		// FloydWarshall algorithm
		Node[] nodes = graph.getNodes().toArray();
		double[][] d = new double[n][n];
		for (int i = 0; i < n && !cancel; ++i)
			for (int j = 0; j < n && !cancel; ++j) {
				if (i == j)
					d[i][j] = 0.0;
				else if (graph.isAdjacent(nodes[i], nodes[j]))
					d[i][j] = 1.0;
				else d[i][j] = Double.POSITIVE_INFINITY;
				Progress.progress(progressTicket);
			}
		for (int k = 0; k < n && !cancel; ++k)
			for (int i = 0; i < n && !cancel; ++i) {
				for (int j = 0; j < n && !cancel; ++j)
					d[i][j] = Math.min(d[i][j], d[i][k] + d[k][j]);
				Progress.progress(progressTicket);
			}

		double sum = 0.0;
		for (int i = 0; i < n && !cancel; ++i)
			for (int j = 0; j < n && !cancel; ++j) {
				if (i != j)
					sum += 1.0 / d[i][j];
				Progress.progress(progressTicket);
			}
		gce = sum / (double)(n * (n - 1));

		graph.readUnlock();
	}

	public void setDirected(boolean isDirected) {
		this.isDirected = isDirected;
	}

	public boolean isDirected() {
		return isDirected;
	}

	public double getGCE() {
		return gce;
	}

	public String getReport() {
		NumberFormat f = new DecimalFormat("#0.0000");

		String report = "<html><body><h1>Global Connection Efficiency Report</h1>"
				+ "<hr>"
				+ "<br>"
				+ "<br><h2>Results:</h2>"
				+ "Global Connection Efficiency: " + f.format(gce)
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
