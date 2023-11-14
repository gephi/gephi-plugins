/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql;

/**
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public interface SparqlRequester {
    /*
     *  Execute a SPARQL request and return the rdf+xml output.
     *  @param request Sparql request the driver has to execute.
     *  @return A string containing the RDF/XMF result.
     */
    public abstract String sparqlQuery(String request);

    /**
     *
     * @param request
     * @return Each line represent the ids of the binding returned by the select call.
     */
    public abstract String[][] selectOnGraph(String request);

}
