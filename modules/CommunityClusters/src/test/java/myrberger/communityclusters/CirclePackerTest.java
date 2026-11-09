package myrberger.communityclusters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CirclePackerTest {

    private static void assertNoOverlap(double[] r, double[][] xy) {
        for (int i = 0; i < r.length; i++) {
            for (int j = i + 1; j < r.length; j++) {
                double d = Math.hypot(xy[0][i] - xy[0][j], xy[1][i] - xy[1][j]);
                assertTrue("circles " + i + " and " + j + " overlap", d >= r[i] + r[j] - 1e-6);
            }
        }
    }

    @Test
    public void emptyAndSingleInput() {
        assertEquals("empty", 0, CirclePacker.pack(new double[0])[0].length);
        double[][] one = CirclePacker.pack(new double[]{10});
        assertEquals("x", 0.0, one[0][0], 1e-9);
        assertEquals("y", 0.0, one[1][0], 1e-9);
    }

    @Test
    public void twoCirclesTouch() {
        double[] r = {10, 5};
        double[][] xy = CirclePacker.pack(r);
        double d = Math.hypot(xy[0][0] - xy[0][1], xy[1][0] - xy[1][1]);
        assertEquals("distance equals sum of radii", 15.0, d, 1e-9);
    }

    @Test
    public void manyCirclesDoNotOverlap() {
        double[] r = {50, 40, 30, 30, 20, 10, 10, 5, 5, 5, 3, 3, 2, 2, 1};
        assertNoOverlap(r, CirclePacker.pack(r));
    }

    @Test
    public void identicalCirclesDoNotOverlap() {
        double[] r = new double[40];
        java.util.Arrays.fill(r, 7);
        assertNoOverlap(r, CirclePacker.pack(r));
    }

    @Test
    public void resultIsCentredOnTheOrigin() {
        double[] r = {50, 40, 30, 20, 10};
        double[][] xy = CirclePacker.pack(r);
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        for (int i = 0; i < r.length; i++) {
            minX = Math.min(minX, xy[0][i] - r[i]);
            maxX = Math.max(maxX, xy[0][i] + r[i]);
        }
        assertEquals("bounding box is symmetric around x=0", 0.0, minX + maxX, 1e-6);
    }
}
