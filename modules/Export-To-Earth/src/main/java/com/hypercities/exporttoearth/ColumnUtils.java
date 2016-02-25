/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hypercities.exporttoearth;

import org.gephi.graph.api.Column;
import org.gephi.graph.api.Table;

/**
 *
 * @author daveshepard
 */
public class ColumnUtils {

    static Column[] getColumns(Table table) {
        Column[] columns = new Column[table.countColumns()];

        for (int i = 0; i < columns.length; i++) {
            columns[i] = table.getColumn(i);
        }

        return columns;
    }
    
}
