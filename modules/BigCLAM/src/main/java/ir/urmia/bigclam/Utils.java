package ir.urmia.bigclam;

public class Utils {
    public static double[][] bigclam(double[][] adjacencyMatrix, int communitiesCount, int iterationsCount, double learningRate) {
        int nodesCount = adjacencyMatrix.length;
        double[][] F = new double[nodesCount][communitiesCount];
        for (int i = 0; i < nodesCount; i++) {
            for (int j = 0; j < communitiesCount; j++) {
                F[i][j] = Math.random();
            }
        }
        for (int k = 0; k < iterationsCount; k++) {
            double[][] FFtAdj = matrixMultiplication(matrixMultiplication(F, transpose(F)), adjacencyMatrix);
            double[][] gradient = matrixSubtraction(adjacencyMatrix, sigmoid(FFtAdj));
            F = matrixAddition(F, matrixScalarMultiplication(learningRate, matrixMultiplication(gradient, F)));


            for (int i = 0; i < nodesCount; i++) {
                for (int j = 0; j < communitiesCount; j++) {
                    if (F[i][j] < 0) {
                        F[i][j] = 0;
                    }
                }
            }

            F = normalizeColumns(F);
        }

        return F;
    }

    public static double[][] sigmoid(double[][] x) {
        double[][] result = new double[x.length][x[0].length];
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                result[i][j] = 1 / (1 + Math.exp(-x[i][j]));
            }
        }
        return result;
    }

    public static double[][] matrixMultiplication(double[][] a, double[][] b) {
        int aRows = a.length;
        int aCols = a[0].length;
        int bCols = b[0].length;
        double[][] result = new double[aRows][bCols];
        for (int i = 0; i < aRows; i++) {
            for (int j = 0; j < bCols; j++) {
                for (int k = 0; k < aCols; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    public static double[][] transpose(double[][] a) {
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = a[i][j];
            }
        }
        return result;
    }

    public static double[][] matrixSubtraction(double[][] a, double[][] b) {
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] - b[i][j];
            }
        }
        return result;
    }

    public static double[][] matrixAddition(double[][] a, double[][] b) {
        int rows = a.length;
        int cols = a[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = a[i][j] + b[i][j];
            }
        }
        return result;
    }

    public static double[][] matrixScalarMultiplication(double scalar, double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i][j] = scalar * matrix[i][j];
            }
        }
        return result;
    }

    public static double[][] normalizeColumns(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[rows][cols];
        for (int j = 0; j < cols; j++) {
            double norm = 0;
            for (int i = 0; i < rows; i++) {
                norm += Math.pow(matrix[i][j], 2);
            }
            norm = Math.sqrt(norm);
            for (int i = 0; i < rows; i++) {
                result[i][j] = matrix[i][j] / norm;
            }
        }
        return result;
    }
}
