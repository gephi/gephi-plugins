package myrberger.communityclusters;

import java.util.Arrays;

/**
 * Barnes-Hut quadtree for the many-body repulsion, the same approximation d3-force uses: groups of
 * nodes that are far enough away are treated as one body at their centre of mass. Pure Java.
 *
 * <p>Built once per tick, then {@link #accumulate} can be called for different nodes from several
 * threads at once (it only reads the tree and writes the velocity of the node it is asked about).
 */
final class RepulsionTree {

    private static final int LEAF_SIZE = 8;
    private static final int MAX_DEPTH = 40;
    /** d3 default: theta = 0.9, squared. */
    static final double THETA2 = 0.81;
    /** A group is only treated as one body if it is narrower than this share of the cut-off distance. */
    static final double MAX_GROUP_WIDTH = 1.0;

    private int[] idx = new int[0];
    private int[] scratch = new int[0];

    // per tree node
    private int nodeCount;
    private double[] comX = new double[0], comY = new double[0];
    private double[] boxX = new double[0], boxY = new double[0], boxW = new double[0];
    private int[] cnt = new int[0];
    private int[] kids = new int[0];   // index of the first of 4 consecutive children, or -1 for a leaf
    private int[] lo = new int[0], hi = new int[0];

    private double[] px, py;

    void build(double[] x, double[] y, int n) {
        this.px = x;
        this.py = y;
        if (idx.length < n) {
            idx = new int[n];
            scratch = new int[n];
        }
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            idx[i] = i;
            minX = Math.min(minX, x[i]);
            maxX = Math.max(maxX, x[i]);
            minY = Math.min(minY, y[i]);
            maxY = Math.max(maxY, y[i]);
        }
        nodeCount = 0;
        if (n == 0) {
            return;
        }
        double w = Math.max(maxX - minX, maxY - minY) * (1 + 1e-9) + 1e-6;
        int root = newNodes(1);
        fill(root, 0, n, minX, minY, w, 0);
    }

    private int newNodes(int k) {
        int first = nodeCount;
        nodeCount += k;
        if (nodeCount > cnt.length) {
            int cap = Math.max(nodeCount, cnt.length * 2 + 16);
            comX = Arrays.copyOf(comX, cap);
            comY = Arrays.copyOf(comY, cap);
            boxX = Arrays.copyOf(boxX, cap);
            boxY = Arrays.copyOf(boxY, cap);
            boxW = Arrays.copyOf(boxW, cap);
            cnt = Arrays.copyOf(cnt, cap);
            kids = Arrays.copyOf(kids, cap);
            lo = Arrays.copyOf(lo, cap);
            hi = Arrays.copyOf(hi, cap);
        }
        return first;
    }

    private void fill(int node, int from, int to, double x0, double y0, double w, int depth) {
        boxX[node] = x0;
        boxY[node] = y0;
        boxW[node] = w;
        lo[node] = from;
        hi[node] = to;
        cnt[node] = to - from;
        double sx = 0, sy = 0;
        for (int p = from; p < to; p++) {
            sx += px[idx[p]];
            sy += py[idx[p]];
        }
        if (to > from) {
            comX[node] = sx / (to - from);
            comY[node] = sy / (to - from);
        }
        if (to - from <= LEAF_SIZE || depth >= MAX_DEPTH) {
            kids[node] = -1;
            return;
        }
        double mx = x0 + w / 2, my = y0 + w / 2;
        int[] qc = new int[5];
        for (int p = from; p < to; p++) {
            qc[1 + quadrant(idx[p], mx, my)]++;
        }
        for (int q = 0; q < 4; q++) {
            qc[q + 1] += qc[q];
        }
        int[] at = {qc[0], qc[1], qc[2], qc[3]};
        for (int p = from; p < to; p++) {
            int q = quadrant(idx[p], mx, my);
            scratch[from + at[q]++] = idx[p];
        }
        System.arraycopy(scratch, from, idx, from, to - from);
        int first = newNodes(4);
        kids[node] = first;
        for (int q = 0; q < 4; q++) {
            fill(first + q, from + qc[q], from + qc[q + 1], x0 + ((q & 1) != 0 ? w / 2 : 0),
                    y0 + ((q & 2) != 0 ? w / 2 : 0), w / 2, depth + 1);
        }
    }

    private int quadrant(int i, double mx, double my) {
        return (px[i] >= mx ? 1 : 0) | (py[i] >= my ? 2 : 0);
    }

    /** Deterministic stand-in for d3's random jiggle, safe to call from several threads. */
    static double jiggle(int i, int j) {
        int h = i * 0x9E3779B1 ^ j * 0x85EBCA6B;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        return ((h & 0xFFFF) / 65536.0 - 0.5) * 1e-6;
    }

    /**
     * Adds the repulsion on node i to vx[i], vy[i].
     *
     * @param k       strength * alpha (negative to push apart)
     * @param dMax2   squared cut-off distance
     */
    void accumulate(int i, double k, double dMax2, double[] vx, double[] vy, int[] stack) {
        if (nodeCount == 0) {
            return;
        }
        double xi = px[i], yi = py[i];
        double ax = 0, ay = 0;
        int sp = 0;
        stack[sp++] = 0;
        while (sp > 0) {
            int node = stack[--sp];
            if (cnt[node] == 0) {
                continue;
            }
            // skip boxes that lie completely beyond the cut-off
            double bx = Math.max(Math.max(boxX[node] - xi, xi - (boxX[node] + boxW[node])), 0);
            double by = Math.max(Math.max(boxY[node] - yi, yi - (boxY[node] + boxW[node])), 0);
            if (bx * bx + by * by >= dMax2) {
                continue;
            }
            double dx = comX[node] - xi, dy = comY[node] - yi;
            double l = dx * dx + dy * dy;
            double w = boxW[node];
            // far enough away (d3's Barnes-Hut rule), and not wider than the cut-off distance
            if (w * w / THETA2 < l && w * w <= MAX_GROUP_WIDTH * MAX_GROUP_WIDTH * dMax2) {
                // far enough: treat the whole group as one body
                if (l < dMax2) {
                    if (dx == 0) {
                        dx = jiggle(i, node);
                        l += dx * dx;
                    }
                    if (dy == 0) {
                        dy = jiggle(node, i);
                        l += dy * dy;
                    }
                    if (l < 1) {
                        l = Math.sqrt(l);
                    }
                    double f = k * cnt[node] / l;
                    ax += dx * f;
                    ay += dy * f;
                }
                continue;
            }
            if (kids[node] < 0) {
                for (int p = lo[node]; p < hi[node]; p++) {
                    int j = idx[p];
                    if (j == i) {
                        continue;
                    }
                    double ddx = px[j] - xi, ddy = py[j] - yi;
                    if (ddx == 0) {
                        ddx = jiggle(i, j);
                    }
                    if (ddy == 0) {
                        ddy = jiggle(j, i);
                    }
                    double ll = ddx * ddx + ddy * ddy;
                    if (ll >= dMax2) {
                        continue;
                    }
                    if (ll < 1) {
                        ll = Math.sqrt(ll);
                    }
                    double f = k / ll;
                    ax += ddx * f;
                    ay += ddy * f;
                }
            } else {
                int first = kids[node];
                stack[sp++] = first;
                stack[sp++] = first + 1;
                stack[sp++] = first + 2;
                stack[sp++] = first + 3;
            }
        }
        vx[i] += ax;
        vy[i] += ay;
    }

    /** Stack depth that is always enough: 3 siblings per level plus one. */
    static int stackSize() {
        return 3 * MAX_DEPTH + 8;
    }
}
