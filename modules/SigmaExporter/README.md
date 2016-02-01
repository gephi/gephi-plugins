## Introduction

A Gephi plugin to create an HTML5 interactive display of your network graph that runs in any modern web browser.

## Plugin Owner's Notes

This plugin contains an export plugin that takes the current graph and creates files to display your network interactively with HTML5. It uses the [open-source sigma.js library](https://github.com/jacomyal/sigma.js). The network data is placed in a ``data.json file`` (using a format equivalent to the [JSONExporter plugin](https://github.com/oxfordinternetinstitute/gephi-plugins/tree/jsonexporter-plugin)) and configuration details (author name, description, etc.) are stored in a ``config.json`` file. Other HTML, CSS, and JavaScript files are copied without modification from a standard template.

Simple customization is possible through manually editing the ``config.json`` file as explained on the [project wiki](https://github.com/oxfordinternetinstitute/gephi-plugins/wiki).

The Java code of this exporter is available under a GPLv3 License.

The HTML5 template was initially created through the [InteractiveVis project](http://blogs.oii.ox.ac.uk/vis/), and subsequently updated by the [NEXUS: Real Time Data Fusion and Network Analysis for Urban Systems project](http://www.oii.ox.ac.uk/research/projects/?id=149) at the [Oxford Internet Institute, University of Oxford](http://www.oii.ox.ac.uk/).

**Important** The files will not run locally with Chrome due to JavaScript security settings. Once uploaded to a webserver, the network will display in any modern web browser (including Chrome). [The HTML/JavaScript/CSS code is available at this repository](https://github.com/oxfordinternetinstitute/InteractiveVis/tree/master/network).

The InteractiveVis project of the Oxford Internet Institute with funding by JISC aimed to allow easy creation of interactive visualisations for geospatial and network data using native web technologies (HTML5, CSS3, and SVG) and allow these visualisations to be self-contained so that they may run entirely offline in ebooks and other media. The project surveyed existing solutions and built the necessary components to fill in missing features and smooth over incompatibilities in between existing libraries. More information about the project is available on the [project blog](http//blogs.oii.ox.ac.uk/vis/).

The project is maintained by [Scott Hale](http://www.scotthale.net/)
