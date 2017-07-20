package org.bitnine.importer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gephi.io.database.drivers.SQLDriver;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author dehowefeng
 */
@ServiceProvider(service = SQLDriver.class)
public class AgensGraphDriver implements SQLDriver {

    public AgensGraphDriver() {
        try {
            Class.forName("net.bitnine.agensgraph.Driver");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AgensGraphDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public Connection getConnection(String connectionUrl, String username, String passwd) throws SQLException {
        return DriverManager.getConnection(connectionUrl, username, passwd);
    }

    @Override
    public String getPrefix() {
        return "agensgraph";
    }

    @Override
    public String toString() {
        return "AgensGraph";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof AgensGraphDriver) {
            return ((AgensGraphDriver) obj).getPrefix().equals(getPrefix());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getPrefix().hashCode();
    }
}
