package complexGenerator.WattsStrogatz.Beta;

import Helpers.GenericParamForm.Params;

import java.util.ArrayList;
import java.util.List;

public class WattsStrogatzBetaParams extends Params<WattsStrogatzBeta> {
    public Integer N = 20;
    public Integer K = 4;
    public Double beta = 0.2;

    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("N - the desired number of nodes");
        description.add("K - the number of edges connected to each node");
        description.add("alpha - the probability of an edge being rewired randomly");
        description.add("Conditions:");
        description.add("1 <= ln(N) < K < N");
        description.add("K is even");
        description.add("0 <= beta <= 1");

        return description;
    }

    @Override
    public void SetGeneratorParams(WattsStrogatzBeta wattsStrogatzBeta) {
        wattsStrogatzBeta.setN(N);
        wattsStrogatzBeta.setK(K);
        wattsStrogatzBeta.setbeta(beta);
    }
}
