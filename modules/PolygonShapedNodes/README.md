<h1>Polygon shaped nodes</h1>

<h2>Description</h2>
<ul>
<li>Extends default node renderer to support polygon shaped nodes with arbitrary number of sides. Add a column of Integers in the data table named "Polygon", the value corresponds to the number of sides. The renderer must be enabled in the "Manage Renderers" tab.
</ul>

<h2>How to use</h2>
<ul>
<li>In the "Data Laboratory" tab, add a column with the title "Polygon" and type "Integer".
<ul>
<li> This number in the Polygon column corresponds to the desired number of sides the node will have in the preview.
<li> NOTE: A null/invalid value (i.e. leaving the column blank or a value less than 3) will result in the node being rendered as a circle.
</ul>
<li>In the "Preview" tab, make sure "Polygon shaped nodes" is checked in the "Manager renderers" tab.
<li>Then in the "Settings" tab make sure "Enable polygon shaped nodes" is checked.
<li>When the preview is refreshed the nodes should be displayed as polygons with sides corresponding to the number in the "Polygon" column.
</ul>