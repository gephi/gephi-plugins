# Changelog

## 1.2.0

A colour key, drawn only when you say what colour means.

### Added

- **Colour legend column** under Nodes. Name the column the node colours
  encode, and the legend adds a colour section below the shapes: one swatch per
  group, in that group's colour, largest group first. Shape and colour can then
  carry two different columns and one legend explains both. **Colour legend
  title** gives the section its own heading.
- The colour section is drawn only when the named column explains the colours
  exactly: every group in one colour, and no colour shared by two groups.
  Otherwise it stays off and the log says why (which group is mixed, or which
  two share a colour). Gephi does not record which column the colours came
  from, and several columns can split the nodes identically, so the plugin
  never guesses.

### Fixed

- Refreshing the preview of another workspace turned this workspace's shapes
  back into circles on its next pan or zoom, while its arrowheads still
  stopped at the old outlines. Gephi shares one renderer across workspaces;
  each node's shape is now kept with the node's own preview item.
- A warning that holds across refreshes (a colour legend column that does not
  match the colours) is logged once, not on every refresh.
- "no value" and "+ 4 more", which are drawn on the figure, are now
  translatable like the rest of the plugin's text.

## 1.1.0

A redesign of how the shapes look, a legend, and rendering fixes.

### Added

- A legend beside the graph, in the preview and in every export, saying which
  shape stands for which category. It uses the categories' colours when colour
  encodes the same column and neutral grey otherwise, lists nodes with no
  value, and can be switched off or retitled under Nodes.

### Changed

- Shapes match the area of the circle they replace instead of being drawn
  inside it, so a square no longer looks a third smaller than a circle of the
  same node size and stars no longer shrink to specks. Shapes with corners
  are trimmed slightly, by at most a sixth, so they look as heavy as the
  circle rather than heavier.
- Corners are softened and borders join round, so small marks stay clean and
  thick borders no longer draw mitred spikes.
- Shapes are centred on their centre of area, so a triangle no longer sits
  visibly high or low on its node.
- A fuller star (inner radius half the outer) that keeps its silhouette when
  small.
- The pentagon, which read as a circle at small sizes, is gone. Shapes six to
  eight are now the cross, a downward triangle, and the hexagon. Graphs with
  five categories or fewer look exactly as before apart from the restyling.
- SVG exports draw each shape as a single `path`.

### Fixed

- Gephi froze when the shape column was named by its title rather than its id,
  as with any column imported from GEXF. Looking the column up left the node
  table locked, and Gephi's interface hung the next time it read the columns.
- The legend's longest label could be clipped at the right edge of an SVG
  opened where its font is not installed. The legend now leaves room for a
  substitute font up to 15% wider.
- Panning or zooming the Preview turned every shape back into a circle until
  the next refresh.
- PDF exports drew triangles and stars upside down.
- Shapes at the edge of the graph could be clipped in SVG and PDF exports,
  because the page was sized from the nodes' circles.
- Edges and arrowheads stopped at the circle Gephi assumes rather than at the
  shape, so arrows fell short of a triangle's base and, once shapes matched
  the circle's area, could disappear under a star's point. Both ends of every
  edge now meet the outline, for straight and curved edges, through a
  replacement for the built-in arrow renderer that leaves edges to circular
  nodes untouched.

## 1.0.0

First release. Assigns node shape from a categorical column, the shape
equivalent of color by partition (gephi/gephi#1291).

### Added

- Preview renderer mapping a node column onto eight shapes, ordered by
  descending category count so the assignment agrees with color by partition.
- Rendering to the on-screen preview and to the SVG, PDF, and PNG exports.
- Column names matched by id first and by title second, so a name read off the
  Data Laboratory resolves even when an import gave the column a different id.
- A logged warning when the partition holds more categories than shape can
  carry, both when shapes repeat and before that, when they merely stop being
  distinguishable.
- A conventional column, `shape_partition`, read when no column is named, so
  the mapping can travel with the data instead of only through the Preview
  panel. A named column that does not resolve is not replaced by it.

### Notes on behaviour

- Shapes are inscribed in the node's declared size and never overflow it.
- Nodes with no value in the column fall through to the default node renderer,
  so color, size, border, and opacity are untouched.
- The renderer replaces the built-in node renderer rather than drawing over it,
  so it needs no enabling.
