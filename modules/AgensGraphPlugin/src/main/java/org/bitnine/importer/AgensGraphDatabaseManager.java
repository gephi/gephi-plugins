/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import org.gephi.io.importer.plugin.database.EdgeListDatabaseImpl;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author dehowefeng
 */
public class AgensGraphDatabaseManager {

    private FileObject databaseConfigurations;
    private List<Database> edgeListDatabases = new ArrayList<>();

    public AgensGraphDatabaseManager() {
        load();
    }

    public List<Database> getEdgeListDatabases() {
        return edgeListDatabases;
    }

    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (Database db : edgeListDatabases) {
            names.add(db.getName());
        }
        return names;
    }

    public void addDatabase(EdgeListDatabaseImpl db) {
        edgeListDatabases.add(db);
    }

    public boolean removeDatabase(EdgeListDatabaseImpl db) {
        return edgeListDatabases.remove(db);
    }

    public void persist() {
        doPersist();
    }

    private void load() {
        if (databaseConfigurations == null) {
            databaseConfigurations
                    = FileUtil.getConfigFile("EdgeListDatabase");
        }

        if (databaseConfigurations != null) {
            InputStream is = null;

            try {
                is = databaseConfigurations.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);
                List<Database> unserialized = (List<Database>) ois.readObject();
                if (unserialized != null) {
                    edgeListDatabases = unserialized;
                }
            } catch (java.io.InvalidClassException e) {
            } catch (EOFException eofe) {
                // Empty configuration: do nothing
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
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

            databaseConfigurations = FileUtil.getConfigRoot().createData("EdgeListDatabase");
            lock = databaseConfigurations.lock();

            ois = new ObjectOutputStream(databaseConfigurations.getOutputStream(lock));
            ois.writeObject(edgeListDatabases);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
