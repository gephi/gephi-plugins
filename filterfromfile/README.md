# Filterfromfile
This is a plugin for Gephi to enable filtering the nodes in a graph by a list of labels provided in a text file

The plugin allows users to filter the graph by providing a text file with the list of node labels they want to keep. 

## Instructions

To run the filter go to the _Filters_ section and select _Attributes>Filter by list_ by double-clickng or dragging it to the _Queries_ area.

The filter allows you to select a text file (*.txt) from your hard drive and use it as a list of the nodes to be kept. The text shall contain one node label per line.

Any line that isn't a label will not be taken into account.

If you wish to keep also the neighbors of the nodes belonging to the list, then check the option "Include neighbors". This will keep all the nodes that are connected to the nodes in the text file via incoming or outgoing edges.
