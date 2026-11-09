# Bearkat Centralization

Bearkat Centralization is a Gephi Statistics plugin that calculates multiple graph centralization metrics from a single report.

## Features

The plugin calculates:

* Degree Centralization
* Weighted Degree Centralization
* In-Degree Centralization
* Out-Degree Centralization
* Betweenness Centralization
* Eigenvector Centralization
* Closeness Centralization
* Harmonic Closeness Centralization

The plugin also writes node-level values to the Data Laboratory so users can inspect individual node scores after running the statistic.

## Usage

1. Open or import a graph in Gephi.
2. Go to the Statistics panel.
3. Run **Centralization**.
4. View graph-level results in the generated report.
5. View node-level values in **Data Laboratory → Nodes**.

## Notes

Harmonic closeness centralization is included because standard closeness centralization can be affected by disconnected graphs or isolates. Harmonic closeness uses reciprocal distances, allowing unreachable nodes to contribute 0.

Eigenvector centralization may vary across software packages because implementations may use different eigenvector normalization methods and graph-level centralization formulas.

## Credits

Programmed by Braell Dotson and Dr. Nate Jones at Sam Houston State University.

## Reference

Freeman, Linton C. “Centrality in Social Networks: Conceptual Clarification.” *Social Networks* 1, no. 3 (1978): 215–239.
