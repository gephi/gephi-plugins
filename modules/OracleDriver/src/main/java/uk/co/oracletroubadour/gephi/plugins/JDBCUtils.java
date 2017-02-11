package uk.co.oracletroubadour.gephi.plugins;

import java.io.File;

import java.sql.Driver;
import java.sql.DriverManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.logging.*;
import java.util.*;

/**
 *
 * @author Stuart Turton
 */
public class JDBCUtils {

    /**
     * An Oracle JDBC thin driver SQLDriver implementation for GEPHI.
     *
     * @see org.gephi.io.database.drivers.SQLDriver
     */
    private static final String CLASS_NAME = JDBCUtils.class.getName();

    private static final Logger logger = Logger.getLogger(CLASS_NAME);

    public void registerDriver(String driverClass, List<String> driverPaths) throws Exception {
        logger.info("registerDriver: Before");
        logger.log(Level.INFO, "registerDriver: driverPaths , size={0}", driverPaths.size());
        for (String path : driverPaths) {
            logger.log(Level.INFO, "registerDriver: driverPath =\"{0}\"", path);
        }

        String[] driverPathsArray = new String[driverPaths.size()];
        logger.log(Level.INFO, "registerDriver: Array size :{0}", driverPathsArray.length);
        registerDriver(driverClass, driverPaths.toArray(driverPathsArray));
        logger.info("registerDriver: After");
    }

    public void registerDriver(String driverClass, String[] driverPaths) throws Exception {

        try {
            Driver driver = getDriver(driverClass, driverPaths);
            DriverManager.registerDriver(driver);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new Exception("Failed to register class \"" + driverClass + "\" using paths \"" + Arrays.toString(driverPaths) + "\".", e);
        }
    }

    public Driver getDriver(String driverClass, List<String> driverPaths) throws Exception {
        return getDriver(driverClass, driverPaths.toArray(new String[driverPaths.size()]));
    }

    public Driver getDriver(String driverClass, String driverPath) throws Exception {
        return getDriver(driverClass, new String[]{driverPath});
    }

    public Driver getDriver(String driverClass, String[] driverPaths) throws Exception {
        List<URL> classpath = null;
        Driver driver = null;

        if (null != driverPaths && driverPaths.length > 0) {
            classpath = new ArrayList<URL>();
            for (String path : driverPaths) {
                classpath.addAll(getExistingUrls(path));
            }
        }

        logger.log(Level.INFO, "classpath has {0} elements", classpath.size());
        logger.log(Level.INFO, "classpath:  {0}", classpath);
        ClassLoader loader = getDriverClassLoader(classpath);
        logger.info("ClassLoader generated");

        try {
            logger.info("searching for Driver with custom ClassLoader");
            driver = (Driver) Class.forName(driverClass, true, loader).newInstance();
            logger.info("Driver returned");
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            logger.throwing(CLASS_NAME, "getDriver", e);
            throw new Exception("Could not find class \"" + driverClass + "\" in paths \"" + Arrays.toString(driverPaths) + "\".", e);
        }

        return driver;
    }

    public ClassLoader getDriverClassLoader(List<URL> classpath) {
        ClassLoader loader;

        if (classpath != null && !classpath.isEmpty()) {
            //Extend the existing ClassLoader with the passed in list of URLs 
            loader = new URLClassLoader(classpath.toArray(new URL[classpath.size()]), getClass().getClassLoader());
        } else {
            loader = getClass().getClassLoader();
        }

        return loader;
    }

    public List<URL> getExistingUrls(String path) throws MalformedURLException {
        List<URL> existingUrls = new ArrayList<URL>();

        String[] pieces = path.split(File.pathSeparator);
        for (String piece : pieces) {
            File file = new File(piece);
            if (file.exists()) {
                existingUrls.add(file.toURI().toURL());
            }
        }

        return existingUrls;
    }

}
