package complexGenerator.ABCD;

import Helpers.GenericParamForm.Params;
import java.util.ArrayList;
import java.util.List;

public class ABCDParams extends Params<ABCD> {

    public Integer n = 1000;
    public Double Imax = 1000.0;
    public Integer cmin = 10;
    public Integer cmax = 100;
    public Double beta = 3.5;
    public Integer wmin = 2;
    public Integer wmax = 10;
    public Double gamma = 2.0;
    public Double xi = 0.1;


    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("N - Number of nodes");
        description.add("Imax - Maximum number of iteration while generating nodes and communities (default number should be enough)");
        description.add("NODES SECTION");
        description.add("wmin - Minimum degree with one node");
        description.add("wmax - Maxium degree with one node");
        description.add("gamma - Degree power law exponent");
        description.add("COMMUNITIES SECTION");
        description.add("cmin - Minimum size of community");
        description.add("cmax - Maximum size of community");
        description.add("beta - Size power law exponent");
        description.add("xi - Mixing parameter for connections outside communities");

        return description;
    }

    @Override
    public void SetGeneratorParams(ABCD abcd) {
        abcd.setN(n);
        abcd.setImax(Imax);
        abcd.setCmin(cmin);
        abcd.setCmax(cmax);
        abcd.setBeta(beta);
        abcd.setWmin(wmin);
        abcd.setWmax(wmax);
        abcd.setGamma(gamma);
        abcd.setXi(xi);
    }
}

