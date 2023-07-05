package complexGenerator.BarabasiAlbert.Generalized;

import Helpers.GenericParamForm.GenericUI;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IBarabasiAlbertGeneralizedUI.class)
public class BarabasiAlbertGeneralizedUI extends GenericUI<BarabasiAlbertGeneralizedParams, BarabasiAlbertGeneralized> implements IBarabasiAlbertGeneralizedUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new BarabasiAblertGeneralizedPanel();
    }
}
