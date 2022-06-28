/*
  Copyright (C) 2016 Gary Briggs

  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.

  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:

  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     in a product, an acknowledgment in the product documentation would be
     appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.

        Gary Briggs <chunky@icculus.org>
*/

package org.icculus.chunky.gephigraphviz;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;

import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.Node;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public class GraphvizLayout extends AbstractLayout implements Layout {

    // http://www.graphviz.org/doc/info/attrs.html
    private String algoName = "dot";
    private String dotBinary = "dot";
    private String rankDir = "LR";
    private String overlap = "false";
    private Boolean concentrate = false;
    
    private Process dotprocess = null;

    private Graph graph;

    public GraphvizLayout(LayoutBuilder layoutBuilder) {
        super(layoutBuilder);
    }

    @Override
    public void initAlgo() {
        this.graph = graphModel.getGraphVisible();
        setConverged(false);
    }

    @Override
    public void goAlgo() {
        // Prepare input
        final StringBuffer dotfile = new StringBuffer();

        String graphtype;
        String edgearrow;
        
        if(graph.isDirected()) {
            graphtype = "digraph";
            edgearrow = " -> ";
        } else if(graph.isUndirected()) {
            graphtype = "graph";
            edgearrow = " -- ";
        } else {
            //graphviz does not support mixed graphs
            graphtype = "digraph";
            edgearrow = " -> ";
        }
        
        dotfile.append(String.format("%s g {\n", graphtype));
        dotfile.append("layout = \"").append(this.algoName).append("\";\n");
        dotfile.append("rankdir = \"").append(this.rankDir).append("\";\n");
        dotfile.append("overlap = \"").append(this.overlap).append("\";\n");
        if (this.concentrate) {
            dotfile.append("concentrate=true;\n");
        }

        try {
            graph.readLock();
        
            for (final Node n : this.graph.getNodes()) {
                dotfile.append(n.getId());
                dotfile.append(" [");
                dotfile.append("pos=\"").append(n.x()).append(',').append(n.y()).append('"');
                dotfile.append("];\n");
            }
            for (final Edge e : this.graph.getEdges()) {
                dotfile.append(e.getSource().getId());
                dotfile.append(edgearrow);
                dotfile.append(e.getTarget().getId());
                dotfile.append(" [weight=");
                dotfile.append(e.getWeight());
                dotfile.append("];\n");
            }
            dotfile.append("}\n");
        } finally {
            graph.readUnlock();
        }

        // Call Graphviz
        // we are calling it directly. However, there is also a java binding
        // http://www.graphviz.org/pdf/gv.3java.pdf
        final List<String> cmd = new ArrayList<String>();
        cmd.add(this.dotBinary);
        cmd.add("-Tdot");
        final ProcessBuilder pb = new ProcessBuilder(cmd);


        try {
            dotprocess = pb.start();
            try (OutputStream out = dotprocess.getOutputStream();
                    BufferedWriter inputForGraphviz = new BufferedWriter(new PrintWriter(out))) {
                inputForGraphviz.append(dotfile);
            }

            processOutput(dotprocess);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, new DotProcessError(ex), "Graphviz process error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (dotprocess != null) {
                dotprocess.destroy();
                dotprocess = null;
            }
            setConverged(true);
        }
    }

    @Override
    public void endAlgo() {
        if(null != dotprocess) {
            dotprocess.destroy();
            dotprocess = null;
        }
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        try {
            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(getClass(), "GraphvizLayout.algorithm.desc"),
                    null,
                    "GraphvizLayout.algorithm.name",
                    NbBundle.getMessage(getClass(), "GraphvizLayout.algorithm.name"),
                    "getAlgoName", "setAlgoName"));

            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(getClass(), "GraphvizLayout.dotbinary.desc"),
                    null,
                    "GraphvizLayout.dotbinary.name",
                    NbBundle.getMessage(getClass(), "GraphvizLayout.dotbinary.name"),
                    "getDotBinary", "setDotBinary"));

            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(getClass(), "GraphvizLayout.rankdir.desc"),
                    null,
                    "GraphvizLayout.rankdir.name",
                    NbBundle.getMessage(getClass(), "GraphvizLayout.rankdir.name"),
                    "getRankDir", "setRankDir"));

            properties.add(LayoutProperty.createProperty(
                    this, String.class,
                    NbBundle.getMessage(getClass(), "GraphvizLayout.overlap.desc"),
                    null,
                    "GraphvizLayout.overlap.name",
                    NbBundle.getMessage(getClass(), "GraphvizLayout.overlap.name"),
                    "getOverlap", "setOverlap"));

            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    NbBundle.getMessage(getClass(), "GraphvizLayout.concentrate.desc"),
                    null,
                    "GraphvizLayout.concentrate.name",
                    NbBundle.getMessage(getClass(), "GraphvizLayout.concentrate.name"),
                    "isConcentrate", "setConcentrate"));
        } catch (MissingResourceException e) {
            Exceptions.printStackTrace(e);
        } catch (NoSuchMethodException e) {
            Exceptions.printStackTrace(e);
        }
        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetPropertiesValues() {
    }

    public String getAlgoName() {
        return algoName;
    }

    public void setAlgoName(String algoName) {
        this.algoName = algoName;
    }

    public String getDotBinary() {
        return dotBinary;
    }

    public void setDotBinary(String dotBinary) {
        this.dotBinary = dotBinary;
    }

    public String getRankDir() {
        return rankDir;
    }

    public void setRankDir(String rankDir) {
        this.rankDir = rankDir;
    }

    public boolean isConcentrate() {
        return concentrate;
    }

    public void setConcentrate(Boolean concentrate) {
        this.concentrate = concentrate;
    }

    public String getOverlap() {
        return overlap;
    }

    public void setOverlap(String overlap) {
        this.overlap = overlap;
    }

    private void processOutput(final Process dotprocess) {
        assert dotprocess != null;
        try (InputStream in = dotprocess.getInputStream()) {

            final BufferedReader outputFromGraphviz = new BufferedReader(new InputStreamReader(in));
            StringBuilder entireOutput = new StringBuilder();
            String line;
            while ((line = outputFromGraphviz.readLine()) != null) {
                entireOutput.append(line);
                entireOutput.append("\n");
            }
            
            final String regex = "^\\s*(?<nodeid>\\S+)\\s+\\[[^\\]]*?[, ]?pos=\"(?<pos>[^\"]+?)\".*?\\]";
            final Pattern pat = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            Matcher matcher = pat.matcher(entireOutput.toString());
            while(matcher.find()) {
                final String nodeid = matcher.group("nodeid");
                
                final Node n = graph.getNode(nodeid);
                if(null == n) {
                    Logger.getLogger("").log(Level.WARNING, "Cannot find nodeid \"{0}\" from matcher group \"{1}\"", new Object[] { nodeid, matcher.group() } );
                    continue;
                }
                String pos = matcher.group("pos");
                String[] pair = pos.trim().split("[, ]");
                if(pair.length != 2) {
                    Logger.getLogger("").log(Level.WARNING, "Don''t know what to do with coordinates != 2; {0}", pos);
                    continue;
                }
                BigDecimal x_bd = new BigDecimal(pair[0]);
                BigDecimal y_bd = new BigDecimal(pair[1]);
                n.setX(x_bd.floatValue());
                n.setY(y_bd.floatValue());
            }
             
        } catch (IOException e) {
            Exceptions.printStackTrace(e);            
        }

        try (InputStream err = dotprocess.getErrorStream()) {
            // Dump any errors
            final InputStreamReader glue = new InputStreamReader(err);
            final BufferedReader errorsFromGraphviz = new BufferedReader(glue);
            String line;
            while ((line = errorsFromGraphviz.readLine()) != null) {
                Logger.getLogger("").warning(line);
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);            
        }
    }
}
