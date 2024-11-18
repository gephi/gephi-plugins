/*
 * Copyright (c) 2011, INRIA
 * All rights reserved.
 */
package fr.inria.edelweiss.sparql.restdriver;

import fr.inria.edelweiss.sparql.SparqlDriver;
import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class responsible for the requests done to REST SPARQL enpoints.
 *
 * @author edemairy
 */
@ServiceProvider(service = SparqlDriver.class)
public class SparqlRestEndPointDriver extends SparqlDriver<SparqlRestEndPointDriverParameters> {

	private static final Logger logger = Logger.getLogger(SparqlRestEndPointDriver.class.getName());
	private URL queryURL;

	/**
	 * Constructor. Create default values for the parameters.
	 */
	public SparqlRestEndPointDriver() {
		setParameters(new SparqlRestEndPointDriverParameters());
	}

	@Override
	public void init() {
	}

	@Override
	public String sparqlQuery(String sparqlQuery) {
		StringBuilder sparqlQueryResult = new StringBuilder();
		try {

			String restQuery = "";
			try {
				restQuery = getParameters().getQueryTagName() + "=" + URLEncoder.encode(sparqlQuery, "UTF-8") + getParameters().makeRequest();
			} catch (UnsupportedEncodingException ex) {
				Exceptions.printStackTrace(ex);
			}
			byte[] restQueryAsBytes = restQuery.getBytes();
			try {
				queryURL = new URL(getParameters().getEndPointUrl());
				HttpURLConnection urlConn = (HttpURLConnection) queryURL.openConnection();
				urlConn.setRequestMethod("POST");
				urlConn.setDoOutput(true);
				urlConn.setDoInput(true);
				urlConn.setUseCaches(false);
				urlConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				urlConn.setRequestProperty("Accept", "application/rdf+xml, txt/xml, rdf/xml");

				Map<String, String> properties = getParameters().getRequestProperties();
				for (String propertyName : properties.keySet()) {
					urlConn.setRequestProperty(propertyName, properties.get(propertyName));
				}

				OutputStream oStream = urlConn.getOutputStream();
				try {

					logger.log(Level.INFO, "{0} executing request: {1}", new Object[]{getClass().getName(), restQuery});
					oStream.write(restQueryAsBytes);
					oStream.flush();

					BufferedReader in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
					String currentLine;
					int nbLines = 0;
					logger.log(Level.INFO, "Result request:");
					while ((currentLine = in.readLine()) != null) {
						if (nbLines < 10) {
							logger.log(Level.INFO, currentLine);
						} else {
							logger.log(Level.FINE, currentLine);
						}
						sparqlQueryResult.append(currentLine + "\n");
						++nbLines;
					}
					logger.log(Level.INFO, "Result contains {0} lines.", nbLines);
					oStream.close();
					in.close();
				} catch (Exception ex) {
					oStream.close();
				}
			} catch (MalformedURLException ex) {
				JOptionPane.showMessageDialog(null, "The URL entered is incorrect");
			}

		} catch (IOException ex) {
			Exceptions.printStackTrace(ex);
		}
		return sparqlQueryResult.toString();
	}

	@Override
	public String getDisplayName() {
		return "Remote - REST endpoint";
	}

	@Override
	public String[][] selectOnGraph(String request) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
