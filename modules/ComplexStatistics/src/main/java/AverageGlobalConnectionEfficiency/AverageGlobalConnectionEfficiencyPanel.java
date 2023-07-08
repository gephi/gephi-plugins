package AverageGlobalConnectionEfficiency;

import GenericParamForm.GenericPanel;

public class AverageGlobalConnectionEfficiencyPanel  extends GenericPanel<AverageGlobalConnectionEfficiencyParam> {
    @Override
    protected void CreateParamObject() {
        this.setTParams(new AverageGlobalConnectionEfficiencyParam());
    }
}
