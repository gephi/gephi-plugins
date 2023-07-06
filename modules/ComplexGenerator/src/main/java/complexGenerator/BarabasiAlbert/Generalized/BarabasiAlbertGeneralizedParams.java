package complexGenerator.BarabasiAlbert.Generalized;

import Helpers.GenericParamForm.Params;
import complexGenerator.BalancedTree.BalancedTree;

import java.util.ArrayList;
import java.util.List;

public class BarabasiAlbertGeneralizedParams extends Params<BarabasiAlbertGeneralized> {
    public Integer N = 50;
    public Integer M = 1;
    public Integer m0 = 1;
    public Double p = 0.25;
    public Double q = 0.25;

    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("N - number of the algorithm;s steps ");
        description.add("m0 - number of isolated nodes at the start time");
        description.add("M - number of edges add, rewire ora coming with a node in every step");
        description.add("p - probability od adding new edges");
        description.add("q - probability of rewiring exising edges");
        description.add("Conditions:");
        description.add("N > 0");
        description.add("m0 > 0");
        description.add("0 < M <= m0");
        description.add("0 <= p < 1");
        description.add("0 <= q < 1");

        return description;
    }

    @Override
    public void SetGeneratorParams(BarabasiAlbertGeneralized barabasiAlbert) {
        barabasiAlbert.setM(M);
        barabasiAlbert.setN(N);
        barabasiAlbert.setm0(m0);
        barabasiAlbert.setp(p);
        barabasiAlbert.setq(q);
    }
}
