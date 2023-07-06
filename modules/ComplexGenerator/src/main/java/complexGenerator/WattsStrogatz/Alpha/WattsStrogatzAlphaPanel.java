package complexGenerator.WattsStrogatz.Alpha;

import Helpers.GenericParamForm.GenericPanel;

public class WattsStrogatzAlphaPanel extends GenericPanel<WattsStrigatzAlphaParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new WattsStrigatzAlphaParams());
    }
}
