/*
 Copyright 2008-2011 Gephi
 Authors : Mathieu Jacomy <mathieu.jacomy@gmail.com>
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

 This version was edited by Wouter Spekkink (wouterspekkink@gmail.com)
 in order to keep only the parts of the code that are useful to run
 the event graph layout plugin. No new code was added.
  

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.wouterspekkink.plugins.layout.orderedgraph;

import org.gephi.graph.api.Node;

/**
 *
 * @author Mathieu Jacomy
 */
public class ForceFactory {

    public static ForceFactory builder = new ForceFactory();

    private ForceFactory() {
    }

    public RepulsionForce buildRepulsion(double coefficient) {
        return new linRepulsion(coefficient);
    }

    public RepulsionForce getStrongGravity(double coefficient) {
        return new strongGravity(coefficient);
    }

    public AttractionForce buildAttraction(double coefficient) {
        return new logAttraction_antiCollision(coefficient);
    }

    public abstract class AttractionForce {

        public abstract void apply(Node n1, Node n2, double e); // Model for node-node attraction (e is for edge weight if needed)
    }

    public abstract class RepulsionForce {

        public abstract void apply(Node n1, Node n2);           // Model for node-node repulsion

        public abstract void apply(Node n, double g);           // Model for gravitation (anti-repulsion)
    }

    //Repulsion
    private class linRepulsion extends RepulsionForce {

        private double coefficient;

        public linRepulsion(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2) {
            OrderedLayoutData n1Layout = n1.getLayoutData();
            OrderedLayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * n1Layout.mass * n2Layout.mass / distance / distance;

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;
            }
        }

        @Override
        public void apply(Node n, double g) {
            OrderedLayoutData nLayout = n.getLayoutData();

            // Get the distance
            double xDist = n.x();
            double yDist = n.y();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nLayout.mass * g / distance;

                nLayout.dx -= xDist * factor;
                nLayout.dy -= yDist * factor;
            }
        }
    }
    //Gravity

    private class strongGravity extends RepulsionForce {

        private double coefficient;

        public strongGravity(double c) {
            coefficient = c / 2;
        }

        @Override
        public void apply(Node n1, Node n2) {
            // Not Relevant
        }

        @Override
        public void apply(Node n, double g) {
            OrderedLayoutData nLayout = n.getLayoutData();

            // Get the distance
            double xDist = n.x();
            double yDist = n.y();
            double distance = (float) Math.sqrt(xDist * xDist + yDist * yDist);

            if (distance > 0) {
                // NB: factor = force / distance
                double factor = coefficient * nLayout.mass * g;

                nLayout.dx -= xDist * factor;
                nLayout.dy -= yDist * factor;
            }
        }
    }

    //Attraction
    private class logAttraction_antiCollision extends AttractionForce {

        private double coefficient;

        public logAttraction_antiCollision(double c) {
            coefficient = c;
        }

        @Override
        public void apply(Node n1, Node n2, double e) {
            OrderedLayoutData n1Layout = n1.getLayoutData();
            OrderedLayoutData n2Layout = n2.getLayoutData();

            // Get the distance
            double xDist = n1.x() - n2.x();
            double yDist = n1.y() - n2.y();
            double distance = Math.sqrt(xDist * xDist + yDist * yDist) - n1.size() - n2.size();

            if (distance > 0) {

                // NB: factor = force / distance
                double factor = -coefficient * e * Math.log(0.5 + distance) / distance; //I changed the 1 to 0.5

                n1Layout.dx += xDist * factor;
                n1Layout.dy += yDist * factor;

                n2Layout.dx -= xDist * factor;
                n2Layout.dy -= yDist * factor;
            }
        }
    }

}
