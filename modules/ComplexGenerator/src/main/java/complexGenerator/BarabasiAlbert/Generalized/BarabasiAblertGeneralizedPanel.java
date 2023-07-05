package complexGenerator.BarabasiAlbert.Generalized;

import Helpers.GenericParamForm.GenericPanel;

public class BarabasiAblertGeneralizedPanel extends GenericPanel<BarabasiAlbertGeneralizedParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new BarabasiAlbertGeneralizedParams());
    }
}