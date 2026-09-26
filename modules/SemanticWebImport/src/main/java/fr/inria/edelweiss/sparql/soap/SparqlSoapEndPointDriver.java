////<editor-fold defaultstate="collapsed" desc="comment">
///*
// * Copyright (c) 2011, INRIA
// * All rights reserved.
// */
////</editor-fold>
//package fr.inria.edelweiss.sparql.soap;
//
//import fr.inria.edelweiss.sparql.SparqlDriver;
//import fr.inria.wimmics.sparql.soap.client.SparqlResult;
//import fr.inria.wimmics.sparql.soap.client.SparqlSoapClient;
//import java.util.Observable;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import org.openide.util.lookup.ServiceProvider;
//
///**
// *
// * @author edemairy
// */
//@ServiceProvider(service = SparqlDriver.class)
//public class SparqlSoapEndPointDriver extends SparqlDriver<SparqlSoapEndPointDriverParameters> {
//	
//	private static final Logger logger = Logger.getLogger(SparqlSoapEndPointDriver.class.getName());
//	
//	@Override
//	public String[][] selectOnGraph(String request) {
//		throw new UnsupportedOperationException("Not supported yet.");
//	}
//	
//	@Override
//	public void update(Observable o, Object arg) {
//	}
//	
//	public SparqlSoapEndPointDriver() {
//		setParameters(new SparqlSoapEndPointDriverParameters());
//	}
//	
//	@Override
//	public void init() {
//	}
//	
//	@Override
//	public final String sparqlQuery(final String request) {
//		logger.log(Level.INFO, "URL called: {0} request:{1}", new Object[]{getParameters().getUrl(), request});
//		SparqlSoapClient client = new SparqlSoapClient();
//		SparqlResult result = client.sparqlQuery(getParameters().getUrl(), request);
//		return result.toString();
//	}
//	
//	@Override
//	public String getDisplayName() {
//		return "Remote - SOAP endpoint";
//	}
//}
