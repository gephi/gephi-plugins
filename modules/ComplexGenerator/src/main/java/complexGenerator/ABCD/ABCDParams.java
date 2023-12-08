package complexGenerator.ABCD;

import Helpers.GenericParamForm.Params;

import java.util.ArrayList;
import java.util.List;

public class ABCDParams extends Params<ABCD> {
    public Integer n = 100;
    public Double kAvg = 4.0;
    public Double kMin = 4.0;
    public Double kMax = 4.0;
    public Double gamma = 5.0;
    public Double beta = 5.0;
    public Double mixing = 0.2;
    public Double sMin = 0.2;
    public Double sMax = 0.2;


    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("N - number of nodes");
        description.add("h - a height od the tree");
        description.add("Conditions:");
        description.add("r >=2:");

        return description;
    }

    @Override
    public void SetGeneratorParams(ABCD abcd) {

    }
}

