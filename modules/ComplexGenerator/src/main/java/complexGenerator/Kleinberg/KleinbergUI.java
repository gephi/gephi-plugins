package complexGenerator.Kleinberg;

import Helpers.GenericParamForm.GenericUI;
import complexGenerator.ErdosRenyi.Gnp.ErdosRenyiGnp;
import complexGenerator.ErdosRenyi.Gnp.IErdosRenyiGnpUI;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IKleinbergUI.class)
public class KleinbergUI extends GenericUI<KleingergParams, Kleinberg> implements IKleinbergUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new KleinbergPanel();
    }
}
