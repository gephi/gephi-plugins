package complexGenerator.BarabasiAlbert.SimplifiedA;

import Helpers.GenericParamForm.Params;

public class BarabasiAlbertSimplifiedAParams extends Params<BarabasiAlbertSimplifiedA> {
    public Integer N;
    public Integer M;
    public Integer m0;

    @Override
    public void SetGeneratorParams(BarabasiAlbertSimplifiedA barabasiAlbert) {
        barabasiAlbert.setM(M);
        barabasiAlbert.setN(N);
        barabasiAlbert.setm0(m0);
    }
}
