package myrberger.communityclusters;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Leiden community detection (Traag, Waltman &amp; van Eck 2019) optimising modularity with a
 * resolution parameter, on an undirected weighted graph. Pure Java, no Gephi dependency.
 */
final class Leiden {

    private static final double THETA = 0.01; // randomness of the refinement step
    private static final double EPS = 1e-12;

    private Leiden() {
    }

    /** Compact CSR graph; k[v] is the total strength of v in the ORIGINAL graph. */
    private static final class G {
        int n;
        int[] start;
        int[] adj;
        double[] wt;
        double[] k;
    }

    /**
     * @return community index (0..c-1) for each of the n nodes. Parallel edges and both directions are summed;
     * self loops and non-positive weights are ignored.
     */
    static int[] run(int n, int[] es, int[] ed, double[] ew, double resolution, long seed) {
        int[] result = new int[n];
        if (n == 0) {
            return result;
        }
        List<Map<Integer, Double>> maps = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            maps.add(new HashMap<>());
        }
        double[] k = new double[n];
        for (int i = 0; i < es.length; i++) {
            int a = es[i], b = ed[i];
            double w = ew == null ? 1.0 : ew[i];
            if (a == b || !(w > 0)) {
                continue;
            }
            maps.get(a).merge(b, w, Double::sum);
            maps.get(b).merge(a, w, Double::sum);
            k[a] += w;
            k[b] += w;
        }
        double twoM = 0;
        for (double v : k) {
            twoM += v;
        }
        if (twoM == 0) {
            for (int i = 0; i < n; i++) {
                result[i] = i;
            }
            return result;
        }

        Random rng = new Random(seed);
        G g0 = build(maps, k);
        int[] part = new int[n];
        for (int i = 0; i < n; i++) {
            part[i] = i;
        }
        // Leiden is iterated: each pass starts from the previous partition, until nothing changes
        for (int round = 0; round < 10; round++) {
            int[] next = pass(g0, part, resolution, twoM, rng);
            renumber(next, new int[n]);
            boolean same = Arrays.equals(part, next);
            part = next;
            if (same) {
                break;
            }
        }
        return part;
    }

    /** One multilevel pass (move, refine, aggregate) starting from the given partition of the original nodes. */
    private static int[] pass(G g0, int[] init, double gamma, double twoM, Random rng) {
        int n = g0.n;
        G g = g0;
        int[] nodeOf = new int[n];
        int[] comm = init.clone();
        for (int i = 0; i < n; i++) {
            nodeOf[i] = i;
        }

        for (int iter = 0; iter < 100; iter++) {
            moveNodes(g, comm, gamma, twoM, rng);
            int distinct = countDistinct(comm, g.n);
            if (distinct == g.n) {
                break;
            }
            int[] ref = refine(g, comm, gamma, twoM, rng);
            int[] map = new int[g.n];
            int m = renumber(ref, map);
            boolean byComm = false;
            if (m == g.n) {
                // refinement merged nothing: aggregate by the communities themselves
                byComm = true;
                map = new int[g.n];
                m = renumber(comm.clone(), map);
            }
            int[] newComm = new int[m];
            for (int v = 0; v < g.n; v++) {
                newComm[map[v]] = byComm ? map[v] : comm[v];
            }
            g = aggregate(g, map, m);
            for (int v = 0; v < n; v++) {
                nodeOf[v] = map[nodeOf[v]];
            }
            comm = compactIds(newComm, m);
        }

        int[] out = new int[n];
        for (int v = 0; v < n; v++) {
            out[v] = comm[nodeOf[v]];
        }
        return out;
    }

    // ---- graph construction ---------------------------------------------------------------------

    private static G build(List<Map<Integer, Double>> maps, double[] k) {
        G g = new G();
        g.n = maps.size();
        g.k = k;
        g.start = new int[g.n + 1];
        for (int i = 0; i < g.n; i++) {
            g.start[i + 1] = g.start[i] + maps.get(i).size();
        }
        g.adj = new int[g.start[g.n]];
        g.wt = new double[g.start[g.n]];
        for (int i = 0; i < g.n; i++) {
            int p = g.start[i];
            for (Map.Entry<Integer, Double> e : maps.get(i).entrySet()) {
                g.adj[p] = e.getKey();
                g.wt[p] = e.getValue();
                p++;
            }
        }
        return g;
    }

    /** Collapses nodes by map[v] -> 0..m-1. Intra-group weight is dropped (it never affects a move). */
    private static G aggregate(G g, int[] map, int m) {
        List<Map<Integer, Double>> maps = new ArrayList<>(m);
        for (int i = 0; i < m; i++) {
            maps.add(new HashMap<>());
        }
        double[] k = new double[m];
        for (int v = 0; v < g.n; v++) {
            int a = map[v];
            k[a] += g.k[v];
            for (int p = g.start[v]; p < g.start[v + 1]; p++) {
                int b = map[g.adj[p]];
                if (a != b) {
                    maps.get(a).merge(b, g.wt[p], Double::sum);
                }
            }
        }
        return build(maps, k);
    }

    // ---- helpers ----------------------------------------------------------------------------------

    private static int countDistinct(int[] comm, int n) {
        boolean[] seen = new boolean[n];
        int c = 0;
        for (int i = 0; i < n; i++) {
            if (!seen[comm[i]]) {
                seen[comm[i]] = true;
                c++;
            }
        }
        return c;
    }

    /** Renumbers ids in place to 0..m-1 in order of first appearance; map[oldNodeIndex] = new id. */
    private static int renumber(int[] ids, int[] map) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            Integer id = seen.get(ids[i]);
            if (id == null) {
                id = seen.size();
                seen.put(ids[i], id);
            }
            ids[i] = id;
            if (i < map.length) {
                map[i] = id;
            }
        }
        return seen.size();
    }

    private static int[] compactIds(int[] ids, int m) {
        int[] out = ids.clone();
        renumber(out, new int[0]);
        return out;
    }

    private static void shuffle(int[] a, Random rng) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int t = a[i];
            a[i] = a[j];
            a[j] = t;
        }
    }

    // ---- phase 1: fast local moving -------------------------------------------------------------

    private static void moveNodes(G g, int[] comm, double gamma, double twoM, Random rng) {
        int n = g.n;
        double[] K = new double[n];
        int[] cnt = new int[n];
        for (int v = 0; v < n; v++) {
            K[comm[v]] += g.k[v];
            cnt[comm[v]]++;
        }
        int[] free = new int[n];
        int nFree = 0;
        for (int c = 0; c < n; c++) {
            if (cnt[c] == 0) {
                free[nFree++] = c;
            }
        }
        int[] order = new int[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        shuffle(order, rng);
        ArrayDeque<Integer> queue = new ArrayDeque<>(n);
        boolean[] inQ = new boolean[n];
        for (int v : order) {
            queue.add(v);
            inQ[v] = true;
        }
        double[] wTo = new double[n];
        int[] stamp = new int[n];
        int[] touched = new int[n];
        int curStamp = 0;

        while (!queue.isEmpty()) {
            int v = queue.poll();
            inQ[v] = false;
            int old = comm[v];
            K[old] -= g.k[v];
            cnt[old]--;

            curStamp++;
            int nt = 0;
            for (int p = g.start[v]; p < g.start[v + 1]; p++) {
                int c = comm[g.adj[p]];
                if (stamp[c] != curStamp) {
                    stamp[c] = curStamp;
                    wTo[c] = 0;
                    touched[nt++] = c;
                }
                wTo[c] += g.wt[p];
            }
            double kv = g.k[v];
            double wOld = stamp[old] == curStamp ? wTo[old] : 0;
            int best = old;
            double bestGain = wOld - gamma * kv * K[old] / twoM;
            for (int t = 0; t < nt; t++) {
                int c = touched[t];
                if (c == old) {
                    continue;
                }
                double gain = wTo[c] - gamma * kv * K[c] / twoM;
                if (gain > bestGain + EPS) {
                    bestGain = gain;
                    best = c;
                }
            }
            if (bestGain < -EPS) {
                // being alone beats every option (gain 0)
                if (cnt[old] == 0) {
                    best = old;
                } else {
                    best = free[--nFree];
                }
            }
            if (best != old && cnt[old] == 0) {
                free[nFree++] = old;
            }
            comm[v] = best;
            K[best] += kv;
            cnt[best]++;
            if (best != old) {
                for (int p = g.start[v]; p < g.start[v + 1]; p++) {
                    int u = g.adj[p];
                    if (comm[u] != best && !inQ[u]) {
                        inQ[u] = true;
                        queue.add(u);
                    }
                }
            }
        }
    }

    // ---- phase 2: refinement ----------------------------------------------------------------------

    private static int[] refine(G g, int[] comm, double gamma, double twoM, Random rng) {
        int n = g.n;
        int[] ref = new int[n];
        double[] Kref = new double[n];
        int[] size = new int[n];
        double[] ext = new double[n];
        double[] Kc = new double[n];
        for (int v = 0; v < n; v++) {
            ref[v] = v;
            Kref[v] = g.k[v];
            size[v] = 1;
            Kc[comm[v]] += g.k[v];
            for (int p = g.start[v]; p < g.start[v + 1]; p++) {
                if (comm[g.adj[p]] == comm[v]) {
                    ext[v] += g.wt[p];
                }
            }
        }
        int[] order = new int[n];
        for (int i = 0; i < n; i++) {
            order[i] = i;
        }
        shuffle(order, rng);

        double[] wTo = new double[n];
        int[] stamp = new int[n];
        int[] touched = new int[n];
        double[] gains = new double[n + 1];
        int[] cand = new int[n + 1];
        int curStamp = 0;

        for (int v : order) {
            if (size[ref[v]] != 1) {
                continue; // only singletons may move
            }
            int c = comm[v];
            double kv = g.k[v];
            // v must be well connected to the rest of its community
            if (ext[v] < gamma * kv * (Kc[c] - kv) / twoM - EPS) {
                continue;
            }
            curStamp++;
            int nt = 0;
            for (int p = g.start[v]; p < g.start[v + 1]; p++) {
                int u = g.adj[p];
                if (comm[u] != c) {
                    continue;
                }
                int d = ref[u];
                if (stamp[d] != curStamp) {
                    stamp[d] = curStamp;
                    wTo[d] = 0;
                    touched[nt++] = d;
                }
                wTo[d] += g.wt[p];
            }
            int nc = 0;
            double maxGain = 0;
            cand[nc] = -1; // stay as a singleton
            gains[nc++] = 0;
            for (int t = 0; t < nt; t++) {
                int d = touched[t];
                if (d == ref[v]) {
                    continue;
                }
                if (ext[d] < gamma * Kref[d] * (Kc[c] - Kref[d]) / twoM - EPS) {
                    continue; // D must itself be well connected
                }
                double gain = wTo[d] - gamma * kv * Kref[d] / twoM;
                if (gain >= 0) {
                    cand[nc] = d;
                    gains[nc++] = gain;
                    maxGain = Math.max(maxGain, gain);
                }
            }
            if (nc == 1) {
                continue;
            }
            double sum = 0;
            for (int i = 0; i < nc; i++) {
                gains[i] = Math.exp((gains[i] - maxGain) / THETA);
                sum += gains[i];
            }
            double r = rng.nextDouble() * sum;
            int pick = nc - 1;
            for (int i = 0; i < nc; i++) {
                r -= gains[i];
                if (r <= 0) {
                    pick = i;
                    break;
                }
            }
            int d = cand[pick];
            if (d < 0) {
                continue;
            }
            int oldRef = ref[v];
            ref[v] = d;
            ext[d] += ext[oldRef] - 2 * wTo[d];
            Kref[d] += kv;
            size[d]++;
            size[oldRef] = 0;
            Kref[oldRef] = 0;
            ext[oldRef] = 0;
        }
        return ref;
    }
}
