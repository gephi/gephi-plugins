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
package org.gephi.desktop.neo4j.ui.util;

import java.util.LinkedList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeTable;
import org.openide.util.Lookup;

/**
 *
 * @author Martin Škurla
 */
public class GephiUtils {

    private GephiUtils() {
    }

    public static String[] edgeColumnNames() {
        return columnsToColumnNames(edgeColumns());
    }

    public static String[] nodeColumnNames() {
        return columnsToColumnNames(nodeColumns());
    }

    private static String[] columnsToColumnNames(AttributeColumn[] columns) {
        List<String> columnNames = new LinkedList<String>();

        for (AttributeColumn attributeColumn : columns) {
            columnNames.add(attributeColumn.getTitle());
        }

        return columnNames.toArray(new String[0]);
    }

    private static AttributeColumn[] edgeColumns() {
        AttributeTable edgeTable =
                Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable();

        return edgeTable.getColumns();
    }

    private static AttributeColumn[] nodeColumns() {
        AttributeTable nodeTable =
                Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();

        return nodeTable.getColumns();
    }
}
