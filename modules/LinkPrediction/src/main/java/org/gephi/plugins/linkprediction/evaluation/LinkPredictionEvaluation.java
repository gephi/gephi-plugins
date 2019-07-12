package org.gephi.plugins.linkprediction.evaluation;

import org.gephi.graph.api.GraphModel;
import org.gephi.plugins.linkprediction.base.EvaluationMetric;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.statistics.spi.Statistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

/**
 * Macro class that triggers the evaluation calculation for all selected algorithms.
 */
public class LinkPredictionEvaluation implements Statistics {

    // List of link prediction evaluations
    private List<EvaluationMetric> evaluations = new ArrayList<>();

    /**
     * Calculates evaluation metrics for all evaluations.
     *
     */
    public void execute(GraphModel graphModel) {

        evaluations.stream().forEach(evaluation -> {
                evaluation.run();
        });
    }

    @Override public String getReport() {
        //This is the HTML report shown when execution ends.
        //One could add a distribution histogram for instance
        String html = "<HTML> <BODY> <h1>Evaluation of different prediction algorithms</h1> " + "<hr>";
        html += "<h2>Parameters:</h2>";
        html += "Number of Iterations: 4";

        Map<String, Double> sortedValues = new HashMap<>();
        for(EvaluationMetric e : evaluations) {
            //html += "<br> Accuracy: " + String.valueOf(e.getResult()) + "<br />";
            sortedValues.put(e.getAlgorithmName(), e.getFinalResult());
        }
        html += "<h2>Results:</h2>";
        // FIXME: HashMap<String, Double> sortedValues = sortByValue(finalResults);
        int counter = 1;
        for (Map.Entry<String, Double> elem : sortedValues.entrySet()) {
            html += "<br>" + counter + ". " + elem.getKey() + ": " + String.valueOf(elem.getValue()) + " %<br />";
            counter++;
        }

        // TODO: Extract method
        // Render diagramm
        Map<Integer, Double> testmap = new HashMap<>();
        testmap.put(1, 34.4);
        testmap.put(2, 42.3);
        testmap.put(3, 56.2);
        testmap.put(4, 57.3);

        Map<Integer, Double> testmap2 = new HashMap<>();
        testmap2.put(1, 32.4);
        testmap2.put(2, 32.3);
        testmap2.put(3, 46.2);
        testmap2.put(4, 47.3);

        XYSeriesCollection dataset = new XYSeriesCollection();
        for(EvaluationMetric e : evaluations) {
            dataset.addSeries(ChartUtils.createXYSeries(e.getIterationResults(), e.getAlgorithmName()));
        }


        JFreeChart chart = ChartFactory.createXYLineChart(
                "Development of Accuracy",
                "Iteration",
                "Accuracy in %",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        formatChart(chart);
        //ChartUtils.scaleChart(chart, dSeries, false);
        String imageFile = ChartUtils.renderChart(chart, "iteration-results.png");

        html += "<h2>Iteration Results:</h2>";
        html += "<br /><br />" + imageFile + "<br /><br />";

        html += "<h2>Algorithms:</h2>";
        html += "<br> Michael Henninger,<i> Link Prediction</i>, in Soziale Netzwerkanalyse 2018 (96)<br/>";

        html += "</BODY></HTML>";
        return html ;
    }

    public List<EvaluationMetric> getEvaluations() {
        return evaluations;
    }

    /**
     * Get specific link prediction algorithm from evaluations list
     * @param statistic Class of searched statistic
     * @return LinkPredictionStatistic
     */
    public EvaluationMetric getEvaluation(EvaluationMetric statistic) {
        return evaluations.get(evaluations.indexOf(statistic));
    }

    public void addEvaluation(EvaluationMetric evaluation) {
        if (!evaluations.contains(evaluation)) evaluations.add(evaluation);
    }

    public void removeEvaluation(EvaluationMetric evaluation) {
        if (evaluations.contains(evaluation)) evaluations.remove(evaluation);
    }

    public void resetEvaluation() {
        evaluations.clear();
    }

    // TODO: Static methoden nach oben
    public static HashMap<String, Double> sortByValue(HashMap<String, Double> allValues)
    {

        LinkedHashMap<String, Double> allValuesSorted = new LinkedHashMap<>();

        allValues.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> allValuesSorted.put(x.getKey(), x.getValue()));

        return allValuesSorted;
    }

    public static void formatChart(JFreeChart chart) {
        XYPlot plot = (XYPlot)chart.getPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        Font legendFont = new Font("SansSerif", Font.PLAIN, 16);
        renderer.setLegendTextFont(0, legendFont);
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new Ellipse2D.Double(0.0D, 0.0D, 2.0D, 2.0D));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.setRenderer(renderer);
    }

    public static void applyChartTheme(JFreeChart chart) {
        final StandardChartTheme chartTheme = (StandardChartTheme)org.jfree.chart.StandardChartTheme.createJFreeTheme();

        // The default font used by JFreeChart unable to render Chinese properly.
        // We need to provide font which is able to support Chinese rendering.
        if (Locale.getDefault().getLanguage().equals(Locale.SIMPLIFIED_CHINESE.getLanguage())) {
            final Font oldExtraLargeFont = chartTheme.getExtraLargeFont();
            final Font oldLargeFont = chartTheme.getLargeFont();
            final Font oldRegularFont = chartTheme.getRegularFont();
            final Font oldSmallFont = chartTheme.getSmallFont();

            final Font extraLargeFont = new Font("Sans-serif", oldExtraLargeFont.getStyle(), oldExtraLargeFont.getSize());
            final Font largeFont = new Font("Sans-serif", oldLargeFont.getStyle(), oldLargeFont.getSize());
            final Font regularFont = new Font("Sans-serif", oldRegularFont.getStyle(), oldRegularFont.getSize());
            final Font smallFont = new Font("Sans-serif", oldSmallFont.getStyle(), oldSmallFont.getSize());

            chartTheme.setExtraLargeFont(extraLargeFont);
            chartTheme.setLargeFont(largeFont);
            chartTheme.setRegularFont(regularFont);
            chartTheme.setSmallFont(smallFont);
        }

        chartTheme.apply(chart);
    }
}
