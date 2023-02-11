## Sea Dragon Plugin

This plugin export 

## Quick Run

- Open Gephi, and make sure you installed the `sea-dragon-plugin` plugin.
- Open the graph you want to render with the plugin.
- Do your spatialisation and filtering on your graph until you're satisfied.
- Go to the `Preview panel` and select a preset on the `Preview Settings`. The plugin will base the output on the style your selected. Have an overview by clicking `Refresh`
- When you're satisfied click on `File >> Export >> Seadragon web`
- Choose where to export the map and the configuration about the size and click `Ok`
- The plugin should be processing now, the time will vary and can be long if you have a lot of entities and asking for big dimension.
- When finished, a message will tell you that seadragon has finished exporting your graph.

Because of security issue, you can't open the html as a normal file on you web brower to make it work. See the solutions bellow to make it works properly

## How to run locally

One solution is to run a server locally. If you have Python installed on your machine, here is the easiest way to make it works :

- With a command line, go to the directory where you exported the graph (it should be a directory with a file called `index.html` )
- Type the command `python -m http.server 8765`
- Open your web browser and go to `http://localhost:8765` 

You should be able to open the web page and use opensea dragon plugin with your graph loaded.

## How to share via a website 

If you have a website, all you need to do is to copy the directory where you exported the graph via the plugin to your server. Then you should be able to access the page and the graph
via your remote website.