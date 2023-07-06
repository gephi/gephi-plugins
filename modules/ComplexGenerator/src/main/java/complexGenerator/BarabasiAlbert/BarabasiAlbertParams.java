package complexGenerator.BarabasiAlbert;

import Helpers.GenericParamForm.Params;
import complexGenerator.BalancedTree.BalancedTree;

import java.util.ArrayList;
import java.util.List;

public class BarabasiAlbertParams extends Params<BarabasiAlbert> {
    public Integer N = 50;
    public Integer M = 1;
    public Integer m0 = 1;
    public Boolean consider = false;
    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("N - number of nodes in generated network ");
        description.add("M - number of edges coming with every new node");
        description.add("m0 - number of edges coming with every new node");
        description.add("consider - Consider existing nodes");
        description.add("Conditions:");
        description.add("N > 0");
        description.add("0 < m0 < N");
        description.add("0 < M <= m0");
        description.add("consider - logic variable");

        return description;
    }

    @Override
    public void SetGeneratorParams(BarabasiAlbert barabasiAlbert) {
        barabasiAlbert.setM(M);
        barabasiAlbert.setN(N);
        barabasiAlbert.setm0(m0);
        barabasiAlbert.setConsiderExistingNodes(consider);
    }
}
