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
 * @author J
 */
public abstract class LayoutAlgorithmProperty extends PropertyEditorSupport{
    
    private String[] listOfAlgorithms = {"Circle Layout", "Grid Layout", "Linear Layout", "ForceAtlas", "ForceAtlas2", "Fruchterman Reingold"};
    private String selectedAlgorithm = "Linear Layout";
    
    protected LayoutAlgorithmProperty(){
    }

    
    @Override
    public String[] getTags() {
        return listOfAlgorithms;
    }

    @Override
    public Object getValue() {
        return selectedAlgorithm;
    }

    @Override
    public void setValue(Object value) {
        for (String algorithm : listOfAlgorithms) {
            if(algorithm.equals((String) value)) {
                selectedAlgorithm = algorithm;
                break;
            }
        }
    }

    @Override
    public String getAsText() {
        return getValue().toString();
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(text);
    }
}