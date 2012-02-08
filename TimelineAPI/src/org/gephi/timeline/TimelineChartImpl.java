/*
Copyright 2008-2011 Gephi
Authors : Mathieu Bastian
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.timeline;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.timeline.api.TimelineChart;

/**
 *
 * @author Mathieu Bastian
 */
public class TimelineChartImpl implements TimelineChart {

    private final AttributeColumn column;
    private final Number[] x;
    private final Number[] y;
    private final Number minY;
    private final Number maxY;

    public TimelineChartImpl(AttributeColumn column, Number[] x, Number y[]) {
        this.column = column;
        this.x = x;
        this.y = y;
        this.minY = calculateMin(y);
        this.maxY = calculateMax(y);
    }

    @Override
    public Number[] getX() {
        return x;
    }

    @Override
    public Number[] getY() {
        return y;
    }

    @Override
    public Number getY(Number x) {
        Number[] xs = this.x;
        double pos = x.doubleValue();
        int minIndex = -1;
        double minX = Double.POSITIVE_INFINITY;
        for (int i = 0; i < xs.length; i++) {
            double diff = Math.abs(xs[i].doubleValue() - x.doubleValue());
            if (diff < minX) {
                minX = diff;
                minIndex = i;
            }
        }
        return y[minIndex];
    }

    @Override
    public Number getMinY() {
        return minY;
    }

    @Override
    public Number getMaxY() {
        return maxY;
    }

    @Override
    public AttributeColumn getColumn() {
        return column;
    }

    private Number calculateMin(Number[] yValues) {
        double min = yValues[0].doubleValue();
        for (Number d : yValues) {
            min = Math.min(min, d.doubleValue());
        }
        Number t = yValues[0];
        if (t instanceof Double) {
            return new Double(min);
        } else if (t instanceof Float) {
            return new Float(min);
        } else if (t instanceof Short) {
            return new Short((short) min);
        } else if (t instanceof Long) {
            return new Long((long) min);
        } else if (t instanceof BigInteger) {
            return new BigDecimal(min);
        }
        return min;
    }

    private Number calculateMax(Number[] yValues) {
        double max = yValues[0].doubleValue();
        for (Number d : yValues) {
            max = Math.max(max, d.doubleValue());
        }
        Number t = yValues[0];
        if (t instanceof Double) {
            return new Double(max);
        } else if (t instanceof Float) {
            return new Float(max);
        } else if (t instanceof Short) {
            return new Short((short) max);
        } else if (t instanceof Long) {
            return new Long((long) max);
        } else if (t instanceof BigInteger) {
            return new BigDecimal(max);
        }
        return max;
    }
}
