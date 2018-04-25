package pl.edu.wat.student.rzepinski.jakub.util;

import static java.lang.Math.abs;
import static java.lang.Math.min;

public class ManhattanDistanceCalculator {
    private final int gridSize;
    private final boolean torusMode;

    public ManhattanDistanceCalculator(int gridSize, boolean torusMode) {
        this.gridSize = gridSize;
        this.torusMode = torusMode;
    }

    public int distance(int x1, int y1, int x2, int y2) {
        return distance(x1, x2) + distance(y1, y2);
    }

    private int distance(int t1, int t2) {
        return torusMode ? min(abs(t2 - t1), gridSize - abs(t2 - t1)) : abs(t2 - t1);
    }

}

