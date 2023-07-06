package complexGenerator.WattsStrogatz.Alpha;

import Helpers.GenericParamForm.Params;

public class WattsStrogatzAlphaParams extends Params<WattsStrogatzAlpha> {
    private Integer n;
    private Integer k;
    private Double alpha;

    @Override
    public void SetGeneratorParams(WattsStrogatzAlpha wattsStrogatzAlpha) {
        wattsStrogatzAlpha.setn(n);
        wattsStrogatzAlpha.setk(k);
        wattsStrogatzAlpha.setalpha(alpha);
    }
}
