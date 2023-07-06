package complexGenerator.ErdosRenyi.Gnp;

import Helpers.GenericParamForm.Params;
import complexGenerator.ErdosRenyi.Gnm.ErdosRenyiGnm;

public class ErdosRenyiGnpParams extends Params<ErdosRenyiGnp> {
    public Integer n;
    public Double p;

    @Override
    public void SetGeneratorParams(ErdosRenyiGnp erdosRenyiGnp) {
        erdosRenyiGnp.setp(p);
        erdosRenyiGnp.setn(n);
    }
}
