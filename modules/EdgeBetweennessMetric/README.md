# Edge Betweenness Metric

Author: Jiri Krizek

Supervisor: Jaroslav Kuchar

This plugin allows compute Edge Betweenness metric, which can be used in Social Network Analysis.

Edge betweenness of an edge as the number of shortest paths between pairs of nodes that run along it.

## Tutorial

Computation of Edge Betweenness can be started using "Statistics" panel. This
panel is usually on the right part of Gephi window. If you don't see this panel,
enable it using "Window/Statistics" from the main menu.

![ebstart](https://raw.github.com/jaroslav-kuchar/EdgeBetweennessMetric/master/images/eb.png)

* Edge Betweenness can be computed either on **directed** or **undirected** graph.
* You can use **Normalize** parameter. This parameter will normalize computed values between 0 and 1.

![ebparameters](https://raw.github.com/jaroslav-kuchar/EdgeBetweennessMetric/master/images/eb2.png)

Computed values will be available in **Data laboratory** (column named Edge Betweenness).

## License
The GPL version 3, http://www.gnu.org/licenses/gpl-3.0.txt