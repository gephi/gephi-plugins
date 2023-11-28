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
package complexGenerator.Grid.BalancedTree;

import org.gephi.graph.api.Node;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Generates a perfectly balanced r-tree of height h (edges are undirected).
 * <p>
 * r >= 2
 * h >= 1
 * <p>
 * O(r^h)
 *
 * @author Cezary Bartosiak
 */
@ServiceProvider(service = Generator.class)
public class Grid implements Generator {
    private boolean cancel = false;
    private ProgressTicket progressTicket;
    private int w = 3;
    private int h = 5;
    private boolean loopedW = false;
    private boolean loopedH = false;

    @Override
    public void generate(ContainerLoader container) {

        int n = ((int) Math.pow(w, h + 1) - 1) / (w - 1);

        Progress.start(progressTicket, n - 1);
        container.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);

        NodeDraft[][] nodeArray = new NodeDraft[h][w];

        int v = 1;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                var node = container.factory().newNodeDraft();
                nodeArray[j][i] = node;
                container.addNode(node);
            }
        }
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                if (loopedH) {
                    AddTopLoppedEdge(container, nodeArray, i, j);
                    AddBottomLoopedEdge(container, nodeArray, i, j);
                } else {
                    AddTopEdge(container, nodeArray, i, j);
                    AddBottomEdge(container, nodeArray, i, j);
                }
                if (loopedW) {
                    AddLeftLoopedEdge(container, nodeArray, i, j);
                    AddRightLoopedEdge(container, nodeArray, i, j);
                } else {
                    AddLeftEdge(container, nodeArray, i, j);
                    AddRightEdge(container, nodeArray, i, j);
                }
            }
        }

        Progress.finish(progressTicket);
        progressTicket = null;
    }

    private void AddRightEdge(ContainerLoader container, NodeDraft[][] nodeArray, int i, int j) {
        if (i + 1 < w) {
            var edge = container.factory().newEdgeDraft();
            edge.setSource(nodeArray[j][i]);
            edge.setTarget(nodeArray[j][i + 1]);
            container.addEdge(edge);
        }
    }

    private void AddRightLoopedEdge(ContainerLoader container, NodeDraft[][] nodeArray, int i, int j) {
        var edge = container.factory().newEdgeDraft();
        var targetI = (i + 1) % w;
        edge.setSource(nodeArray[j][i]);
        edge.setTarget(nodeArray[j][targetI]);
        container.addEdge(edge);
    }

    private void AddLeftEdge(ContainerLoader container, NodeDraft[][] nodeArray, int i, int j) {
        if (i > 0) {
            var edge = container.factory().newEdgeDraft();
            edge.setSource(nodeArray[j][i]);
            edge.setTarget(nodeArray[j][i - 1]);
            container.addEdge(edge);
        }
    }

    private void AddLeftLoopedEdge(ContainerLoader container, NodeDraft[][] nodeArray, int i, int j) {
        var edge = container.factory().newEdgeDraft();
        var targetI = (i - 1) < 0 ? w - 1 : i - 1;
        edge.setSource(nodeArray[j][i]);
        edge.setTarget(nodeArray[j][targetI]);
        container.addEdge(edge);
    }

    private void AddBottomEdge(ContainerLoader container, NodeDraft[][] nodeArray, int i, int j) {
        if (j + 1 < h) {
            var edge = container.factory().newEdgeDraft();
            edge.setSource(nodeArray[j][i]);
            edge.setTarget(nodeArray[j + 1][i]);
            container.addEdge(edge);
        }
    }

    private void AddBottomLoopedEdge(ContainerLoader container, NodeDraft[][] nodeArray, int i, int j) {
        var edge = container.factory().newEdgeDraft();
        var targetJ = (j + 1) % h;
        edge.setSource(nodeArray[j][i]);
        edge.setTarget(nodeArray[targetJ][i]);
        container.addEdge(edge);
    }

    private void AddTopEdge(ContainerLoader container, NodeDraft[][] nodeArray, int i, int j) {
        if (j > 0) {
            var edge = container.factory().newEdgeDraft();
            edge.setSource(nodeArray[j][i]);
            edge.setTarget(nodeArray[j - 1][i]);
            container.addEdge(edge);
        }
    }

    private void AddTopLoppedEdge(ContainerLoader container, NodeDraft[][] nodeArray, int i, int j) {
        var edge = container.factory().newEdgeDraft();
        var targetJ = (j - 1) < 0 ? h - 1 : j - 1;
        edge.setSource(nodeArray[j][i]);
        edge.setTarget(nodeArray[targetJ][i]);
        container.addEdge(edge);
    }

    public int getr() {
        return w;
    }

    public int geth() {
        return h;
    }

    public void setw(int w) {
        this.w = w;
    }

    public void setloopedW(boolean loopedW) {
        this.loopedW = loopedW;
    }

    public void setloopedH(boolean loopedH) {
        this.loopedH = loopedH;
    }

    public void seth(int h) {
        this.h = h;
    }

    @Override
    public String getName() {
        return "Grid";
    }

    @Override
    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(IGridUI.class);
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
