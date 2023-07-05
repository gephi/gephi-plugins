package complexGenerator.BalancedTree;

import Helpers.GenericParamForm.Params;

public class BalancedTreeParams extends Params<BalancedTree> {
    public Integer r;
    public Integer h;

    @Override
    public void SetGeneratorParams(BalancedTree balancedTree) {
        balancedTree.seth(h);
        balancedTree.setr(r);
    }
}
