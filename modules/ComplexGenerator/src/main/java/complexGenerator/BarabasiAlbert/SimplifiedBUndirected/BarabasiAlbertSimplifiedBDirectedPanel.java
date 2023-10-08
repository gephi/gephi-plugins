package complexGenerator.BarabasiAlbert.SimplifiedBUndirected;

import Helpers.GenericParamForm.GenericPanel;

public class BarabasiAlbertSimplifiedBDirectedPanel extends GenericPanel<BarabasiAlbertSimplifiedBDirectedParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new BarabasiAlbertSimplifiedBDirectedParams());
    }
}
