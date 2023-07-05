package complexGenerator.BalancedTree;

import Helpers.GenericParamForm.GenericPanel;

public class BalancedTreePanel extends GenericPanel<BalancedTreeParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new BalancedTreeParams());
    }
}
