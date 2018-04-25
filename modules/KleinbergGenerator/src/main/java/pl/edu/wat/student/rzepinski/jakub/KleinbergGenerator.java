package pl.edu.wat.student.rzepinski.jakub;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Generator.class)
public class KleinbergGenerator implements Generator {

    private ProgressTicket progressTicket;
    private boolean cancelled = false;

    @Override
    public boolean cancel() {
        this.cancelled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    @Override
    public void generate(ContainerLoader containerLoader) {
        //TODO
    }

    @Override
    public String getName() {
        return "Kleinberg model";
    }

    @Override
    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(KleinbergGeneratorUI.class);
    }
}
