package complexGenerator.BarabasiAlbert.Multi;

import Helpers.GenericParamForm.GenericUI;
import complexGenerator.BarabasiAlbert.Generalized.BarabasiAlbertGeneralized;
import complexGenerator.BarabasiAlbert.Generalized.IBarabasiAlbertGeneralizedUI;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IMultiBarabasiAlbertUI.class)
public class MultiBarabasiAlbertUI extends GenericUI<MultiBarabasiAlbertParams, MultiBarabasiAlbert> implements IMultiBarabasiAlbertUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new MultiBarabasiAlbertPanel();
    }
}
