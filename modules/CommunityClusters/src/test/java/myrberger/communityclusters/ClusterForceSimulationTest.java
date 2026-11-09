package myrberger.communityclusters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.Test;

public class ClusterForceSimulationTest {

    private static final int GROUPS = 5;
    private static final int PER_GROUP = 40;

    private static final class Fixture {
        final int n = GROUPS * PER_GROUP;
        final double[] radius = new double[n];
        final int[] cluster = new int[n];
        final boolean[] fixed = new boolean[n];
        final double[] startX = new double[n];
        final double[] startY = new double[n];
        final int[] src;
        final int[] dst;

        Fixture() {
            Random r = new Random(1);
            List<int[]> edges = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                radius[i] = 5;
                cluster[i] = i / PER_GROUP;
            }
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    if (r.nextDouble() < (cluster[i] == cluster[j] ? 0.12 : 0.004)) {
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

        ClusterForceSimulation simulation(ClusterForceSimulation.Params p) {
            return new ClusterForceSimulation(radius, cluster, fixed, startX, startY, src, dst, p);
        }
    }

    private static int runToEnd(ClusterForceSimulation sim) {
        int ticks = 0;
        while (!sim.isConverged() && ticks < 5000) {
            sim.tick();
            ticks++;
        }
        return ticks;
    }

    @Test
    public void convergesWithFiniteCoordinates() {
        Fixture f = new Fixture();
        ClusterForceSimulation sim = f.simulation(new ClusterForceSimulation.Params());
        int ticks = runToEnd(sim);
        assertTrue("converges in a few hundred steps, took " + ticks, ticks > 100 && ticks < 1000);
        for (int i = 0; i < f.n; i++) {
            assertFalse("x is finite", Double.isNaN(sim.x[i]) || Double.isInfinite(sim.x[i]));
            assertFalse("y is finite", Double.isNaN(sim.y[i]) || Double.isInfinite(sim.y[i]));
        }
    }

    @Test
    public void clustersEndUpSeparated() {
        Fixture f = new Fixture();
        ClusterForceSimulation sim = f.simulation(new ClusterForceSimulation.Params());
        runToEnd(sim);
        double[] cx = new double[GROUPS], cy = new double[GROUPS];
        for (int i = 0; i < f.n; i++) {
            cx[f.cluster[i]] += sim.x[i] / PER_GROUP;
            cy[f.cluster[i]] += sim.y[i] / PER_GROUP;
        }
        double spread = 0;
        for (int i = 0; i < f.n; i++) {
            spread += Math.hypot(sim.x[i] - cx[f.cluster[i]], sim.y[i] - cy[f.cluster[i]]) / f.n;
        }
        double minGap = Double.MAX_VALUE;
        for (int a = 0; a < GROUPS; a++) {
            for (int b = a + 1; b < GROUPS; b++) {
                minGap = Math.min(minGap, Math.hypot(cx[a] - cx[b], cy[a] - cy[b]));
            }
        }
        assertTrue("centres (" + minGap + " apart) are well separated compared with cluster spread (" + spread + ")",
                minGap > 2 * spread);
    }

    @Test
    public void sameSeedGivesSamePositions() {
        Fixture f = new Fixture();
        ClusterForceSimulation a = f.simulation(new ClusterForceSimulation.Params());
        ClusterForceSimulation b = f.simulation(new ClusterForceSimulation.Params());
        for (int t = 0; t < 50; t++) {
            a.tick();
            b.tick();
        }
        for (int i = 0; i < f.n; i++) {
            assertEquals("x of node " + i, a.x[i], b.x[i], 0.0);
            assertEquals("y of node " + i, a.y[i], b.y[i], 0.0);
        }
    }

    @Test
    public void fixedNodesDoNotMove() {
        Fixture f = new Fixture();
        f.fixed[3] = true;
        f.startX[3] = 123;
        f.startY[3] = -45;
        ClusterForceSimulation sim = f.simulation(new ClusterForceSimulation.Params());
        runToEnd(sim);
        assertEquals("x", 123.0, sim.x[3], 0.0);
        assertEquals("y", -45.0, sim.y[3], 0.0);
    }

    @Test
    public void tinyAndDegenerateGraphsAreHandled() {
        ClusterForceSimulation.Params p = new ClusterForceSimulation.Params();
        ClusterForceSimulation one = new ClusterForceSimulation(new double[]{5}, new int[]{0}, new boolean[1],
                new double[1], new double[1], new int[0], new int[0], p);
        assertTrue("single node converges", runToEnd(one) > 0);
        // two nodes at the same place, joined by a self loop and a normal edge
        ClusterForceSimulation two = new ClusterForceSimulation(new double[]{5, 5}, new int[]{0, 0},
                new boolean[2], new double[2], new double[2], new int[]{0, 0}, new int[]{0, 1}, p);
        runToEnd(two);
        assertFalse("no NaN", Double.isNaN(two.x[0]) || Double.isNaN(two.x[1]));
    }
}
