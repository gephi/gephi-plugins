package DisjoinMetricDistance;

import GenericParamForm.GenericPanel;
import java.awt.GridBagConstraints;
import java.util.concurrent.atomic.AtomicInteger;

public class DisjoinMetricDistancePanel extends GenericPanel<DisjoinMetricDistanceParam> {

    @Override
    protected void CreateParamObject() {
        this.setTParams(new DisjoinMetricDistanceParam());
    }

    @Override
    protected void CreateCustomFieldOption(GridBagConstraints constraints, AtomicInteger gridYIterator) {
    }
}