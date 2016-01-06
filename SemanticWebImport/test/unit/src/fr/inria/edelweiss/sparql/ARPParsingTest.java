/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql;

import fr.com.hp.hpl.jena.rdf.arp.ALiteral;
import fr.com.hp.hpl.jena.rdf.arp.ARP;
import fr.com.hp.hpl.jena.rdf.arp.AResource;
import fr.com.hp.hpl.jena.rdf.arp.StatementHandler;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.junit.Test;
import org.openide.util.Lookup;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author edemairy
 */
public class ARPParsingTest {
    private static final Logger LOGGER = Logger.getLogger(ARPParsingTest.class.getName());
    @Test
    public void simpleParsing() {
        final ARP arp = new ARP();
        arp.setLaxErrorMode();

        arp.setErrorHandler(new ErrorHandler() {

            @Override
            public void fatalError(SAXParseException e) {
            }

            @Override
            public void error(SAXParseException e) {
            }

            @Override
            public void warning(SAXParseException e) {
            }
        });
        SimpleParsingStatementHandler statementHandler = new SimpleParsingStatementHandler();
        arp.setStatementHandler(statementHandler);

        try {
            arp.load(new StringReader(
                    "<rdf:RDF  xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>\n"
                    + "<rdf:Description>"
                    + "<rdf:value rdf:parseType='Literal'><b>hello</b></rdf:value>\n"
                    + "<rdf:value rdf:parseType='Literal'>literal1</rdf:value>\n"
                    + "<rdf:value rdf:parseType='Literal'>literal2</rdf:value>\n"
                    + "</rdf:Description>"
                    + "</rdf:RDF>"));
        } catch (IOException ioe) {
            // something unexpected went wrong
        } catch (SAXParseException s) {
            // This error will have been reported
        } catch (SAXException ss) {
            // This error will not have been reported.
        }
        assert (statementHandler.literals.size() == 3);
    }

    @Test
    public void DBPediaParsing() {
        ARP arp = new ARP();

        // initialisation - uses ARPConfig interface only.

        arp.setLaxErrorMode();

        arp.setErrorHandler(new ErrorHandler() {

            @Override
            public void fatalError(SAXParseException e) {
            }

            @Override
            public void error(SAXParseException e) {
            }

            @Override
            public void warning(SAXParseException e) {
            }
        });
        SimpleParsingStatementHandler statementHandler = new SimpleParsingStatementHandler();
        arp.setStatementHandler(statementHandler);

        // parsing.

        try {
            // Loading fixed input ...
            arp.load(this.getClass().getResourceAsStream("/test_files/DBPedia_Movies_short.rdf"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Assert.assertEquals(1189, statementHandler.literals.size());
        Assert.assertEquals(812, statementHandler.resources.size());
    }

    public class SimpleParsingStatementHandler implements StatementHandler {

        private ArrayList<ALiteral> literals = new ArrayList<ALiteral>();
        private ArrayList<AResource> resources = new ArrayList<AResource>();
        private Graph graph;
        private final GraphController graphController;
        private final GraphModel graphModel;
        private final Workspace workspace;
        private final ProjectController projectController;

        public SimpleParsingStatementHandler() {
            projectController = Lookup.getDefault().lookup(ProjectController.class);
            graphController = Lookup.getDefault().lookup(GraphController.class);
            graphModel = graphController.getModel();
            workspace = projectController.getCurrentWorkspace();
        }

        @Override
        public void statement(AResource a, AResource b, ALiteral l) {
            literals.add(l);
        }

        @Override
        public void statement(AResource a, AResource b, AResource l) {
            resources.add(l);
        }
    }
}
