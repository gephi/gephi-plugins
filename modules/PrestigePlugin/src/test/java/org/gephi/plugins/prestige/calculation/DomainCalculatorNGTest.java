/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.plugins.prestige.calculation;

import org.testng.annotations.Test;

/**
 *
 * @author Michael Henninger <gephi@michihenninger.ch>
 */
public class DomainCalculatorNGTest extends AbstractCalculatorNGTest {

    private final DomainCalculator calc = new DomainCalculator();

    @Test(timeOut = 2000)
    public void emptyGraph_AllZeroPrestige() {
        super.emptyGraph_AllZeroPrestige(calc);
    }

    @Test(timeOut = 2000)
    public void unconnectedGraph_OneNode_ZeroPrestige() {
        super.unconnectedGraph_OneNode_ZeroPrestige(calc);
    }

    @Test(timeOut = 2000)
    public void unconnectedGraph_FiveNodes_AllZeroPrestige() {
        super.unconnectedGraph_FiveNodes_AllZeroPrestige(calc);
    }

    @Test(timeOut = 2000)
    public void circleGraph_FiveNodes_AllSamePrestige() {
        super.circleGraph_FiveNodes_AllSamePrestige(calc, 1D, -1);
    }

    @Test(timeOut = 2000)
    public void completeGraph_FiveNodes_AllSamePrestige() {
        super.completeGraph_FiveNodes_AllSamePrestige(calc, 1D, -1);
    }

    @Test(timeOut = 2000)
    public void completeGraph_FiveNodesAndParallelEdges_AllSamePrestige() {
        super.completeGraph_FiveNodesAndParallelEdges_AllSamePrestige(calc, 1D, -1);
    }

    @Test(timeOut = 2000)
    public void completeGraph_FiveNodesAndSelfLoops_AllSamePrestige() {
        super.completeGraph_FiveNodesAndSelfLoops_AllSamePrestige(calc, 1D, -1);
    }

    @Test(timeOut = 5000)
    public void completeGraph_FiveNodes_MultipleExecutions_AllSamePrestige() {
        super.completeGraph_FiveNodes_MultipleExecutions_AllSamePrestige(calc, 1D, -1);
    }

    @Test(timeOut = 2000)
    public void starGraph_FiveNodes_AllOuterZeroCenterOneLarger() {
        super.starGraph_FiveNodes_AllOuterZeroCenterOneLarger(calc, 1D, -1);
    }

    @Override
    protected String getPrestigeColumnKey() {
        return DomainCalculator.DOMAIN_KEY;
    }

    @Override
    protected String getNormalizedPrestigeColumnKey() {
        return null;
    }

}
