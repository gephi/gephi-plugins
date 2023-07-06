package complexGenerator.BarabasiAlbert.Multi;

import Helpers.GenericParamForm.GenericPanel;

public class MultiBarabasiAlbertPanel extends GenericPanel<MultiBarabasiAlbertParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new MultiBarabasiAlbertParams());
    }
}