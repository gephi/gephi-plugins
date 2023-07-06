package complexGenerator.WattsStrogatz.Alpha;

import Helpers.GenericParamForm.GenericUI;
import complexGenerator.Kleinberg.IKleinbergUI;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IWattsStrogatzAlphaUI.class)
public class WattsStrogatzAlpgaUI extends GenericUI<WattsStrigatzAlphaParams, WattsStrogatzAlpha> implements IWattsStrogatzAlphaUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new WattsStrogatzAlphaPanel();
    }
}
