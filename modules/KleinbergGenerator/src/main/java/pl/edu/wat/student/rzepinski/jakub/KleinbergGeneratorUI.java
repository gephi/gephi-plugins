package pl.edu.wat.student.rzepinski.jakub;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.*;

@ServiceProvider(service = KleinbergGeneratorUI.class)
public class KleinbergGeneratorUI implements GeneratorUI {

    @Override
    public JPanel getPanel() {
        //TODO
        return new JPanel();
    }

    @Override
    public void setup(Generator generator) {
        //TODO
    }

    @Override
    public void unsetup() {
        //TODO
    }
}