package complexGenerator.ABCD;

import Helpers.GenericParamForm.GenericPanel;

public class ABCDPanel extends GenericPanel<ABCDParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new ABCDParams());
    }
}
