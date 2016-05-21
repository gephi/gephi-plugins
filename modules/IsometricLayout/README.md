## Isometric Layout

This layout uses an isometric perspective to visualize networks. 
I.e. it generates 3D coordinates for all network nodes (x,y,z).
Thus, is a method for the visual representation of three-dimensional nodes in two dimensions.
It can be used to split a network into distinct Z-Layers 
(e.g. to better visualize high-performers and/or low-performers, or communities after using modularity algorithms).
Besides, computed Z-layers can be used in Gephi ranking and/or partition procedures.
To use it, is quite simple, network nodes should have an attribute containing "[z]" in its name. 
E.g., something like "Degree [z]" or "YourNodeColumn[z]".
The IsometricLayout will compute and segment z-Levels regarding your "[z]" column values (with any ranges).
If your network don't have a "[z]" column, no problem, all nodes will be placed in Z-Layer 0 (Zero).  
  
##Examples:  
  

![Isometric Layout View](http://www.relationalcapitalvalue.com/myresources/example-0-isometric-layout.png)  
  
 
  
  
Learning trail with a quick tutorial at:  
http://www.relationalcapitalvalue.com/gephiplugins.html  
  
  

