## Distance Backbone Toolbox

This plugin allows you to compute the metric and ultrametric distance backbones defined in Simas et al 2021 [https://doi.org/10.1093/comnet/cnab021]. For larger networks, we recommend using the Python library distanceclosure [https://github.com/CASCI-lab/distanceclosure]. 

Those backbones are a parameter-free and algebraically-principles network sparsification methodologies which remove edges that break a generalized triangular inequality and, as a consequence, are redundant for shortest-path computations. Interestingly, the ultrametric backbone expands the concept of a minimum spanning tree to directed graphs and is itself a subgraph of the metric backbone for both directed and undirected networks.
