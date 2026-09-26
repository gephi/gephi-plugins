package com.elwin013.gephi.ergenerator;

import static com.elwin013.gephi.ergenerator.Defaults.DEFAULT_EDGE_CREATE_PROBABILITY;
import static com.elwin013.gephi.ergenerator.Defaults.DEFAULT_NUMBER_OF_NODES;

import java.util.Random;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDirectionDefault;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.lookup.ServiceProvider;

/**
 * Generates Erdős-Rényi G(n,p) random graph
 *
 * Constraints:
 *  - n > 0
 *  - p >= 0
 *  - p <= 1
 *
 */
@ServiceProvider(service = Generator.class)
public class ErdosRenyiGnp implements Generator {
    private int noOfNodes = DEFAULT_NUMBER_OF_NODES;
    private double edgeCreateProbability = DEFAULT_EDGE_CREATE_PROBABILITY;

    private ProgressTicket progressTicket = null;
    private boolean cancelled = false;
    private ContainerLoader containerLoader;

    public void generate(ContainerLoader containerLoader) {
        this.containerLoader = containerLoader;
        Progress.start(progressTicket, noOfNodes + noOfNodes * noOfNodes);
        containerLoader.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);

        createNodes(noOfNodes);
        createEdges(edgeCreateProbability);
    }

    @SuppressWarnings("Duplicates")
    private void createNodes(int noOfNodes) {
        for (int i = 0; i < noOfNodes && !cancelled; i++) {
            NodeDraft node = containerLoader.factory().newNodeDraft(String.valueOf(i));
            containerLoader.addNode(node);

            Progress.progress(progressTicket);

        }
    }

    private void createEdges(double edgeCreateProbability) {
        Random rnd = new Random();
        for (int i = 0; i < noOfNodes && !cancelled; i++) {
            // Undirected graph - we can start from i + 1 (no loops!)
            for (int j = i + 1; j < noOfNodes && !cancelled; j++) {
                if (rnd.nextDouble() <= edgeCreateProbability) {
                    EdgeDraft edge = containerLoader.factory().newEdgeDraft();
                    edge.setSource(containerLoader.getNode(String.valueOf(i)));
                    edge.setTarget(containerLoader.getNode(String.valueOf(j)));
                    containerLoader.addEdge(edge);
                }

                Progress.progress(progressTicket);
            }
        }
    }

    public String getName() {
        return "Erdos-Renyi G(n,p) model";
    }

    public GeneratorUI getUI() {
        return new ErdosRenyiGnpUI();
    }

    public boolean cancel() {
        cancelled = true;
        return true;
    }

    public int getNoOfNodes() {
        return noOfNodes;
    }

    public void setNoOfNodes(int noOfNodes) {
        this.noOfNodes = noOfNodes;
    }

    public double getEdgeCreateProbability() {
        return edgeCreateProbability;
    }

    public void setEdgeCreateProbability(double edgeCreateProbability) {
        this.edgeCreateProbability = edgeCreateProbability;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
