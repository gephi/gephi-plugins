# Social Network Analysis Visualization Plugin

This plugin is written as assignment for the course Software Engineering at the Leiden Institute of Advanced Computer Science (LIACS). In this course teams of students realize software solutions.
This Gephi plugin is an implementation of an algorithm of which an implementation already exists in C++. The algorithm was written by  Frank W. Takes and Walter A. Kosters (https://doi.org/10.1145/2063576.2063748). 
This algorithm is believed to be much more efficient than the already implemented algorithm in Gephi.


## Abouts

This plugin is able to do the things below: 
- Designed to handle millions of nodes and billions of edges.
- Focus is on the analysis of the topology/structure of huge undirected graphs, read from a file.
- Particular focus on distance-related functions for computing the exact and approximate distance distribution and the following distance-based network metrics the diameter, center size, radius, periphery size and eccentricity. 
- No support for graph models/generators, node/edge attributes, node/edge weights, multi-partite networks, community detection algorithms or visualization (there are other tools that can do that).
- Users are able to color network image based on the calculated results

## Features

* Fast computation of the diameter of real-world networks, implementing the *BoundingDiameters* algorithm presented in:

  > F.W. Takes and W.A. Kosters, Determining the Diameter of Small World Networks, in Proceedings of the 20th ACM International Conference on Information and Knowledge Management (CIKM 2011), pp. 1191-1196, 2011. doi: [10.1145/2063576.2063748](http://dx.doi.org/10.1145/2063576.2063748)
  
* Fast computation of extreme distance metrics: the radius, diameter, center size, periphery size and eccentricity distribution of large real-world networks, based on the algorithms presented in:
 
  > F.W. Takes and W.A. Kosters, Computing the Eccentricity Distribution of Large Graphs, Algorithms 6(1): 100-118, 2013. doi: [10.3390/a6010100](http://dx.doi.org/10.3390/a6010100)

* Computation of closeness centrality in parallel, exact or approximated using the method discussed in:

  > D. Eppstein and J. Wang, Fast Approximation of Centrality, Journal of Graph Algorithms and Applications 8(1): 39--45, 2004. doi: [10.7155/jgaa.00081](http://dx.doi.org/10.7155/jgaa.00081)

* Computation of betweenness centrality in parallel, exact or an adapted approximate version of the method introduced in:

  > U. Brandes, A Faster Algorithm for Betweenness Centrality, Journal of Mathematical Sociology 25(2): 163-177, 2001.

Of course, all credit goes to the original authors of these algorithms. 