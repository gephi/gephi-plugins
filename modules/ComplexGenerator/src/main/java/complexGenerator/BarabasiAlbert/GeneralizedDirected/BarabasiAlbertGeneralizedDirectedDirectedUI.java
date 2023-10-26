package complexGenerator.BarabasiAlbert.GeneralizedDirected;

import Helpers.GenericParamForm.GenericUI;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IBarabasiAlbertGeneralizedDirectedUI.class)
public class BarabasiAlbertGeneralizedDirectedDirectedUI extends GenericUI<BarabasiAlbertGeneralizedDirectedParams, BarabasiAlbertGeneralizedDirected> implements IBarabasiAlbertGeneralizedDirectedUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new BarabasiAblertGeneralizedDirectedPanel();
    }
}
