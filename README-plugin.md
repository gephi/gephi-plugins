# Licensing

This plugin is developped inside [Inria](http://www.inria.fr), by the [Wimmics](http://wimmics.inria.fr) research team, with the support of the [Dream](http://www-sop.inria.fr/dream) team.

This plugin is made available through the [CeCILL-B licence](http://www.cecill.info/licences/Licence_CeCILL-B_V1-en.html).

# Installation

**Follow the procedure described at https://github.com/Wimmics/update-semanticwebimport**

# Versions

- SemanticWebImport: 1.4.0
- Embedded Corese: 4.4.0

# Main Repositories

- Source code: https://github.com/Wimmics/gephi-semantic-web-import.
- The update center repository: https://github.com/Wimmics/update-semanticwebimport

# Installation

The installation is done through a custom "update center". See [installation](https://github.com/Wimmics/update-semanticwebimport).

# Disclaimer

All the description below requires a complete update and is provided only as is, since some informations could still be
useful.

# Videos

The main page for the following videos can found at [SemanticWebImport Plugin Videos](http://wimmics.inria.fr/node/35).

* [Installation of the plugin](http://www-sop.inria.fr/teams/edelweiss/media/installation.swf)
* [Overview of the parts of the plugin](http://www-sop.inria.fr/teams/edelweiss/media/parts_overview.swf)
* [Access to local RDF data](http://www-sop.inria.fr/teams/edelweiss/media/first_example_local.swf)

# Description

The SemanticWebImport plugin is intended to allow the import of semantic data into Gephi. The imported data are obtained by processing a SPARQL request on the semantic data. The data can be accessed following three manners:

1. by accessing local rdf, rdfs, rul files and using the embedded Corese engine to apply the SPARQL request;
2. by accessing a remote REST SPARQL endpoint. In that case, the SPARQL request is applied remotely and the graph is built locally by analyzing the result sent by the REST endpoint;
3. by accessing a remote SOAP SPARQL endpoint. As for the REST endpoint, the resulting graph is built from the result returned by the endpoint.

We begin by showing how to make run the preset examples which come with the plugin. Then we detail the three drivers allowing to import semantic data.

**In all the following cases, it is required there is a currently opened project, otherwise the graph can not be built.**

# General Description of the GUI

The plugin consist of fourth tabs:

## How to access the data

![First tab of the plugin](https://cloud.githubusercontent.com/assets/197285/13900370/d67a6006-ee04-11e5-8ed2-0bc788e252a2.png)

First tab of the plugin

This tab allows to select among the available SPARQL drivers how the semantic data can be accessed. Currently, the choice can be made between (i) access to local data through the Corese engine; (ii) access to a remote REST SPARQL endpoint; (iii) access to a remote SOAP SPARQL enpoint.

## Write the SPARQL query

![Second tab of the plugin](https://cloud.githubusercontent.com/assets/197285/13900373/d84b21c2-ee04-11e5-99c5-6ffa80919f34.png)

Second tab of the plugin

The SPARQL editor to enter a SPARQL request that extract the data used to build the graph. **It is mandatory to be a construct request.**

## Execution log

![Third tab of the plugin](https://cloud.githubusercontent.com/assets/197285/13900374/d8aa9526-ee04-11e5-967e-5dabc30c66e7.png)

Third tab of the plugin

This tab contains the log, i.e. the outputs of the plugin.

## Configurations management

![Fourth tab of the plugin](https://cloud.githubusercontent.com/assets/197285/13900375/d97158c8-ee04-11e5-856b-8c3a71c96fbb.png)

Fourth tab of the plugin

This tab allows to manage the configurations. I.e. it contains: (i) the selector for preset examples and the load button to activate them.

## Launch the query

Note that to launch the query, the Run button is set at the bottom of the window. 

# Use

To use the plugin, follow these steps:

1. choose in tab 1 a driver between the three available;
2. parameterize the driver (tab 1, and see the following sections for more details);
3. enter the SPARQL request, making sure it is a construct SPARQL request. All the relations "?x ?r ?y" in the construct part are creating nodes ?x and ?y and an edge to connect both;
4. In tab 4, choose:
  1. Wether the workspace has to be reset;
  2. If blank nodes muste be ignored;
  3. Which level of follow your nose recursion you want.
  4. The Python pre and post processing scripts can be used, using the python scripting plugin (see https://github.com/gephi/gephi/wiki/Scripting-Plugin for more details). The /fr/inria/edelweiss/examples/autolayout.py comes as a example of script.
5. The processing of the query can be launched by clicking on the Run button (at bottom).

# BBC Preset Examples

To obtain the BBC example, foolow the following steps:

1. Create a new empty project;
2. Select in the Load Examples/Configurations (i.e. the part 4 in previous image) the "BBC" example;
3. Click on load. The GUI should be similar to ![View of the SemanticWebPlugin after loading the BBC example](https://cloud.githubusercontent.com/assets/197285/13900376/da5a36d8-ee04-11e5-8ba1-3f2a9142a562.png) (View of the SemanticWebPlugin after loading the BBC example)
4. Launch the SPARQL driver by clicking on the start button (part 5). The BBC example connects to a SOAP SPARQL endpoint, and the SPARQL request is processed remotely, then the result is returned to the plugin. The graph obtained should be similar to ![View of the SemanticWebPlugin after processing the BBC example](https://cloud.githubusercontent.com/assets/197285/13900377/daf7547c-ee04-11e5-9930-2e5798509721.png) (View of the SemanticWebPlugin after processing the BBC example)

# Using Local Files

The Corese driver allows to process locally a SPARQL request on RDF files. The RDF files can be provided as:

1. local files.
2. internet files, i.e. beginning with http://. For example, http://dbpedia.org/data/The_Beatles.rdf can be added and used as an input.
3. resource files (i.e. RDF files coming from a jar file run by gephi). The file must begin with /. Three such files are coming embedded inside the plugin, /fr/inria/edelweis/examples/human_2007_09_11.rdf, /fr/inria/edelweis/examples/human_2007_09_11.rdfs, /fr/inria/edelweis/examples/human_2007_09_11.rul.

![CoreseDriver Panel](https://cloud.githubusercontent.com/assets/197285/13900378/dba7609c-ee04-11e5-8aaa-9cf8196b2210.png)

CoreseDriver Panel

The CoreseDriver is made of three parts:

1. buttons allowing to add a local file (+) or remove a resource (-);
2. the list of resources;
3. a text field and a button to add external resources, i.e. rdf files on the internet.

# Access with REST

The REST SPARQL driver allows to make process a SPARQL request on a remote SPARQL endpoint with REST interface.

![Rest Driver Panel](https://cloud.githubusercontent.com/assets/197285/13900379/dc4c2596-ee04-11e5-8bee-cfeb7f856c23.png)

Rest Driver Panel

The Rest panel is made of the following parts:

1. the URL for the endpoint;
2. the name given to the query tag. Most often it is "query"; some endpoints use "q".
3. some parameters to be added the request. For example "debug=on" can be provided by:
  1. Writing "debug" instead of "REST name" in the part 3.
  2. Writing "on" instead of "REST value" in the part 3.
  3. Clicking on +.

## Access with SOAP

The SOAP SPARQL driver has a single parameter, the URL of the endpoint.

![Soap Driver Panel](https://cloud.githubusercontent.com/assets/197285/13900380/dd0a4fe4-ee04-11e5-92a8-2c84e3565695.png)

Soap Driver Panel

# Gephi: extensions in the SPARQL query

When building a query, some special keywords can be used to customize the results in gephi. http://gephi.org/ is used as the namespace for this extension. It is counselled to add the line "namespace gephi: <http://gephi.org/>" at the beginning of the query.

* ?node **gephi:label** ?node_label fill the label of the node with the content of ?node_label;
* ?node **gephi:size** ?value sets the size of the node to the content of ?value;
* ?node **gephi:color** ?color_name sets the color of the node according to the content of ? color_name. The known names are those defined in http://docs.oracle.com/javase/6/docs/api/java/awt/Color.html.
* ?node **gephi:color_r** ?value sets the red part of the color of the node with ?value. ?value must be set between 0 and 255, inclusives,
* ?node **gephi:color_g** ?value sets the green part of the color of the node with ?value. ?value must be set between 0 and 255, inclusives,
* ?node **gephi:color_b** ?value sets the blue part of the color of the node with ?value. ?value must be set between 0 and 255, inclusives,
* ?node **gephi:AttributeName**  ?anyValue creates a new attribute called "AttributeName" for all the nodes of the graph, and set the attribute for the current node with the content of ?anyValue.

# Pre and post processing using python

As previously stated, the import of data can be pre or post processed with a script plugin.

```python
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

print("executing layout")
# Run the layout.
fa2.initAlgo()
for i in range(5000):
   fa2.goAlgo()
```
