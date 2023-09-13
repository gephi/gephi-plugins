package ReachMetrics;

import GenericParamForm.Params;
import java.util.ArrayList;
import java.util.List;

public class ReachMetricsParam extends Params<ReachMetrics> {

    @Override
    protected String ShortDescription() {
        return "?";
    }

    @Override
    protected List<String> Descritpion() {
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("directed - is graph directed (default undirected)");
        return description;
    }

    @Override
    public void SetGeneratorParams(ReachMetrics reachMetrics) {
    }

}
