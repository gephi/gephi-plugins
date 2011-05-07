package org.gephi.neo4j.plugin.impl;


import java.util.Collection;
import org.gephi.neo4j.plugin.api.FilterDescription;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.helpers.Predicate;


class DepthAndNodeFilterEvaluator implements Evaluator {
    private final Evaluator depthEvaluator;
    private final Predicate<Path> nodeReturnFilter;


    DepthAndNodeFilterEvaluator(Collection<FilterDescription> filterDescriptions, boolean restrictMode,
            boolean matchCase, int maxDepth) {
        depthEvaluator = Evaluators.toDepth(maxDepth);

        nodeReturnFilter = new NodeReturnFilter(filterDescriptions, restrictMode, matchCase);
    }


    @SuppressWarnings("fallthrough")
    @Override
    public Evaluation evaluate(Path path) {
        Evaluation evaluation = depthEvaluator.evaluate(path);

        boolean returnNode = nodeReturnFilter.accept(path);

        switch (evaluation) {
            case INCLUDE_AND_CONTINUE:
                if (!returnNode)
                    return Evaluation.EXCLUDE_AND_CONTINUE;

            case INCLUDE_AND_PRUNE:
                if (!returnNode)
                    return Evaluation.EXCLUDE_AND_PRUNE;

            // for both EXCLUDE_AND_CONTINUE and EXCLUDE_AND_PRUNE:
            default:
                return evaluation;
        }
    }
}
