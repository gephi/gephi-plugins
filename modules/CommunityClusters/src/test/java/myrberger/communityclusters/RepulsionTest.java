package myrberger.communityclusters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;
import org.junit.Test;

/** The tree-based repulsion must give the same kind of layout as the exact calculation. */
public class RepulsionTest {

    private static ClusterForceSimulation simulation(int n, int clusters, long seed) {
        Random r = new Random(seed);
        int m = n * 4;
        int[] src = new int[m], dst = new int[m], cluster = new int[n];
        double[] radius = new double[n];
        Arrays.fill(radius, 5);
        for (int i = 0; i < n; i++) {
            cluster[i] = i % clusters;
        }
        for (int i = 0; i < m; i++) {
            int u = r.nextInt(n);
            int v = r.nextDouble() < 0.8 ? (u / clusters * clusters + r.nextInt(Math.max(1, n / clusters)) * clusters
                    + u % clusters) % n : r.nextInt(n);
            src[i] = u;
            dst[i] = v;
        }
        return new ClusterForceSimulation(radius, cluster, new boolean[n], new double[n], new double[n], src, dst,
                new ClusterForceSimulation.Params());
    }

    private static void runToEnd(ClusterForceSimulation sim) {
        int ticks = 0;
        while (!sim.isConverged() && ticks < 2000) {
            sim.tick();
            ticks++;
        }
    }

    /** Mean distance from a node to the centre of its cluster. */
    private static double spread(ClusterForceSimulation sim, int n, int clusters) {
        double[] cx = new double[clusters], cy = new double[clusters];
        int[] count = new int[clusters];
        for (int i = 0; i < n; i++) {
            cx[i % clusters] += sim.x[i];
            cy[i % clusters] += sim.y[i];
            count[i % clusters]++;
        }
        for (int c = 0; c < clusters; c++) {
            cx[c] /= count[c];
            cy[c] /= count[c];
        }
        double sum = 0;
        for (int i = 0; i < n; i++) {
            sum += Math.hypot(sim.x[i] - cx[i % clusters], sim.y[i] - cy[i % clusters]);
        }
        return sum / n;
    }

    /** Share of sampled nodes whose nearest neighbour is closer than the two radii (nodes overlapping). */
    private static double overlapShare(ClusterForceSimulation sim, int n) {
        int sample = Math.min(n, 300), overlapping = 0;
        for (int i = 0; i < sample; i++) {
            double nearest = Double.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (j != i) {
                    nearest = Math.min(nearest, Math.hypot(sim.x[i] - sim.x[j], sim.y[i] - sim.y[j]));
                }
            }
            if (nearest < 10 - 1e-6) {
                overlapping++;
            }
        }
        return (double) overlapping / sample;
    }

    @Test
    public void treeAndExactGiveTheSameKindOfLayout() {
        int n = 1600, clusters = 3;
        ClusterForceSimulation exact = simulation(n, clusters, 5);
        exact.forceExactRepulsion = true;
        runToEnd(exact);
        ClusterForceSimulation tree = simulation(n, clusters, 5);
        tree.forceExactRepulsion = false;
        runToEnd(tree);
        double a = spread(exact, n, clusters), b = spread(tree, n, clusters);
        assertTrue("cluster size with the tree (" + b + ") is within 10% of exact (" + a + ")",
                Math.abs(b / a - 1) < 0.10);
        assertTrue("no overlapping nodes with the tree", overlapShare(tree, n) < 0.01);
    }

    @Test
    public void crowdedClustersStillComeOutFiniteAndSeparated() {
        int n = 6000, clusters = 2;
        ClusterForceSimulation sim = simulation(n, clusters, 9); // crowded, so the tree is chosen automatically
        for (int t = 0; t < 120; t++) {
            sim.tick();
        }
        for (int i = 0; i < n; i++) {
            assertFalse("finite coordinate", Double.isNaN(sim.x[i]) || Double.isInfinite(sim.y[i]));
        }
        double[] cx = new double[2], cy = new double[2];
        for (int i = 0; i < n; i++) {
            cx[i % 2] += sim.x[i] / (n / 2);
            cy[i % 2] += sim.y[i] / (n / 2);
        }
        assertTrue("the two cluster centres are apart", Math.hypot(cx[0] - cx[1], cy[0] - cy[1]) > 100);
    }

    @Test
    public void runsWithSeveralThreadsAreRepeatable() {
        ClusterForceSimulation a = simulation(2500, 4, 7), b = simulation(2500, 4, 7);
        a.forceExactRepulsion = false;
        b.forceExactRepulsion = false;
        for (int t = 0; t < 40; t++) {
            a.tick();
            b.tick();
        }
        for (int i = 0; i < 2500; i++) {
            assertEquals("x of node " + i, a.x[i], b.x[i], 0.0);
            assertEquals("y of node " + i, a.y[i], b.y[i], 0.0);
        }
    }

    @Test
    public void closeNodesArePushedApart() {
        // 500 nodes on top of each other in a tiny area, no ties: repulsion alone has to spread them out
        int n = 500;
        double[] radius = new double[n];
        Arrays.fill(radius, 1);
        ClusterForceSimulation.Params p = new ClusterForceSimulation.Params();
        p.clusterPull = 0;
        p.resetPositions = false;
        double[] sx = new double[n], sy = new double[n];
        Random r = new Random(1);
        for (int i = 0; i < n; i++) {
            sx[i] = r.nextDouble();
            sy[i] = r.nextDouble();
        }
        ClusterForceSimulation sim = new ClusterForceSimulation(radius, new int[n], new boolean[n], sx, sy,
                new int[0], new int[0], p);
        sim.forceExactRepulsion = false;
        for (int t = 0; t < 30; t++) {
            sim.tick();
        }
        double maxDist = 0;
        for (int i = 0; i < n; i++) {
            maxDist = Math.max(maxDist, Math.hypot(sim.x[i], sim.y[i]));
        }
        assertTrue("nodes moved well away from the starting square (max distance " + maxDist + ")", maxDist > 20);
    }

    @Test
    public void oneStepOfTheTreeAgreesWithTheExactForce() {
        // spread-out nodes, so near neighbours dominate the force and the two methods can be compared directly
        int n = 800;
        double[] radius = new double[n];
        Arrays.fill(radius, 1);
        double[] sx = new double[n], sy = new double[n];
        Random r = new Random(3);
        for (int i = 0; i < n; i++) {
            sx[i] = r.nextDouble() * 600;
            sy[i] = r.nextDouble() * 600;
        }
        ClusterForceSimulation.Params p = new ClusterForceSimulation.Params();
        p.resetPositions = false;
        ClusterForceSimulation sim = new ClusterForceSimulation(radius, new int[n], new boolean[n], sx, sy,
                new int[0], new int[0], p);
        Arrays.fill(sim.vx, 0);
        Arrays.fill(sim.vy, 0);
        sim.applyChargeExact();
        double[] ex = sim.vx.clone(), ey = sim.vy.clone();
        Arrays.fill(sim.vx, 0);
        Arrays.fill(sim.vy, 0);
        sim.applyChargeBarnesHut();
        double error = 0, size = 0;
        for (int i = 0; i < n; i++) {
            error += Math.hypot(sim.vx[i] - ex[i], sim.vy[i] - ey[i]);
            size += Math.hypot(ex[i], ey[i]);
        }
        assertTrue("force from the tree is not zero", size > 0);
        assertTrue("force from the tree differs from the exact force by " + Math.round(100 * error / size)
                + "% (allowed: 25%)", error / size < 0.25);
    }
}
