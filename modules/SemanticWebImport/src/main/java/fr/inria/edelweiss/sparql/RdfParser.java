/**
 * Copyright (c) 2011, INRIA All rights reserved.
 */
package fr.inria.edelweiss.sparql;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.ARP;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.StatementHandler;
import fr.inria.edelweiss.semantic.analyzer.GephiExtension;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.openide.util.Exceptions;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Fill a Gephi graph from the content of an rdf stream.
 *
 * @author Erwan Demairy <Erwan.Demairy@inria.fr>
 */
public class RdfParser {

	private static final Logger logger = Logger.getLogger(RdfParser.class.getName());
	private InputStream fileToParse;
	private GraphModel modelToFill;
	private int tripleNumber;
	int depth = 0;

	public RdfParser(final InputStream fileToParse, final GraphModel modelToFill, int depth) {
		this.fileToParse = fileToParse;
		this.modelToFill = modelToFill;
		this.depth = depth;
	}

	/**
	 * @return the resulting graph.
	 */
	public final Graph parse() {
		final ARP arp = new ARP();

		// initialisation - uses ARPConfig interface only.
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
		final ARPParsingHandler statementHandler = new ARPParsingHandler(getModelToFill(), this.getDepth());
		arp.getHandlers().setStatementHandler(statementHandler);

		// parsing.
		GephiUtils.addAttributeToNodes(GephiUtils.SPARQLID, String.class);
		try {
			arp.load(getFileToParse());
		} catch (SAXException ex) {
			logger.log(Level.SEVERE, ex.getMessage());
		} catch (IOException ex) {
			logger.log(Level.SEVERE, ex.getMessage());
		}
		tripleNumber = statementHandler.getTripleNumber();
		return modelToFill.getGraph();
	}

	public InputStream getFileToParse() {
		return fileToParse;
	}

	public void setFileToParse(final InputStream fileToParse) {
		this.fileToParse = fileToParse;
	}

	public GraphModel getModelToFill() {
		return modelToFill;
	}

	public void setModelToFill(final GraphModel modelToFill) {
		this.modelToFill = modelToFill;
	}

	public int getDepth() {
		return this.depth;
	}

	public void setDepth(final int depth) {
		this.depth = depth;
	}

	/**
	 * @return the tripleNumber
	 */
	public int getTripleNumber() {
		return tripleNumber;
	}

	/**
	 * Class responsible for converting the rdf file in a graph.
	 */
	public static class ARPParsingHandler implements StatementHandler {

		private GraphModel modelToFill;
		final transient private GephiUtils gephiUtils;
		private int tripleNumber;
		int count;

		public ARPParsingHandler(final GraphModel modelToFill, final int count) {
			setModelToFill(modelToFill);
			gephiUtils = new GephiUtils(modelToFill);
			tripleNumber = 0;
			this.count = count;
		}

		@Override
		public void statement(final AResource nodeSource, final AResource edge, final ALiteral nodeTarget) {
			addEdge(nodeSource.toString(), edge.toString(), nodeTarget.toString());
			tripleNumber++;
		}

		@Override
		public void statement(final AResource nodeSource, final AResource edge, final AResource nodeTarget) {
			addEdge(nodeSource.toString(), edge.toString(), nodeTarget.toString());
			tripleNumber++;
		}

		private void addEdge(final String sourceLabel, final String edgeLabel, final String targetLabel) {

			String sourceDecoded = GephiUtils.decodeString(sourceLabel);
			String edgeDecoded = GephiUtils.decodeString(edgeLabel);
			String targetDecoded = GephiUtils.decodeString(targetLabel);

			if (GephiExtension.isGephiExtension(edgeDecoded)) {
				GephiExtension.processGephiExtension(sourceDecoded, edgeDecoded, targetDecoded, gephiUtils);
			} else {
				gephiUtils.addNode(sourceDecoded);
				gephiUtils.addNode(targetDecoded);
				gephiUtils.addEdge(sourceDecoded, edgeDecoded, targetDecoded);
				if (count == 0) {
					return;
				} else {
					InputStream followRDF;
					followRDF = getRDF(targetDecoded.toString());
					RdfParser temp = new RdfParser(followRDF, modelToFill, --count);
					temp.parse();
					return;
				}
			}
		}

		public GraphModel getModelToFill() {
			return modelToFill;
		}

		final public void setModelToFill(final GraphModel modelToFill) {
			this.modelToFill = modelToFill;
		}

		public int getTripleNumber() {
			return tripleNumber;
		}

		public static String sendGetRequest(String endpoint, String requestParameters) {
			String result = null;
			if (endpoint.startsWith("http://")) {
				// Send a GET request to the servlet
				try {
					// Construct data
					StringBuffer data = new StringBuffer();
					// Send data
					String urlStr = endpoint;
					if (requestParameters != null && requestParameters.length() > 0) {
						urlStr += "?" + requestParameters;
					}
					final URL url = new URL(urlStr);
					final URLConnection conn = url.openConnection();
					conn.addRequestProperty("Accept", "application/rdf+xml");
					// Get the response
					final InputStream inputStream = conn.getInputStream();
					final BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
					StringBuffer sb = new StringBuffer();
					String line;
					while ((line = rd.readLine()) != null) {
						sb.append(line);
					}
					rd.close();
					result = sb.toString();
				} catch (Exception e) {
					Exceptions.printStackTrace(e);
				}
			}
			return result;
		}

		public static InputStream getRDF(final String strURI) {
			InputStream result = null;
			try {
				URL url = new URL(strURI);
				URLConnection conn = url.openConnection();
				conn.addRequestProperty("Accept", "application/rdf+xml");
				InputStream rdf = new BufferedInputStream(conn.getInputStream());
				result = rdf;
			} catch (Exception e) {
				Exceptions.printStackTrace(e);
			}
			return result;
		}
	}
}
