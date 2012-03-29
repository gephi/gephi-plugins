This repository is an out of the box development environment for [Gephi](http://gephi.org) plugins. It contains the latest stable version of Gephi modules so you can run a development version of Gephi in seconds. Checkout the [Bootcamp](https://github.com/gephi/gephi-plugins-bootcamp) for examples of the different type of plugins you can create.

Branches contains Gephi Plugins supported by the core team:

- [Neo4j Graph Database Support](https://gephi.org/plugins/neo4j-graph-database-support)
- [Graph streaming](https://gephi.org/plugins/graph-streaming)
- [GeoLayout](https://gephi.org/plugins/geolayout/)
- Python Scripting (In Development)

## Get started

Follow the steps to get the right development environment for developing Gephi plug-ins.

- Download and install the latest version of [Netbeans IDE](http://netbeans.org).
- Fork and checkout the latest version of this repository:

        git clone git@github.com:username/gephi-plugins.git

- Start Netbeans and Open Project. This folder is automatically recognized as a module suite.
- Right click on the project and select 'Run'. This starts Gephi.

You can also run Gephi from the command-line using the 'ant run' command.

## Create a plugin

### Create a new module

- In Netbeans, expand the '''Gephi Plugins''' project and right-click on Modules. Select '''Add New...'''
- Enter the plugin name: '''MyFirstPlugin''' and click '''Next'''.
- In the next panel, enter a unique codebase name, for instance '''org.myname.myfirstplugin''' and click '''Finish'''.
- In the Netbeans Project tree, you should see now your module '''MyFirstPlugin'''. Expand it to find its Source Package. Here you will place your code.

### Brand your plugin

- Right-click on your plugin project '''MyFirstPlugin''' and select '''Properties'''.
- Select '''Display''' on the left panel and fill '''Display Category''', '''Short Description''' and '''Long Description'''.
- Select '''Packaging''' on the left panel and fill '''License''', '''Homepage''' (if exists) and '''Author''' information.
- Click on OK to validate changes.


### Distribute your plugin ###

When you successfully tested your plugin, it's time to create a release. Verify that you have latest updates of Gephi JARs:

- Pull latest changes:

        git checkout master
        git pull
        git checkout mybranch

- Test your plugin again in case of an update. If it's okay:
- Right-click on the project and select '''Package As''' and then '''NMBs'''.
- Go to the '''build''' folder and find the created '''plugin-release.zip''' file. Individual NBM files (one per each module) are packaged in this zip.

Now you can publish you plugin on the [**Gephi Plugin portal**](http://gephi.org/plugins).

## Plugins Portal

Our [**wiki**](http://wiki.gephi.org/index.php/Plugins_portal) contains a good list of tutorials to code plugins. Also checkout the [Bootcamp](https://github.com/gephi/gephi-plugins-bootcamp) for real examples.