package myrberger.communityclusters;

import java.util.Arrays;
import java.util.Random;

/**
 * Port of the "Clusters" view of SoFab: a d3-force style simulation in which every node is pulled
 * towards a home position for its cluster, ties inside a cluster keep full strength and ties between
 * clusters are weakened. Pure Java, no Gephi dependency, so it can be unit tested on its own.
 */
final class ClusterForceSimulation {

    /** Tunable dials; defaults are SoFab's. */
    static final class Params {
        double repel = 40;           // many-body strength
        double tieLen = 24;          // link distance on top of the two node radii
        double tieStr = 1;           // multiplies the per-tie strength
        double spacing = 2;          // extra room kept by the collision force
        double clusterPull = 0.14;   // pull towards the cluster's home
        double cooling = 0.0228;     // alphaDecay
        double interTieFactor = 0.03;// strength multiplier for ties between clusters
        double scale = 1;            // multiplies all distances
        double repelDistanceMax = 140;
        long seed = 42;
        boolean resetPositions = true;
    }

    private static final double ALPHA_MIN = 0.001;
    private static final double VELOCITY_DECAY = 0.6; // d3: 1 - 0.4

    final int n;
    final double[] x, y, vx, vy;
    private final double[] radius;
    private final int[] cluster;
    private final boolean[] fixed;
    private final double[] homeX, homeY; // per node
    private final int[] src, dst;
    private final double[] linkDist, linkStr, linkBias;
    private final Params p;
    private final Random rng;
    private double alpha = 1;
    private int[] cellStart = new int[0];
    private int[] cellItems = new int[0];

    /**
     * @param radius   node radius (graph units)
     * @param cluster  cluster index per node, 0..k-1
     * @param fixed    nodes that must not move
     * @param startX   current positions, used only when {@code p.resetPositions} is false
     */
    ClusterForceSimulation(double[] radius, int[] cluster, boolean[] fixed, double[] startX, double[] startY,
                           int[] edgeSrc, int[] edgeDst, Params params) {
        this.n = radius.length;
        this.radius = radius;
        this.cluster = cluster;
        this.fixed = fixed;
        this.p = params;
        this.rng = new Random(params.seed);
        x = new double[n];
        y = new double[n];
        vx = new double[n];
        vy = new double[n];
        homeX = new double[n];
        homeY = new double[n];

        // drop self loops
        int m = 0;
        for (int i = 0; i < edgeSrc.length; i++) {
            if (edgeSrc[i] != edgeDst[i]) {
                m++;
            }
        }
        src = new int[m];
        dst = new int[m];
        m = 0;
        for (int i = 0; i < edgeSrc.length; i++) {
            if (edgeSrc[i] != edgeDst[i]) {
                src[m] = edgeSrc[i];
                dst[m] = edgeDst[i];
                m++;
            }
        }

        computeHomes();
        initPositions(startX, startY);

        // link force setup (d3: strength = 1/min(degree) scaled by the dial, held at 1)
        double[] cnt = new double[n];
        for (int i = 0; i < m; i++) {
            cnt[src[i]]++;
            cnt[dst[i]]++;
        }
        linkDist = new double[m];
        linkStr = new double[m];
        linkBias = new double[m];
        for (int i = 0; i < m; i++) {
            int s = src[i], t = dst[i];
            linkDist[i] = p.tieLen * p.scale + radius[s] + radius[t];
            double base = Math.min(1, p.tieStr / Math.min(cnt[s], cnt[t]));
            linkStr[i] = cluster[s] == cluster[t] ? base : base * p.interTieFactor;
            linkBias[i] = cnt[s] / (cnt[s] + cnt[t]);
        }
    }

    private void computeHomes() {
        int k = 0;
        for (int c : cluster) {
            k = Math.max(k, c + 1);
        }
        if (k == 0) {
            return;
        }
        int[] counts = new int[k];
        double maxR = 0;
        for (int i = 0; i < n; i++) {
            counts[cluster[i]]++;
            maxR = Math.max(maxR, radius[i]);
        }
        // largest cluster first; stable for ties
        Integer[] order = new Integer[k];
        for (int i = 0; i < k; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (a, b) -> Integer.compare(counts[b], counts[a]));

        double unit = (9 + maxR * 0.9) * p.scale;
        double[] r = new double[k];
        for (int i = 0; i < k; i++) {
            r[i] = Math.sqrt(counts[order[i]]) * unit + 12 * p.scale;
        }
        double[][] xy = CirclePacker.pack(r);
        double[] cx = new double[k], cy = new double[k];
        for (int i = 0; i < k; i++) {
            cx[order[i]] = xy[0][i];
            cy[order[i]] = xy[1][i];
        }
        for (int i = 0; i < n; i++) {
            homeX[i] = cx[cluster[i]];
            homeY[i] = cy[cluster[i]];
        }
    }

    private void initPositions(double[] startX, double[] startY) {
        for (int i = 0; i < n; i++) {
            if (!p.resetPositions || fixed[i]) {
                x[i] = startX[i];
                y[i] = startY[i];
            } else {
                x[i] = homeX[i] + (rng.nextDouble() - 0.5) * 30 * p.scale;
                y[i] = homeY[i] + (rng.nextDouble() - 0.5) * 30 * p.scale;
            }
        }
    }

    boolean isConverged() {
        return alpha < ALPHA_MIN;
    }

    double alpha() {
        return alpha;
    }

    /** One simulation step, in the same order d3 applies its forces. */
    void tick() {
        alpha += (0 - alpha) * p.cooling;
        applyLinks();
        applyCharge();
        applyCollide();
        applyHome();
        for (int i = 0; i < n; i++) {
            if (fixed[i]) {
                vx[i] = 0;
                vy[i] = 0;
            } else {
                vx[i] *= VELOCITY_DECAY;
                vy[i] *= VELOCITY_DECAY;
                x[i] += vx[i];
                y[i] += vy[i];
            }
        }
    }

    private double jiggle() {
        return (rng.nextDouble() - 0.5) * 1e-6;
    }

    private void applyLinks() {
        for (int i = 0; i < src.length; i++) {
            int s = src[i], t = dst[i];
            double dx = x[t] + vx[t] - x[s] - vx[s];
            double dy = y[t] + vy[t] - y[s] - vy[s];
            if (dx == 0) {
                dx = jiggle();
            }
            if (dy == 0) {
                dy = jiggle();
            }
            double l = Math.sqrt(dx * dx + dy * dy);
            l = (l - linkDist[i]) / l * alpha * linkStr[i];
            dx *= l;
            dy *= l;
            double b = linkBias[i];
            vx[t] -= dx * b;
            vy[t] -= dy * b;
            b = 1 - b;
            vx[s] += dx * b;
            vy[s] += dy * b;
        }
    }

    // ---- uniform grid for the two short-range forces -----------------------------------------------

    private int gw, gh;
    private double gx0, gy0, gcell;

    private void buildGrid(double[] px, double[] py, double minCell) {
        double minX = Double.POSITIVE_INFINITY, minY = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            minX = Math.min(minX, px[i]);
            maxX = Math.max(maxX, px[i]);
            minY = Math.min(minY, py[i]);
            maxY = Math.max(maxY, py[i]);
        }
        // keep the number of cells proportional to n; bigger cells stay correct, they just test more pairs
        double extent = Math.max(maxX - minX, maxY - minY);
        gcell = Math.max(minCell, extent / Math.max(1, Math.sqrt(4.0 * n)));
        if (!(gcell > 0)) {
            gcell = 1;
        }
        gx0 = minX;
        gy0 = minY;
        gw = (int) ((maxX - minX) / gcell) + 1;
        gh = (int) ((maxY - minY) / gcell) + 1;
        int cells = gw * gh;
        if (cellStart.length < cells + 1) {
            cellStart = new int[cells + 1];
        } else {
            Arrays.fill(cellStart, 0, cells + 1, 0);
        }
        if (cellItems.length < n) {
            cellItems = new int[n];
        }
        int[] cellOf = new int[n];
        for (int i = 0; i < n; i++) {
            int cx = (int) ((px[i] - gx0) / gcell), cy = (int) ((py[i] - gy0) / gcell);
            cellOf[i] = cy * gw + cx;
            cellStart[cellOf[i] + 1]++;
        }
        for (int c = 0; c < cells; c++) {
            cellStart[c + 1] += cellStart[c];
        }
        int[] fill = new int[cells];
        for (int i = 0; i < n; i++) {
            cellItems[cellStart[cellOf[i]] + fill[cellOf[i]]++] = i;
        }
    }

    /** Many-body repulsion with a hard distance cut-off (d3's forceManyBody with distanceMax). */
    private void applyCharge() {
        if (n < 2 || p.repel == 0) {
            return;
        }
        double dMax = p.repelDistanceMax * p.scale;
        double dMax2 = dMax * dMax;
        double strength = -p.repel * p.scale * p.scale;
        buildGrid(x, y, dMax);
        for (int i = 0; i < n; i++) {
            if (fixed[i]) {
                continue;
            }
            int cx = (int) ((x[i] - gx0) / gcell), cy = (int) ((y[i] - gy0) / gcell);
            for (int gy = Math.max(0, cy - 1); gy <= Math.min(gh - 1, cy + 1); gy++) {
                for (int gxx = Math.max(0, cx - 1); gxx <= Math.min(gw - 1, cx + 1); gxx++) {
                    int c = gy * gw + gxx;
                    for (int q = cellStart[c]; q < cellStart[c + 1]; q++) {
                        int j = cellItems[q];
                        if (j == i) {
                            continue;
                        }
                        double dx = x[j] - x[i], dy = y[j] - y[i];
                        if (dx == 0) {
                            dx = jiggle();
                        }
                        if (dy == 0) {
                            dy = jiggle();
                        }
                        double l = dx * dx + dy * dy;
                        if (l >= dMax2) {
                            continue;
                        }
                        if (l < 1) {
                            l = Math.sqrt(l);
                        }
                        double w = strength * alpha / l;
                        vx[i] += dx * w;
                        vy[i] += dy * w;
                    }
                }
            }
        }
    }

    /** d3's forceCollide with strength 1 and one iteration. */
    private void applyCollide() {
        if (n < 2) {
            return;
        }
        double sp = p.spacing * p.scale;
        double maxR = 0;
        for (int i = 0; i < n; i++) {
            maxR = Math.max(maxR, radius[i] + sp);
        }
        double[] px = new double[n], py = new double[n];
        for (int i = 0; i < n; i++) {
            px[i] = x[i] + vx[i];
            py[i] = y[i] + vy[i];
        }
        buildGrid(px, py, 2 * maxR);
        for (int i = 0; i < n; i++) {
            double ri = radius[i] + sp, ri2 = ri * ri;
            double xi = px[i], yi = py[i];
            int cx = (int) ((xi - gx0) / gcell), cy = (int) ((yi - gy0) / gcell);
            for (int gy = Math.max(0, cy - 1); gy <= Math.min(gh - 1, cy + 1); gy++) {
                for (int gxx = Math.max(0, cx - 1); gxx <= Math.min(gw - 1, cx + 1); gxx++) {
                    int c = gy * gw + gxx;
                    for (int q = cellStart[c]; q < cellStart[c + 1]; q++) {
                        int j = cellItems[q];
                        if (j <= i) {
                            continue;
                        }
                        double rj = radius[j] + sp;
                        double rr = ri + rj;
                        double dx = xi - (x[j] + vx[j]), dy = yi - (y[j] + vy[j]);
                        double l = dx * dx + dy * dy;
                        if (l < rr * rr) {
                            if (dx == 0) {
                                dx = jiggle();
                                l += dx * dx;
                            }
                            if (dy == 0) {
                                dy = jiggle();
                                l += dy * dy;
                            }
                            l = Math.sqrt(l);
                            l = (rr - l) / l;
                            dx *= l;
                            dy *= l;
                            double share = (rj * rj) / (ri2 + rj * rj);
                            vx[i] += dx * share;
                            vy[i] += dy * share;
                            share = 1 - share;
                            vx[j] -= dx * share;
                            vy[j] -= dy * share;
                        }
                    }
                }
            }
        }
    }

    /** forceX / forceY towards the cluster's home. */
    private void applyHome() {
        double k = p.clusterPull * alpha;
        for (int i = 0; i < n; i++) {
            vx[i] += (homeX[i] - x[i]) * k;
            vy[i] += (homeY[i] - y[i]) * k;
        }
    }
}
