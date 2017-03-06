/*
 * The MIT License
 *
 * Copyright 2017 LEVALLOIS.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.clementlevallois.graphgenerator;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Multiset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.clementlevallois.utils.Clock;
import net.clementlevallois.model.Pair;
import net.clementlevallois.model.PairWithWeight;
import no.uib.cipr.matrix.MatrixEntry;
import no.uib.cipr.matrix.sparse.FlexCompColMatrix;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.io.importer.api.EdgeDirection;
import org.gephi.io.importer.api.EdgeDraft;
import org.gephi.io.importer.api.NodeDraft;

/**
 *
 * @author LEVALLOIS
 */
public class GraphOperations {

    public void createGraph(ContainerLoader container, Map<String, Map<String, Multiset<String>>> datastruct, ImmutableBiMap<String, Integer> dataPreparation, List<FlexCompColMatrix> similarityMatrices) {
        
        NodeDraft node;

        Clock clock = new Clock("adding nodes to Gephi");

        //add nodes
        for (String nodeName : datastruct.keySet()) {
            node = container.factory().newNodeDraft(nodeName);
            node.setLabel(nodeName);
            container.addNode(node);
        }

        clock.closeAndPrintClock();

        //add edges
        Integer idEdge = 0;
        EdgeDraft edge;
        Iterator<MatrixEntry> itSM;

        Map<String, PairWithWeight> edges = new HashMap();

        Logger.getLogger("").log(Level.INFO, "converting similarity matrices into a graph");

        int i = 1;

        for (FlexCompColMatrix simMatrix : similarityMatrices) {

            Logger.getLogger("").log(Level.INFO, "converting matrix {0}", i++);

            itSM = simMatrix.iterator();
            MatrixEntry currElement;
            clock = new Clock("iterating on the cells");

            ImmutableBiMap<Integer, String> inverse = dataPreparation.inverse();

            while (itSM.hasNext()) {

                currElement = itSM.next();
                double csCoeff = currElement.get();
                if (csCoeff <= 0) {
                    continue;
                }
                if (currElement.column() == currElement.row()) {
                    continue;
                }

                String source = inverse.get((int) currElement.column());
                String target = inverse.get((int) currElement.row());

                //complex stuff just to add the weights of multiple edges between nodes (because they are similar on multiple attributes)
                Pair pair;
                if (source.compareTo(target) < 0) {
                    pair = new Pair(source, target);
                } else {
                    pair = new Pair(target, source);
                }
                PairWithWeight pww = new PairWithWeight();
                pww.setPair(pair);
                pww.setWeight(csCoeff);
                String pwwId = pww.getPair().getId();
                if (edges.keySet().contains(pwwId)) {
                    PairWithWeight get = edges.remove(pwwId);
                    get.setWeight(get.getWeight() + csCoeff);
                    edges.put(pwwId, get);
                } else {
                    edges.put(pwwId, pww);
                }

            }
            clock.closeAndPrintClock();
        }

        Logger.getLogger("").log(Level.INFO, "edges computed for all matrices");

        clock = new Clock("Adding edges to Gephi");

        //now we can add all these edges
        for (String pwwId : edges.keySet()) {

            PairWithWeight pww = edges.get(pwwId);

            edge = container.factory().newEdgeDraft(String.valueOf(idEdge));
            idEdge = idEdge + 1;
            edge.setSource(container.getNode((String) pww.getPair().getLeft()));
            edge.setTarget(container.getNode((String) pww.getPair().getRight()));
            edge.setWeight((float) pww.getWeight());
            edge.setDirection(EdgeDirection.UNDIRECTED);
            container.addEdge(edge);
        }

        clock.closeAndPrintClock();

    }

}
