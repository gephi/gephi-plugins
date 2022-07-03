## Erdős-Rényi model generator

This plugin enables users to generate Erdős-Rényi G(n,p) and G(n,m) random graph model.

### ER model G(n,p)

Generates random graph with *n* nodes. Edges are generated randomly with *p* probability.

Constraints:
- n > 0
- p >= 0
- p <= 1


### ER model G(n,m)

Generates random graph with *n* nodes and *m* edges. Edges are created with same probability.

Constraints:
- n > 0
- m >= 0
- m < n (n - 1) / 2

### Bibliography

- P. Erdős, A. Rényi. On random graphs. Publicationes Mathematicae Debrecen, 6: 290-297, 1959
  * Access \[13.05.2019\]: <https://www.renyi.hu/~p_erdos/1959-11.pdf>
  * Access \[13.05.2019\]: <http://snap.stanford.edu/class/cs224w-readings/erdos59random.pdf>
- P. Erdős, A. Rényi. On the evolution of random graphs. Publicationes of the Mathematical
  Institute of the Hungarian Academy of Sciences, 5: 17-61, 1960.
  * Access \[13.05.2019\]: <https://www.renyi.hu/~p_erdos/1960-10.pdf>
  * Access \[13.05.2019\]: <http://snap.stanford.edu/class/cs224w-readings/erdos60random.pdf>