package complexGenerator.BarabasiAlbert.SimplifiedADirected;

import Helpers.GenericParamForm.GenericUI;
import complexGenerator.BarabasiAlbert.SimplifiedA.IBarabasiAlbertSimplifiedAUI;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IBarabasiAlbertSimplifiedAUI.class)
public class BarabasiAlbertSimplifiedADirectedUI extends GenericUI<BarabasiAlbertSimplifiedADirectedParams, BarabasiAlbertSimplifiedADirected> implements IBarabasiAlbertSimplifiedAUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new BarabasiAlbertSimplifiedADirectedPanel();
    }
}
