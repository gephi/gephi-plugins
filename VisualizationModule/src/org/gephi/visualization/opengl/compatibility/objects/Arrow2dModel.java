/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.visualization.opengl.compatibility.objects;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.graph.api.EdgeData;
import org.gephi.graph.api.MetaEdge;
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.VizController;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.lib.gleem.linalg.Vec2f;
import org.gephi.lib.gleem.linalg.Vecf;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class Arrow2dModel extends ModelImpl<NodeData> {

    protected static float ARROW_WIDTH = 1f;
    protected static float ARROW_HEIGHT = 1.1f;
    protected EdgeData edge;
    protected float weight;
    
    private Arrow2dModel() {
        super();
        octants = new Octant[1];
    }

    public Arrow2dModel(EdgeData edge) {
        this();
        this.edge = edge;
    }

    @Override
    public void display(GL gl, GLU glu, VizModel vizModel) {
        if (!selected && vizModel.isHideNonSelectedEdges()) {
            return;
        }
        NodeData nodeFrom = edge.getSource();
        NodeData nodeTo = edge.getTarget();

        //Edge weight
        GraphLimits limits = vizModel.getLimits();
        float w;
        if (edge.getEdge() instanceof MetaEdge) {
            float weightRatio;
            if (limits.getMinMetaWeight() == limits.getMaxMetaWeight()) {
                weightRatio = Edge2dModel.WEIGHT_MINIMUM / limits.getMinMetaWeight();
            } else {
                weightRatio = Math.abs((Edge2dModel.WEIGHT_MAXIMUM - Edge2dModel.WEIGHT_MINIMUM) / (limits.getMaxMetaWeight() - limits.getMinMetaWeight()));
            }
            float edgeScale = vizModel.getEdgeScale() * vizModel.getMetaEdgeScale();
            w = weight;
            w = ((w - limits.getMinMetaWeight()) * weightRatio + Edge2dModel.WEIGHT_MINIMUM) * edgeScale;
        } else {
            float weightRatio;
            if (limits.getMinWeight() == limits.getMaxWeight()) {
                weightRatio = Edge2dModel.WEIGHT_MINIMUM / limits.getMinWeight();
            } else {
                weightRatio = Math.abs((Edge2dModel.WEIGHT_MAXIMUM - Edge2dModel.WEIGHT_MINIMUM) / (limits.getMaxWeight() - limits.getMinWeight()));
            }
            float edgeScale = vizModel.getEdgeScale();
            w = weight;
            w = ((w - limits.getMinWeight()) * weightRatio + Edge2dModel.WEIGHT_MINIMUM) * edgeScale;
        }
        //

        //Edge size
        float arrowWidth = ARROW_WIDTH * w * 2f;
        float arrowHeight = ARROW_HEIGHT * w * 2f;

        float x2 = nodeTo.x();
        float y2 = nodeTo.y();
        float x1 = nodeFrom.x();
        float y1 = nodeFrom.y();

        //Edge vector
        Vec2f edgeVector = new Vec2f(x2 - x1, y2 - y1);
        edgeVector.normalize();

        //Get collision distance between nodeTo and arrow point
        double angle = Math.atan2(y2 - y1, x2 - x1);
        if (nodeTo.getModel() == null) {
            return;
        }
        float collisionDistance = ((ModelImpl) nodeTo.getModel()).getCollisionDistance(angle);

        //Point of the arrow
        float targetX = x2 - edgeVector.x() * collisionDistance;
        float targetY = y2 - edgeVector.y() * collisionDistance;

        //Base of the arrow
        float baseX = targetX - edgeVector.x() * arrowHeight * 2f;
        float baseY = targetY - edgeVector.y() * arrowHeight * 2f;

        //Side vector
        float sideVectorX = y1 - y2;
        float sideVectorY = x2 - x1;
        float norm = (float) Math.sqrt(sideVectorX * sideVectorX + sideVectorY * sideVectorY);
        sideVectorX /= norm;
        sideVectorY /= norm;

        //Color
        if (!selected) {
            float r;
            float g;
            float b;
            float a;
            r = edge.r();
            if (r == -1f) {
                if (vizModel.isEdgeHasUniColor()) {
                    float[] uni = vizModel.getEdgeUniColor();
                    r = uni[0];
                    g = uni[1];
                    b = uni[2];
                    a = uni[3];
                } else {
                    NodeData source = edge.getSource();
                    r = 0.498f * source.r();
                    g = 0.498f * source.g();
                    b = 0.498f * source.b();
                    a = edge.alpha();
                }
            } else {
                g = 0.498f * edge.g();
                b = 0.498f * edge.b();
                r *= 0.498f;
                a = edge.alpha();
            }
            if (vizModel.getConfig().isLightenNonSelected()) {
                float lightColorFactor = vizModel.getConfig().getLightenNonSelectedFactor();
                a = a - (a - 0.01f) * lightColorFactor;
                gl.glColor4f(r, g, b, a);
            } else {
                gl.glColor4f(r, g, b, a);
            }
        } else {
            float r = 0f;
            float g = 0f;
            float b = 0f;
            if (vizModel.isEdgeSelectionColor()) {
                ModelImpl m1 = (ModelImpl) edge.getSource().getModel();
                ModelImpl m2 = (ModelImpl) edge.getTarget().getModel();
                if (m1.isSelected() && m2.isSelected()) {
                    float[] both = vizModel.getEdgeBothSelectionColor();
                    r = both[0];
                    g = both[1];
                    b = both[2];
                } else if (m1.isSelected()) {
                    float[] out = vizModel.getEdgeOutSelectionColor();
                    r = out[0];
                    g = out[1];
                    b = out[2];
                } else if (m2.isSelected()) {
                    float[] in = vizModel.getEdgeInSelectionColor();
                    r = in[0];
                    g = in[1];
                    b = in[2];
                }
            } else {
                r = edge.r();
                if (r == -1f) {
                    NodeData source = edge.getSource();
                    r = source.r();
                    g = source.g();
                    b = source.b();
                } else {
                    g = edge.g();
                    b = edge.b();
                }
            }
            gl.glColor4f(r, g, b, 1f);
        }

        //Draw the triangle
        gl.glVertex2d(baseX + sideVectorX * arrowWidth, baseY + sideVectorY * arrowWidth);
        gl.glVertex2d(baseX - sideVectorX * arrowWidth, baseY - sideVectorY * arrowWidth);
        gl.glVertex2d(targetX, targetY);
    }

    @Override
    public float getCollisionDistance(double angle) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isInOctreeLeaf(Octant leaf) {
        if (obj.getModel() == null) {
            return false;
        }
        return ((ModelImpl) obj.getModel()).getOctants()[0] == leaf;
    }

    @Override
    public int[] octreePosition(float centerX, float centerY, float centerZ,
            float size) {
        return new int[] {};
    }

    @Override
    public boolean selectionTest(Vecf distanceFromMouse, float selectionSize) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String toSVG() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setOctant(Octant octant) {
        this.octants[0] = octant;
    }

    @Override
    public Octant[] getOctants() {
        if (obj.getModel() == null) {
            return this.octants;
        }
        Octant[] oc = ((ModelImpl) obj.getModel()).getOctants();
        if (oc[0] == null) //The node has been destroyed
        {
            oc = this.octants;
        }
        return oc;
    }

    /*@Override
    public boolean isCacheMatching(int cacheMarker) {
    if (edge.getModel() != null) {
    return ((ModelImpl) edge.getModel()).isCacheMatching(cacheMarker);
    }
    return false;
    }*/
    public EdgeData getEdge() {
        return edge;
    }

    @Override
    public void cleanModel() {
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
