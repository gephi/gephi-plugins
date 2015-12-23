# Gephi Plugins

This repository is an out-of-the-box development environment for Gephi plugins. Gephi plugins are implemented in Java and can extend [Gephi](https://gephi.org) in many different ways, adding or improving features. Getting started is easy with this repository but also checkout the [Bootcamp](https://github.com/gephi/gephi-plugins-bootcamp) for examples of plugins you can create. 

## Migrate Gephi 0.8 plugins

The process in which plugins are developed and submitted had an overhaul when Gephi 0.9 was released. Details can be read on this article: [Plugin development gets new tools and opens-up to the community](https://gephi.wordpress.com/2015/12/16/plugin-development-gets-new-tools-and-opens-up-to-the-community/).

This section is a step-by-step guide to migrate 0.8 plugins. Before going through the code and configuration, let's summerize the key differences between the two environements.

`TODO`

## Get started

### Requirements

Developing Gephi plugins requires [JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html) or later and [Maven](http://maven.apache.org/). Although any IDE/Editor can be used, [Netbeans IDE](https://netbeans.org/) is recommend as Gephi itself is based on [Netbeans Platform](https://netbeans.org/features/platform/index.html).

### Create a plugin

The creation of a new plugin is simple thanks to our custom [Gephi Maven Plugin](https://github.com/gephi/gephi-maven-plugin). The `generate` goal asks a few questions and then configures everything for you.

- Fork and checkout the latest version of this repository:

        git clone git@github.com:username/gephi-plugins.git
- Run the following command and answer the questions:

        mvn org.gephi:gephi-maven-plugin:generate

This is an example of what this process will ask:
        Name of organization (e.g. my.company): org.foo
        Name of artifact (e.g my-plugin): my-plugin
        Version (e.g. 1.0.0): 1.0.0
        Directory name (e.g MyPlugin): MyPlugin
        Branding name (e.g My Plugin): My Plugin
        Category (e.g Layout, Filter, etc.): Layout
        Author: My Name
        Author email (optional):
        Author URL (optional):
        License (e.g Apache 2.0): Apache 2.0
        Short description (i.e. one sentence): Plugin catch-phrase
        Long description (i.e multiple sentences): Plugin features are great
        Would you like to add a README.md file (yes|no): yes

The plugin configuration is created. Now you can (in any order):

- Add some Java code in the `src/main/java` folder of your plugin
- Add some resources (e.g. Bundle.properties, images) into the `src/main/resources/` folder of your plugin
- Change the version, author or license information into the `pom.xml` file, which is in your plugin folder
- Edit the description or category details into the `src/main/nbm/manifest.mf` file in your plugin folder 

### Build a plugin

Run the following command to compile and build your plugin

       mvn clean package

### Run Gephi with plugin

Run the following command to run Gephi with your plugin pre-installed. Make sure to run `mvn package` beforehand to rebuild.

       mvn org.gephi:gephi-maven-plugin:run

## Submit your plugin

Submitting a Gephi plugin for approval is a simple process based on GitHub's [pull request](https://help.github.com/articles/using-pull-requests/) mechanism.

- First, make sure you're working on a fork of [gephi-plugins](https://github.com/gephi/gephi-plugins). You can check taht by running `git remote -v` and look at the url, it should contain your GitHub username, for example `git@github.com:username/gephi-plugins.git`.

- Add and commit your work. It's recommended to keep your fork synced with the upstream repository, as explained [here](https://help.github.com/articles/syncing-a-fork/), so you can run `git merge upstream/master` beforehand.

- Push your commits to your fork with `git push origin master`.

- Navigate to your fork's URL and create a pull request, submit. 
 

## IDE Support

### Netbeans IDE

- Start Netbeans and go to `File` and then `Open Project`. Navigate to your fork repository, Netbeans automatically recognizes it as Maven project. 
- Each plugin module can be found in the `Modules` folder.
