package complexGenerator.ErdosRenyi.Gnp;

import Helpers.GenericParamForm.GenericPanel;

public class ErdosRenyiGnpPanel extends GenericPanel<ErdosRenyiGnpParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new ErdosRenyiGnpParams());
    }
}
