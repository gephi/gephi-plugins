package complexGenerator.ErdosRenyi.Gnm;

import Helpers.GenericParamForm.Params;

import java.util.ArrayList;
import java.util.List;

public class ErdosRenyiGnmParams extends Params<ErdosRenyiGnm> {
    public Integer n = 50;
    public Integer m = 50;

    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("n - number of nodes in generated network");
        description.add("m - number of edges in generated network");
        description.add("Conditions:");
        description.add("n > 0");
        description.add("0 <= m <= n*(n-1)/2");

        return description;
    }

    @Override
    public void SetGeneratorParams(ErdosRenyiGnm erdosRenyiGnm) {
        erdosRenyiGnm.setm(m);
        erdosRenyiGnm.setn(n);
    }
}
