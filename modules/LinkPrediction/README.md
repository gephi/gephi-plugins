# Link Prediction
[![license](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

Link-prediction plugin for Gephi, which allows to predict the next edges to be formed using different prediction algorithms. Edges that are added to the network based on the prediction are marked accordingly. Users can limit the number of edges predicted. The plugin contains an evaluation component, which allows to compare the quality of the different algorithms regarding the graph on hand.

The plugin is released under the Apache 2.0 license.

## Features

In release 1.3.0 the plugin contains the following functionality:

* __Statistics__: New edges can be added to an undirected graph using selected algorithms in the `statistics` tab. The number of new edges can be specified. In doing so, _n_ new edges are added to the graph iteratively. The calculation of the next predicted edge is always based on the graph of the preceding iteration step.
* __Filter__: The added edges can be displayed by means of filters. On the one hand, the corresponding algorithm is specified as the filter criterion. On the other hand, the number of added edges can also be restricted.
* __Evaluation__: Based on an initial graph and a validation graph the accuracy of the link predictions using different algorithms are evaluated. Besides the final accuracy, the generated report also shows the accuracy after each iteration step.  

## Get started

### Run Gephi with installed plugin

If you checked out the sources via Maven, you can run Gephi with your plugin pre-installed using the following command. Make sure to run `mvn package` beforehand to rebuild.

       mvn org.gephi:gephi-maven-plugin:run

If you downloaded the plugin distribution files _(*.nbm)_ you can just navigate to `Tools` > `Plugins` > `Downloaded` and add the Plugin there.

### Predict new edges

To predict new edges, run a new link prediction using `Statistics` > `Edge Overview` > `Link Predictions`.
The number of new edges can be specified. In doing so, n new edges are added to the graph iteratively. The calculation of the next predicted edge is always based on the graph of the preceding iteration step.
Information to the newly added edges are visible in the following columns under `Data Laboratory` > `Edges` :

* __Chosen link prediction algorithm__: Algorithm that was used to predict the edge.
* __Added in run__: Iteration, in which the edge was added.
* __Last link prediction value__: Calculated link prediction value according to the used algorithm. The values are not normalized.

### Filter predictions

The filters under `Filters` > `Link Prediction` then allow you to narrow down the corresponding edges. 
Edges can be filtered according to the algorithms with which they were added.
Furthermore, the number of added edges can also be restricted to the first *n* added edges.

### Evaluate prediction algorithms

The evaluation can be run using `Statistics` > `Edge Overview` > `Eval. Link Predictions Algorithms`. 
Starting with an initial graph <a href="https://www.codecogs.com/eqnedit.php?latex=G_i_,_t" target="_blank"><img src="https://latex.codecogs.com/gif.latex?G_i_,_t" title="G_i_,_t" /></a> 
at time *t* we predict *n* edges <a href="https://www.codecogs.com/eqnedit.php?latex=E_i" target="_blank"><img src="https://latex.codecogs.com/gif.latex?E_i" title="E_i" /></a>
which results in a Graph <a href="https://www.codecogs.com/eqnedit.php?latex=G_i_,_t_&plus;_n" target="_blank"><img src="https://latex.codecogs.com/gif.latex?G_i_,_t_&plus;_n" title="G_i_,_t_+_n" /></a> at time *t + n*.
For each algorithm a new workspace is created which is used to apply the predictions. The initial and validation graph are therefore not changed.
The following figure shows the graphs <a href="https://www.codecogs.com/eqnedit.php?latex=G_i_,_t" target="_blank"><img src="https://latex.codecogs.com/gif.latex?G_i_,_t" title="G_i_,_t" /></a> 
(on the left) and <a href="https://www.codecogs.com/eqnedit.php?latex=G_i_,_t_&plus;_n" target="_blank"><img src="https://latex.codecogs.com/gif.latex?G_i_,_t_&plus;_n" title="G_i_,_t_+_n" /></a> (on the right):

![Initial graph](https://github.com/gephi/gephi-plugins/raw/link-prediction-plugin/modules/LinkPrediction/src/main/resources/graph_init.jpg "Initial graph") 

To evaluate their accuracy using a validation graph <a href="https://www.codecogs.com/eqnedit.php?latex=G_v_,_t_&plus;_n" target="_blank"><img src="https://latex.codecogs.com/gif.latex?G_v_,_t_&plus;_n" title="G_v_,_t_+_n" /></a>
and its additional edges <a href="https://www.codecogs.com/eqnedit.php?latex=E_v" target="_blank"><img src="https://latex.codecogs.com/gif.latex?E_v" title="E_v" /></a>
at time *t+n* are used. In comparison to the Graph <a href="https://www.codecogs.com/eqnedit.php?latex=G_i_,_t" target="_blank"><img src="https://latex.codecogs.com/gif.latex?G_i_,_t" title="G_i_,_t" /></a>
this graph additionally contains the edges <a href="https://www.codecogs.com/eqnedit.php?latex=(A,C)" target="_blank"><img src="https://latex.codecogs.com/gif.latex?(A,C)" title="(A,C)" /></a>,
<a href="https://www.codecogs.com/eqnedit.php?latex=(E,H)" target="_blank"><img src="https://latex.codecogs.com/gif.latex?(E,H)" title="(E,H)" /></a> and
<a href="https://www.codecogs.com/eqnedit.php?latex=(H,I)" target="_blank"><img src="https://latex.codecogs.com/gif.latex?(H,I)" title="(H,I)" /></a>:

![Validation graph](https://github.com/gephi/gephi-plugins/raw/link-prediction-plugin/modules/LinkPrediction/graph_validation.jpg "Validation graph") 

The accuracy then is calculated as percentage of the correct predicted edges. In the current implementation, the results are rounded to two places.

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=Acc&space;=&space;|&space;E_i&space;\&space;\cap&space;\&space;E_v|&space;\&space;/&space;\&space;|E_v|&space;*&space;100" target="_blank"><img src="https://latex.codecogs.com/gif.latex?Acc&space;=&space;|&space;E_i&space;\&space;\cap&space;\&space;E_v|&space;\&space;/&space;\&space;|E_v|&space;*&space;100" title="Acc = | E_i \ \cap \ E_v| \ / \ |E_v| * 100" /></a>

In the above example of three additional edges the edges <a href="https://www.codecogs.com/eqnedit.php?latex=(A,C)" target="_blank"><img src="https://latex.codecogs.com/gif.latex?(A,C)" title="(A,C)" /></a> 
and <a href="https://www.codecogs.com/eqnedit.php?latex=(H,I)" target="_blank"><img src="https://latex.codecogs.com/gif.latex?(H,I)" title="(H,I)" /></a>
were predicted correctly. Therefore an accuracy of 66.67% is achieved:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=E_i&space;=&space;\{(A,C),&space;(A,F),&space;(H,I)\}" target="_blank"><img src="https://latex.codecogs.com/gif.latex?E_i&space;=&space;\{(A,C),&space;(A,F),&space;(H,I)\}" title="E_i = \{(A,C), (A,F), (H,I)\}" /></a>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=E_v&space;=&space;\{(A,C),&space;(E,H),&space;(H,I)\}" target="_blank"><img src="https://latex.codecogs.com/gif.latex?E_v&space;=&space;\{(A,C),&space;(E,H),&space;(H,I)\}" title="E_v = \{(A,C), (E,H), (H,I)\}" /></a>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=|&space;E_i&space;\&space;\cap&space;\&space;E_v|&space;=&space;|\{(A,C),&space;(H,I)\}|&space;=&space;2" target="_blank"><img src="https://latex.codecogs.com/gif.latex?|&space;E_i&space;\&space;\cap&space;\&space;E_v|&space;=&space;|\{(A,C),&space;(H,I)\}|&space;=&space;2" title="| E_i \ \cap \ E_v| = |\{(A,C), (H,I)\}| = 2" /></a>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=Acc&space;=&space;|&space;E_i&space;\&space;\cap&space;\&space;E_v|&space;\&space;/&space;\&space;|E_v|&space;*&space;100&space;=&space;2&space;\&space;/&space;\&space;3&space;*&space;100&space;=&space;\doteq&space;66.67" target="_blank"><img src="https://latex.codecogs.com/gif.latex?Acc&space;=&space;|&space;E_i&space;\&space;\cap&space;\&space;E_v|&space;\&space;/&space;\&space;|E_v|&space;*&space;100&space;=&space;2&space;\&space;/&space;\&space;3&space;*&space;100&space;=&space;&space;66.67" title="Acc = | E_i \ \cap \ E_v| \ / \ |E_v| * 100 = 2 \ / \ 3 * 100 = 66.67" /></a>
## Algorithms

Link prediction is based on an existing network and attempts to predict new edges. The most popular application is the suggestion of new friends on social networking platforms.
To predict a new edge, different algorithms exist. The plugin allows to easily add new algorithms. Currently, the algorithms [common neighbours](#common-neighbours) and [preferential attachment](#preferential-attachment) are implemented. To show the functionality of the algorithms, the following example graph is used:
![Example graph](https://github.com/gephi/gephi-plugins/raw/link-prediction-plugin/modules/LinkPrediction/src/main/resources/graph_example.jpg "Example graph") 

### Common neighbours

Common neighbours calculates for two unconnected nodes how many common neighbours exist. The higher the calculated value, the more likely a new edge will be added between the two nodes.

The following formula represents, how the number of common neighbours of two nodes <a href="https://www.codecogs.com/eqnedit.php?latex=X" target="_blank"><img src="https://latex.codecogs.com/gif.latex?X" title="X" /></a>
and <a href="https://www.codecogs.com/eqnedit.php?latex=Y" target="_blank"><img src="https://latex.codecogs.com/gif.latex?Y" title="Y" /></a>
can be calculated. The call to the function <a href="https://www.codecogs.com/eqnedit.php?latex=\inline&space;N(::)" target="_blank"><img src="https://latex.codecogs.com/gif.latex?\inline&space;N(::)" title="N(::)" /></a>
returns all neighbours of a node in a set, e.g. <a href="https://www.codecogs.com/eqnedit.php?latex=\inline&space;N(A)" target="_blank"><img src="https://latex.codecogs.com/gif.latex?\inline&space;N(A)" title="N(A)" /></a>
returns all neighbour nodes of node <a href="https://www.codecogs.com/eqnedit.php?latex=\inline&space;A" target="_blank"><img src="https://latex.codecogs.com/gif.latex?\inline&space;A" title="A" /></a>. 

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=cn&space;(X,&space;Y)&space;=&space;|N(X)&space;\cap&space;N(Y)|" target="_blank"><img src="https://latex.codecogs.com/gif.latex?cn&space;(X,&space;Y)&space;=&space;|N(X)&space;\cap&space;N(Y)|" title="cn (X, Y) = |N(X) \cap N(Y)|" /></a>

Applied to the above example graph, the common neighbour algorithm would predict a new edge between nodes <a href="https://www.codecogs.com/eqnedit.php?latex=\inline&space;A" target="_blank"><img src="https://latex.codecogs.com/gif.latex?\inline&space;A" title="A" /></a>
and <a href="https://www.codecogs.com/eqnedit.php?latex=\inline&space;C" target="_blank"><img src="https://latex.codecogs.com/gif.latex?\inline&space;C" title="C" /></a>.
 The following examples show some of the calculated values:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=cn(A,C)&space;=&space;|{B,&space;D}|&space;=&space;2" target="_blank"><img src="https://latex.codecogs.com/gif.latex?cn(A,C)&space;=&space;|{B,&space;D}|&space;=&space;2" title="cn(A,C) = |{B, D}| = 2" /></a>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=cn(A,F)&space;=&space;|{E}|&space;=&space;1" target="_blank"><img src="https://latex.codecogs.com/gif.latex?cn(A,F)&space;=&space;|{E}|&space;=&space;1" title="cn(A,F) = |{E}| = 1" /></a>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=cn(A,I)&space;=&space;|{\left&space;\{&space;\right&space;\}}|&space;=&space;0" target="_blank"><img src="https://latex.codecogs.com/gif.latex?cn(A,I)&space;=&space;|{\left&space;\{&space;\right&space;\}}|&space;=&space;0" title="cn(A,I) = |{\left \{ \right \}}| = 0" /></a>

The current implementation of the algorithm has a complexity class of  <a href="https://www.codecogs.com/eqnedit.php?latex=O(n^2)" target="_blank"><img src="https://latex.codecogs.com/gif.latex?O(n^2)" title="O(n^2)" /></a>.

### Preferential attachment

The basic assumption with Preferential Attachment is that the probability that a node is affected by a newly added edge, is just proportional to the number of neighbours. The more neighbours a node has, the larger the likelihood that it will be affected.

To calculate preferential attachment, the number of neighbours of both nodes are multiplied by each other. The call to the function <a href="https://www.codecogs.com/eqnedit.php?latex=\inline&space;N(::)" target="_blank"><img src="https://latex.codecogs.com/gif.latex?\inline&space;N(::)" title="N(::)" /></a>
returns again all neighbours of a node in a set, e.g. <a href="https://www.codecogs.com/eqnedit.php?latex=\inline&space;N(A)" target="_blank"><img src="https://latex.codecogs.com/gif.latex?\inline&space;N(A)" title="N(A)" /></a>
returns all neighbours of node <a href="https://www.codecogs.com/eqnedit.php?latex=\inline&space;A" target="_blank"><img src="https://latex.codecogs.com/gif.latex?\inline&space;A" title="A" /></a>. 

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=pa&space;(X,&space;Y)&space;=&space;|N(X)|&space;*&space;|N(Y)|" target="_blank"><img src="https://latex.codecogs.com/gif.latex?pa&space;(X,&space;Y)&space;=&space;|N(X)|&space;*&space;|N(Y)|" title="pa (X, Y) = |N(X)| * |N(Y)|" /></a>

Applied to the above example graph, preferential attachment would predict an edge between node <a href="https://www.codecogs.com/eqnedit.php?latex=\inline&space;A" target="_blank"><img src="https://latex.codecogs.com/gif.latex?\inline&space;A" title="A" /></a>
 and <a href="https://www.codecogs.com/eqnedit.php?latex=\inline&space;G" target="_blank"><img src="https://latex.codecogs.com/gif.latex?\inline&space;G" title="G" /></a>
  and calculate the following values:

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=pa(A,G)&space;=&space;3&space;*&space;4&space;=&space;12" target="_blank"><img src="https://latex.codecogs.com/gif.latex?pa(A,G)&space;=&space;3&space;*&space;4&space;=&space;12" title="pa(A,G) = 3 * 4 = 12" /></a>

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="https://www.codecogs.com/eqnedit.php?latex=pa(A,C)&space;=&space;3&space;*&space;3&space;=&space;9" target="_blank"><img src="https://latex.codecogs.com/gif.latex?pa(A,C)&space;=&space;3&space;*&space;3&space;=&space;9" title="pa(A,C) = 3 * 3 = 9" /></a>

The implementation of the preferential attachment algorithm is of complexity class <a href="https://www.codecogs.com/eqnedit.php?latex=O(n^2)" target="_blank"><img src="https://latex.codecogs.com/gif.latex?O(n^2)" title="O(n^2)" /></a>.

## Limitations

With the limitations of the implemented algorithms, only undirected, unweighted graphs are supported currently. 

The plugin was tested using graphs with less than a thousand nodes. Link predictions in larger networks can lead to long runtimes.
