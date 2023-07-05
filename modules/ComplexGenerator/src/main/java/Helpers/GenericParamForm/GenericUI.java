package Helpers.GenericParamForm;

import complexGenerator.BalancedTree.IBalancedTreeUI;
import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;

import javax.swing.*;

public abstract class GenericUI<TParam extends Params<TGenerator>, TGenerator extends Generator> implements GeneratorUI
{
    protected GenericPanel<TParam> panel;
    private TGenerator generator;

    protected abstract void CreatePanel();

    public GenericUI(){
        CreatePanel();
    };

    @Override
    public JPanel getPanel() {
        return panel;
    }

    @Override
    public void setup(Generator generator) {
        this.generator = (TGenerator) generator;
    }

    @Override
    public void unsetup() {
        panel.getTParams().SetGeneratorParams(generator);
        panel = null;
    }
}