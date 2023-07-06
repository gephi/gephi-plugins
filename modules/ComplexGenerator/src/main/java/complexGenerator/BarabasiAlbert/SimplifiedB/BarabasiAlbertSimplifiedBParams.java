package complexGenerator.BarabasiAlbert.SimplifiedB;

import Helpers.GenericParamForm.Params;
import complexGenerator.BarabasiAlbert.SimplifiedA.BarabasiAlbertSimplifiedA;

public class BarabasiAlbertSimplifiedBParams extends Params<BarabasiAlbertSimplifiedB> {
    public Integer N;
    public Integer M;
    public Integer m0;

    @Override
    public void SetGeneratorParams(BarabasiAlbertSimplifiedB barabasiAlbert) {
        barabasiAlbert.setM(M);
        barabasiAlbert.setN(N);
    }
}
