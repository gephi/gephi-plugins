package complexGenerator.WattsStrogatz.Beta;

import Helpers.GenericParamForm.GenericUI;
import complexGenerator.Kleinberg.IKleinbergUI;
import complexGenerator.Kleinberg.Kleinberg;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IWattsStrogatzBetaUI.class)
public class WattsStrogatzUI extends GenericUI<WattsStrogatzBetaParams, WattsStrogatzBeta> implements IWattsStrogatzBetaUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new WattsStrogatzBetaPanel();
    }
}
