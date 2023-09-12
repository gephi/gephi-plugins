package AverageGlobalConnectionEfficiency;

import GenericParamForm.Params;
import java.util.ArrayList;
import java.util.List;

public class AverageGlobalConnectionEfficiencyParam extends Params<AverageGlobalConnectionEfficiency> {

    public Boolean directed = false;
    public Integer samplesCount = 10;
    public Integer k = 0;
    public Boolean exactlyK = false;
    public MsType msType = MsType.Random;

    @Override
    protected String ShortDescription() {
        return "Measures the average global connection efficiency of the network. In each iteration k or at most k nodes (in case of Random Random) will be removed using one of the methods below.";
    }

    // todo: multiple option builder :)
//    private String mstype = "Random";
    @Override
    protected List<String> Descritpion() {
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("directed - is graph directed (default undirected)");
        description.add("samplesCount - count of uses saples");
        description.add("Removal strategy options:");
        description.add("k - count of uses samples");
        description.add("exactlyK - is k exactly restricted");
        description.add("Removal Strategy.");
        return description;
    }

    @Override
    public void SetGeneratorParams(AverageGlobalConnectionEfficiency averageGlobalConnectionEfficiency) {
        averageGlobalConnectionEfficiency.setDirected(directed);
        averageGlobalConnectionEfficiency.setK(k);
        averageGlobalConnectionEfficiency.setSamplesCount(samplesCount);
        averageGlobalConnectionEfficiency.setExactlyK(exactlyK);
        averageGlobalConnectionEfficiency.setMstype(msType);
    }
}
