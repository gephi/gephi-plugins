package complexGenerator.Grid.BalancedTree;

import Helpers.GenericParamForm.GenericUI;
import org.openide.util.lookup.ServiceProvider;


@ServiceProvider(service = IGridUI.class)
public class GridUI extends GenericUI<GridParams, Grid> implements IGridUI
{
    @Override
    protected void CreatePanel() {
        this.panel = new GrdPanel();
    }
}
