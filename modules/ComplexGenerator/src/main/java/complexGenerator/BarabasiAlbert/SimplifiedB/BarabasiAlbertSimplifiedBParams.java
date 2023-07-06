package complexGenerator.BarabasiAlbert.SimplifiedB;

import Helpers.GenericParamForm.Params;
import complexGenerator.BarabasiAlbert.SimplifiedA.BarabasiAlbertSimplifiedA;

import java.util.ArrayList;
import java.util.List;

public class BarabasiAlbertSimplifiedBParams extends Params<BarabasiAlbertSimplifiedB> {
    public Integer N = 50;
    public Integer M = 50;

    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("N - number of nodes in generated network ");
        description.add("M - number of edges in generated network");
        description.add("Conditions:");
        description.add("N > 0");
        description.add("0 < M <= N*(N-1)/2");
        return description;
    }

    @Override
    public void SetGeneratorParams(BarabasiAlbertSimplifiedB barabasiAlbert) {
        barabasiAlbert.setM(M);
        barabasiAlbert.setN(N);
    }
}
