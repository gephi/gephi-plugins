package complexGenerator.BarabasiAlbert.SimplifiedA;

import Helpers.GenericParamForm.Params;

import java.util.ArrayList;
import java.util.List;

public class BarabasiAlbertSimplifiedAParams extends Params<BarabasiAlbertSimplifiedA> {
    public Integer N = 50;
    public Integer M = 1;
    public Integer m0 = 1;

    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("N - number of nodes in generated network ");
        description.add("m0 - number of nodes at the start time");
        description.add("M - number of edges coming with every new node");
        description.add("Conditions:");
        description.add("N > 0");
        description.add("0 < m0 < N");
        description.add("0 < M <= m0");

        return description;
    }

    @Override
    public void SetGeneratorParams(BarabasiAlbertSimplifiedA barabasiAlbert) {
        barabasiAlbert.setM(M);
        barabasiAlbert.setN(N);
        barabasiAlbert.setm0(m0);
    }
}
