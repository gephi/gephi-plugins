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
package org.gephi.visualization.opengl.octree;

import com.sun.opengl.util.GLUT;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.utils.collection.avl.AVLItemAccessor;
import org.gephi.utils.collection.avl.ParamAVLTree;
import org.gephi.utils.collection.avl.AVLItem;
import org.gephi.visualization.apiimpl.ModelImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class Octant implements AVLItem {

    //Static
    private static int OctantIDs = 0;
    //Octree
    private Octree octree;
    //Coordinates
    private float size;
    private float posX;
    private float posY;
    private float posZ;
    private int depth;
    //Attributes
    private final int octantID;
    private int objectsCount = 0;
    private Octant[] children;
    private AtomicBoolean updateFlag = new AtomicBoolean();
    //Models
    private List<ParamAVLTree<ModelImpl>> modelClasses;

    public Octant(Octree octree, int depth, float posX, float posY, float posZ, float size) {
        this.size = size;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.depth = depth;
        this.octree = octree;
        this.octantID = OctantIDs++;
    }

    public void addObject(int classID, ModelImpl obj) {
        if (children == null && depth < octree.getMaxDepth()) {
            //Create children
            subdivide();
        }

        if (depth == octree.getMaxDepth()) {
            //First item add - Initialize
            if (objectsCount == 0) {
                octree.addLeaf(this);

                modelClasses = new ArrayList<ParamAVLTree<ModelImpl>>(octree.getClassesCount());
                for (int i = 0; i < octree.getClassesCount(); i++) {
                    modelClasses.add(new ParamAVLTree<ModelImpl>(new AVLItemAccessor<ModelImpl>() {

                        public int getNumber(ModelImpl item) {
                            return item.getNumber();
                        }
                    }));
                }
            }

            //Get the list
            ParamAVLTree<ModelImpl> objectClass = this.modelClasses.get(classID);

            //Set Octant
            obj.setOctant(this);

            //Add at this node
            obj.setID(octree.getNextObjectID());
            if (objectClass.add(obj)) {
                objectsCount++;
            }
        } else {
            if (classID == 0 && this == octree.root) {
                //Clamp Hack to avoid nodes to be outside octree
                float quantum = size / 2;
                float x = obj.getObj().x();
                float y = obj.getObj().y();
                float z = obj.getObj().z();
                if (x > posX + quantum) {
                    obj.getObj().setX(posX + quantum);
                } else if (x < posX - quantum) {
                    obj.getObj().setX(posX - quantum);
                }
                if (y > posY + quantum) {
                    obj.getObj().setY(posY + quantum);
                } else if (y < posY - quantum) {
                    obj.getObj().setY(posY - quantum);
                }
                if (z > posZ + quantum) {
                    obj.getObj().setZ(posZ + quantum);
                } else if (z < posZ - quantum) {
                    obj.getObj().setZ(posZ - quantum);
                }
            }

            for (int index : obj.octreePosition(posX, posY, posZ, size)) {
                children[index].addObject(classID, obj);
            }
        }
    }

    public void removeObject(int classID, ModelImpl obj) {
        //Get the list
        ParamAVLTree<ModelImpl> objectClass = this.modelClasses.get(classID);

        if (objectClass.remove(obj)) {
            objectsCount--;
        }

        if (objectsCount == 0) {
            //Remove leaf
            octree.removeLeaf(this);
        }

    }

    public void clear(int classID) {
        ParamAVLTree<ModelImpl> tree = getTree(classID);
        int count = tree.getCount();
        tree.clear();
        objectsCount -= count;
        if (objectsCount == 0) {
            //Remove leaf
            octree.removeLeaf(this);
        }
    }

    public void subdivide() {
        float quantum = size / 4;
        float newSize = size / 2;
        Octant o1 = new Octant(octree, depth + 1, posX + quantum, posY + quantum, posZ - quantum, newSize);
        Octant o2 = new Octant(octree, depth + 1, posX - quantum, posY + quantum, posZ - quantum, newSize);
        Octant o3 = new Octant(octree, depth + 1, posX + quantum, posY + quantum, posZ + quantum, newSize);
        Octant o4 = new Octant(octree, depth + 1, posX - quantum, posY + quantum, posZ + quantum, newSize);

        Octant o5 = new Octant(octree, depth + 1, posX + quantum, posY - quantum, posZ - quantum, newSize);
        Octant o6 = new Octant(octree, depth + 1, posX - quantum, posY - quantum, posZ - quantum, newSize);
        Octant o7 = new Octant(octree, depth + 1, posX + quantum, posY - quantum, posZ + quantum, newSize);
        Octant o8 = new Octant(octree, depth + 1, posX - quantum, posY - quantum, posZ + quantum, newSize);

        children = new Octant[]{o1, o2, o3, o4, o5, o6, o7, o8};
    }

    public Iterator<ModelImpl> iterator(int classID) {
        return this.modelClasses.get(classID).iterator();
    }

    public ParamAVLTree<ModelImpl> getTree(int classID) {
        return modelClasses.get(classID);
    }

    public void displayOctant(GL gl) {
        /*if(children==null && depth==octree.getMaxDepth() && objectsCount>0)
        {*/

        float quantum = size / 2;
        gl.glBegin(GL.GL_QUAD_STRIP);
        gl.glVertex3f(posX + quantum, posY + quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY - quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY + quantum, posZ - quantum);
        gl.glVertex3f(posX + quantum, posY - quantum, posZ - quantum);
        gl.glVertex3f(posX - quantum, posY + quantum, posZ - quantum);
        gl.glVertex3f(posX - quantum, posY - quantum, posZ - quantum);
        gl.glVertex3f(posX - quantum, posY + quantum, posZ + quantum);
        gl.glVertex3f(posX - quantum, posY - quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY + quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY - quantum, posZ + quantum);
        gl.glEnd();
        gl.glBegin(GL.GL_QUADS);
        gl.glVertex3f(posX - quantum, posY + quantum, posZ - quantum);
        gl.glVertex3f(posX - quantum, posY + quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY + quantum, posZ + quantum);
        gl.glVertex3f(posX + quantum, posY + quantum, posZ - quantum);

        gl.glVertex3f(posX - quantum, posY - quantum, posZ + quantum);
        gl.glVertex3f(posX - quantum, posY - quantum, posZ - quantum);
        gl.glVertex3f(posX + quantum, posY - quantum, posZ - quantum);
        gl.glVertex3f(posX + quantum, posY - quantum, posZ + quantum);
        gl.glEnd();
        /*}
        else if(children!=null)
        {
        for(Octant o : children)
        {
        o.displayOctreeNode(gl);
        }
        }*/
    }

    public void displayOctantInfo(GL gl, GLU glu) {
        GLUT glut = new GLUT();

        float quantum = size / 2;
        float height = 15;

        gl.glPushMatrix();
        gl.glTranslatef(posX - quantum, posY + quantum - height, posZ + quantum);
        gl.glScalef(0.1f, 0.1f, 0.1f);
        gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
        glut.glutStrokeString(GLUT.STROKE_MONO_ROMAN, "ID: " + octantID);
        gl.glPopMatrix();

        height += 15;
        gl.glPushMatrix();
        gl.glTranslatef(posX - quantum, posY + quantum - height, posZ + quantum);
        gl.glScalef(0.1f, 0.1f, 0.1f);
        gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
        glut.glutStrokeString(GLUT.STROKE_MONO_ROMAN, "objectsCount: " + objectsCount);
        gl.glPopMatrix();

        int i = 0;
        for (ParamAVLTree<ModelImpl> p : modelClasses) {
            height += 15;
            gl.glPushMatrix();
            gl.glTranslatef(posX - quantum, posY + quantum - height, posZ + quantum);
            gl.glScalef(0.1f, 0.1f, 0.1f);
            gl.glColor4f(0.0f, 0.0f, 1.0f, 1.0f);
            glut.glutStrokeString(GLUT.STROKE_MONO_ROMAN, "class" + (i++) + ": " + p.getCount());
            gl.glPopMatrix();
        }
    }

    public int getNumber() {
        return octantID;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public float getPosZ() {
        return posZ;
    }

    public float getSize() {
        return size;
    }

    public boolean isRequiringUpdatePosition() {
        return updateFlag.get();
    }

    public void requireUpdatePosition() {
        if (!updateFlag.getAndSet(true)) {
            octree.vizController.getScheduler().requireUpdatePosition();
        }
    }

    public void resetUpdatePositionFlag() {
        updateFlag.set(false);
    }
}
