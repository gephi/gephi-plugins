/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.desktop.neo4j;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.gephi.neo4j.plugin.api.ClassNotFulfillRequirementsException;
import org.gephi.neo4j.plugin.api.FileSystemClassLoader;
import org.gephi.neo4j.plugin.api.Neo4jDelegateNodeDebugger;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Škurla
 */
public class Neo4jDebugFileFilter extends FileFilter {

    private static final String ACCEPTED_FILE_SUFFIX = ".class";
    private static final boolean REQUIRED_NONPARAM_CONSTRUCTOR = true;
    private static final Class<?>[] REQUIRED_IMPLEMENTED_INTERFACES = {Neo4jDelegateNodeDebugger.class};

    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }

        if (!file.getName().endsWith(ACCEPTED_FILE_SUFFIX)) {
            return false;
        }

        FileSystemClassLoader classLoader =
                Lookup.getDefault().lookup(FileSystemClassLoader.class);

        try {
            classLoader.loadClass(file,
                    REQUIRED_NONPARAM_CONSTRUCTOR,
                    REQUIRED_IMPLEMENTED_INTERFACES);
            return true;
        } catch (ClassNotFoundException cnfe) {
            return false;
        } catch (NoClassDefFoundError ncdfe) {
            return false;
        } catch (ClassNotFulfillRequirementsException cnfre) {
            return false;
        } catch (IllegalArgumentException iae) {
            return false;
        }
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(Neo4jDebugFileFilter.class, "CTL_Neo4j_FileFilterDescription");
    }
}
