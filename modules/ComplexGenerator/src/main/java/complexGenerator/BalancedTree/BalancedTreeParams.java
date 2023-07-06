package complexGenerator.BalancedTree;

import Helpers.GenericParamForm.Params;

import java.util.ArrayList;
import java.util.List;

public class BalancedTreeParams extends Params<BalancedTree> {
    public Integer r = 3;
    public Integer h = 5;

    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("r - degree of the root ");
        description.add("h - a height od the tree");
        description.add("Conditions:");
        description.add("r >=2:");

        return description;
    }

    @Override
    public void SetGeneratorParams(BalancedTree balancedTree) {
        balancedTree.seth(h);
        balancedTree.setr(r);
    }
}
