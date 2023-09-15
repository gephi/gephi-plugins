package Helper;

import org.gephi.graph.api.GraphController;
import org.gephi.statistics.plugin.PageRank;
import org.openide.util.Lookup;

public class NodeSelectorHelper {
    public static void CenrtialityRate(){
        var graphController = Lookup.getDefault().lookup(GraphController.class);
        var graphModel = graphController.getGraphModel();
        var graph = graphModel.getGraph();
        var pr = new PageRank();

        pr.execute(graphModel);
    }
}
