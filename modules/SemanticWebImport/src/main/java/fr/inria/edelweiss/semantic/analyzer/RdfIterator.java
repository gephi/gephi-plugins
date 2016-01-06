/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.semantic.analyzer;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.ARP;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.StatementHandler;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 *
 * @author edemairy
 */
public class RdfIterator implements Iterator<Edge> {

    private static final Logger logger = Logger.getLogger(RdfIterator.class.getName());
    private InputStream rdf;
    private boolean parsed;
    ArrayList<Edge> content;
    Iterator<Edge> internalIterator;

    public RdfIterator(InputStream rdf) {
        this.rdf = rdf;
        parsed = false;
        this.content = new ArrayList<Edge>();
    }

    @Override
    public boolean hasNext() {
        initIfNecesary();
        return internalIterator.hasNext();
    }

    @Override
    public Edge next() {
        initIfNecesary();
        return internalIterator.next();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Operation not implemented.");
    }

    private void initIfNecesary() {
        if (parsed) {
            return;
        } else {
            parse();
            internalIterator = content.iterator();
            parsed = true;
        }
    }

    private void parse() {
        final ARP arp = new ARP();

        arp.getHandlers().setErrorHandler(new ErrorHandler() {

            @Override
            public void fatalError(final SAXParseException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }

            @Override
            public void error(final SAXParseException e) {
                logger.log(Level.SEVERE, e.getMessage());
            }

            @Override
            public void warning(final SAXParseException e) {
                logger.log(Level.INFO, e.getMessage());
            }
        });
        arp.getHandlers().setStatementHandler(new ArpParsingHandler());

        // parsing.

        try {
            arp.load(rdf);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage());
        }
    }

    private class ArpParsingHandler implements StatementHandler {

        @Override
        public void statement(AResource subj, AResource pred, AResource obj) {
            content.add(new Edge(subj.getURI(), pred.getURI(), obj.getURI()));
        }

        @Override
        public void statement(AResource subj, AResource pred, ALiteral lit) {
            content.add(new Edge(subj.getURI(), pred.getURI(), lit.toString()));
        }
    }
}
