This plugin performs a Multidimensional Scaling Analysis, using the Stress Minimization algorithm included in the MDSJ library. The MDSJ library was created by Algorithmics Group of the University of Konstanz (see reference below). The MDSJ library was made available under the Creative Commons License "by-nc-sa" 3.0. The license is available at http://creativecommons.org/licenses/by-nc-sa/3.0/.

The plugin uses path distances between nodes as input for the Multidimensional Scaling analysis, and it assigns geometric coordinates to the nodes in such a way that nodes with shorter path distances are close together and nodes with longer path distances are far apart. The plugin also reports a stress value that indicates the fit of the configuration. The lower the stress, the better the configuration represents that actual path distances between the nodes. A weight matrix is used to determine how much the distance between any pair of nodes contributes to stress (weight[i][j]  = distance[i][j]^exponent). In the default setting, no weighting of the distances occurs (all distances are treated equally). It is also possible to downweigh large distances and upweigh small distances (an exponent of -2 is used). According to the authors of the MDSJ library this is the more common choice.

Optionally, edge weights can be interpreted by the plugin as representing distances / dissimilarities or proximities / similarities. In that case the distances between nodes will be based on the edge weights (which are first normalized to values from 1 to 2). These options will not make sense for all applications, and the plugin will in some cases return missing values. 

The plugin produces coordinates on up to 10 dimensions (to be selected by the user) and assigns these to the nodes in the nodes list (see the data laboratory). The coordinates can then be used by the MDS_Layout plugin (needs to be downloaded separately). 

The direction of edges/paths is currently ignored. With directed paths the resulting MDS configuration typically has a lot of overlapping nodes.

Reference:
Algorithmics Group. MDSJ: Java Library for Multidimensional Scaling (Version 0.2). Available at http://www.inf.uni-konstanz.de/algo/software/mdsj/. University of Konstanz, 2009.
