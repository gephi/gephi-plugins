package pl.edu.wat.student.rzepinski.jakub.kleinberg.generator;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import pl.edu.wat.student.rzepinski.jakub.kleinberg.ui.KleinbergGeneratorUI;
import pl.edu.wat.student.rzepinski.jakub.kleinberg.ui.Labels;

@ServiceProvider(service = Generator.class)
public class KleinbergGenerator implements Generator {
    public static final int DEFAULT_GRID_SIZE = 10;
    public static final int DEFAULT_CLUSTERING_COEFFICIENT = 2;
    public static final boolean DEFAULT_TORUS_MODE = true;

    private ProgressTicket progressTicket;
    private boolean cancelled = false;
    private int clusteringCoefficient = DEFAULT_CLUSTERING_COEFFICIENT;
    private int gridSize = DEFAULT_GRID_SIZE;
    private boolean torusMode = DEFAULT_TORUS_MODE;

    @Override
    public boolean cancel() {
        this.cancelled = true;
        return true;
    }

    @Override
    public void generate(ContainerLoader containerLoader) {
        Progress.start(progressTicket, gridSize * gridSize);
        Runnable worker = new KleinbergGeneratorWorker(this, containerLoader);
        worker.run();
    }

    public int getClusteringCoefficient() {
        return clusteringCoefficient;
    }

    public void setClusteringCoefficient(int clusteringCoefficient) {
        this.clusteringCoefficient = clusteringCoefficient;
    }

    public int getGridSize() {
        return gridSize;
    }

    public void setGridSize(int gridSize) {
        this.gridSize = gridSize;
    }

    @Override
    public String getName() {
        return Labels.KLEINBERG_GENERATOR_NAME;
    }

    public ProgressTicket getProgressTicket() {
        return progressTicket;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
        this.cancelled = false;
    }

    @Override
    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(KleinbergGeneratorUI.class);
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isTorusMode() {
        return torusMode;
    }

    public void setTorusMode(boolean torusMode) {
        this.torusMode = torusMode;
    }
}
