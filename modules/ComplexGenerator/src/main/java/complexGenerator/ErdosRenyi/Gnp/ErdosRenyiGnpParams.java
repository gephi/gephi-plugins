package complexGenerator.ErdosRenyi.Gnm;

import Helpers.GenericParamForm.Params;

public class ErdosRenyiGnmParams extends Params<ErdosRenyiGnm> {
    public Integer n;
    public Integer m;

    @Override
    public void SetGeneratorParams(ErdosRenyiGnm erdosRenyiGnm) {
        erdosRenyiGnm.setm(m);
        erdosRenyiGnm.setn(n);
    }
}
