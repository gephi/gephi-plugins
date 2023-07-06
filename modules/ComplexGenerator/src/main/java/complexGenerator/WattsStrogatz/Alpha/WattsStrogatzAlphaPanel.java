package complexGenerator.WattsStrogatz.Alpha;

import Helpers.GenericParamForm.GenericPanel;

public class WattsStrogatzAlphaPanel extends GenericPanel<WattsStrogatzAlphaParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new WattsStrogatzAlphaParams());
    }
}
