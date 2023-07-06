package complexGenerator.MultiGenerator;

import Helpers.GenericParamForm.GenericPanel;

public class MultiGeneratorPanel extends GenericPanel<MultiGeneratorParams> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new MultiGeneratorParams());
    }
}
