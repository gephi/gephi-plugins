package com.elwin013.gephi.ergenerator;

import static com.elwin013.gephi.ergenerator.Defaults.DEFAULT_NUMBER_OF_EDGES;
import static com.elwin013.gephi.ergenerator.Defaults.DEFAULT_NUMBER_OF_NODES;

import java.util.ArrayList;
import java.util.List;
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
 * Generates Erdős-Rényi G(n,m) random graph
 *
 * Constraints:
 *  - n > 0
 *  - m >= 0
 *  - m < n (n - 1) / 2
 *
 */
@ServiceProvider(service = Generator.class)
public class ErdosRenyiGnm implements Generator {
    private int noOfNodes = DEFAULT_NUMBER_OF_NODES;
    private int noOfEdges = DEFAULT_NUMBER_OF_EDGES;

    private ProgressTicket progressTicket = null;
    private boolean cancelled = false;
    private ContainerLoader containerLoader;

    public void generate(ContainerLoader containerLoader) {
        this.containerLoader = containerLoader;
        Progress.start(progressTicket, noOfNodes + noOfEdges + noOfEdges);
        containerLoader.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);

        createNodes(noOfNodes);
        createEdges(noOfNodes, noOfEdges);
    }

    @SuppressWarnings("Duplicates")
    private void createNodes(int noOfNodes) {
        for (int i = 0; i < noOfNodes && !cancelled; i++) {
            NodeDraft node = containerLoader.factory().newNodeDraft(String.valueOf(i));
            containerLoader.addNode(node);

            Progress.progress(progressTicket);

        }
    }

    private void createEdges(int noOfNodes, int noOfEdges) {
        Random rnd = new Random();
        int i = 0;
        while (i < noOfEdges && !cancelled) {
            int source = rnd.nextInt(noOfNodes);
            int target = rnd.nextInt(noOfNodes);

            if (source != target && !containerLoader.edgeExists(Integer.toString(source), Integer.toString(target))) {
                EdgeDraft edgeDraft = containerLoader.factory().newEdgeDraft();
                edgeDraft.setSource(containerLoader.getNode(Integer.toString(source)));
                edgeDraft.setTarget(containerLoader.getNode(Integer.toString(target)));
                containerLoader.addEdge(edgeDraft);
                Progress.progress(progressTicket);
                i++;
            }
        }
    }

    public String getName() {
        return "Erdos-Renyi G(n,m) model";
    }

    public GeneratorUI getUI() {
        return new ErdosRenyiGnmUI();
    }

    @Override
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

    public int getNoOfEdges() {
        return noOfEdges;
    }

    public void setNoOfEdges(int noOfEdges) {
        this.noOfEdges = noOfEdges;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
