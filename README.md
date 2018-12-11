# Social Network Analysis Visualization Plugin

This plugin is written as assignment for the course Software Engineering at the Leiden Institute of Advanced Computer Science (LIACS).
This Gephi plugin is an implementation of an algorithm of which an implementation already exists in C++. The algorithm was written by  Frank W. Takes and Walter A. Kosters (https://doi.org/10.1145/2063576.2063748). 
This algorithm is believed to be much more efficient than the already implemented algorithm in Gephi.

## Abouts

This plugin is able to do the things below: 
- Designed to handle significantlty large of nodes and edges. 
- Works on the largest weakly component of nodes. 
- Focus is on the analysis of the topology/structure of huge undirected graphs, read from a file.
- Particular focus on distance-related functions for computing the exact and approximate distance distribution graph and the following distance-based network metrics the diameter, center size, radius, periphery size and eccentricity. 
- No support for graph models/generators, node/edge attributes, node/edge weights, multi-partite networks, community detection algorithms or visualization (there are other tools that can do that).
- Users are able to color network image based on the calculated results.


## Features

* Fast computation of the diameter of real-world networks, implementing the *BoundingDiameters* algorithm presented in:

  > F.W. Takes and W.A. Kosters, Determining the Diameter of Small World Networks, in Proceedings of the 20th ACM International Conference on Information and Knowledge Management (CIKM 2011), pp. 1191-1196, 2011. doi: [10.1145/2063576.2063748](http://dx.doi.org/10.1145/2063576.2063748)
  
* Fast computation of extreme distance metrics: the radius, diameter, center size, periphery size and eccentricity distribution of large real-world networks, based on the algorithms presented in:
 
  > F.W. Takes and W.A. Kosters, Computing the Eccentricity Distribution of Large Graphs, Algorithms 6(1): 100-118, 2013. doi: [10.3390/a6010100](http://dx.doi.org/10.3390/a6010100)

Of course, all credit goes to the original authors of these algorithms. 


## Installation
We're working on getting the plugin included in the gephi marketplace. In the meantime, you can download the compiled modules under [releases](https://github.com/alexiooo/BoundingDiameters/releases). A different README explaining how manual plugin installation works is included.

## Support

If you have a problem, want to add some new features to the plugin or you want to know more about Gephi you can use the documentation in the following link https://github.com/gephi/gephi-plugins/blob/master/README.md.
For any other questions about the plugin you can contact f.w.takes@liacs.leidenuniv.nl.

## Disclaimer

This code was written for open-source usage, and is mainly written as part of the course Software Engineering with the attempt to write a good piece of code with respect to programming- or software-engineering standards as good as possible. However there is a change that are still some bugs in the code.
It comes without any warranty of merchantability or suitability for a particular purpose. The plugin has exclusively been tested under Windows.
