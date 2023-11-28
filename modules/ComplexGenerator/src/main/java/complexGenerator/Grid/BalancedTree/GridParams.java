package complexGenerator.Grid.BalancedTree;

import Helpers.GenericParamForm.Params;

import java.util.ArrayList;
import java.util.List;

public class GridParams extends Params<Grid> {
    public Integer w = 3;
    public Integer h = 5;
    public Boolean looped = false;

    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("h - height of grid");
        description.add("w - width of grid");
        description.add("looped - if grid has to be looped by width");
        return description;
    }

    @Override
    public void SetGeneratorParams(Grid balancedTree) {
        balancedTree.seth(h);
        balancedTree.setw(w);
        balancedTree.setlooped(looped);
    }
}
