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

import com.sun.opengl.util.BufferUtil;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import org.gephi.utils.collection.avl.ResetableIterator;
import org.gephi.utils.collection.avl.AVLItemAccessor;
import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.gephi.utils.collection.avl.ParamAVLTree;
import org.gephi.visualization.GraphLimits;
import org.gephi.visualization.VizArchitecture;
import org.gephi.visualization.VizController;
import org.gephi.visualization.opengl.AbstractEngine;
import org.gephi.visualization.apiimpl.ModelImpl;
import org.gephi.lib.gleem.linalg.Vec3f;
import org.gephi.visualization.swing.GraphDrawableImpl;

/**
 *
 * @author Mathieu Bastian
 */
public class Octree implements VizArchitecture {

    //Architecture
    private GraphDrawableImpl drawable;
    private AbstractEngine engine;
    private GraphLimits limits;
    protected VizController vizController;
    //Attributes
    private int modelIDs;
    private int maxDepth;
    private int classesCount;
    private int size;
    private int cacheMarker;
    //Octant
    protected Octant root;
    private ParamAVLTree<Octant> leaves;
    //Iterator
    private ConcurrentLinkedQueue<OctreeIterator> iteratorQueue;
    //States
    protected List<Octant> visibleLeaves;
    protected List<Octant> selectedLeaves;
    //Utils
    protected ParamAVLIterator<ModelImpl> updatePositionIterator = new ParamAVLIterator<ModelImpl>();
    protected ParamAVLIterator<ModelImpl> cleanObjectsIterator = new ParamAVLIterator<ModelImpl>();

    public Octree(int maxDepth, int size, int nbClasses) {
        this.maxDepth = maxDepth;
        this.classesCount = nbClasses;
        this.size = size;
    }

    public void initArchitecture() {
        this.engine = VizController.getInstance().getEngine();
        this.drawable = VizController.getInstance().getDrawable();
        this.limits = VizController.getInstance().getLimits();
        this.vizController = VizController.getInstance();

        leaves = new ParamAVLTree<Octant>(new AVLItemAccessor<Octant>() {

            public int getNumber(Octant item) {
                return item.getNumber();
            }
        });
        visibleLeaves = new ArrayList<Octant>();
        selectedLeaves = new ArrayList<Octant>();

        iteratorQueue = new ConcurrentLinkedQueue<OctreeIterator>();
        iteratorQueue.add(new OctreeIterator());
        iteratorQueue.add(new OctreeIterator());
        iteratorQueue.add(new OctreeIterator());
        iteratorQueue.add(new OctreeIterator());

        float dis = size / (float) Math.pow(2, this.maxDepth + 1);
        root = new Octant(this, 0, dis, dis, dis, size);
    }

    public void addObject(int classID, ModelImpl obj) {
        Octant[] octants = obj.getOctants();
        boolean manualAdd = true;
        for (int i = 0; i < octants.length; i++) {
            Octant o = octants[i];
            if (o != null) {
                o.addObject(classID, obj);
                manualAdd = false;
            }
        }

        if (manualAdd) {
            root.addObject(classID, obj);
        }
    }

    public void removeObject(int classID, ModelImpl obj) {
        Octant[] octants = obj.getOctants();
        for (int i = 0; i < octants.length; i++) {
            Octant o = obj.getOctants()[i];
            if (o != null) {
                octants[i].removeObject(classID, obj);
            }
        }
    }

    public void updateVisibleOctant(GL gl) {
        //Limits
        refreshLimits();

        //Switch to OpenGL select mode
        int capacity = 1 * 4 * leaves.getCount();      //Each object take in maximium : 4 * name stack depth
        IntBuffer hitsBuffer = BufferUtil.newIntBuffer(capacity);
        gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
        gl.glRenderMode(GL.GL_SELECT);
        gl.glInitNames();
        gl.glPushName(0);
        gl.glDisable(GL.GL_CULL_FACE);      //Disable flags
        //Draw the nodes cube in the select buffer
        for (Octant n : leaves) {
            n.resetUpdatePositionFlag();        //Profit from the loop to do this, because this method is always after updating position
            gl.glLoadName(n.getNumber());
            n.displayOctant(gl);
        }
        int nbRecords = gl.glRenderMode(GL.GL_RENDER);
        if (vizController.getVizModel().isCulling()) {
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
        }
        visibleLeaves.clear();

        //Get the hits and add the nodes' objects to the array
        int depth = Integer.MAX_VALUE;
        int minDepth = -1;
        for (int i = 0; i < nbRecords; i++) {
            int hit = hitsBuffer.get(i * 4 + 3); 		//-1 Because of the glPushName(0)
            int minZ = hitsBuffer.get(i * 4 + 1);
            if (minZ < depth) {
                depth = minZ;
                minDepth = hit;
            }

            Octant nodeHit = leaves.getItem(hit);
            visibleLeaves.add(nodeHit);
        }
        if (minDepth != -1) {
            Octant closestOctant = leaves.getItem(minDepth);
            Vec3f pos = new Vec3f(closestOctant.getPosX(), closestOctant.getPosY(), closestOctant.getPosZ());
            limits.setClosestPoint(pos);
        }
        //System.out.println(minDepth);
    }

    public void updateSelectedOctant(GL gl, GLU glu, float[] mousePosition, float[] pickRectangle) {
        //Start Picking mode
        int capacity = 1 * 4 * visibleLeaves.size();      //Each object take in maximium : 4 * name stack depth
        IntBuffer hitsBuffer = BufferUtil.newIntBuffer(capacity);

        gl.glSelectBuffer(hitsBuffer.capacity(), hitsBuffer);
        gl.glRenderMode(GL.GL_SELECT);
        gl.glDisable(GL.GL_CULL_FACE);      //Disable flags

        gl.glInitNames();
        gl.glPushName(0);

        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPushMatrix();
        gl.glLoadIdentity();

        glu.gluPickMatrix(mousePosition[0], mousePosition[1], pickRectangle[0], pickRectangle[1], drawable.getViewport());
        gl.glMultMatrixd(drawable.getProjectionMatrix());

        gl.glMatrixMode(GL.GL_MODELVIEW);

        //Draw the nodes' cube int the select buffer
        int hitName = 1;
        for (int i = 0; i < visibleLeaves.size(); i++) {
            Octant node = visibleLeaves.get(i);
            gl.glLoadName(hitName);
            node.displayOctant(gl);
            hitName++;
        }

        //Restoring the original projection matrix
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glFlush();

        //Returning to normal rendering mode
        int nbRecords = gl.glRenderMode(GL.GL_RENDER);
        if (vizController.getVizModel().isCulling()) {
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
        }

        //Clean previous selection
        selectedLeaves.clear();

        //Get the hits and put the node under selection in the selectionArray
        for (int i = 0; i < nbRecords; i++) {
            int hit = hitsBuffer.get(i * 4 + 3) - 1; 		//-1 Because of the glPushName(0)

            Octant nodeHit = visibleLeaves.get(hit);
            selectedLeaves.add(nodeHit);
        }
    }

    public void cleanDeletedObjects(int classID) {
        for (Octant o : leaves) {
            for (cleanObjectsIterator.setNode(o.getTree(classID)); cleanObjectsIterator.hasNext();) {
                ModelImpl obj = cleanObjectsIterator.next();
                if (!obj.isCacheMatching(cacheMarker)) {
                    removeObject(classID, obj);
                    obj.resetOctant();
                    if (vizController.getVizConfig().isCleanDeletedModels()) {
                        obj.cleanModel();
                    }
                }
            }
        }
    }

    public void resetObjectClass(int classID) {
        for (Octant o : leaves) {
            ParamAVLTree<ModelImpl> tree = o.getTree(classID);

            //Reset octants in objects
            for (cleanObjectsIterator.setNode(tree); cleanObjectsIterator.hasNext();) {
                ModelImpl obj = cleanObjectsIterator.next();
                obj.resetOctant();
                obj.cleanModel();
                obj.destroy();
            }

            //Empty the tree
            o.clear(classID);
        }
    }

    public void updateObjectsPosition(int classID) {
        for (Octant o : leaves) {
            if (o.isRequiringUpdatePosition()) {
                for (updatePositionIterator.setNode(o.getTree(classID)); updatePositionIterator.hasNext();) {
                    ModelImpl obj = updatePositionIterator.next();
                    if (!obj.isInOctreeLeaf(o)) {
                        o.removeObject(classID, obj);
                        obj.resetOctant();
                        addObject(classID, obj);
                        //TODO break the loop somehow
                    }
                }
            }
        }
    }

    public Iterator<ModelImpl> getObjectIterator(int classID) {
        OctreeIterator itr = borrowIterator();
        itr.reset(visibleLeaves, classID);
        return itr;
    }

    public Iterator<ModelImpl> getSelectedObjectIterator(int classID) {
        OctreeIterator itr = borrowIterator();
        itr.reset(selectedLeaves, classID);
        return itr;
    }

    public int countSelectedObjects(int classID) {
        int res = 0;
        for (int i = 0; i < selectedLeaves.size(); i++) {
            Octant o = selectedLeaves.get(i);
            res += o.getTree(classID).getCount();
        }
        return res;
    }

    public void displayOctree(GL gl, GLU glu) {
        gl.glDisable(GL.GL_CULL_FACE);
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_LINE);
        for (Octant o : visibleLeaves) {
            gl.glColor3f(1, 0.5f, 0.5f);
            o.displayOctant(gl);
            o.displayOctantInfo(gl, glu);
        }
        if (!vizController.getVizConfig().isWireFrame()) {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL.GL_FILL);
        }

        if (vizController.getVizModel().isCulling()) {
            gl.glEnable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
        }
    }

    void addLeaf(Octant leaf) {
        leaves.add(leaf);
    }

    void removeLeaf(Octant leaf) {
        leaves.remove(leaf);
    }

    private void refreshLimits() {

        float minX = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY;
        float minY = Float.POSITIVE_INFINITY;
        float maxY = Float.NEGATIVE_INFINITY;
        float minZ = Float.POSITIVE_INFINITY;
        float maxZ = Float.NEGATIVE_INFINITY;

        for (Octant o : leaves) {
            float octanSize = o.getSize() / 2f;
            minX = Math.min(minX, o.getPosX() - octanSize);
            maxX = Math.max(maxX, o.getPosX() + octanSize);
            minY = Math.min(minY, o.getPosY() - octanSize);
            maxY = Math.max(maxY, o.getPosY() + octanSize);
            minZ = Math.min(minZ, o.getPosZ() - octanSize);
            maxZ = Math.max(maxZ, o.getPosZ() + octanSize);
        }

        int viewportMinX = Integer.MAX_VALUE;
        int viewportMaxX = Integer.MIN_VALUE;
        int viewportMinY = Integer.MAX_VALUE;
        int viewportMaxY = Integer.MIN_VALUE;
        double[] point;

        point = drawable.myGluProject(minX, minY, minZ);        //bottom far left
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(minX, minY, maxZ);        //bottom near left
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(minX, maxY, maxZ);        //up near left
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(maxX, minY, maxZ);        //bottom near right
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(maxX, minY, minZ);        //bottom far right
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(maxX, maxY, minZ);        //up far right
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(maxX, maxY, maxZ);        //up near right
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        point = drawable.myGluProject(minX, maxY, minZ);        //up far left
        viewportMinX = Math.min(viewportMinX, (int) point[0]);
        viewportMinY = Math.min(viewportMinY, (int) point[1]);
        viewportMaxX = Math.max(viewportMaxX, (int) point[0]);
        viewportMaxY = Math.max(viewportMaxY, (int) point[1]);

        limits.setMinXoctree(minX);
        limits.setMaxXoctree(maxX);
        limits.setMinYoctree(minY);
        limits.setMaxYoctree(maxY);
        limits.setMinZoctree(minZ);
        limits.setMaxZoctree(maxZ);

        limits.setMinXviewport(viewportMinX);
        limits.setMaxXviewport(viewportMaxX);
        limits.setMinYviewport(viewportMinY);
        limits.setMaxYviewport(viewportMaxY);
    }

    int getClassesCount() {
        return classesCount;
    }

    int getMaxDepth() {
        return maxDepth;
    }

    int getNextObjectID() {
        return modelIDs++;
    }

    private OctreeIterator borrowIterator() {
        OctreeIterator itr = iteratorQueue.poll();
        if (itr == null) {
            System.err.println("Octree iterator starved");
        }
        return itr;
    }

    private void returnIterator(OctreeIterator iterator) {
        iteratorQueue.add(iterator);

    }

    public void setCacheMarker(int cacheMarker) {
        this.cacheMarker = cacheMarker;
    }

    private class OctreeIterator implements Iterator<ModelImpl>, ResetableIterator {

        private int i = 0;
        private int classID;
        private List<Octant> octants;
        private ParamAVLIterator<ModelImpl> octantIterator;

        public OctreeIterator() {
            octantIterator = new ParamAVLIterator<ModelImpl>();
        }

        public OctreeIterator(List<Octant> octants, int classID) {
            this();
            this.octants = octants;
            this.classID = classID;
        }

        public void reset(List<Octant> octants, int classID) {
            this.octants = octants;
            this.classID = classID;
            i = 0;
        }

        public boolean hasNext() {
            if (!octantIterator.hasNext()) {
                while (i < octants.size()) {
                    octantIterator.setNode(octants.get(i).getTree(classID));
                    i++;
                    if (octantIterator.hasNext()) {
                        return true;
                    }
                }
                returnIterator(this);
                return false;
            }
            return true;
        }

        public ModelImpl next() {
            ModelImpl obj = octantIterator.next();
            return obj;
        }

        public void remove() {
            octantIterator.remove();
        }
    }
}
