package org.gephi.plugins.linkprediction.statistics;

import org.gephi.plugins.linkprediction.base.LinkPredictionStatisticsUI;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = StatisticsUI.class)
// TODO: Use LinkPredictionMacro to make sure only one Statistics UI exists
public class PreferentialAttachmentStatisticsUI extends LinkPredictionStatisticsUI {

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return PreferentialAttachmentStatistics.class;
    }

    @Override
    public String getDisplayName() {
        return "Preferential Attachment Link Prediction";
    }

}
