/*
 * Copyright 2008-2010 Gephi
 * Authors : Cezary Bartosiak
 * Website : http://www.gephi.org
 * 
 * This file is part of Gephi.
 *
 * Gephi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Gephi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.complexgenerator.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Generates an undirected connected graph.
 *
 * http://en.wikipedia.org/wiki/Barabási–Albert_model
 * http://www.barabasilab.com/pubs/CCNR-ALB_Publications/199910-15_Science-Emergence/199910-15_Science-Emergence.pdf
 * http://www.facweb.iitkgp.ernet.in/~niloy/COURSE/Spring2006/CNT/Resource/ba-model-2.pdf
 *
 * N  > 0
 * m0 > 0 && m0 <  N
 *
 * O(N^2 * avgM)
 *
 * @author Cezary Bartosiak
 */
// @ServiceProvider(service = Generator.class)
public class MultiBarabasiAlbert implements Generator {
	private boolean cancel = false;
	private ProgressTicket progressTicket;

	private int N  = 50;
	private int m0 = 1;

	private Map<Integer, Integer> Mmap = new HashMap<Integer, Integer>();

	public MultiBarabasiAlbert() {
		Mmap.put(1, 1);
	}

	@Override
	public void generate(ContainerLoader container) {
		Progress.start(progressTicket, N);
		Random random = new Random();
		container.setEdgeDefault(EdgeDefault.UNDIRECTED);

		// Timestamps
		int vt = 1;
		int et = 1;

		NodeDraft[] nodes = new NodeDraft[N];
		int[] degrees = new int[N];

		// Creating m0 nodes
		for (int i = 0; i < m0 && !cancel; ++i) {
			NodeDraft node = container.factory().newNodeDraft();
			node.setLabel("Node " + i);
			node.addTimeInterval("0", (N - m0) + "");
			nodes[i] = node;
			degrees[i] = 0;
			container.addNode(node);
			Progress.progress(progressTicket);
		}

		// Linking every node with each other (no self-loops)
		for (int i = 0; i < m0 && !cancel; ++i)
			for (int j = i + 1; j < m0 && !cancel; ++j) {
				EdgeDraft edge = container.factory().newEdgeDraft();
				edge.setSource(nodes[i]);
				edge.setTarget(nodes[j]);
				edge.addTimeInterval("0", (N - m0) + "");
				degrees[i]++;
				degrees[j]++;
				container.addEdge(edge);
			}

		// Adding N - m0 nodes, each with Mi edges
		int Mi = 0;
		for (int i = m0; i < N && !cancel; ++i, ++vt, ++et) {
			if (Mmap.containsKey(i))
				Mi = Mmap.get(i);

			int mi = 0;
			for (int j = 0; j < i; ++j)
				if (degrees[j] < i)
					mi++;
			if (Mi < mi)
				mi = Mi;

			// Adding new node
			NodeDraft node = container.factory().newNodeDraft();
			node.setLabel("Node " + i);
			node.addTimeInterval(vt + "", (N - m0) + "");
			nodes[i] = node;
			degrees[i] = 0;
			container.addNode(node);

			// Adding mi edges out of the new node
			double sum = 0.0; // sum of all nodes degrees
			for (int j = 0; j < i && !cancel; ++j)
				sum += degrees[j];
			double s = 0.0;
			for (int m = 0; m < mi && !cancel; ++m) {
				double r = random.nextDouble();
				double p = 0.0;
				for (int j = 0; j < i && !cancel; ++j) {
					if (container.edgeExists(nodes[i], nodes[j]) || container.edgeExists(nodes[j], nodes[i]))
						continue;

					if (i == 1)
						p = 1.0;
					else p += degrees[j] / sum + s / (i - m);

					if (r <= p) {
						s += degrees[j] / sum;

						EdgeDraft edge = container.factory().newEdgeDraft();
						edge.setSource(nodes[i]);
						edge.setTarget(nodes[j]);
						edge.addTimeInterval(et + "", (N - m0) + "");
						degrees[i]++;
						degrees[j]++;
						container.addEdge(edge);
						Progress.progress(progressTicket);

						break;
					}
				}
			}

			Progress.progress(progressTicket);
		}

		Progress.finish(progressTicket);
		progressTicket = null;
	}

	public int getN() {
		return N;
	}

	public int getm0() {
		return m0;
	}

	public Map<Integer, Integer> getMmap() {
		return Mmap;
	}

	public void setN(int N) {
		this.N = N;
	}

	public void setm0(int m0) {
		this.m0 = m0;
	}

	public void setMmap(Map<Integer, Integer> Mmap) {
		this.Mmap = Mmap;
	}

	@Override
	public String getName() {
		return "Multi Barabasi-Albert Scale Free model";
	}

	@Override
	public GeneratorUI getUI() {
		return Lookup.getDefault().lookup(MultiBarabasiAlbertUI.class);
	}

	@Override
	public boolean cancel() {
		cancel = true;
		return true;
	}

	@Override
	public void setProgressTicket(ProgressTicket progressTicket) {
		this.progressTicket = progressTicket;
	}
}
