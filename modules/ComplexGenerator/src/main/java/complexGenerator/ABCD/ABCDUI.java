package complexGenerator.ABCD;

import Helpers.GenericParamForm.GenericUI;
import complexGenerator.BalancedTree.BalancedTree;
import complexGenerator.BalancedTree.BalancedTreePanel;
import complexGenerator.BalancedTree.BalancedTreeParams;
import complexGenerator.BalancedTree.IBalancedTreeUI;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = IBalancedTreeUI.class)
public class ABCDUI extends GenericUI<ABCDParams, ABCD> implements IBalancedTreeUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new ABCDPanel();
    }
}