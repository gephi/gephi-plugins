/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
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

package org.gephi.plugins.positionranking;

import org.gephi.appearance.api.Ranking;
import org.gephi.appearance.spi.RankingTransformer;
import org.gephi.appearance.spi.Transformer;
import org.gephi.graph.api.Node;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Alexis Jacomy, Mathieu Bastian
 */
@ServiceProvider(service = Transformer.class, position = 1000)
public class PositionTransformer implements RankingTransformer<Node> {

    public static String X_AXIS = "X";
    public static String Y_AXIS = "Y";
    public static String Z_AXIS = "Z";

    @Override
    public void transform(Node node, Ranking ranking, Number value, float normalisedValue) {
        float coordinate = normalisedValue * (max - min) + min;

        if (this.axe.equals(X_AXIS)) {
            node.setX(coordinate);
        } else if (this.axe.equals(Y_AXIS)) {
            node.setY(coordinate);
        } else if (this.axe.equals(Z_AXIS)) {
            node.setZ(coordinate);
        }
    }

    protected float min = 0f;
    protected float max = 500f;
    protected String axe = X_AXIS;

    public float getMin() {
        return min;
    }

    public float getMax() {
        return max;
    }

    public String getAxe() {
        return axe;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public void setAxe(String axe) {
        this.axe = axe;
    }

    @Override
    public boolean isNode() {
        return true;
    }

    @Override
    public boolean isEdge() {
        return false;
    }
}
