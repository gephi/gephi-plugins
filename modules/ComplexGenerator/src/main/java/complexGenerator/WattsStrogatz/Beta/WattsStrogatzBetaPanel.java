package complexGenerator.WattsStrogatz.Beta;

import Helpers.GenericParamForm.GenericPanel;

public class WattsStrogatzBetaPanel extends GenericPanel<WattsStrogatzBetaParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new WattsStrogatzBetaParams());
    }
}
