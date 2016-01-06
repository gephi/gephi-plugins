/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */


package fr.inria.edelweiss.sparql.semantictweet;

import fr.inria.edelweiss.sparql.SparqlDriverParameters;
import java.util.Properties;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class SemanticTweetDriverParameters extends SparqlDriverParameters {
    public static final String ROOT = "Root";
    public static final String DEPTH = "Depth";
    public static final String MAX_NODES = "MaxNodes";
    public static final String DEFAULT_ROOT = "http://semantictweet.com/your-twitter-screen-name/all";
    public static final int DEFAULT_DEPTH = 2;
    public static final int DEFAULT_MAX_NODES = 1000;
    String root;
    int depth;
    int maxNodes;

    public SemanticTweetDriverParameters() {
        root = DEFAULT_ROOT;
        depth = DEFAULT_DEPTH;
        maxNodes = DEFAULT_MAX_NODES;
    }

    @Override
    public void readProperties(Properties configuration) {
        setRoot(configuration.getProperty(ROOT, DEFAULT_ROOT));
        setDepth(Integer.decode(configuration.getProperty(DEPTH, Integer.toString(DEFAULT_DEPTH))));
        setMaxNodes(Integer.decode(configuration.getProperty(MAX_NODES, Integer.toString(DEFAULT_MAX_NODES))));
    }

    @Override
    public void writeProperties(Properties p) {
        p.setProperty(ROOT, root);
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getRoot() {
        return root;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return depth;
    }

    public void setMaxNodes(Integer maxNodes) {
        this.maxNodes = maxNodes;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

}
