/*
Copyright 2008-2011 Gephi
Authors : Martin Škurla
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.neo4j.plugin.impl;


import java.util.Collection;
import org.gephi.neo4j.plugin.api.FilterDescription;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.traversal.Evaluation;
import org.neo4j.graphdb.traversal.Evaluator;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.helpers.Predicate;


/**
 *
 * @author Martin Škurla
 */
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
