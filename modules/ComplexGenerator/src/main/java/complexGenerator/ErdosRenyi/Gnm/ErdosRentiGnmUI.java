package complexGenerator.ErdosRenyi.Gnm;

import Helpers.GenericParamForm.GenericUI;
import complexGenerator.BarabasiAlbert.BarabasiAblertPanel;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IErdosRenyiGnmUI.class)
public class ErdosRentiGnmUI extends GenericUI<ErdosRenyiGnmParams, ErdosRenyiGnm> implements IErdosRenyiGnmUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new ErdosRenyiGnmPanel();
    }
}
