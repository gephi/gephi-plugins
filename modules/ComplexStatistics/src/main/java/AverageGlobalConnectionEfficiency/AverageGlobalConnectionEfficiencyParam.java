package AverageGlobalConnectionEfficiency;

import GenericParamForm.Params;

import java.util.ArrayList;
import java.util.List;

public class AverageGlobalConnectionEfficiencyParam extends Params<AverageGlobalConnectionEfficiency> {

    private Boolean directed = false;
    private Integer samplesCount = 10;
    private Integer k = 0;
    private Boolean exactlyK = false;
    // todo
//    private String mstype = "Random";
    @Override
    protected List<String> Descritpion() {
        var description = new ArrayList<String>();
        description.add("Measures the average global connection efficiency of the network. In each iteration k or at most k nodes (in case of Random Random) will be removed using one of the methods below.");
        description.add("Parameters description:");
        description.add("directed - is graph directed (default undirected)");
        description.add("samplesCount - count of uses saples");
        description.add("Removal strategy options:");
        description.add("k - count of uses samples");
        description.add("exactlyK - is k exactly restricted");
        description.add("mstype - strategyK");
        return description;
    }

    @Override
    public void SetGeneratorParams(AverageGlobalConnectionEfficiency generator) {
    }
}
