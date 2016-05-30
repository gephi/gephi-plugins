This plugin identifies the ancestors and descendants of a node (identified by the user) in a directed graph (not suitable for undirected graphs).

Usage: After installing the plugin, the Lineage option can be found in the statistics menu. The user is asked to submit a node (use the Id, not the Label!) and the plugin will find the ancestors and descendants for that node. In the nodes list, a variable “Lineage” is added that indicates for each node whether it is the origin node, an ancestor, a descendant, both ancestor and descendant (it will be marked as a hybrid), or unrelated. Two boolean variables are also added that indicate for each node whether they are ancestor or descendant (they can be both when there are cycles in the graph). Two integers are added to record the distance of ancestors and descendants from the origin. The distances of ancestors are recorded as negative values.

The “Lineage” variable can be used for (for example) partition coloring and filtering.
