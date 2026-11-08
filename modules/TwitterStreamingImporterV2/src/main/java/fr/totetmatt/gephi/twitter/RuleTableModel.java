/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fr.totetmatt.gephi.twitter;

import com.twitter.clientlib.model.Rule;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author totetmatt
 */
public class RuleTableModel extends AbstractTableModel {

    private final String[] columnNames = {"Id", "Tag", "Value"};
    private List<Rule> rules = new ArrayList<>();

    public RuleTableModel() {

    }

    public RuleTableModel(List<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public int getRowCount() {

        return rules.size();
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Rule selected = rules.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return selected.getId();
            case 1:
                return selected.getTag();
            case 2:
                return selected.getValue();
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
}
