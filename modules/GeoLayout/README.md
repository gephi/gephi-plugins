## Introduction

A Gephi plugin to display your geocoded graphs.

## Plugin Owner’s Notes

This plugin contains only a layout (“GeoLayout”) to display your graph according to geocoded attributes. To use it:

1. Select the two attributes which contain the geocoded coordinates of your nodes (latitude and longitude). The attributes must have a numeric type.
2. Select the projection you want to use
3. Set the other parameters according to the selected projection
4. Launch the algorithm

The available projections are:

* [Mercator](http://bit.ly/JQuvw)
* [Tranverse Mercator](http://bit.ly/btWRTI)
* [Miller cylindrical](http://bit.ly/agJOxd)
* [Gall-Peters](http://bit.ly/Nj5cW)
* [Sinusoidal](http://bit.ly/a8SsNg)
* [Lambert cylindrical](http://bit.ly/cnuxqr)
* [Winkel Tripel](http://en.wikipedia.org/wiki/Winkel_tripel_projection)
* [Equirectangular](http://bit.ly/vxEmy)

For any questions or just more information, you can check the related [thread](http://bit.ly/2vISDFd) on the Gephi forum.

And if, for any reason, some of your nodes do not have valid values on the selected attributes (for latitude and longitude), they will be displayed on a line on the bottom of your graph (then, you can delete them or do what you want). Also make sure your latitude and longitude columns are of a numeric type, not 'string'.
