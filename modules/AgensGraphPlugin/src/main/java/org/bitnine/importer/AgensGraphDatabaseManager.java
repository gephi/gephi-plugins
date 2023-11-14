package org.bitnine.importer;

/**
 *
 * @author dehowefeng
 */
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.gephi.io.importer.api.Database;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author dehowefeng
 */
public class AgensGraphDatabaseManager {

    private FileObject databaseConfigurations;
    private List<Database> agensGraphDatabases = new ArrayList<>();

    public AgensGraphDatabaseManager() {
        load();
    }

    public List<Database> getAgensGraphDatabases() {
        return agensGraphDatabases;
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (Database db : agensGraphDatabases) {
            names.add(db.getName());
        }
        return names;
    }

    public void addDatabase(AgensGraphDatabaseImpl db) {
        agensGraphDatabases.add(db);
    }

    public boolean removeDatabase(AgensGraphDatabaseImpl db) {
        return agensGraphDatabases.remove(db);
    }

    public void persist() {
        doPersist();
    }

    private void load() {
        if (databaseConfigurations == null) {
            databaseConfigurations
                    = FileUtil.getConfigFile("AgensGraphDatabase");
        }

        if (databaseConfigurations != null) {
            InputStream is = null;

            try {
                is = databaseConfigurations.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                List<Database> unserialized = (List<Database>) ois.readObject();
                if (unserialized != null) {
                    agensGraphDatabases = unserialized;
                }
            } catch (java.io.InvalidClassException e) {
            } catch (EOFException eofe) {
                // Empty configuration: do nothing
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            } catch (ClassNotFoundException e) {
                Exceptions.printStackTrace(e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    private void doPersist() {
        FileLock lock = null;
        ObjectOutputStream ois = null;

        try {
            if (databaseConfigurations != null) {
                databaseConfigurations.delete();
            }

            databaseConfigurations = FileUtil.getConfigRoot().createData("AgensGraphDatabase");
            lock = databaseConfigurations.lock();

            ois = new ObjectOutputStream(databaseConfigurations.getOutputStream(lock));
            ois.writeObject(agensGraphDatabases);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                }
            }
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }
}
