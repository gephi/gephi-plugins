package complexGenerator.BalancedTree;

import org.gephi.io.generator.spi.Generator;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

@ServiceProvider(service = IBalancedTreeUI.class)
public class BalancedTreeUI implements IBalancedTreeUI
{
    private BalancedTreePanel panel;
    private BalancedTree balancedTree;

    public BalancedTreeUI(){
        panel = new BalancedTreePanel();
    }
    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void setup(Generator generator) {
        panel.getRField().setText("3");
        panel.getHField().setText("5");
        balancedTree = (BalancedTree) generator;
    }

    @Override
    public void unsetup() {
        balancedTree.seth(panel.getHValue());
        balancedTree.setr(panel.getRValue());
        panel = null;
    }
}
