package complexGenerator.WattsStrogatz.Alpha;

import Helpers.GenericParamForm.GenericUI;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IWattsStrogatzAlphaUI.class)
public class WattsStrogatzAlpgaUI extends GenericUI<WattsStrogatzAlphaParams, WattsStrogatzAlpha> implements IWattsStrogatzAlphaUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new WattsStrogatzAlphaPanel();
    }
}
