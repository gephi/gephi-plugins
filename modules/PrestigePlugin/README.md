### Gephi Prestige Metrics

This plugin can calculate the following four prestige metrics on DIRECTED Graphs.

## Indegree Prestige
The *Indegree Prestige* metric for a node *v* is defined by the number of other nodes that are directly pointing to *v*. As a result there are two additional node properties created:
- **pr_indegree**: Indegree prestige of *v*
- **pr_indegree_normalized** : Normalized *Indegree prestige*, which is calculated by dividing "Indegree Prestige" by (Number of nodes -1). This value will always between 0 and 1 (0 = no other nodes are directly pointing to *v*, wherease 1 means that all other nodes are directly pointing to *v*)

## Domain Prestige
The *Domain Prestige* metric for a node *v* is the fraction of nodes within a network that are directly od indirectly pointing to *v*. This answers the question: Which fraction of the network can reach node *v*. The Result is stored in the node property **pr_domain**

## Proximity Prestige
The *Proximity Prestige* can be seen as an extension of the *Indegree Prestige*. *Indegree Prestige* is a local measurement because only directly connected nodes are counted. Proximity prestige for a node *v* also considers nodes that are indirectly pointing to *v*. The path lengths are used for weighting; shorter paths are more valuable.

The formula is:

Paremeters:

`v`: Node for which the *Proximity Prestige* is calculated

`I`: Set of nodes which are directly or indirectly pointing to *v*

`Sum(d)`: Sum of all shortest paths from all Nodes in `I` to `v`

*above* = Fraction of Nodes pointing (directly or indirectly) to *v* = `|I| / (n-1)`

*below* = Average shortest path lengths of Nodes pointing to *v*: `SUM(d) / |I|`

ProxiityPrestige (v) = `above / below`

The result is stored in the node attribute **pr_proximity**.

## Rank Prestige
The *Rank Prestige* is similar to the *Indegree Prestige*. But instead of just counting the number of connected nodes the rank / status / prominence of the neighbour nodes is considered and must be defined. Therefore the setup is a bit more complicated. You have to select a nummeric or boolean attribute called *prominence*, which indicates the status of a person (e.g. Number of fans).
To calculate the *Rank Prestige* of a node *v*, all the *prominence* values from nodes directly pointing to *v* are summed up.

There is an option to *log-transform* the result.

Finally two node attributes are created:
- **pr_rank**: Rank-Value for node
- **pr_rank_min-max-normalized**: Min-Max-Normalized Rank-Value for node
