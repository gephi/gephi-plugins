package cz.cvut.fit.gephi.multimode;

import java.util.Arrays;

/**
 *
 * @author Jaroslav Kuchar
 *
 */
final public class Matrix {

    private final int m;
    private final int n;
    private final double[][] data;

    public Matrix(int M, int N) {
        this.m = M;
        this.n = N;
        data = new double[M][N];
    }

    public void set(int i, int j, double v) {
        this.data[i][j] = v;
    }

    public double get(int i, int j) {
        return this.data[i][j];
    }

    public Matrix transpose() {
        Matrix temp = new Matrix(n, m);
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                temp.data[j][i] = this.data[i][j];
            }
        }
        return temp;
    }

    public Matrix times(Matrix right) {
        Matrix left = this;
        if (left.n != right.m) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix result = new Matrix(left.m, right.n);
        for (int i = 0; i < result.m; i++) {
            for (int j = 0; j < result.n; j++) {
                for (int k = 0; k < left.n; k++) {
                    result.data[i][j] += (left.data[i][k] * right.data[k][j]);
                }
            }
        }
        return result;
    }

    public Matrix timesIndexed(Matrix right) {
        Matrix left = this;
        if (left.n != right.m) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        Matrix result = new Matrix(left.m, right.n);
        for (int i = 0; i < result.m; i++) {
            double[] iRowA = left.data[i];
            double[] iRowC = result.data[i];
            for (int k = 0; k < left.n; k++) {
                double[] kRowB = right.data[k];
                double ikA = iRowA[k];
                for (int j = 0; j < result.n; j++) {
                    iRowC[j] += ikA * kRowB[j];
                }
            }
        }

        return result;
    }

    /**
     * http://www.ateji.com/px/whitepapers/Ateji%20PX%20MatMult%20Whitepaper%20v1.2.pdf
     *
     * @param right
     * @return
     */
    public Matrix timesParallel(final Matrix right) {
        final Matrix left = this;
        if (left.n != right.m) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        final Matrix result = new Matrix(left.m, right.n);

        // start parallel
        Runtime runtime = Runtime.getRuntime();
        final int nThreads = runtime.availableProcessors();
        final int blockSize = result.m / nThreads;
        Thread[] threads = new Thread[nThreads];
        for (int n = 0; n < nThreads; n++) {
            final int finalN = n;
            threads[n] = new Thread() {

                @Override
                public void run() {
                    final int beginIndex = finalN * blockSize;
                    final int endIndex = (finalN == (nThreads - 1)) ? result.m : (finalN + 1) * blockSize;
                    for (int i = beginIndex; i < endIndex; i++) {
                        for (int j = 0; j < result.n; j++) {
                            for (int k = 0; k < left.n; k++) {
                                result.data[i][j] += left.data[i][k] * right.data[k][j];
                            }
                        }
                    }
                }
            };
            threads[n].start();
        }

        for (int t = 0; t < nThreads; t++) {
            try {
                threads[t].join();
            } catch (InterruptedException e) {
                System.exit(-1);
            }
        }

        return result;
    }

    /**
     * http://www.daniweb.com/software-development/csharp/code/355645
     *
     * @param right
     * @return
     */
    public Matrix timesParallelIndexed(final Matrix right) {
        final Matrix left = this;
        if (left.n != right.m) {
            throw new RuntimeException("Illegal matrix dimensions.");
        }
        final Matrix result = new Matrix(left.m, right.n);

        // start parallel
        Runtime runtime = Runtime.getRuntime();
        final int nThreads = runtime.availableProcessors();
        final int blockSize = result.m / nThreads;
        Thread[] threads = new Thread[nThreads];
        for (int n = 0; n < nThreads; n++) {
            final int finalN = n;
            threads[n] = new Thread() {

                @Override
                public void run() {
                    final int beginIndex = finalN * blockSize;
                    final int endIndex = (finalN == (nThreads - 1)) ? result.m : (finalN + 1) * blockSize;
                    for (int i = beginIndex; i < endIndex; i++) {
                        double[] iRowA = left.data[i];
                        double[] iRowC = result.data[i];
                        for (int k = 0; k < left.n; k++) {
                            double[] kRowB = right.data[k];
                            double ikA = iRowA[k];
                            for (int j = 0; j < result.n; j++) {
                                iRowC[j] += ikA * kRowB[j];
                            }
                        }
                    }
                }
            };
            threads[n].start();
        }

        for (int t = 0; t < nThreads; t++) {
            try {
                threads[t].join();
            } catch (InterruptedException e) {
                System.exit(-1);
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return Arrays.deepToString(data);
    }

    public int getM() {
        return m;
    }

    public int getN() {
        return n;
    }
}
