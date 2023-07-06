package complexGenerator.WattsStrogatz.Alpha;

import Helpers.GenericParamForm.Params;

public class WattsStrigatzAlphaParams extends Params<WattsStrogatzAlpha> {
    private Integer    n     = 20;
    private Integer    k     = 4;
    private Double alpha = 3.5;

    @Override
    public void SetGeneratorParams(WattsStrogatzAlpha wattsStrogatzAlpha) {
        wattsStrogatzAlpha.setn(n);
        wattsStrogatzAlpha.setk(k);
        wattsStrogatzAlpha.setalpha(alpha);
    }
}
