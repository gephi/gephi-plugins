package complexGenerator.BarabasiAlbert.SimplifiedA;

import Helpers.GenericParamForm.GenericUI;
import complexGenerator.BarabasiAlbert.BarabasiAlbert;
import complexGenerator.BarabasiAlbert.IBarabasiAlbertUI;
import org.openide.util.lookup.ServiceProvider;



@ServiceProvider(service = IBarabasiAlbertSimplifiedAUI.class)
public class BarabasiAlbertSimplifiedAUI extends GenericUI<BarabasiAlbertSimplifiedAParams, BarabasiAlbertSimplifiedA> implements IBarabasiAlbertSimplifiedAUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new BarabasiAlbertSimplifiedAPanel();
    }
}
