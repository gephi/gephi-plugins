package complexGenerator.MultiGenerator;

import Helpers.GenericParamForm.GenericUI;
import complexGenerator.Kleinberg.IKleinbergUI;
import complexGenerator.Kleinberg.Kleinberg;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IMultiGeneratorUI.class)
public class MultiGeneratorUI extends GenericUI<MultiGeneratorParams, MultiGenerator> implements IMultiGeneratorUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new MultiGeneratorPanel();
    }
}
