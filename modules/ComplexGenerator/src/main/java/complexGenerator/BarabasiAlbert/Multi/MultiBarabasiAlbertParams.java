package complexGenerator.BarabasiAlbert.Multi;

import Helpers.GenericParamForm.Params;

import java.util.ArrayList;
import java.util.List;

public class MultiBarabasiAlbertParams extends Params<MultiBarabasiAlbert> {
    public Integer N = 50;
    public Integer m0 = 1;

    @Override
    protected List<String> Descritpion(){
        var description = new ArrayList<String>();
        description.add("Parameters description:");
        description.add("N - degree of the root ");
        description.add("h - a height od the tree");
        description.add("Conditions:");
        description.add("r >=2:");

        return description;
    }
    @Override
    public void SetGeneratorParams(MultiBarabasiAlbert barabasiAlbert) {
        barabasiAlbert.setN(N);
        barabasiAlbert.setm0(m0);
    }
}
