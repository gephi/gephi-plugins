package complexGenerator.BalancedTree;

import Helpers.GenericParamForm.GenericUI;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IBalancedTreeUI.class)
public class BalancedTreeUI extends GenericUI<BalancedTreeParams, BalancedTree> implements IBalancedTreeUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new BalancedTreePanel();
    }
}
