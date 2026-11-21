## Introduction

A Gephi plugin to display your geocoded graphs.

## Plugin Owner’s Notes

This plugin contains only a layout (“GeoLayout”) to display your graph according to geocoded attributes. To use it:

1. Select the two attributes which contain the geocoded coordinates of your nodes (latitude and longitude). The attributes must have a numeric type.
2. Select the projection you want to use
3. Set the other parameters according to the selected projection
4. Launch the algorithm

The available projections are:

* [Mercator](https://en.wikipedia.org/wiki/Mercator_projection)
* [Tranverse Mercator](https://en.wikipedia.org/wiki/Transverse_Mercator_projection)
* [Miller cylindrical](https://en.wikipedia.org/wiki/Miller_cylindrical_projection)
* [Gall-Peters](https://en.wikipedia.org/wiki/Gall%E2%80%93Peters_projection)
* [Sinusoidal](https://en.wikipedia.org/wiki/Sinusoidal_projection)
* [Lambert cylindrical](https://en.wikipedia.org/wiki/Lambert_cylindrical_equal-area_projection)
* [Winkel Tripel](http://en.wikipedia.org/wiki/Winkel_tripel_projection)
* [Equirectangular](https://en.wikipedia.org/wiki/Equirectangular_projection)

Latitudes and longitudes are in degrees. A node has valid coordinates when its latitude is between -90 and 90 and its longitude is between -360 and 360 (so both -180..180 and 0..360 work). If, for any reason, some of your nodes do not have valid values on the selected attributes (missing, or a latitude out of range, for example because latitude and longitude were swapped), they will be displayed on a line at the bottom of your graph (then, you can delete them or do what you want). Also make sure your latitude and longitude columns are of a numeric type, not 'string'.

The Mercator projections can't show the poles, so latitudes beyond ±85.05° (the usual Web Mercator limit) are drawn at that limit.
