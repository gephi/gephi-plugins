import org.openide.util.Lookup as Lookup
import org.gephi.ranking.api.RankingController
import org.gephi.ranking.api.Ranking as Ranking
import org.gephi.ranking.api.Transformer as Transformer
import java.awt.Color as Color


rankingController = Lookup.getDefault().lookup(org.gephi.ranking.api.RankingController)

# Set the color in function of the degree.
degreeRanking = rankingController.getModel().getRanking(Ranking.NODE_ELEMENT, Ranking.DEGREE_RANKING);
colorTransformer =  rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_COLOR)
colorTransformer.setColors([Color.BLUE, Color.YELLOW])

rankingController.transform(degreeRanking, colorTransformer)

# Set the size in function of the degree of the nodes.
sizeTransformer = rankingController.getModel().getTransformer(Ranking.NODE_ELEMENT, Transformer.RENDERABLE_SIZE)
sizeTransformer.setMinSize(3)
sizeTransformer.setMaxSize(40)
rankingController.transform(degreeRanking, sizeTransformer)


### Layout of the graph
# Construction of a layout object
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder as ForceAtlas2Builder
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2 as ForceAtlas2
fa2builder = ForceAtlas2Builder()
fa2 = ForceAtlas2(fa2builder)

# Setting the layout object
import org.gephi.graph.api.GraphController as GraphController
graphModel = Lookup.getDefault().lookup(GraphController).getModel()
fa2.setGraphModel(graphModel)
fa2.setAdjustSizes(True) # To prevent overlap

print("setting z coordinates")
### Setting a random z coordinate on each node
import random
graph = graphModel.getGraph()
for n in graph.getNodes():
  n.getNodeData().setZ(graph.getDegree(n)*10)

print("executing layout")
# Run the layout.
fa2.initAlgo()
for i in range(5000):
   fa2.goAlgo()



