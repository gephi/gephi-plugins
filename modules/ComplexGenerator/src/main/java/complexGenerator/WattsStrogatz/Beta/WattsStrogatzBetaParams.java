package complexGenerator.WattsStrogatz.Beta;

import Helpers.GenericParamForm.Params;
import complexGenerator.Kleinberg.Kleinberg;

public class WattsStrogatzBetaParams extends Params<WattsStrogatzBeta> {
    private Integer    N    = 20;
    private Integer    K    = 4;
    private Double beta = 0.2;

    @Override
    public void SetGeneratorParams(WattsStrogatzBeta wattsStrogatzBeta) {
        wattsStrogatzBeta.setN(N);
        wattsStrogatzBeta.setK(K);
        wattsStrogatzBeta.setbeta(beta);
    }
}
