package complexGenerator.Grid.BalancedTree;

import Helpers.GenericParamForm.GenericPanel;

public class GrdPanel extends GenericPanel<GridParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new GridParams());
    }
}
