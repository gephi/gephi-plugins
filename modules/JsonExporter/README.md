## Introduction

A Gephi plugin to create a JSON file of your network graph.

## Plugin Owner's Notes

This plugin contains a export plugin that takes the current graph and creates a simple JSON file with the nodes and edges of the graph and their attributes. The JSON file has two arrays: one, named "nodes", contains one JSON object for each node in the graph and the other, named "edges", has one JSON object for each edge.

This file is suitable to be used with the [JSON parser for Sigma.js](https://github.com/jacomyal/sigma.js/tree/master/plugins/sigma.parsers.json). Using JSON with Sigma.js improves load time and browser compatibility.

The code is available under a GPLv3 License.

