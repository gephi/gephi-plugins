/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.oracletroubadour.gephi.plugins; 

import java.io.File;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.logging.*;
import java.util.*;


/**
 *
 * @author Stuart Turton
 */
public class JDBCUtils 
{
    /**
     * An Oracle JDBC thin driver SQLDriver implementation for GEPHI.
     * 
     * @see  org.gephi.io.database.drivers.SQLDriver
     */
    private static final String CLASS_NAME =JDBCUtils.class.getName();

    private static final Logger logger = Logger.getLogger(CLASS_NAME);


    public void registerDriver(String driverClass, List<String> driverPaths) throws Exception  
    {
	    logger.warning("registerDriver: Before") ;
	    logger.warning("registerDriver: driverPaths , size="+ driverPaths.size() ) ;
	    for (String path : driverPaths)
	    {
		    logger.warning("registerDriver: driverPath =\""+ path +"\"" ) ;
	    }


	    String[] driverPathsArray = new String[ driverPaths.size() ] ;
	    logger.warning("registerDriver: Array size :"+ driverPathsArray.length ) ;
	    registerDriver(driverClass, driverPaths.toArray(driverPathsArray) );
	    logger.warning("registerDriver: After") ;
    }

    public void registerDriver(String driverClass, String[] driverPaths) throws Exception  
    {

	    try 
	    {
		    Driver driver = getDriver(driverClass, driverPaths ); 
		    DriverManager.registerDriver(driver) ;
	    }
	    catch (Exception e) 
	    {
		    System.err.println(e);
		    throw new Exception ("Failed to register class \""+driverClass+"\" using paths \"" + driverPaths + "\"."
				        ,e 
				        );
	    }
    }

    public Driver getDriver(String driverClass, List<String> driverPaths) throws Exception
    {
	    return getDriver(driverClass, driverPaths.toArray(new String[driverPaths.size()] )  ) ;
    }

    public Driver getDriver(String driverClass, String driverPath) throws Exception
    {
	    return getDriver(driverClass, new String[] { driverPath } ) ;
    }

    public Driver getDriver(String driverClass, String[] driverPaths) throws Exception
    {
	    List<URL> classpath = null ;
	    ClassLoader loader = null ; 
	    Driver driver = null;

	    if( null != driverPaths && driverPaths.length > 0 )   
	    {
		    classpath = new ArrayList<URL>() ;
		    for (String path : driverPaths )
		    {
			    classpath.addAll( getExistingUrls(path) );
		    }
	    }

	    logger.warning("classpath has "+ classpath.size() + " elements" ) ;
	    logger.warning("classpath:  "+ classpath ) ;
	    loader = getDriverClassLoader(classpath);
	    logger.warning("ClassLoader generated" ) ;

	    try 
	    {
		    logger.warning("searching for Driver with custom ClassLoader" ) ;
		    driver = (Driver) Class.forName(driverClass, true, loader).newInstance(); 
		    logger.warning("Driver returned" ) ;
	    }
	    catch (Exception e) 
	    {
		    System.err.println(e);
		    logger.throwing (CLASS_NAME, "getDriver", e ) ; 
		    throw new Exception ("Could not find class \""+driverClass+"\" in paths \"" + driverPaths + "\"."
				         , e 
				        );
	    }

	    return driver ; 
    }


    public ClassLoader getDriverClassLoader (List<URL>  classpath) 
    {
	    ClassLoader loader =  null ;

	    if ( null != classpath && classpath.size() > 0 )
	    {
		    //Extend the existing ClassLoader with the passed in list of URLs 
		    loader = new URLClassLoader(classpath.toArray( new URL[classpath.size()])
					       ,getClass().getClassLoader()
				               );
	    }
	    else
	    {
		    loader = getClass().getClassLoader();
	    }


	    return loader ;
    }



    public List<URL> getExistingUrls (String path)  throws MalformedURLException
    {
	    List<URL> existingUrls =  new ArrayList<URL>() ;

	    String[] pieces = path.split(File.pathSeparator); 
	    for (String piece : pieces )
	    {
		    File file = new File(piece);
		    if (file.exists())
		    {
			    existingUrls.add(file.toURI().toURL()) ;
		    }
	    }

	    return existingUrls ;
    }



}



   
 

