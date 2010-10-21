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
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.Icon;
import org.netbeans.swing.dirchooser.spi.CustomDirectoryProvider;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Martin Škurla
 */
@ServiceProvider(service = CustomDirectoryProvider.class)
public class Neo4jCustomDirectoryProvider implements CustomDirectoryProvider {

    private static final Set<String> NEO4J_REQUIRED_FILE_NAMES;
    private static boolean enabled = false;

    static {
        String[] requiredFileNames = {"neostore", "neostore.id",
            "neostore.nodestore.db", "neostore.nodestore.db.id",
            "neostore.propertystore.db", "neostore.propertystore.db.id",
            "neostore.propertystore.db.arrays", "neostore.propertystore.db.arrays.id",
            "neostore.propertystore.db.index", "neostore.propertystore.db.index.id",
            "neostore.propertystore.db.index.keys", "neostore.propertystore.db.index.keys.id",
            "neostore.propertystore.db.strings", "neostore.propertystore.db.strings.id",
            "neostore.relationshipstore.db", "neostore.relationshipstore.db.id",
            "neostore.relationshiptypestore.db", "neostore.relationshiptypestore.db.id",
            "neostore.relationshiptypestore.db.names", "neostore.relationshiptypestore.db.names.id"};

        NEO4J_REQUIRED_FILE_NAMES = new LinkedHashSet<String>(Arrays.asList(requiredFileNames));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public static void setEnabled(boolean enable) {
        Neo4jCustomDirectoryProvider.enabled = enable;
    }

    @Override
    public boolean isValidCustomDirectory(File directory) {
        if (!directory.isDirectory()) {
            return false;
        }

        int existingRequiredFiles = 0;
        for (File file : directory.listFiles()) {
            if (NEO4J_REQUIRED_FILE_NAMES.contains(file.getName())) {
                existingRequiredFiles++;
            }
        }

        return existingRequiredFiles == NEO4J_REQUIRED_FILE_NAMES.size();
    }

    @Override
    public Icon getCustomDirectoryIcon() {
        return ImageUtilities.loadImageIcon("org/gephi/desktop/neo4j/resources/Neo4j-logo.png", false);
    }
}
