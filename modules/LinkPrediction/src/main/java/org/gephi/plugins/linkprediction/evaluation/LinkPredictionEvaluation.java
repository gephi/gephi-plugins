package org.gephi.plugins.linkprediction.evaluation;

import org.gephi.graph.api.GraphModel;
import org.gephi.plugins.linkprediction.base.EvaluationMetric;
import org.gephi.statistics.spi.Statistics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.*;

/**
 * Macro class that triggers the evaluation calculation for all selected algorithms.
 */
public class LinkPredictionEvaluation implements Statistics {

    private HashMap<String, Double> allAccuracies = new HashMap<String, Double>();

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
        html += "<h2>Accuracy:</h2>";

        for(EvaluationMetric e : evaluations) {
            //html += "<br> Accuracy: " + String.valueOf(e.getResult()) + "<br />";
            allAccuracies.put(e.getAlgorithmName(), e.getResult());
        }

        HashMap<String, Double> sortedValues = sortByValue(allAccuracies);

        int counter = 1;

        for (Map.Entry<String, Double> elem : sortedValues.entrySet()) {
            html += "<br>" + counter + ". " + elem.getKey() + ": " + String.valueOf(elem.getValue()) + "<br />";
            counter++;
        }

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

    public static HashMap<String, Double> sortByValue(HashMap<String, Double> allValues)
    {

        LinkedHashMap<String, Double> allValuesSorted = new LinkedHashMap<>();

        allValues.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(x -> allValuesSorted.put(x.getKey(), x.getValue()));

        return allValuesSorted;
    }
}
