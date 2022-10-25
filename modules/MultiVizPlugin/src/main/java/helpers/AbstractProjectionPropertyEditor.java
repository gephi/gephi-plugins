/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package helpers;

import java.beans.PropertyEditorSupport;

/**
 *
 * @author Alexis Jacomy
 */
abstract class AbstractProjectionPropertyEditor extends PropertyEditorSupport {

    protected AbstractProjectionPropertyEditor() {
    }
    
    private String selectedColumn;
    private String[] tempoColumns = {"Node Label","Edge Type"};

    @Override
    public String[] getTags() {
        if (multiviz.MultiLayerVisualization.selectableColumns.isEmpty()){
            return tempoColumns;
        } else {
            return multiviz.MultiLayerVisualization.selectableColumns.toArray(new String[multiviz.MultiLayerVisualization.selectableColumns.size()]);
        }
    }

    @Override
    public Object getValue() {
        return selectedColumn;
    }

    @Override
    public void setValue(Object value) {
        if(multiviz.MultiLayerVisualization.selectableColumns.isEmpty()){
            for (String dummyColumn : tempoColumns) {
                if (dummyColumn.equals((String)value)) {
                    selectedColumn = dummyColumn;
                    break;
                }
            }
        } else {
            for(int i=0;i<multiviz.MultiLayerVisualization.selectableColumns.toArray().length;i++){
                if(multiviz.MultiLayerVisualization.selectableColumns.get(i).equals((String)value)){
                    selectedColumn = multiviz.MultiLayerVisualization.selectableColumns.get(i);
                    break;
                }
            }
        }
    }

    @Override
    public String getAsText() {
        return (String)getValue();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(text);
    }

    public boolean isNumberColumn(String column) {
        return false;
    }

    public boolean isStringColumn(String column) {
        return true;
    }
}
