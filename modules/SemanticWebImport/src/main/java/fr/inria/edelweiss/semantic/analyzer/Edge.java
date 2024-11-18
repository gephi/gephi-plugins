/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.analyzer;

/**
 *
 * @author edemairy
 */
public class Edge {
    private String source;
    private String id;
    private String destination;

    Edge(String urI, String urI0, String urI1) {
        source = urI;
        id = urI0;
        destination = urI1;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String toString() {
        return source+" -- "+id+" --> "+destination;
    }
}
