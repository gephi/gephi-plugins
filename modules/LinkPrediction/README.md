## Link Prediction

Link-prediction plugin for Gephi, which allows to predict the next edges to be formed using different prediction algorithms. Edges that are added to the network based on the prediction are marked accordingly. Users can limit the number of edges predicted. The plugin contains an evaluation component, which allows to compare the quality of the different algorithms.

The plugin is released under the Apache 2.0 license.

## Features

The plugin contains the following functionality:

* __Statistics__ : New edges can be added to an undirected graph using selected implemented prediction algorithms in the statistics tab. The number of new edges can be specified. In doing so, _n_ new edges are added to the graph iteratively. The calculation of the next predicted edge is always based on the graph of the preceding iteration step.
* __Filter__ : The added edges can be displayed by means of filters. On the one hand, the corresponding algorithm is specified as the filter criterion. On the other hand, the number of added edges can also be restricted.

## Algorithms


Link prediction is based on an existing network and attempts to predict new edges. The most popular application is the suggestion of new friends on social networking platforms.
To predict a new edge, different algorithms exist. The plugin allows to easily add new algorithms. Currently the following algorithms are implemented:

### Common neighbours

Common neighbours calculates for two unconnected nodes how many common neighbours exist. The higher the calculated value, the more likely a new edge will be added between the two nodes.

The following formula represents, how the number of common neighbors of two nodes X and Y can be calculated

<a href="https://www.codecogs.com/eqnedit.php?latex=cn&space;(X,&space;Y)&space;=&space;|N(X)&space;\cap&space;N(Y)|" target="_blank"><img src="https://latex.codecogs.com/gif.latex?cn&space;(X,&space;Y)&space;=&space;|N(X)&space;\cap&space;N(Y)|" title="cn (X, Y) = |N(X) \cap N(Y)|" /></a>
### Preferential attachment

The basic assumption with Preferential Attachment is that the probability that a node is affected by a newly added edge, is just proportional to the number of neighbors. The more neighbours a node has, the larger the likelihood that it will be affected.

To calculate preferential attachment the number of neighbours of both nodes are multiplied by each other:

<a href="https://www.codecogs.com/eqnedit.php?latex=pa&space;(X,&space;Y)&space;=&space;|N(X)|&space;*&space;|N(Y)|" target="_blank"><img src="https://latex.codecogs.com/gif.latex?pa&space;(X,&space;Y)&space;=&space;|N(X)|&space;*&space;|N(Y)|" title="pa (X, Y) = |N(X)| * |N(Y)|" /></a>
## Get started

The statistics serve as a starting point with which new edges can be added to the graph.

The filters then allow you to narrow down the corresponding edges.
