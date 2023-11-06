package complexGenerator.BarabasiAlbert.SimplifiedBDirected;

import Helpers.GenericParamForm.GenericUI;
import org.openide.util.lookup.ServiceProvider;



@ServiceProvider(service = IBarabasiAlbertSimplifiedBDirectedUI.class)
public class BarabasiAlbertSimplifiedBDirectedUI extends GenericUI<BarabasiAlbertSimplifiedBDirectedParams, BarabasiAlbertSimplifiedBDirected> implements
        IBarabasiAlbertSimplifiedBDirectedUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new BarabasiAlbertSimplifiedBDirectedPanel();
    }
}
