package Helpers.GenericParamForm;

import org.gephi.io.generator.spi.Generator;

import javax.swing.*;

public abstract class Params<TGenerator extends Generator> extends JPanel {
    public abstract void SetGeneratorParams(TGenerator generator);
}
