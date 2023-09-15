package ReachMetrics;

import GenericParamForm.GenericPanel;
import java.awt.GridBagConstraints;
import java.util.concurrent.atomic.AtomicInteger;

public class ReachMetricsPanel extends GenericPanel<ReachMetricsParam> {

    @Override
    protected void CreateParamObject() {
        this.setTParams(new ReachMetricsParam());
    }

    @Override
    protected void CreateCustomFieldOption(GridBagConstraints constraints, AtomicInteger gridYIterator) {
    }
}