package myrberger.communityclusters;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.Test;

public class LeidenTest {

    /** Random graph with planted communities of equal size. */
    private static final class Planted {
        final int n;
        final int[] truth;
        final int[] src;
        final int[] dst;

        Planted(int groups, int perGroup, double pIn, double pOut, long seed) {
            n = groups * perGroup;
            truth = new int[n];
            Random r = new Random(seed);
            List<int[]> edges = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                truth[i] = i / perGroup;
            }
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (r.nextDouble() < (truth[i] == truth[j] ? pIn : pOut)) {
                        edges.add(new int[]{i, j});
                    }
                }
            }
            src = new int[edges.size()];
            dst = new int[edges.size()];
            for (int i = 0; i < src.length; i++) {
                src[i] = edges.get(i)[0];
                dst[i] = edges.get(i)[1];
            }
        }
    }

    /** Newman modularity of a partition of an unweighted graph. */
    private static double modularity(int[] src, int[] dst, int[] comm) {
        double m = src.length;
        Map<Integer, Double> inside = new HashMap<>();
        Map<Integer, Double> total = new HashMap<>();
        for (int i = 0; i < src.length; i++) {
            if (comm[src[i]] == comm[dst[i]]) {
                inside.merge(comm[src[i]], 1.0, Double::sum);
            }
            total.merge(comm[src[i]], 1.0, Double::sum);
            total.merge(comm[dst[i]], 1.0, Double::sum);
        }
        double q = 0;
        for (Map.Entry<Integer, Double> e : total.entrySet()) {
            double share = e.getValue() / (2 * m);
            q += inside.getOrDefault(e.getKey(), 0.0) / m - share * share;
        }
        return q;
    }

    private static int clusterCount(int[] comm) {
        return Arrays.stream(comm).max().getAsInt() + 1;
    }

    @Test
    public void recoversPlantedCommunities() {
        Planted g = new Planted(5, 40, 0.15, 0.005, 3);
        int[] found = Leiden.run(g.n, g.src, g.dst, null, 1.0, 42);
        assertEquals("number of clusters", 5, clusterCount(found));
        assertTrue("modularity at least that of the planted partition",
                modularity(g.src, g.dst, found) >= modularity(g.src, g.dst, g.truth) - 0.005);
        // every planted group should be dominated by a single detected cluster
        int agree = 0;
        for (int group = 0; group < 5; group++) {
            Map<Integer, Integer> counts = new HashMap<>();
            for (int i = 0; i < g.n; i++) {
                if (g.truth[i] == group) {
                    counts.merge(found[i], 1, Integer::sum);
                }
            }
            agree += counts.values().stream().max(Integer::compare).get();
        }
        assertTrue("purity above 90%", agree > 0.9 * g.n);
    }

    @Test
    public void resultIsLocallyOptimal() {
        Planted g = new Planted(10, 30, 0.15, 0.005, 3);
        int[] found = Leiden.run(g.n, g.src, g.dst, null, 1.0, 1);
        double q = modularity(g.src, g.dst, found);
        int k = clusterCount(found);
        for (int v = 0; v < g.n; v++) {
            for (int c = 0; c <= k; c++) {
                if (c == found[v]) {
                    continue;
                }
                int[] moved = found.clone();
                moved[v] = c;
                assertTrue("moving node " + v + " to " + c + " must not improve modularity",
                        modularity(g.src, g.dst, moved) <= q + 1e-12);
            }
        }
    }

    @Test
    public void sameSeedGivesSameResult() {
        Planted g = new Planted(5, 40, 0.15, 0.005, 3);
        assertArrayEquals("same seed", Leiden.run(g.n, g.src, g.dst, null, 1.0, 7),
                Leiden.run(g.n, g.src, g.dst, null, 1.0, 7));
    }

    @Test
    public void higherResolutionGivesMoreClusters() {
        Planted g = new Planted(8, 40, 0.15, 0.01, 5);
        int low = clusterCount(Leiden.run(g.n, g.src, g.dst, null, 0.2, 1));
        int high = clusterCount(Leiden.run(g.n, g.src, g.dst, null, 4.0, 1));
        assertTrue("resolution 4 (" + high + " clusters) should split more than resolution 0.2 (" + low + ")",
                high > low);
    }

    @Test
    public void nodesWithoutEdgesAreSingletons() {
        assertArrayEquals("no edges", new int[]{0, 1, 2, 3}, Leiden.run(4, new int[0], new int[0], null, 1, 1));
        assertEquals("empty graph", 0, Leiden.run(0, new int[0], new int[0], null, 1, 1).length);
    }

    @Test
    public void twoTrianglesWithABridgeSplitInTwo() {
        int[] src = {0, 1, 2, 3, 4, 5, 2};
        int[] dst = {1, 2, 0, 4, 5, 3, 3};
        assertArrayEquals("triangles", new int[]{0, 0, 0, 1, 1, 1}, Leiden.run(6, src, dst, null, 1, 1));
    }

    @Test
    public void weightsAreUsedAndSelfLoopsAndNegativeWeightsIgnored() {
        // 0-1 doubled, 2-3 strong, weak 1-2 bridge, a self loop and a negative edge
        int[] src = {0, 0, 1, 2, 2, 3};
        int[] dst = {1, 1, 2, 3, 2, 2};
        double[] w = {1, 1, 0.1, 1, 5, -1};
        assertArrayEquals("weighted", new int[]{0, 0, 1, 1}, Leiden.run(4, src, dst, w, 1, 1));
    }
}
