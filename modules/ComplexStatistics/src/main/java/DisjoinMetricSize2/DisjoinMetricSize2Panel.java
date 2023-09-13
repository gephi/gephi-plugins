package DisjoinMetricSize2;

import GenericParamForm.GenericPanel;
import java.awt.GridBagConstraints;
import java.util.concurrent.atomic.AtomicInteger;

public class DisjoinMetricSize2Panel extends GenericPanel<DisjoinMetricSize2Param> {

    @Override
    protected void CreateParamObject() {
        this.setTParams(new DisjoinMetricSize2Param());
    }

    @Override
    protected void CreateCustomFieldOption(GridBagConstraints constraints, AtomicInteger gridYIterator) {
    }
}