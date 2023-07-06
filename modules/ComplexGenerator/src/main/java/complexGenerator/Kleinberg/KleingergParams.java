package complexGenerator.Kleinberg;

import Helpers.GenericParamForm.Params;
import complexGenerator.ErdosRenyi.Gnp.ErdosRenyiGnp;

import java.util.ArrayList;
import java.util.List;

public class KleingergParams extends Params<Kleinberg> {
    public Integer n = 10;
    public Integer p = 2;
    public Integer q = 2;
    public Integer r = 0;
    public Boolean torusBased = false;

    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("n - size o f lattice ");
        description.add("p - lattice distance to local contacts");
        description.add("q - long-range contacts");
        description.add("r - clustering exponent");
        description.add("torusBasted - is torus based");
        description.add("Conditions:");
        description.add("n >= 2");
        description.add("1 <= p <= 2n-2");
        description.add("0 <= q <= n^2 - p * (p+3)/2 -1 for p < n");
        description.add("0 <= q <= (2n - p -3)*(2n-p)/2+1 for p >= n");
        description.add("r >= 0");
        description.add("torusBased - logical");

        return description;
    }

    @Override
    public void SetGeneratorParams(Kleinberg kleinberg) {
        kleinberg.setp(p);
        kleinberg.setq(q);
        kleinberg.setr(r);
        kleinberg.setn(n);
        kleinberg.setTorusBased(torusBased);
    }
}
