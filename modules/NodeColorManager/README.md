## Node Color Manager

> Tool which can save/restore node colors to/from file.

this plugin is useful if you have to keep the same node to be the same color across diffrent graph/projects

this plugin will export the node color to a file ( you must specify where it is ) and later you can restore the colors from that file in a diffrent graph/project


# how to use

The tool has two button and a text field. you should fill the text field with file name( or absolute path ) and press "save" button, then the colors were saved to the file you specified in the text field. The same works for "restore" button.


# color file format

a Unix style(line break with LF) pure text file, just like below:

```
Myriel:rgb:0.35686275,0.35686275,0.9607843
Napoleon:rgb:0.35686275,0.35686275,0.9607843
MlleBaptistine:rgb:0.9607843,0.35686275,0.35686275
MmeMagloire:rgb:0.9607843,0.35686275,0.35686275
CountessDeLo:rgb:0.35686275,0.35686275,0.9607843
Geborand:rgb:0.35686275,0.35686275,0.9607843
Champtercier:rgb:0.35686275,0.35686275,0.9607843
Cravatte:rgb:0.35686275,0.35686275,0.9607843
Count:rgb:0.35686275,0.35686275,0.9607843
OldMan:rgb:0.35686275,0.35686275,0.9607843
Labarre:rgb:0.9607843,0.35686275,0.35686275
```

which is

```
[node label]:rgb:[red value],[green value],[blue value]
```

if you want a #XXYYZZ color format, you can get `XX= Integer.toHexString(Math.round([red value]*255))` in Java


# Notes

The tool identify nodes by their labels, so your graph must have unique label for each node.If two or more node has same label, only one will be restored.

When restore from file, only the nodes which match the labels in file would be colored, others will remain their original color.

# Feed back

please use issue.
