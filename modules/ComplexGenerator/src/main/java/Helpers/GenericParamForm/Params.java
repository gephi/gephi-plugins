package Helpers.GenericParamForm;

import org.gephi.io.generator.spi.Generator;

import javax.swing.*;
import java.util.List;

public abstract class Params<TGenerator extends Generator> extends JPanel {

    protected abstract List<String> Descritpion();
    public abstract void SetGeneratorParams(TGenerator generator);
}
