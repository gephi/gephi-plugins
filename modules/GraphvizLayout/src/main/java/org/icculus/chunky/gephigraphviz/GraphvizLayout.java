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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final Logger LOG = Logger.getLogger(GraphvizLayout.class.getName());

    // Graphviz never sees Gephi's node ids. The node at index i of the array given to
    // toDot() is written as n<i>, a plain identifier that dot writes back unquoted. Gephi ids
    // can be of any type and contain any character, and dot removes the quotes around an id
    // when it doesn't need them, so they can't be matched reliably in its output.
    private static final String DOT_NODE_PREFIX = "n";
    private static final Pattern DOT_NODE_POSITION = Pattern.compile(
            "^\\s*" + DOT_NODE_PREFIX + "(?<index>\\d+)\\s+\\[[^\\]]*?(?<=[\\[,\\s])pos=\"(?<pos>[^\"]*)\"",
            Pattern.MULTILINE);

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

    /**
     * Writes the visible graph in the dot language, naming each node after its index in
     * {@code nodes}. The caller holds the graph's read lock.
     */
    String toDot(final Node[] nodes) {
        final StringBuilder dotfile = new StringBuilder();

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

        final Map<Node, Integer> indices = new HashMap<>();
        for (int i = 0; i < nodes.length; i++) {
            final Node n = nodes[i];
            indices.put(n, i);
            dotfile.append(DOT_NODE_PREFIX).append(i);
            dotfile.append(" [");
            dotfile.append("pos=\"").append(n.x()).append(',').append(n.y()).append('"');
            dotfile.append("];\n");
        }
        for (final Edge e : this.graph.getEdges()) {
            dotfile.append(DOT_NODE_PREFIX).append(indices.get(e.getSource()));
            dotfile.append(edgearrow);
            dotfile.append(DOT_NODE_PREFIX).append(indices.get(e.getTarget()));
            dotfile.append(" [weight=");
            dotfile.append(e.getWeight());
            dotfile.append("];\n");
        }
        dotfile.append("}\n");
        return dotfile.toString();
    }

    @Override
    public void goAlgo() {
        final Node[] nodes;
        final String dotfile;
        graph.readLock();
        try {
            nodes = graph.getNodes().toArray();
            dotfile = toDot(nodes);
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

            processOutput(dotprocess, nodes);
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

    private void processOutput(final Process dotprocess, final Node[] nodes) {
        assert dotprocess != null;
        final StringBuilder entireOutput = new StringBuilder();
        try (InputStream in = dotprocess.getInputStream()) {

            final BufferedReader outputFromGraphviz = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = outputFromGraphviz.readLine()) != null) {
                entireOutput.append(line);
                entireOutput.append("\n");
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);            
        }

        final int positioned = applyPositions(entireOutput, nodes);
        if (positioned < nodes.length) {
            LOG.log(Level.WARNING, "Graphviz gave a position to {0} of {1} nodes",
                    new Object[] { positioned, nodes.length });
            LOG.log(Level.FINE, "Graphviz output:\n{0}", entireOutput);
        }

        try (InputStream err = dotprocess.getErrorStream()) {
            // Dump any errors
            final InputStreamReader glue = new InputStreamReader(err);
            final BufferedReader errorsFromGraphviz = new BufferedReader(glue);
            String line;
            while ((line = errorsFromGraphviz.readLine()) != null) {
                LOG.warning(line);
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);            
        }
    }

    /**
     * Moves each node to the position Graphviz gave it in {@code dotOutput}, where node
     * {@code n<i>} is {@code nodes[i]} as written by {@link #toDot(Node[])}.
     *
     * @return the number of nodes moved
     */
    static int applyPositions(final CharSequence dotOutput, final Node[] nodes) {
        int positioned = 0;
        final Matcher matcher = DOT_NODE_POSITION.matcher(dotOutput);
        while (matcher.find()) {
            final String pos = matcher.group("pos");
            final String[] pair = pos.trim().split("[, ]");
            if (pair.length != 2) {
                LOG.log(Level.WARNING, "Don''t know what to do with coordinates != 2; {0}", pos);
                continue;
            }
            final int index;
            final float x;
            final float y;
            try {
                index = Integer.parseInt(matcher.group("index"));
                x = new BigDecimal(pair[0]).floatValue();
                y = new BigDecimal(pair[1]).floatValue();
            } catch (NumberFormatException e) {
                LOG.log(Level.WARNING, "Can''t read node position from \"{0}\"", matcher.group());
                continue;
            }
            if (index >= nodes.length) {
                LOG.log(Level.WARNING, "Graphviz returned unknown node \"{0}\"", matcher.group());
                continue;
            }
            nodes[index].setX(x);
            nodes[index].setY(y);
            positioned++;
        }
        return positioned;
    }
}
