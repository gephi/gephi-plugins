package org.openuniversityofisrael.katzcentrality;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;
import org.gephi.graph.api.*;
import org.junit.Assert;
import org.junit.Test;

public class KatzCentralityTest {

    private static final double EPSILON = 1e-4;

    @Test
    public void testDirectedGraphWithoutWeights() {
        GraphModel model = GraphGenerator.generateDirectedGraphWithoutWeights();
        KatzCentrality kc = new KatzCentrality();
        kc.setDirected(true);
        kc.execute(model);
        double[] expectedVector = new double[] {1.0, 1.5, 2.25};
        assertResultVector(kc.getResultVector(), expectedVector);

    }

    @Test
    public void testGraphWithoutEdges() {
        GraphModel model = GraphGenerator.generateGraphWithoutEdges();
        KatzCentrality kc = new KatzCentrality();

        kc.execute(model);
        Assert.assertEquals(kc.getError(), KatzCentrality.NO_EDGES_MESSAGE);

    }

    @Test
    public void testUndirectedGraphWithLoop() {
        GraphModel model = GraphGenerator.generateUndirectedGraphWithSelfLoop();
        KatzCentrality kc = new KatzCentrality();
        kc.setDirected(false);
        kc.execute(model);
        double[] expectedVector = new double[]{-4.0, -4.0, -6};
        assertResultVector(kc.getResultVector(), expectedVector);

    }

    @Test
    public void testDirectedGraphWithWeights() {
        GraphModel model = GraphGenerator.generateGraphWithWeights(true, false);
        KatzCentrality kc = new KatzCentrality();
        kc.setDirected(true);
        kc.execute(model);
        double[] expectedVector = new double[]{1.0, 3.5, 22};
        assertResultVector(kc.getResultVector(), expectedVector);
    }

    @Test
    public void testUndirectedGraphWithWeights() {
        GraphModel model = GraphGenerator.generateGraphWithWeights(false, false);
        KatzCentrality kc = new KatzCentrality();
        kc.setDirected(false);
        kc.execute(model);
        double[] expectedVector = new double[]{-0.0923, -0.1730, -0.1884};
        assertResultVector(kc.getResultVector(), expectedVector);
    }

    @Test
    public void testSingularMatrix() {
        GraphModel model = GraphGenerator.generateSingularMatrixGraph(true);
        KatzCentrality kc = new KatzCentrality();
        kc.setDirected(false);
        kc.setAlpha(1);
        kc.execute(model);

        Assert.assertEquals(kc.getError(), KatzCentrality.SINGULAR_MATRIX_ERROR_MESSAGE);
        Assert.assertEquals(kc.getReport().contains(KatzCentrality.SINGULAR_MATRIX_ERROR_MESSAGE), true);
    }

    @Test
    public void testGraphWithoutLabels() {
        GraphModel model = GraphGenerator.generateGraphWithWeights(true, false);
        KatzCentrality kc = new KatzCentrality();
        kc.setDirected(true);
        kc.execute(model);
        String report = kc.getReport();
        String[] lines = report.split("\n");
        int firstRow = 2;
        String rowLabel = String.format("data-type=\"%s\">2<", KatzCentrality.LABEL);
        Assert.assertEquals(lines[firstRow].contains(rowLabel), true);
    }

    @Test
    public void testGraphWithLabels() {
        GraphModel model = GraphGenerator.generateGraphWithWeights(true, true);
        KatzCentrality kc = new KatzCentrality();
        kc.setDirected(true);
        kc.execute(model);
        String report = kc.getReport();
        String[] lines = report.split("\n");
        int firstRow = 2;
        String rowLabel = String.format("data-type=\"%s\">n2<", KatzCentrality.LABEL);
        Assert.assertEquals(lines[firstRow].contains(rowLabel), true);
    }

    @Test
    public void testGraphWithNegativeEigenvalues() {
        GraphModel model = GraphGenerator.generateGraphResultingInNegativeEigenvalues(false);
        KatzCentrality kc = new KatzCentrality();
        kc.setDirected(false);
        kc.setAlpha(0.5);
        kc.execute(model);
        Assert.assertEquals(kc.getError(), KatzCentrality.SINGULAR_MATRIX_ERROR_MESSAGE);
    }

    private void assertResultVector(RealMatrix resultVector, double[] expectedVector) {
        double[] vector = resultVector.getColumnVector(0).toArray();

        for (int i = 0; i < vector.length; i++) {
            Assert.assertEquals(vector[i], expectedVector[i], EPSILON);
        }
    }
}
