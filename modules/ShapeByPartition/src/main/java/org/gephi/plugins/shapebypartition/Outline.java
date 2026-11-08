/*
 * Copyright 2026 Matt Artz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gephi.plugins.shapebypartition;

/**
 * A polygon with softened corners: straight edges joined by quadratic curves
 * whose control point is the original vertex.
 * <p>
 * Stored as one triple of points per corner, <code>{inX, inY, vertexX,
 * vertexY, outX, outY, ...}</code>: the edge arrives at <i>in</i>, curves
 * round the vertex, and leaves from <i>out</i> towards the next corner.
 * <p>
 * Free of Gephi and Swing types so the geometry can be tested on its own.
 *
 * @author Matt Artz
 */
final class Outline {

    /** Curve samples per corner when an outline is flattened for measurement. */
    private static final int FLATTEN_STEPS = 24;

    private final double[] corners;

    private Outline(double[] corners) {
        this.corners = corners;
    }

    /**
     * Rounds every corner of a polygon.
     * <p>
     * Rounding is a fraction of the shorter edge meeting at a corner: 0 keeps
     * the sharp vertex, 1 lets neighbouring curves meet at the edge midpoints.
     * Convex and concave corners take separate amounts because an inner corner
     * of a star or cross needs less softening to look even.
     *
     * @param vertices        interleaved x and y, in drawing order
     * @param convexRounding  rounding for corners that point outwards
     * @param concaveRounding rounding for corners that point inwards
     * @return the rounded outline
     */
    static Outline rounded(double[] vertices, double convexRounding, double concaveRounding) {
        int count = vertices.length / 2;
        double winding = Math.signum(signedArea(vertices));
        double[] corners = new double[count * 6];
        for (int i = 0; i < count; i++) {
            int prev = (i + count - 1) % count;
            int next = (i + 1) % count;
            double vx = vertices[i * 2];
            double vy = vertices[i * 2 + 1];
            double px = vertices[prev * 2] - vx;
            double py = vertices[prev * 2 + 1] - vy;
            double nx = vertices[next * 2] - vx;
            double ny = vertices[next * 2 + 1] - vy;
            double toPrev = Math.hypot(px, py);
            double toNext = Math.hypot(nx, ny);
            boolean convex = Math.signum(px * ny - py * nx) != winding;
            double cut = (convex ? convexRounding : concaveRounding) * Math.min(toPrev, toNext) / 2d;
            corners[i * 6] = vx + px * cut / toPrev;
            corners[i * 6 + 1] = vy + py * cut / toPrev;
            corners[i * 6 + 2] = vx;
            corners[i * 6 + 3] = vy;
            corners[i * 6 + 4] = vx + nx * cut / toNext;
            corners[i * 6 + 5] = vy + ny * cut / toNext;
        }
        return new Outline(corners);
    }

    /**
     * Returns this outline moved so its centre of area sits on the origin and
     * scaled so it covers exactly the area of a circle of the given diameter.
     * Equal area is what makes a square and a circle of the same node size
     * read as the same size.
     */
    Outline normalisedTo(double diameter) {
        double[] flat = flatten();
        double area = Math.abs(signedArea(flat));
        double[] centre = centroid(flat);
        double target = Math.PI * diameter * diameter / 4d;
        double scale = Math.sqrt(target / area);
        double[] out = new double[corners.length];
        for (int i = 0; i < corners.length; i += 2) {
            out[i] = (corners[i] - centre[0]) * scale;
            out[i + 1] = (corners[i + 1] - centre[1]) * scale;
        }
        return new Outline(out);
    }

    /** Returns this outline scaled by a factor and then moved by an offset. */
    Outline placed(double offsetX, double offsetY, double scale) {
        double[] out = new double[corners.length];
        for (int i = 0; i < corners.length; i += 2) {
            out[i] = corners[i] * scale + offsetX;
            out[i + 1] = corners[i + 1] * scale + offsetY;
        }
        return new Outline(out);
    }

    /** Sends the outline to a path, starting where the last corner leaves. */
    void trace(PathSink sink) {
        int n = corners.length;
        sink.moveTo(corners[n - 2], corners[n - 1]);
        for (int i = 0; i < n; i += 6) {
            sink.lineTo(corners[i], corners[i + 1]);
            sink.quadTo(corners[i + 2], corners[i + 3], corners[i + 4], corners[i + 5]);
        }
        sink.close();
    }

    /**
     * How far a polygon reaches from the origin in a direction: the distance
     * along the ray from the origin to where it leaves the polygon. Every
     * shape here is star-shaped about its centre, so the ray leaves once.
     *
     * @param polygon interleaved x and y, around the origin
     * @param dx      x of the direction, unit length
     * @param dy      y of the direction, unit length
     * @return the distance to the boundary, or 0 if the ray misses it
     */
    static double reach(double[] polygon, double dx, double dy) {
        double best = 0;
        int n = polygon.length;
        for (int i = 0; i < n; i += 2) {
            int j = (i + 2) % n;
            double px = polygon[i];
            double py = polygon[i + 1];
            double ex = polygon[j] - px;
            double ey = polygon[j + 1] - py;
            double denominator = dx * ey - dy * ex;
            if (Math.abs(denominator) < 1e-12) {
                continue;
            }
            double t = (px * ey - py * ex) / denominator;
            double s = (px * dy - py * dx) / denominator;
            if (t > best && s >= 0 && s <= 1) {
                best = t;
            }
        }
        return best;
    }

    /** Number of corners, which is the vertex count of the underlying polygon. */
    int cornerCount() {
        return corners.length / 6;
    }

    /**
     * The smallest axis-aligned box holding the outline.
     *
     * @return <code>{minX, minY, maxX, maxY}</code>
     */
    double[] bounds() {
        double[] flat = flatten();
        double[] box = {flat[0], flat[1], flat[0], flat[1]};
        for (int i = 2; i < flat.length; i += 2) {
            box[0] = Math.min(box[0], flat[i]);
            box[1] = Math.min(box[1], flat[i + 1]);
            box[2] = Math.max(box[2], flat[i]);
            box[3] = Math.max(box[3], flat[i + 1]);
        }
        return box;
    }

    /** The outline as a dense polygon, interleaved x and y. */
    double[] flatten() {
        int count = cornerCount();
        double[] out = new double[count * (FLATTEN_STEPS + 1) * 2];
        int k = 0;
        for (int c = 0; c < count; c++) {
            int i = c * 6;
            for (int s = 0; s <= FLATTEN_STEPS; s++) {
                double t = (double) s / FLATTEN_STEPS;
                double a = (1 - t) * (1 - t);
                double b = 2 * t * (1 - t);
                double d = t * t;
                out[k++] = a * corners[i] + b * corners[i + 2] + d * corners[i + 4];
                out[k++] = a * corners[i + 1] + b * corners[i + 3] + d * corners[i + 5];
            }
        }
        return out;
    }

    /** Shoelace area of a polygon; the sign gives its winding. */
    static double signedArea(double[] polygon) {
        double sum = 0;
        int n = polygon.length;
        for (int i = 0; i < n; i += 2) {
            int j = (i + 2) % n;
            sum += polygon[i] * polygon[j + 1] - polygon[j] * polygon[i + 1];
        }
        return sum / 2d;
    }

    /** Centre of area of a simple polygon. */
    static double[] centroid(double[] polygon) {
        double cx = 0;
        double cy = 0;
        int n = polygon.length;
        for (int i = 0; i < n; i += 2) {
            int j = (i + 2) % n;
            double cross = polygon[i] * polygon[j + 1] - polygon[j] * polygon[i + 1];
            cx += (polygon[i] + polygon[j]) * cross;
            cy += (polygon[i + 1] + polygon[j + 1]) * cross;
        }
        double factor = 1d / (6d * signedArea(polygon));
        return new double[]{cx * factor, cy * factor};
    }
}
