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
import org.gephi.graph.api.Model;
import org.gephi.graph.api.NodeData;
import org.gephi.visualization.VizModel;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.lib.gleem.linalg.Vecf;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.opengl.octree.Octant;

/**
 *
 * @author Mathieu Bastian
 */
public class Edge2dModel extends ModelImpl<EdgeData> {

    protected static final float WEIGHT_MINIMUM = 0.4f;
    protected static final float WEIGHT_MAXIMUM = 8f;
    //An edge is set in both source node and target node octant. Hence edges are not drawn when none of
    //these octants are visible.
    protected Arrow2dModel arrow;
    protected float weight;

    public Edge2dModel() {
        octants = new Octant[2];
    }

    @Override
    public int[] octreePosition(float centerX, float centerY, float centerZ, float size) {
        //Not used, because getOctants() already returns right octant to add the edge
        NodeData nodeFrom = obj.getSource();
        NodeData nodeTo = obj.getTarget();

        int index1 = -1, index2 = -1;

        size /= 2;

        //True if the point is in the big cube.
        if (!(Math.abs(nodeFrom.x() - centerX) > size || Math.abs(nodeFrom.y() - centerY) > size || Math.abs(nodeFrom.z() - centerZ) > size)) {
            index1 = 0;

            //Point1
            if (nodeFrom.y() < centerY) {
                index1 += 4;
            }
            if (nodeFrom.z() > centerZ) {
                index1 += 2;
            }
            if (nodeFrom.x() < centerX) {
                index1 += 1;
            }
        }

        if (!(Math.abs(nodeTo.x() - centerX) > size || Math.abs(nodeTo.y() - centerY) > size || Math.abs(nodeTo.z() - centerZ) > size)) {
            index2 = 0;

            //Point2
            if (nodeTo.y() < centerY) {
                index2 += 4;
            }
            if (nodeTo.z() > centerZ) {
                index2 += 2;
            }
            if (nodeTo.x() < centerX) {
                index2 += 1;
            }
        }

        if (index1 >= 0 && index2 >= 0) {
            if (index1 != index2) {
                return new int[]{index1, index2};
            } else {
                return new int[]{index1};
            }
        } else if (index1 >= 0) {
            return new int[]{index1};
        } else if (index2 >= 0) {
            return new int[]{index2};
        } else {
            return new int[]{};
        }
    }

    @Override
    public boolean isInOctreeLeaf(Octant leaf) {
        NodeData nodeFrom = obj.getSource();
        NodeData nodeTo = obj.getTarget();
        if (nodeFrom.getModel() == null || nodeTo.getModel() == null) {
            return false;
        }
        boolean res = true;
        if (octants[0] == leaf) {
            if (octants[0] != ((ModelImpl) nodeFrom.getModel()).getOctants()[0]) //0 = nodeFrom
            {
                res = false;
            }
        }
        if (octants[1] == leaf) {
            if (octants[1] != ((ModelImpl) nodeTo.getModel()).getOctants()[0]) //1 = nodeTo
            {
                res = false;
            }
        }
        if (octants[0] != leaf && octants[1] != leaf) {
            res = false;
        }

        return res;
    }

    @Override
    public void display(GL gl, GLU glu, VizModel vizModel) {
        if (this.arrow != null) {
            this.arrow.setSelected(selected);
        }
        if (!selected && vizModel.isHideNonSelectedEdges()) {
            return;
        }
        if (selected && vizModel.isAutoSelectNeighbor()) {
            ModelImpl m1 = (ModelImpl) obj.getSource().getModel();
            ModelImpl m2 = (ModelImpl) obj.getTarget().getModel();
            m1.mark = true;
            m2.mark = true;
        }

        //Edge weight
        GraphLimits limits = vizModel.getLimits();
        float w;
        if (obj.getEdge() instanceof MetaEdge) {
            float weightRatio;
            if (limits.getMinMetaWeight() == limits.getMaxMetaWeight()) {
                weightRatio = WEIGHT_MINIMUM / limits.getMinMetaWeight();
            } else {
                weightRatio = Math.abs((WEIGHT_MAXIMUM - WEIGHT_MINIMUM) / (limits.getMaxMetaWeight() - limits.getMinMetaWeight()));
            }
            float edgeScale = vizModel.getEdgeScale() * vizModel.getMetaEdgeScale();
            w = weight;
            w = ((w - limits.getMinMetaWeight()) * weightRatio + WEIGHT_MINIMUM) * edgeScale;
        } else {
            float weightRatio;
            if (limits.getMinWeight() == limits.getMaxWeight()) {
                weightRatio = WEIGHT_MINIMUM / limits.getMinWeight();
            } else {
                weightRatio = Math.abs((WEIGHT_MAXIMUM - WEIGHT_MINIMUM) / (limits.getMaxWeight() - limits.getMinWeight()));
            }
            float edgeScale = vizModel.getEdgeScale();
            w = weight;
            w = ((w - limits.getMinWeight()) * weightRatio + WEIGHT_MINIMUM) * edgeScale;
        }
        //

        float x1 = obj.getSource().x();
        float x2 = obj.getTarget().x();
        float y1 = obj.getSource().y();
        float y2 = obj.getTarget().y();
        float t1 = w;
        float t2 = w;

        float sideVectorX = y1 - y2;
        float sideVectorY = x2 - x1;
        float norm = (float) Math.sqrt(sideVectorX * sideVectorX + sideVectorY * sideVectorY);
        sideVectorX /= norm;
        sideVectorY /= norm;

        float x1Thick = sideVectorX / 2f * t1;
        float x2Thick = sideVectorX / 2f * t2;
        float y1Thick = sideVectorY / 2f * t1;
        float y2Thick = sideVectorY / 2f * t2;

        if (!selected) {
            float r;
            float g;
            float b;
            float a;
            r = obj.r();
            if (r == -1f) {
                if (vizModel.isEdgeHasUniColor()) {
                    float[] uni = vizModel.getEdgeUniColor();
                    r = uni[0];
                    g = uni[1];
                    b = uni[2];
                    a = uni[3];
                } else {
                    NodeData source = obj.getSource();
                    r = 0.498f * source.r();
                    g = 0.498f * source.g();
                    b = 0.498f * source.b();
                    a = obj.alpha();
                }
            } else {
                g = 0.498f * obj.g();
                b = 0.498f * obj.b();
                r *= 0.498f;
                a = obj.alpha();
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
                ModelImpl m1 = (ModelImpl) obj.getSource().getModel();
                ModelImpl m2 = (ModelImpl) obj.getTarget().getModel();
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
                r = obj.r();
                if (r == -1f) {
                    NodeData source = obj.getSource();
                    r = source.r();
                    g = source.g();
                    b = source.b();
                } else {
                    g = obj.g();
                    b = obj.b();
                }
            }
            gl.glColor4f(r, g, b, 1f);
        }

        gl.glVertex2f(x1 + x1Thick, y1 + y1Thick);
        gl.glVertex2f(x1 - x1Thick, y1 - y1Thick);
        gl.glVertex2f(x2 - x2Thick, y2 - y2Thick);
        gl.glVertex2f(x2 - x2Thick, y2 - y2Thick);
        gl.glVertex2f(x2 + x2Thick, y2 + y2Thick);
        gl.glVertex2f(x1 + x1Thick, y1 + y1Thick);
    }

    @Override
    public boolean selectionTest(Vecf distanceFromMouse, float selectionSize) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public float getCollisionDistance(double angle) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getCameraDistance() {
        return (obj.getSource().getModel().getCameraDistance() + obj.getTarget().getModel().getCameraDistance()) / 2f;
    }

    @Override
    public boolean isAutoSelected() {
        Model nSource = obj.getSource().getModel();
        Model nTarget = obj.getTarget().getModel();
        if (nSource != null && nTarget != null) {
            return obj.getSource().getModel().isSelected() || obj.getTarget().getModel().isSelected();
        }
        return false;
    }

    @Override
    public boolean onlyAutoSelect() {
        return true;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public String toSVG() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setOctant(Octant octant) {
        if (((ModelImpl) obj.getSource().getModel()).getOctants()[0] == octant) {
            octants[0] = octant;
        }
        if (((ModelImpl) obj.getTarget().getModel()).getOctants()[0] == octant) {
            octants[1] = octant;
        }
    }

    @Override
    public void resetOctant() {
        octants[0] = null;
        octants[1] = null;
    }

    @Override
    public Octant[] getOctants() {
        if (this.octants[0] == null && this.octants[1] == null) {
            Octant sourceOctant = ((ModelImpl) obj.getSource().getModel()).getOctants()[0];
            Octant targetOctant = ((ModelImpl) obj.getTarget().getModel()).getOctants()[0];
            if (sourceOctant == targetOctant) {
                return new Octant[]{sourceOctant};
            } else {
                return new Octant[]{sourceOctant, targetOctant};
            }
        } else {
            return this.octants;
        }
    }

    @Override
    public boolean isValid() {
        return octants[0] != null || octants[1] != null;
    }

    public Arrow2dModel getArrow() {
        return arrow;
    }

    public void setArrow(Arrow2dModel arrow) {
        this.arrow = arrow;
    }

    @Override
    public float getViewportX() {
        return (obj.getSource().getModel().getViewportX() + 2 * obj.getTarget().getModel().getViewportX()) / 3f;
    }

    @Override
    public float getViewportY() {
        return (obj.getSource().getModel().getViewportY() + 2 * obj.getTarget().getModel().getViewportY()) / 3f;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
