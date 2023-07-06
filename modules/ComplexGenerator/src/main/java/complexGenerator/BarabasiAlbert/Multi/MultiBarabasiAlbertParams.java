package complexGenerator.BarabasiAlbert.Multi;

import Helpers.GenericParamForm.Params;

public class MultiBarabasiAlbertParams extends Params<MultiBarabasiAlbert> {
    public Integer N;
    public Integer m0;

    @Override
    public void SetGeneratorParams(MultiBarabasiAlbert barabasiAlbert) {
        barabasiAlbert.setN(N);
        barabasiAlbert.setm0(m0);
    }
}
