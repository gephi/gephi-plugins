package complexGenerator.ABCD;

import complexGenerator.BarabasiAlbert.Utils.Pair;
import lombok.Getter;
import lombok.Setter;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// @author Tobiasz Waszkowiak

@ServiceProvider(service = Generator.class)
public class ABCD implements Generator {
    private boolean cancel = false;
    private ProgressTicket progressTicket;

    @Getter
    @Setter
    private Integer n = 1000;
    @Getter
    @Setter
    private Double Imax = 1000.0;

    //    Communities
    @Getter
    @Setter
    private Integer cmin = 10;
    @Getter
    @Setter
    private Integer cmax = 100;
    @Getter
    @Setter
    private Double beta = 3.5;

    //    Nodes
    @Getter
    @Setter
    private Integer wmin = 2; // Minimum degree
    @Getter
    @Setter
    private Integer wmax = 10; // Maximum degree
    @Getter
    @Setter
    private Double gamma = 2.0; // Degree power law exponent

    //    Connecting
    @Getter
    @Setter
    private Double xi = 0.1;

    @Override
    public void generate(ContainerLoader container) {
        Progress.start(progressTicket, n - 1);
        container.setEdgeDefault(EdgeDirectionDefault.UNDIRECTED);
        List<Integer> W = generateDegreeSequence();
        List<Integer> C = generateCommunitySizes();
        List<List<Integer>> assigned = assignNodesToCommunities(W, C);
        Set<Pair<Integer, Integer>> generated = generateABCDGraph(assigned, W, container);
        Progress.finish(progressTicket);
        progressTicket = null;
    }

    public Set<Pair<Integer, Integer>> generateABCDGraph(List<List<Integer>> communityAssignments, List<Integer> W, ContainerLoader container) {
        Set<Pair<Integer, Integer>> edges = new HashSet<>();
        Map<Integer, NodeDraft> nodes = new HashMap<>();
        int totalDegree = W.stream().mapToInt(Integer::intValue).sum();

        // Create nodes
        for (int i = 0; i < W.size(); i++) {
            NodeDraft node = container.factory().newNodeDraft();
            node.setLabel("Node " + i);
            node.addInterval("0", W.size() + "");
            container.addNode(node);
            nodes.put(i, node);
        }

        // Internal edges within communities
        for (List<Integer> community : communityAssignments) {
            int Wi = community.stream().mapToInt(W::get).sum();
            int internalEdges = (int) ((1 - xi) * Wi / 2);
            edges.addAll(sampleInternalEdges(community, W, internalEdges, nodes, container));
        }

        // External edges outside communities
        for (List<Integer> community : communityAssignments) {
            int Wi = community.stream().mapToInt(W::get).sum();
            int externalEdges = (int) (xi * Wi / 2);

            edges.addAll(sampleExternalEdges(edges, community, W, externalEdges, nodes, container));
        }
        return edges;
    }

    private Set<Pair<Integer, Integer>> sampleInternalEdges(List<Integer> community,
                                                            List<Integer> W,
                                                            int edgeCount,
                                                            Map<Integer, NodeDraft> nodes,
                                                            ContainerLoader container) {
        Random rand = new Random();
        Set<Pair<Integer, Integer>> newEdges = new HashSet<>();

        int iterator = 0;
        while (newEdges.size() < edgeCount && iterator < 10000) {
            int nodeIdA = pickRandomNode(community, W, rand, newEdges);
            int nodeIdB = pickRandomNode2(community, W, rand, newEdges);

            if (nodeIdA != nodeIdB
                    && newEdges.stream().noneMatch(edge -> edge.getFirst() == nodeIdA && edge.getSecond() == nodeIdB)
                    && newEdges.stream().noneMatch(edge -> edge.getFirst() == nodeIdB && edge.getSecond() == nodeIdA)) {
                NodeDraft nodeA = nodes.get(nodeIdA);
                NodeDraft nodeB = nodes.get(nodeIdB);
                EdgeDraft edge = container.factory().newEdgeDraft();

                if (nodeIdA < nodeIdB) {
                    edge.setSource(nodeA);
                    edge.setTarget(nodeB);
                } else {
                    edge.setSource(nodeB);
                    edge.setTarget(nodeA);
                }
                edge.addInterval("0", W.size() + "");
                container.addEdge(edge);

                newEdges.add(new Pair<>(Math.min(nodeIdA, nodeIdB), Math.max(nodeIdA, nodeIdB)));
            }
            iterator++;
        }
        return newEdges;
    }

    private Set<Pair<Integer, Integer>> sampleExternalEdges(Set<Pair<Integer, Integer>> allEdges,
                                                            List<Integer> community,
                                                            List<Integer> W,
                                                            int externalEdges,
                                                            Map<Integer, NodeDraft> nodes,
                                                            ContainerLoader container) {
        Random rand = new Random();
        Set<Pair<Integer, Integer>> newEdges = new HashSet<>();

        int iterator = 0;
        while (newEdges.size() < externalEdges && iterator < 10000) {
            int nodeIdA = pickRandomInternalNode(community, W, rand, allEdges);
            int nodeIdB = pickRandomExternalNode(community, W, rand, allEdges);

            if (nodeIdA != nodeIdB
                    && allEdges.stream().noneMatch(edge -> edge.getFirst() == nodeIdA && edge.getSecond() == nodeIdB)
                    && allEdges.stream().noneMatch(edge -> edge.getFirst() == nodeIdB && edge.getSecond() == nodeIdA)) {
                NodeDraft nodeA = nodes.get(nodeIdA);
                NodeDraft nodeB = nodes.get(nodeIdB);
                EdgeDraft edge = container.factory().newEdgeDraft();

                if (nodeIdA < nodeIdB) {
                    edge.setSource(nodeA);
                    edge.setTarget(nodeB);
                } else {
                    edge.setSource(nodeB);
                    edge.setTarget(nodeA);
                }
                edge.addInterval("0", W.size() + "");
                container.addEdge(edge);
                newEdges.add(new Pair<>(Math.min(nodeIdA, nodeIdB), Math.max(nodeIdA, nodeIdB)));
                allEdges.add(new Pair<>(Math.min(nodeIdA, nodeIdB), Math.max(nodeIdA, nodeIdB)));
            }
            iterator++;
        }
        return newEdges;
    }

    private int pickRandomExternalNode(List<Integer> communityNodesIndexes,
                                       List<Integer> wNodes,
                                       Random rand,
                                       Set<Pair<Integer, Integer>> allEdges) {
        List<Integer> filteredNodesIndexes = IntStream.range(0, wNodes.size())
                .boxed()
                .filter(index -> !communityNodesIndexes.contains(index))
                .filter(index -> wNodes.get(index) >= getNodeDegree(index, allEdges))
                .collect(Collectors.toList());

        if (!filteredNodesIndexes.isEmpty()) {
            return filteredNodesIndexes.get(rand.nextInt(filteredNodesIndexes.size()));
        }
        return wNodes.get(rand.nextInt(wNodes.size()));
    }

    private int pickRandomInternalNode(List<Integer> community,
                                       List<Integer> W,
                                       Random rand,
                                       Set<Pair<Integer, Integer>> allEdges) {
        List<Integer> edges = community.stream().filter(edge -> W.get(edge) >= getNodeDegree(edge, allEdges)).collect(Collectors.toList());
        if (!edges.isEmpty()) {
            return edges.get(rand.nextInt(edges.size()));
        }
        return community.get(rand.nextInt(community.size()));
    }

    private int pickRandomNode(List<Integer> community,
                               List<Integer> W,
                               Random rand,
                               Set<Pair<Integer, Integer>> existingEdges) {
        List<Integer> edgesWithNoConnection = community.stream().filter(edge -> !existingEdges.stream().anyMatch(existing -> existing.getFirst() == edge || existing.getSecond() == edge)).collect(Collectors.toList());
        List<Integer> edges = community.stream().filter(edge -> W.get(edge) >= getNodeDegree(edge, existingEdges)).collect(Collectors.toList());
        if (!edgesWithNoConnection.isEmpty()) {
            return edgesWithNoConnection.get(rand.nextInt(edgesWithNoConnection.size()));
        } else if (!edges.isEmpty()) {
            return edges.get(rand.nextInt(edges.size()));
        }
        return community.get(rand.nextInt(community.size()));
    }

    private int pickRandomNode2(List<Integer> community,
                                List<Integer> W,
                                Random rand,
                                Set<Pair<Integer, Integer>> existingEdges) {
        List<Integer> edges = community.stream().filter(edge -> W.get(edge) >= getNodeDegree(edge, existingEdges)).collect(Collectors.toList());
        if (!edges.isEmpty()) {
            return edges.get(rand.nextInt(edges.size()));
        }
        return community.get(rand.nextInt(community.size()));
    }

    private Long getNodeDegree(Integer edge, Set<Pair<Integer, Integer>> existingEdges) {
        return existingEdges.stream().filter(pair -> pair.getFirst() == edge || pair.getSecond() == edge).count();
    }


    //    Connecting
    public List<List<Integer>> assignNodesToCommunities(List<Integer> W, List<Integer> C) {
        Collections.sort(W, Collections.reverseOrder());
        Collections.sort(C, Collections.reverseOrder());

        List<Integer> freeSpots = new ArrayList<>(C);
        List<List<Integer>> communityAssignments = new ArrayList<>();
        for (int i = 0; i < C.size(); i++) {
            communityAssignments.add(new ArrayList<>());
        }

        for (int i = 0; i < W.size(); i++) {
            int nodeDegree = W.get(i);
            List<Integer> positions = getPositions(freeSpots, nodeDegree);
            int communityIndex = pickRandomCommunity(positions, freeSpots);
            communityAssignments.get(communityIndex).add(i);
            freeSpots.set(communityIndex, freeSpots.get(communityIndex) - 1);
        }

        return communityAssignments;
    }

    public List<Integer> getPositions(List<Integer> freeSpots, int nodeDegree) {
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < freeSpots.size(); i++) {
            if (freeSpots.get(i) > nodeDegree) {
                positions.add(i);
            }
        }
        return positions;
    }

    private int pickRandomCommunity(List<Integer> positions, List<Integer> freeSpots) {
        if (!positions.isEmpty()) {
            Random rand = new Random();
            Integer randomPosition = rand.nextInt(positions.size());
            return positions.get(randomPosition);
        }
        int highestValue = freeSpots.get(0);
        int position = 0;

        for (int i = 1; i < freeSpots.size(); i++) {
            if (freeSpots.get(i) > highestValue) {
                highestValue = freeSpots.get(i);
                position = i;
            }
        }

        return position;
    }

    //    Nodes
    public List<Integer> generateDegreeSequence() {
        List<Integer> W = new ArrayList<>();
        int I = 0;

        while (I < Imax) {
            W.clear();
            for (int i = 0; i < n; i++) {
                int w = samplePowerLaw(gamma, wmin, wmax);
                W.add(w);
            }

            if (isSumEven(W)) {
                return W;
            }

            I++;
        }

        adjustDegreeSequence(W);
        return W;
    }

    private boolean isSumEven(List<Integer> W) {
        int sum = W.stream().mapToInt(Integer::intValue).sum();
        return sum % 2 == 0;
    }

    private void adjustDegreeSequence(List<Integer> W) {
        Collections.sort(W, Collections.reverseOrder());
        int index = 0;
        while (!isSumEven(W)) {
            if (W.get(index) > 0) {
                W.set(index, W.get(index) - 1);
            }
            index = (index + 1) % W.size();
        }
    }

    //    Communities
    public List<Integer> generateCommunitySizes() {
        int sbest = Integer.MAX_VALUE;
        List<Integer> Sbest = new ArrayList<>();
        int I = 0;

        while (I < Imax) {
            int s = 0;
            List<Integer> X = new ArrayList<>();
            while (s < n) {
                int x = samplePowerLaw(beta, cmin, cmax);
                X.add(x);
                s += x;
            }

            if (s == n) {
                return X;
            } else {
                if (s < sbest) {
                    sbest = s;
                    Sbest = new ArrayList<>(X);
                }
            }
            I++;
        }

        adjustCommunitySizes(Sbest, sbest, n, cmin, cmax);
        return Sbest;
    }

    private int samplePowerLaw(double beta, int cmin, int cmax) {
        return (int) (Math.pow(Math.random(), beta) * (cmax - cmin) + cmin);
    }

    private void adjustCommunitySizes(List<Integer> Sbest, int sbest, int n, int cmin, int cmax) {
        while (sbest != n) {
            Collections.shuffle(Sbest);
            for (int i = 0; i < Sbest.size(); i++) {
                if (sbest > n && Sbest.get(i) > cmin) {
                    Sbest.set(i, Sbest.get(i) - 1);
                    sbest--;
                } else if (sbest < n && Sbest.get(i) < cmax) {
                    Sbest.set(i, Sbest.get(i) + 1);
                    sbest++;
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Artificial Benchmark for Community Detecion";
    }

    @Override
    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(IABCDUI.class);
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

    class Edge {
        int nodeA;
        int nodeB;

        public Edge(int nodeA, int nodeB) {
            this.nodeA = nodeA;
            this.nodeB = nodeB;
        }

        public boolean isValid() {
            return nodeA != nodeB;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Edge)) return false;
            Edge edge = (Edge) o;
            return (nodeA == edge.nodeA && nodeB == edge.nodeB) || (nodeA == edge.nodeB && nodeB == edge.nodeA);
        }

        @Override
        public int hashCode() {
            return nodeA < nodeB ? nodeA * 31 + nodeB : nodeB * 31 + nodeA;
        }

        @Override
        public String toString() {
            return "(" + nodeA + ", " + nodeB + ")";
        }
    }
}
