package complexGenerator.BarabasiAlbert;

import Helpers.GenericParamForm.Params;
import complexGenerator.BalancedTree.BalancedTree;

public class BarabasiAlbertParams extends Params<BarabasiAlbert> {
    public Integer N;
    public Integer M;
    public Integer m0;

    @Override
    public void SetGeneratorParams(BarabasiAlbert barabasiAlbert) {
        barabasiAlbert.setM(M);
        barabasiAlbert.setN(N);
        barabasiAlbert.setm0(m0);
    }
}
