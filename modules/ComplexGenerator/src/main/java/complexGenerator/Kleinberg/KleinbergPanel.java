package complexGenerator.Kleinberg;

import Helpers.GenericParamForm.GenericPanel;

public class KleinbergPanel extends GenericPanel<KleingergParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new KleingergParams());
    }
}
