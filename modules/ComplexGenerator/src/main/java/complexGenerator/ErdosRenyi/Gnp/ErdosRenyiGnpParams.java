package complexGenerator.ErdosRenyi.Gnp;

import Helpers.GenericParamForm.Params;
import complexGenerator.ErdosRenyi.Gnm.ErdosRenyiGnm;

import java.util.ArrayList;
import java.util.List;

public class ErdosRenyiGnpParams extends Params<ErdosRenyiGnp> {
    public Integer n;
    public Double p;

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
    public void SetGeneratorParams(ErdosRenyiGnp erdosRenyiGnp) {
        erdosRenyiGnp.setp(p);
        erdosRenyiGnp.setn(n);
    }
}
