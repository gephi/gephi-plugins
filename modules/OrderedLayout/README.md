## Ordered Layout

Author: Wouter Spekkink (wouterspekkink(at)gmail.com)

This is the source code for the Event Graph Layout plugin for Gephi (http://gephi.org/).

Most of the code used for this layout plugin is the original source code of the Force Atlas 2 plugin. Probably about 99 percent of the code is a stripped-down version of the Force Atlas 2 source code and some minor additions were made to add the options that were required for the event graph layout. Some inspiration for the implementation of the ideas for this plugin was taken from the GeoLayout plugin. The event graph layout plugin was written to allow for the easy creation of event graphs. In an event graph the nodes represent events and the edges/arcs represent relationships between the events (e.g. causality). The layout plugin places the events in a user-specified order on the x-axis. If vertical force is activated, the plugin will push unconnected groups of events away from each other on the y-axis, and keep connected nodes together. The user needs to add numerical variable to the node list and import it as integer, float or double. The variable should indicate the positions of the node on the x-axis. Once activated, the layout plugin will run continuously until the user stops it. If the plugin is activated, then the position of the nodes on the x-axis will remain fixed, but the user is free to move nodes on the y-axis. This is only useful if the user turns of the verticla force option. 

The plugin has only a few controls:
Scale of order: determines the distance between the nodes on the x-axis.
Order: a dropdown selection menu to select the user-supplied 'order-variable.'
Set Vertical Force: turn vertical force on or off. 
Vertical scale: determines the distance between the nodes on the y-axis.
Strong Gravity Mode: activates stronger gravity.
Gravity: set the strength of the gravity.
Jitter Tolerance: Sets 
Center: Centers the graph. Sometimes useful for smaller datasets.

An example of an application of the layout plugin is used in a forthcoming publication:

Spekkink, W.A.H. (forthcoming). Building Capacity for Sustainable Regional Industrial Systems: 
An Event Sequence Analysis of Developments in the Sloe Area and Canal Zone. 
Accepted for publication in the Journal of Cleaner Production.
Available online at: http://www.sciencedirect.com/science/article/pii/S0959652614008506
