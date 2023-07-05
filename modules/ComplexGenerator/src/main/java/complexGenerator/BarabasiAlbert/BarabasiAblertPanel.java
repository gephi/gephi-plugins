package complexGenerator.BarabasiAlbert;

import Helpers.GenericParamForm.GenericPanel;

public class BarabasiAblertPanel extends GenericPanel<BarabasiAlbertParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new BarabasiAlbertParams());
    }
}
