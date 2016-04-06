## Network Splitter 3D


This layout can be used to split a network layout into distinct Z-Layers  
(Network Splitter 3D Z-Layers are user-defined clusters).  

E.g. after using layouts such as: Yifan Hu, Fruchterman Reingold, Force Atlas, Circular, Layered, OpenOrd, etc.  

Computed Z-Layers can be used in Gephi ranking and/or partition procedures.  

To use this plugin it is quite simple: network nodes should have an attribute containing “[z]” in its name.  
E.g. something like “Degree [z]” or “YourNodeColumn[z]“.  

The Network Splitter 3D will compute and segment z-Levels regarding your “[z]” column values (with any ranges).  
  
If your network don’t have a “[z]” column, no problem, all nodes will be placed in Z-Layer 0 (Zero).  
  
  
Another usefull feature:  
You can use this plugin to rotate your graph over the X-Axis.  
  
##Examples:  
  
![Network Splitter 3D](http://www.relationalcapitalvalue.com/myresources/example-0-networksplitter3d-layout.png)  
  

##Learning trail with a quick tutorial at:  
http://www.relationalcapitalvalue.com/gephiplugins.html  


