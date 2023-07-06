package complexGenerator.ErdosRenyi.Gnp;

import Helpers.GenericParamForm.GenericUI;
import complexGenerator.ErdosRenyi.Gnm.ErdosRenyiGnm;
import complexGenerator.ErdosRenyi.Gnm.IErdosRenyiGnmUI;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IErdosRenyiGnpUI.class)
public class ErdosRentiGnpUI extends GenericUI<ErdosRenyiGnpParams, ErdosRenyiGnp> implements IErdosRenyiGnpUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new ErdosRenyiGnpPanel();
    }
}
