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
package complexGenerator.BarabasiAlbert.Generalized;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import java.util.stream.Collectors;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Generates an undirected not necessarily connected graph.
 *
 * http://en.wikipedia.org/wiki/Barabási–Albert_model
 * http://www.barabasilab.com/pubs/CCNR-ALB_Publications/199910-15_Science-Emergence/199910-15_Science-Emergence.pdf
 * http://www.facweb.iitkgp.ernet.in/~niloy/COURSE/Spring2006/CNT/Resource/ba-model-2.pdf
 *
 * N  > 0
 * m0 > 0
 * M  > 0 && M <= m0
 * 0 <= p < 1
 * 0 <= q < 1 - p
 *
 * Ω(N^2 * M)
 *
 * @author Cezary Bartosiak
 */

@ServiceProvider(service = Generator.class)
public class BarabasiAlbertGeneralized implements Generator {
    private boolean cancel = false;
    private ProgressTicket progressTicket;
    private int    N  = 50;
    private int    m0 = 1;
    private int    M  = 1;
    private double p  = 0.25;
    private double q  = 0.25;

    NodeDraft[] nodes;
    List<EdgeDraft> edges;

    public BarabasiAlbertGeneralized() {
        edges = new ArrayList<>();
    }

    @Override
    public void generate(ContainerLoader container) {
        Progress.start(progressTicket, N);
        Random random = new Random();
        container.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
        edges = new ArrayList<>();

        // Timestamps
        int vt = 1;
        int et = 1;
        ArrayList<Pair<NodeDraft, Integer>> nodesList = new ArrayList<>();

        // Creating m0 isolated nodes
        for (int i = 0; i < m0 && !cancel; ++i) {
            NodeDraft node = container.factory().newNodeDraft();
            node.setLabel("Node " + i);
            node.addInterval("0", N + "");
            nodesList.add(new Pair(node, 1));
            container.addNode(node);
        }

        int i = 0;
        while (i < N && !cancel) {
            double r = random.nextDouble();
            if (r <= p) { // adding M edges
                int summedDegree = nodesList.stream().map(Pair::getSecond).reduce(0, Integer::sum);  //sum of degrees for all nodes
                for (int m = 0; m < M && !cancel; ++m) {
                    double probabilityValue = random.nextDouble();
                    NodeDraft nodeSource = null;
                    NodeDraft nodeDestination = null;

                    int randomIndex = random.nextInt(nodesList.size());
                    Integer nodeValue = 0;

                    nodeSource = nodesList.get(randomIndex).getFirst();
                    summedDegree -= nodesList.get(randomIndex).getSecond();
                    int destinationIndex = -1;

                    for (Pair<NodeDraft, Integer> pair : nodesList) {
                        if (nodeDestination == null) {
                            destinationIndex++;
                        }
                        if (pair.getFirst() != nodeSource) {
                            nodeValue += pair.getSecond();
                            if (nodeValue.doubleValue()/summedDegree >= probabilityValue && nodeDestination == null) {
                                nodeDestination = pair.getFirst();
                                // chosen destination by preferential probability
                            }
                        }
                    }

                    if (nodeDestination != null && !edgeExists(container, nodeSource, nodeDestination) && nodeDestination.getId() != nodeSource.getId()) {
                        EdgeDraft edge = container.factory().newEdgeDraft();
                        edge.setSource(nodeSource);
                        edge.setTarget(nodeDestination);
                        edge.addInterval(et + "", N + "");
                        container.addEdge(edge);
                        edges.add(edge);
                        summedDegree += 2;
                        nodesList.set(randomIndex, new Pair(nodeSource, nodesList.get(randomIndex).getSecond() + 1));
                        nodesList.set(destinationIndex, new Pair(nodeDestination, nodesList.get(destinationIndex).getSecond() + 1));
                    }
                }
            } else if (r <= p + q) { // rewiring M edges
                int summedDegree = nodesList.stream().map(Pair::getSecond).reduce(0, Integer::sum);  //sum of degrees for all nodes
                double probabilityValue = random.nextDouble();
                NodeDraft nodeSource = null;
                NodeDraft nodeNewDestination = null;
                NodeDraft nodeOldDestination = null;

                int randomIndex = random.nextInt(nodesList.size());
                Integer nodeValue = 0;

                nodeSource = nodesList.get(randomIndex).getFirst();
                summedDegree -= nodesList.get(randomIndex).getSecond();

                List<Pair<NodeDraft, Integer>> connectedNodes = getNodesConnceted(container, nodesList, nodeSource);
                if (connectedNodes.size() > 0) {
                    int nodeConnectionToDeleteIndex = random.nextInt(connectedNodes.size());
                    nodeOldDestination = connectedNodes.get(nodeConnectionToDeleteIndex).getFirst();
                    int newDestinationIndex = -1;

                    for (Pair<NodeDraft, Integer> pair : nodesList) {
                        if (nodeNewDestination == null) {
                            newDestinationIndex++;
                        }
                        if (pair.getFirst() != nodeSource) {
                            nodeValue += pair.getSecond();
                            if (nodeValue.doubleValue()/summedDegree >= probabilityValue && nodeNewDestination == null) {
                                nodeNewDestination = pair.getFirst();
                                // chosen destination by preferential probability
                            }
                        }
                    }

                    if (nodeNewDestination != null && nodeOldDestination != null && !edgeExists(container, nodeSource, nodeNewDestination) && nodeNewDestination.getId() != nodeSource.getId()) {
                        var edgeToRemove = getEdge(container, nodeSource, nodeOldDestination).get();
                        container.removeEdge(edgeToRemove);
                        edges.remove(edgeToRemove);
                        connectedNodes.set(nodeConnectionToDeleteIndex, new Pair(nodeOldDestination, connectedNodes.get(nodeConnectionToDeleteIndex).getSecond() - 1));

                        EdgeDraft edge = container.factory().newEdgeDraft();
                        edge.setSource(nodeSource);
                        edge.setTarget(nodeNewDestination);
                        container.addEdge(edge);
                        edges.add(edge);
                        nodesList.set(newDestinationIndex, new Pair(nodeNewDestination, nodesList.get(newDestinationIndex).getSecond() + 1));
                    }
                }
            } else { // adding a new node with M edges
                NodeDraft node = container.factory().newNodeDraft();
                node.setLabel("Node " + nodesList.size() + 1);
                node.addInterval(vt + "", N + "");
                container.addNode(node);
                int newNodeValue = 1;

//                nodesList.add(Pair.of(node, 1));
                int summedDegree = nodesList.stream().map(Pair::getSecond).reduce(0, Integer::sum);  //sum of degrees for all nodes (except new one)
                NodeDraft nodeSource = node;
                for (int m = 0; m < M && !cancel; ++m) {
                    Integer nodeValue = 0;
                    int destinationIndex = -1;
                    double probabilityValue = random.nextDouble();
                    NodeDraft nodeDestination = null;

                    for (Pair<NodeDraft, Integer> pair : nodesList) {
                        if (nodeDestination == null) {
                            destinationIndex++;
                        }
                        if (pair.getFirst() != nodeSource) {
                            nodeValue += pair.getSecond();
                            if (nodeValue.doubleValue()/summedDegree >= probabilityValue && nodeDestination == null) {
                                nodeDestination = pair.getFirst();
                                // chosen destination by preferential probability
                            }
                        }
                    }

                    if (nodeDestination != null && !edgeExists(container, nodeSource, nodeDestination) && nodeDestination.getId() != nodeSource.getId()) {
                        EdgeDraft edge = container.factory().newEdgeDraft();
                        edge.setSource(nodeSource);
                        edge.setTarget(nodeDestination);
                        edge.addInterval(et + "", N + "");
                        container.addEdge(edge);
                        edges.add(edge);
                        summedDegree += 3;
                        newNodeValue++;
                        nodesList.set(destinationIndex, new Pair(nodeDestination, nodesList.get(destinationIndex).getSecond() + 1));
                    }
                }
                nodesList.add(new Pair(node, newNodeValue));
                i++;
                Progress.progress(progressTicket);
            }
        }
        Progress.finish(progressTicket);
        progressTicket = null;
    }

    private List<Pair<NodeDraft, Integer>> getNodesConnceted(ContainerLoader container, ArrayList<Pair<NodeDraft, Integer>> nodesList, NodeDraft nodeSource) {
        return nodesList.stream().filter(nodeDestination -> edgeExists(container, nodeSource, nodeDestination.getFirst())).collect(Collectors.toList());
    }

    private boolean edgeExists(ContainerLoader container, NodeDraft sourceNode, NodeDraft tergetNode) {
        return edges.stream().anyMatch(x -> (x.getSource() == sourceNode && x.getTarget() == tergetNode) || (x.getSource() == tergetNode && x.getTarget() == sourceNode));
    }

    private Optional<EdgeDraft> getEdge(ContainerLoader container, NodeDraft sourceNode, NodeDraft tergetNode) {
        return edges.stream().filter(x -> (x.getSource() == sourceNode && x.getTarget() == tergetNode) || (x.getSource() == tergetNode && x.getTarget() == sourceNode) )
                .findFirst();
    }

    public int getN() {
        return N;
    }

    public int getm0() {
        return m0;
    }

    public int getM() {
        return M;
    }

    public double getp() {
        return p;
    }

    public double getq() {
        return q;
    }

    public void setN(int N) {
        this.N = N;
    }

    public void setm0(int m0) {
        this.m0 = m0;
    }

    public void setM(int M) {
        this.M = M;
    }

    public void setp(double p) {
        this.p = p;
    }

    public void setq(double q) {
        this.q = q;
    }

    @Override
    public String getName() {
        return "Generalized Barabasi-Albert Scale Free model";
    }

    @Override
    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(IBarabasiAlbertGeneralizedUI.class);
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
