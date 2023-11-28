package complexGenerator.Grid.BalancedTree;

import Helpers.GenericParamForm.Params;

import java.util.ArrayList;
import java.util.List;

public class GridParams extends Params<Grid> {
    public Integer w = 5;
    public Integer h = 5;
    public Boolean loopedW = false;
    public Boolean loopedH = false;

    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("h - height of grid");
        description.add("w - width of grid");
        description.add("loopedW - if grid has to be looped by width");
        description.add("loopedH - if grid has to be looped by height");
        return description;
    }

    @Override
    public void SetGeneratorParams(Grid balancedTree) {
        balancedTree.seth(h);
        balancedTree.setw(w);
        balancedTree.setloopedW(loopedW);
        balancedTree.setloopedH(loopedH);
    }
}
