package complexGenerator.BarabasiAlbert.Generalized;

import Helpers.GenericParamForm.Params;
import complexGenerator.BalancedTree.BalancedTree;

public class BarabasiAlbertGeneralizedParams extends Params<BarabasiAlbertGeneralized> {
    public Integer N;
    public Integer M;
    public Integer m0;
    public Double p;
    public Double q;

    @Override
    public void SetGeneratorParams(BarabasiAlbertGeneralized barabasiAlbert) {
        barabasiAlbert.setM(M);
        barabasiAlbert.setN(N);
        barabasiAlbert.setm0(m0);
        barabasiAlbert.setp(p);
        barabasiAlbert.setq(q);
    }
}
