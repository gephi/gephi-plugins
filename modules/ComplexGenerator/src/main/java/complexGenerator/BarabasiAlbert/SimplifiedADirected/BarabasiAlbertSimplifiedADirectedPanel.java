package complexGenerator.BarabasiAlbert.SimplifiedADirected;

import Helpers.GenericParamForm.GenericPanel;

public class BarabasiAlbertSimplifiedADirectedPanel extends GenericPanel<BarabasiAlbertSimplifiedADirectedParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new BarabasiAlbertSimplifiedADirectedParams());
    }
}
