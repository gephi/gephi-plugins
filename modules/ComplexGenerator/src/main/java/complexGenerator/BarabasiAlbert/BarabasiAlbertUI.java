package complexGenerator.BarabasiAlbert;

import Helpers.GenericParamForm.GenericUI;
import org.openide.util.lookup.ServiceProvider;



@ServiceProvider(service = IBarabasiAlbertUI.class)
public class BarabasiAlbertUI extends GenericUI<BarabasiAlbertParams, BarabasiAlbert> implements IBarabasiAlbertUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new BarabasiAblertPanel();
    }
}
