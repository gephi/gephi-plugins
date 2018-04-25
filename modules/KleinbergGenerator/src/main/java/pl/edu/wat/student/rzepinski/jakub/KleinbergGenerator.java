package pl.edu.wat.student.rzepinski.jakub;

import org.gephi.io.generator.spi.Generator;
import org.gephi.io.generator.spi.GeneratorUI;
import org.gephi.io.importer.api.ContainerLoader;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Generator.class)
public class KleinbergGenerator implements Generator {

    public static final int DEFAULT_GRID_SIZE = 10;
    public static final int DEFAULT_CLUSTERING_COEFFICIENT = 2;
    public static final boolean DEFAULT_TORUS_MODE = false;

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
        return "Kleinberg model";
    }

    @Override
    public GeneratorUI getUI() {
        return Lookup.getDefault().lookup(KleinbergGeneratorUI.class);
    }

    public boolean isTorusMode() {
        return torusMode;
    }

    public void setTorusMode(boolean torusMode) {
        this.torusMode = torusMode;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
