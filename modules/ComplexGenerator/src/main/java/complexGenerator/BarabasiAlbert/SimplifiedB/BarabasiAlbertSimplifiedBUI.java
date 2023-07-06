package complexGenerator.BarabasiAlbert.SimplifiedB;

import Helpers.GenericParamForm.GenericUI;
import complexGenerator.BarabasiAlbert.SimplifiedA.BarabasiAlbertSimplifiedA;
import complexGenerator.BarabasiAlbert.SimplifiedA.IBarabasiAlbertSimplifiedAUI;
import org.openide.util.lookup.ServiceProvider;



@ServiceProvider(service = IBarabasiAlbertSimplifiedBUI.class)
public class BarabasiAlbertSimplifiedBUI extends GenericUI<BarabasiAlbertSimplifiedBParams, BarabasiAlbertSimplifiedB> implements IBarabasiAlbertSimplifiedBUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new BarabasiAlbertSimplifiedBPanel();
    }
}
