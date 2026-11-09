package myrberger.communityclusters;

/**
 * Port of d3-hierarchy's packSiblings (front-chain circle packing).
 * Pure Java, no Gephi dependency.
 */
final class CirclePacker {

    private CirclePacker() {
    }

    private static final class Node {
        final int idx;
        Node next, previous;

        Node(int idx) {
            this.idx = idx;
        }
    }

    /**
     * Packs circles of the given radii. Returns {x[], y[]}, centred (by bounding box) on the origin.
     * Radii must be positive.
     */
    static double[][] pack(double[] r) {
        final int n = r.length;
        final double[] x = new double[n];
        final double[] y = new double[n];
        if (n == 0) {
            return new double[][]{x, y};
        }
        if (n == 1) {
            return new double[][]{x, y};
        }

        // first two circles
        x[0] = -r[1];
        y[0] = 0;
        x[1] = r[0];
        y[1] = 0;
        if (n > 2) {
            place(x, y, r, 1, 0, 2);

            Node a = new Node(0), b = new Node(1), c = new Node(2);
            a.next = c.previous = b;
            b.next = a.previous = c;
            c.next = b.previous = a;

            pack:
            for (int i = 3; i < n; ++i) {
                place(x, y, r, a.idx, b.idx, i);
                c = new Node(i);

                Node j = b.next, k = a.previous;
                double sj = r[b.idx], sk = r[a.idx];
                do {
                    if (sj <= sk) {
                        if (intersects(x, y, r, j.idx, c.idx)) {
                            b = j;
                            a.next = b;
                            b.previous = a;
                            --i;
                            continue pack;
                        }
                        sj += r[j.idx];
                        j = j.next;
                    } else {
                        if (intersects(x, y, r, k.idx, c.idx)) {
                            a = k;
                            a.next = b;
                            b.previous = a;
                            --i;
                            continue pack;
                        }
                        sk += r[k.idx];
                        k = k.previous;
                    }
                } while (j != k.next);

                // insert c between a and b
                c.previous = a;
                c.next = b;
                a.next = c;
                b.previous = c;
                b = c;

                // closest pair to the centroid
                double aa = score(x, y, r, a);
                Node cc = c;
                while ((cc = cc.next) != b) {
                    double ca = score(x, y, r, cc);
                    if (ca < aa) {
                        a = cc;
                        aa = ca;
                    }
                }
                b = a.next;
            }
        }

        // centre the bounding box on the origin
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < n; i++) {
            minX = Math.min(minX, x[i] - r[i]);
            maxX = Math.max(maxX, x[i] + r[i]);
            minY = Math.min(minY, y[i] - r[i]);
            maxY = Math.max(maxY, y[i] + r[i]);
        }
        double cx = (minX + maxX) / 2, cy = (minY + maxY) / 2;
        for (int i = 0; i < n; i++) {
            x[i] -= cx;
            y[i] -= cy;
        }
        return new double[][]{x, y};
    }

    private static void place(double[] x, double[] y, double[] r, int b, int a, int c) {
        double dx = x[b] - x[a], dy = y[b] - y[a], d2 = dx * dx + dy * dy;
        if (d2 > 0) {
            double a2 = r[a] + r[c];
            a2 *= a2;
            double b2 = r[b] + r[c];
            b2 *= b2;
            if (a2 > b2) {
                double px = (d2 + b2 - a2) / (2 * d2);
                double py = Math.sqrt(Math.max(0, b2 / d2 - px * px));
                x[c] = x[b] - px * dx - py * dy;
                y[c] = y[b] - px * dy + py * dx;
            } else {
                double px = (d2 + a2 - b2) / (2 * d2);
                double py = Math.sqrt(Math.max(0, a2 / d2 - px * px));
                x[c] = x[a] + px * dx - py * dy;
                y[c] = y[a] + px * dy + py * dx;
            }
        } else {
            x[c] = x[a] + r[c];
            y[c] = y[a];
        }
    }

    private static boolean intersects(double[] x, double[] y, double[] r, int a, int b) {
        double dr = r[a] + r[b] - 1e-6, dx = x[b] - x[a], dy = y[b] - y[a];
        return dr > 0 && dr * dr > dx * dx + dy * dy;
    }

    private static double score(double[] x, double[] y, double[] r, Node node) {
        int a = node.idx, b = node.next.idx;
        double ab = r[a] + r[b];
        double dx = (x[a] * r[b] + x[b] * r[a]) / ab;
        double dy = (y[a] * r[b] + y[b] * r[a]) / ab;
        return dx * dx + dy * dy;
    }
}
