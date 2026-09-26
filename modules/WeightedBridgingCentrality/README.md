# Weighted Bridging Centrality for Gephi

Weighted Bridging Centrality is a Gephi plugin for calculating bridging centrality in weighted networks.

The plugin extends the standard bridging-centrality calculation by allowing edge weights to represent connection strength. For shortest-path calculations, edge strength is converted to effective distance according to:

d = 1 / w

where `w` is the edge weight and `d` is the effective distance. Consequently, stronger connections are treated as shorter paths.

The plugin calculates three node-level measures and adds them to the Gephi Data Laboratory:

- **Weighted Betweenness Centrality**
- **Bridging Coefficient**
- **Weighted Bridging Centrality**

Weighted Bridging Centrality is calculated as:

Weighted Bridging Centrality = Weighted Betweenness Centrality × Bridging Coefficient

## Origin and Attribution

This plugin is an adaptation of the original **Bridging Centrality** plugin for Gephi. The original implementation is credited in the source code to Getúlio de Morais Pereira, Anderson Rodrigues dos Santos, and Luis Felipe Nunes Reis.

The weighted adaptation was developed by **Nathan Steinmeyer**. It preserves the original plugin's bridging-coefficient calculation while modifying the betweenness-centrality calculation to support weighted shortest paths, interpreting edge weights as connection strengths through the transformation `d = 1/w`.

The plugin has also been updated and packaged for the current Gephi plugin development environment.

## Usage

1. Install the plugin in Gephi.
2. Import or create a weighted network.
3. Ensure that the edge `Weight` values represent **connection strength**, where larger weights indicate stronger connections.
4. Open the **Statistics** panel and run **Weighted Bridging Centrality**.
5. Select whether the network should be treated as directed and whether the results should be normalized.
6. After the calculation finishes, the following columns are added to the Data Laboratory:
   - Weighted Betweenness Centrality
   - Bridging Coefficient
   - Weighted Bridging Centrality

### Weight Interpretation

This plugin assumes that edge weights represent **strength rather than distance**.

For shortest-path calculations, each edge weight is transformed according to:

`d = 1 / w`

where:

- `w` = edge weight (connection strength)
- `d` = effective distance used for shortest-path calculations

Therefore, a larger edge weight produces a shorter effective distance. For example, an edge with weight `10` has an effective distance of `0.1`, while an edge with weight `1` has an effective distance of `1`.

Edge weights must be greater than zero.

## Validation

The weighted shortest-path implementation was manually validated against small networks with analytically determined results.

### Four-Node Weighted Test

Undirected network:

| Edge | Weight |
| --- | ---: |
| A–B | 10 |
| B–D | 10 |
| A–C | 1 |
| C–D | 1 |

With normalization disabled, the expected and observed results were:

| Node | Weighted Betweenness | Bridging Coefficient | Weighted Bridging Centrality |
| --- | ---: | ---: | ---: |
| A | 0.5 | 0.5 | 0.25 |
| B | 1.0 | 0.5 | 0.50 |
| C | 0.0 | 0.5 | 0.00 |
| D | 0.5 | 0.5 | 0.25 |

### Equal-Weight Shortest-Path Test

The same four-node network was tested with all edge weights set to `1`. This creates multiple equally short paths and tests whether shortest-path contributions are divided correctly.

All four nodes returned:

- Weighted Betweenness Centrality: `0.5`
- Bridging Coefficient: `0.5`
- Weighted Bridging Centrality: `0.25`

### Six-Node Weighted Test

A more complex six-node network was also tested:

| Edge | Weight |
| --- | ---: |
| A–B | 10 |
| B–D | 10 |
| A–C | 1 |
| C–D | 1 |
| B–E | 5 |
| D–E | 5 |
| C–F | 4 |
| E–F | 2 |

With normalization disabled, the plugin returned:

| Node | Weighted Betweenness | Bridging Coefficient | Weighted Bridging Centrality |
| --- | ---: | ---: | ---: |
| A | 0 | 0.75 | 0 |
| B | 3 | 0.2857 | 0.8571 |
| C | 0 | 0.25 | 0 |
| D | 0 | 0.3333 | 0 |
| E | 5 | 0.2857 | 1.4286 |
| F | 3 | 0.75 | 2.25 |

These tests verify weighted shortest-path selection, handling of multiple equal shortest paths, calculation of the bridging coefficient, and calculation of Weighted Bridging Centrality.

## References

The calculations implemented in this plugin draw on the following algorithms and measures:

- Brandes, Ulrik. 2001. “A Faster Algorithm for Betweenness Centrality.” *The Journal of Mathematical Sociology* 25 (2): 163–177. DOI: 10.1080/0022250X.2001.9990249.

- Dijkstra, E. W. 1959. “A Note on Two Problems in Connexion with Graphs.” *Numerische Mathematik* 1: 269–271. DOI: 10.1007/BF01386390.

- Hwang, Woochang, Taehyong Kim, Murali Ramanathan, and Aidong Zhang. 2008. “Bridging Centrality: Graph Mining from Element Level to Group Level.” In *Proceedings of the 14th ACM SIGKDD International Conference on Knowledge Discovery and Data Mining*, 336–344. DOI: 10.1145/1401890.1401934.

Brandes provides the algorithmic basis for efficient betweenness-centrality calculation, including weighted networks. Dijkstra provides the shortest-path algorithm used for non-negative edge distances. Hwang et al. introduce the bridging coefficient and bridging-centrality measure on which the original Gephi plugin is based.

## Requirements

- Gephi 0.11.x
- Java 17 or later

## Building

From the root of the `gephi-plugins` repository, run:

`mvn clean package`

The compiled `.nbm` plugin package will be generated under:

`modules/WeightedBridgingCentrality/target/nbm/`

## License

This plugin is distributed under the GNU General Public License, version 3 (GPL-3.0). See `LICENSE.txt` for the full license text.

This project is derived from the earlier Bridging Centrality plugin and retains attribution to its original contributors as described above.