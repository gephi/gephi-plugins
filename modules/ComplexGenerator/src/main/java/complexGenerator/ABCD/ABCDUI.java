package complexGenerator.ABCD;

import Helpers.GenericParamForm.GenericUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = IABCDUI.class)
public class ABCDUI extends GenericUI<ABCDParams, ABCD> implements IABCDUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new ABCDPanel();
    }
}