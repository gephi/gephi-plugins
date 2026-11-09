package myrberger.communityclusters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.NbBundle;

/**
 * Gephi layout that reproduces the "Clusters" view of SoFab. Each community gets a home position
 * (circles packed by size); nodes are pulled towards their home while ties and repulsion shape the
 * inside of each cluster.
 */
public class CommunityClusters implements Layout {

    private static final Logger LOG = Logger.getLogger(CommunityClusters.class.getName());
    private static final String SAVED_COLUMN = "community_cluster";

    private final LayoutBuilder builder;
    private GraphModel graphModel;
    private ClusterForceSimulation sim;
    private Node[] nodes;
    private int[] lastCluster;

    // dials (defaults are SoFab's)
    private String clusterColumn;
    private Double repulsion;
    private Double tieLength;
    private Double tieStrength;
    private Double nodeSpacing;
    private Double clusterPull;
    private Double settlingSpeed;
    private Double interClusterTieFactor;
    private Double scale;
    private Integer randomSeed;
    private Double resolution;
    private Boolean saveClusters;
    private Boolean resetPositions;

    public CommunityClusters(LayoutBuilder builder) {
        this.builder = builder;
        resetPropertiesValues();
    }

    @Override
    public void resetPropertiesValues() {
        clusterColumn = "modularity_class";
        repulsion = 40.0;
        tieLength = 24.0;
        tieStrength = 1.0;
        nodeSpacing = 2.0;
        clusterPull = 0.14;
        settlingSpeed = 0.0228;
        interClusterTieFactor = 0.03;
        scale = 1.0;
        randomSeed = 42;
        resolution = 1.0;
        saveClusters = true;
        resetPositions = true;
    }

    @Override
    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
    }

    @Override
    public void initAlgo() {
        sim = null;
        nodes = null;
        lastCluster = null;
        pendingSave = false;
        if (graphModel == null) {
            return;
        }
        Graph graph = graphModel.getGraphVisible();
        graph.readLock();
        try {
            nodes = graph.getNodes().toArray();
            int n = nodes.length;
            if (n == 0) {
                return;
            }
            Map<Node, Integer> index = new HashMap<>(n * 2);
            double[] radius = new double[n], sx = new double[n], sy = new double[n];
            boolean[] fixed = new boolean[n];
            double maxSize = 0;
            for (int i = 0; i < n; i++) {
                Node node = nodes[i];
                index.put(node, i);
                radius[i] = Math.max(1.0, node.size());
                maxSize = Math.max(maxSize, radius[i]);
                sx[i] = node.x();
                sy[i] = node.y();
                fixed[i] = node.isFixed();
            }

            List<int[]> pairs = new ArrayList<>();
            List<Double> weights = new ArrayList<>();
            for (Edge e : graph.getEdges()) {
                Integer s = index.get(e.getSource());
                Integer t = index.get(e.getTarget());
                if (s != null && t != null) {
                    pairs.add(new int[]{s, t});
                    weights.add(e.getWeight());
                }
            }
            int[] es = new int[pairs.size()], ed = new int[pairs.size()];
            for (int i = 0; i < es.length; i++) {
                es[i] = pairs.get(i)[0];
                ed[i] = pairs.get(i)[1];
            }

            double[] ew = new double[es.length];
            for (int i = 0; i < ew.length; i++) {
                ew[i] = weights.get(i);
            }
            int[] cluster = readClusters(nodes, es, ed, ew);

            ClusterForceSimulation.Params p = new ClusterForceSimulation.Params();
            p.repel = repulsion;
            p.tieLen = tieLength;
            p.tieStr = tieStrength;
            p.spacing = nodeSpacing;
            p.clusterPull = clusterPull;
            p.cooling = settlingSpeed;
            p.interTieFactor = interClusterTieFactor;
            p.scale = scale;
            p.seed = randomSeed;
            p.resetPositions = resetPositions;
            lastCluster = cluster;
            sim = new ClusterForceSimulation(radius, cluster, fixed, sx, sy, es, ed, p);
        } finally {
            graph.readUnlock();
        }
        if (pendingSave && nodes != null && lastCluster != null) {
            saveClusterColumn(nodes, lastCluster);
        }
        pendingSave = false;
    }

    /**
     * Cluster index per node: read from the chosen node column, or, when no column is given or it does not
     * exist, detected with Leiden (modularity, resolution and seed from the properties).
     */
    private int[] readClusters(Node[] nodes, int[] es, int[] ed, double[] ew) {
        int n = nodes.length;
        Column col = findColumn(clusterColumn);
        if (col != null) {
            int[] out = new int[n];
            Map<String, Integer> ids = new HashMap<>();
            for (int i = 0; i < n; i++) {
                Object v = nodes[i].getAttribute(col);
                String key = v == null ? "" : String.valueOf(v);
                Integer id = ids.get(key);
                if (id == null) {
                    id = ids.size();
                    ids.put(key, id);
                }
                out[i] = id;
            }
            return out;
        }
        if (clusterColumn != null && !clusterColumn.trim().isEmpty()) {
            LOG.log(Level.WARNING, "Node column \"{0}\" not found; detecting clusters with Leiden instead.",
                    clusterColumn);
        }
        int[] out = Leiden.run(n, es, ed, ew, resolution, randomSeed);
        pendingSave = saveClusters;
        return out;
    }

    private boolean pendingSave;

    /** Stores Leiden's result in a node column so it can be used for colouring and partitioning. */
    private void saveClusterColumn(Node[] nodes, int[] cluster) {
        try {
            Column col = graphModel.getNodeTable().getColumn(SAVED_COLUMN);
            if (col == null) {
                col = graphModel.getNodeTable().addColumn(SAVED_COLUMN, Integer.class);
            }
            if (col.getTypeClass() != Integer.class) {
                return;
            }
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].setAttribute(col, cluster[i]);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Could not save clusters to column " + SAVED_COLUMN, e);
        }
    }

    private Column findColumn(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        Column c = graphModel.getNodeTable().getColumn(name.trim());
        if (c != null) {
            return c;
        }
        for (Column other : graphModel.getNodeTable()) {
            if (name.trim().equalsIgnoreCase(other.getId()) || name.trim().equalsIgnoreCase(other.getTitle())) {
                return other;
            }
        }
        return null;
    }

    @Override
    public void goAlgo() {
        if (sim == null || nodes == null || graphModel == null) {
            return;
        }
        Graph graph = graphModel.getGraphVisible();
        graph.readLock();
        try {
            sim.tick();
            for (int i = 0; i < nodes.length; i++) {
                if (!nodes[i].isFixed()) {
                    nodes[i].setX((float) sim.x[i]);
                    nodes[i].setY((float) sim.y[i]);
                }
            }
        } finally {
            graph.readUnlock();
        }
    }

    @Override
    public boolean canAlgo() {
        return sim != null && !sim.isConverged();
    }

    @Override
    public void endAlgo() {
        sim = null;
        nodes = null;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> props = new ArrayList<>();
        try {
            String clusters = msg("CommunityClusters.category.clusters");
            String forces = msg("CommunityClusters.category.forces");
            String run = msg("CommunityClusters.category.run");
            props.add(property(String.class, clusters, "clusterColumn", "getClusterColumn", "setClusterColumn"));
            props.add(property(Double.class, clusters, "resolution", "getResolution", "setResolution"));
            props.add(property(Boolean.class, clusters, "saveClusters", "getSaveClusters", "setSaveClusters"));
            props.add(property(Double.class, clusters, "clusterPull", "getClusterPull", "setClusterPull"));
            props.add(property(Double.class, clusters, "interClusterTieFactor",
                    "getInterClusterTieFactor", "setInterClusterTieFactor"));
            props.add(property(Double.class, forces, "repulsion", "getRepulsion", "setRepulsion"));
            props.add(property(Double.class, forces, "tieLength", "getTieLength", "setTieLength"));
            props.add(property(Double.class, forces, "tieStrength", "getTieStrength", "setTieStrength"));
            props.add(property(Double.class, forces, "nodeSpacing", "getNodeSpacing", "setNodeSpacing"));
            props.add(property(Double.class, forces, "scale", "getScale", "setScale"));
            props.add(property(Double.class, run, "settlingSpeed", "getSettlingSpeed", "setSettlingSpeed"));
            props.add(property(Integer.class, run, "randomSeed", "getRandomSeed", "setRandomSeed"));
            props.add(property(Boolean.class, run, "resetPositions", "getResetPositions", "setResetPositions"));
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Could not create layout properties", e);
        }
        return props.toArray(new LayoutProperty[0]);
    }

    /** Builds a property whose display name and description come from Bundle.properties ("<id>.name/.desc"). */
    private LayoutProperty property(Class<?> type, String category, String id, String getter, String setter)
            throws Exception {
        return LayoutProperty.createProperty(this, type, msg("CommunityClusters." + id + ".name"), category, id,
                msg("CommunityClusters." + id + ".desc"), getter, setter);
    }

    private static String msg(String key) {
        return NbBundle.getMessage(CommunityClusters.class, key);
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    // ---- property accessors (looked up by name through reflection) ----------------------------------

    public String getClusterColumn() { return clusterColumn; }
    public void setClusterColumn(String v) { clusterColumn = v; }
    public Double getRepulsion() { return repulsion; }
    public void setRepulsion(Double v) { repulsion = Math.max(0, v); }
    public Double getTieLength() { return tieLength; }
    public void setTieLength(Double v) { tieLength = Math.max(0, v); }
    public Double getTieStrength() { return tieStrength; }
    public void setTieStrength(Double v) { tieStrength = Math.max(0.01, v); }
    public Double getNodeSpacing() { return nodeSpacing; }
    public void setNodeSpacing(Double v) { nodeSpacing = Math.max(0, v); }
    public Double getClusterPull() { return clusterPull; }
    public void setClusterPull(Double v) { clusterPull = Math.max(0, v); }
    public Double getSettlingSpeed() { return settlingSpeed; }
    public void setSettlingSpeed(Double v) { settlingSpeed = Math.min(0.5, Math.max(0.0005, v)); }
    public Double getInterClusterTieFactor() { return interClusterTieFactor; }
    public void setInterClusterTieFactor(Double v) { interClusterTieFactor = Math.max(0, v); }
    public Double getScale() { return scale; }
    public void setScale(Double v) { scale = Math.max(0.01, v); }
    public Integer getRandomSeed() { return randomSeed; }
    public void setRandomSeed(Integer v) { randomSeed = v; }
    public Double getResolution() { return resolution; }
    public void setResolution(Double v) { resolution = Math.max(0.01, v); }
    public Boolean getSaveClusters() { return saveClusters; }
    public void setSaveClusters(Boolean v) { saveClusters = v; }
    public Boolean getResetPositions() { return resetPositions; }
    public void setResetPositions(Boolean v) { resetPositions = v; }
}
