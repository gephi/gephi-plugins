# Ordered layout
Author: Wouter Spekkink (wahsp(at)tutanota.com)

This is the source code for the Ordered Graph layout plugin for Gephi (http://gephi.org/).

## Introduction
Most of the code used for this layout plugin is the original source code of the Force Atlas 2 plugin. Probably about 99 percent of the code is a stripped-down version of the Force Atlas 2 source code and some minor additions were made to add the options that were required for the ordered layout. Some inspiration for the implementation of the ideas for this plugin was taken from the GeoLayout plugin. The Ordered Graph layout plugin is useful when the nodes of your graph have some meaningful order (e.g., order in time or a hierarchical order) that is expressed in a numeric variable in the nodes list (specified by the user). The ordered layout plugin allows you to create a layout that visualizes that order on one axis. It also offers the possibility to layout the nodes on the remaining (free) axis automatically, using simulated forces.

## Instructions
Once activated, the layout plugin will run continuously until the user stops it. If the plugin is activated, then the position of the nodes will remain fixed on one axis, but the user is free to move nodes on the free axis. If the force is activated on the free axis, the nodes will be automatically laid out on that axis as well. The user can toggle whether the nodes are fixed on the horizontal or the vertical axis. Note that the effects of this are usually only visible when the force on the free axis is activated.

### Controls
The plugin has only a few controls: 
- Vertical layout: Toggle to change from a horizontally ordered to a vertically ordered layout. Effects are sometimes only noticeable when force on free axis is activated. 
- Inverted layout: Toggle to invert layout from left-right / top-bottom to right-left / bottom-top.
- Order: a dropdown selection menu to select the user-supplied 'order-variable.' 
- Scale of order: determines the distance between the nodes on the fixed axis. 
- Set Force on free axis: Toggle force on free axis on or off. 
- Force strength: Sets the strength of the forces on the free axis. 
- Strong Gravity Mode: activates stronger gravity among the simulated forces. 
- Gravity: set the strength of the gravity. 
- Jitter Tolerance: Determines how much nodes will jitter when bumping into each other. A higher jitter force makes the layout more erratic, but sometimes allows nodes to pass each other when they are blocked.
- Threads: Sets the number of CPU threads that the layout is allowed to use. 
- Sets Center: Centers the graph. Sometimes useful for smaller datasets.

## Reference
An example of an application of the layout plugin is used in the following publication:

Spekkink, W.A.H. (2015). Building Capacity for Sustainable Regional Industrial Systems: An Event Sequence Analysis of Developments in the Sloe Area and Canal Zone. Journal of Cleaner Production 98. 
