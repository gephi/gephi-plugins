package complexGenerator.WattsStrogatz.Alpha;

import Helpers.GenericParamForm.Params;

import java.util.ArrayList;
import java.util.List;

public class WattsStrogatzAlphaParams extends Params<WattsStrogatzAlpha> {
    public Integer n = 20;
    public Integer k = 4;
    public Double alpha = 3.5;

    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("n - the desired number of nodes");
        description.add("k - the average degree of the graph");
        description.add("alpha - a tunable parameter");
        description.add("Conditions:");
        description.add("0 < k < n");
        description.add("alpha >= 0");

        return description;
    }

    @Override
    public void SetGeneratorParams(WattsStrogatzAlpha wattsStrogatzAlpha) {
        wattsStrogatzAlpha.setn(n);
        wattsStrogatzAlpha.setk(k);
        wattsStrogatzAlpha.setalpha(alpha);
    }
}
