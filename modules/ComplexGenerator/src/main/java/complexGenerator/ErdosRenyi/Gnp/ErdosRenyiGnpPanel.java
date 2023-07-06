package complexGenerator.ErdosRenyi.Gnm;

import Helpers.GenericParamForm.GenericPanel;

public class ErdosRenyiGnmPanel extends GenericPanel<ErdosRenyiGnmParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new ErdosRenyiGnmParams());
    }
}
