# Social Network Analysis Visualization Plugin

This plugin is written as assignment for the course Software Engineering at the Leiden Institute of Advanced Computer Science (LIACS). In this course teams of students realize software solutions.
This plugin is an implementation of an algorithm of which an implementation already exists in C++. This algorithm was written by  Frank W. Takes and Walter A. Kosters (https://doi.org/10.1145/2063576.2063748). 


## Abouts

This plugin is able to do the things below: 
- Designed to handle millions of nodes and billions of edges.
- Focus is on the analysis of the topology/structure of huge undirected graphs, read from a file.
- Particular focus on distance-related functions for computing the exact and approximate distance distribution and the following distance-based network metrics the diameter, center, radius, periphery and eccentricity. 
- No support for graph models/generators, node/edge attributes, node/edge weights, multi-partite networks, community detection algorithms or visualization (there are other tools that can do that).
- Users are able to color network image based on the calculated results

## Features

- Fast computation of the diameter of real-world networks, implementing the BoundingDiameters algorithm presented in:
F.W. Takes and W.A. Kosters, Determining the Diameter of Small World Networks, in Proceedings of the 20th ACM International Conference on Information and Knowledge Management (CIKM 2011), pp. 1191-1196, 2011. doi: 10.1145/2063576.2063748


