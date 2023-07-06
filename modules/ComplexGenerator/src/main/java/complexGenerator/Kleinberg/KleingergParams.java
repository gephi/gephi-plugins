package complexGenerator.Kleinberg;

import Helpers.GenericParamForm.Params;
import complexGenerator.ErdosRenyi.Gnp.ErdosRenyiGnp;

public class KleingergParams extends Params<Kleinberg> {
    private Integer n = 10;
    private Integer p = 2;
    private Integer q = 2;
    private Integer r = 0;

    @Override
    public void SetGeneratorParams(Kleinberg kleinberg) {
        kleinberg.setp(p);
        kleinberg.setq(q);
        kleinberg.setr(r);
        kleinberg.setn(n);
    }
}
