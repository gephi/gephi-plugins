package DisjoinMetricSize1;

import GenericParamForm.GenericPanel;
import java.awt.GridBagConstraints;
import java.util.concurrent.atomic.AtomicInteger;

public class DisjoinMetricSize1Panel extends GenericPanel<DisjoinMetricSize1Param> {

    @Override
    protected void CreateParamObject() {
        this.setTParams(new DisjoinMetricSize1Param());
    }

    @Override
    protected void CreateCustomFieldOption(GridBagConstraints constraints, AtomicInteger gridYIterator) {
    }
}