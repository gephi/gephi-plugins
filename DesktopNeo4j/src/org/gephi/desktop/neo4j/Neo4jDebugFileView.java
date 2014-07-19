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
import javax.swing.Icon;
import javax.swing.filechooser.FileView;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Martin Škurla
 */
public class Neo4jDebugFileView extends FileView {

    private final Neo4jDebugFileFilter neo4jDebugFileFilter;

    public Neo4jDebugFileView() {
        neo4jDebugFileFilter = new Neo4jDebugFileFilter();
    }

    @Override
    public Icon getIcon(File file) {
        return (file.isFile() && neo4jDebugFileFilter.accept(file))
                ? ImageUtilities.loadImageIcon("org/gephi/desktop/neo4j/resources/Neo4j-logo.png", false)
                : null;
    }
}
