package complexGenerator.BarabasiAlbert.GeneralizedDirected;

import Helpers.GenericParamForm.GenericPanel;

public class BarabasiAblertGeneralizedDirectedPanel extends GenericPanel<BarabasiAlbertGeneralizedDirectedParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new BarabasiAlbertGeneralizedDirectedParams());
    }
}