package ec.loxa.sna.gephi.websiteexport.util.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author jorgaf
 */
public class GraphStatistic {

    private String name;
    private String title;
    private String tip;
    private String nodes;
    private String edges;
    private String description;
    private String imgColorDescription;
    private String graphfile;
    private String pdffile;
    private String browsegraph;
    private String type;
    private List<Metrics> metrics = new ArrayList<Metrics>();

    public GraphStatistic() {
        name = "";
        title = "";
        tip = "";
        nodes = "";
        edges = "";
        description = "";
        imgColorDescription = "";
        graphfile = "";
        pdffile = "";
        browsegraph = "";
        type = "";
    }

    public GraphStatistic(String name, String title, String tip, String nodes,
            String edges, String description, String imgColorDescription,
            String graphfile, String pdffile, String browsegraph) {
        this.name = name;
        this.title = title;
        this.tip = tip;
        this.nodes = nodes;
        this.edges = edges;
        this.description = description;
        this.imgColorDescription = imgColorDescription;
        this.graphfile = graphfile;
        this.pdffile = pdffile;
        this.browsegraph = browsegraph;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the nodes
     */
    public String getNodes() {
        return nodes;
    }

    /**
     * @param nodes the nodes to set
     */
    public void setNodes(String nodes) {
        this.nodes = nodes;
    }

    /**
     * @return the edges
     */
    public String getEdges() {
        return edges;
    }

    /**
     * @param edges the edges to set
     */
    public void setEdges(String edges) {
        this.edges = edges;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the imgColorDescription
     */
    public String getImgColorDescription() {
        return imgColorDescription;
    }

    /**
     * @param imgColorDescription the imgColorDescription to set
     */
    public void setImgColorDescription(String imgColorDescription) {
        this.imgColorDescription = imgColorDescription;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the nodesfile
     */
    public String getGraphfile() {
        return graphfile;
    }

    /**
     * @param nodesfile the nodesfile to set
     */
    public void setGraphfile(String graphfile) {
        this.graphfile = graphfile;
    }

    /**
     * @return the edgesfile
     */
    public String getPdffile() {
        return pdffile;
    }

    /**
     * @param edgesfile the edgesfile to set
     */
    public void setPdffile(String pdffile) {
        this.pdffile = pdffile;
    }

    /**
     * @return the tip
     */
    public String getTip() {
        return tip;
    }

    /**
     * @param tip the tip to set
     */
    public void setTip(String tip) {
        this.tip = tip;
    }

    /**
     * @return the browsegraph
     */
    public String getBrowsegraph() {
        return browsegraph;
    }

    /**
     * @param browsegraph the browsegraph to set
     */
    public void setBrowsegraph(String browsegraph) {
        this.browsegraph = browsegraph;
    }

    public List<Metrics> getMetrics() {
        return metrics;
    }

    public void addMetric(Metrics metrics) {
        this.metrics.add(metrics);
    }

}
