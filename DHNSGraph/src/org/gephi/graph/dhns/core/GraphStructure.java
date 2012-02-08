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
package org.gephi.graph.dhns.core;

import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;
import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import org.gephi.utils.collection.avl.ParamAVLIterator;
import org.gephi.graph.api.GraphEvent.EventType;
import org.gephi.graph.dhns.edge.AbstractEdge;
import org.gephi.graph.dhns.event.ViewEvent;
import org.gephi.graph.dhns.node.AbstractNode;
import org.gephi.graph.dhns.node.NodeDataImpl;
import org.gephi.graph.dhns.node.iterators.TreeListIterator;

/**
 *
 * @author Mathieu Bastian
 */
public class GraphStructure {

    private final AtomicInteger viewId = new AtomicInteger(1);
    private final Dhns dhns;
    private final GraphViewImpl mainView;
    private final Queue<GraphViewImpl> views;
    private final GraphDictionnary dictionnary;
    private GraphViewImpl visibleView;
    //Destroy
    private final Object lock = new Object();
    private final ConcurrentLinkedQueue<GraphViewImpl> destroyQueue;

    public GraphStructure(Dhns dhns) {
        this.dhns = dhns;
        views = new ConcurrentLinkedQueue<GraphViewImpl>();
        dictionnary = new GraphDictionnary();

        //Main view
        mainView = new GraphViewImpl(dhns, 0);
        views.add(mainView);
        visibleView = mainView;

        //Destructor
        destroyQueue = new ConcurrentLinkedQueue<GraphViewImpl>();
        ViewDestructorThread viewDestructorThread = new ViewDestructorThread(this);
        viewDestructorThread.start();
    }

    public GraphViewImpl[] getViews() {
        return views.toArray(new GraphViewImpl[0]);
    }

    public GraphViewImpl getMainView() {
        return mainView;
    }

    public GraphViewImpl createView(int viewId) {   //used by deserializer
        this.viewId.set(Math.max(viewId + 1, this.viewId.get()));
        return new GraphViewImpl(dhns, viewId);
    }

    public GraphViewImpl getNewView() {
        return copyView(mainView);
    }

    public GraphViewImpl copyView(GraphViewImpl view) {
        GraphViewImpl viewCopy = new GraphViewImpl(dhns, viewId.getAndIncrement());
        TreeStructure newStructure = viewCopy.getStructure();
        dhns.writeLock();

        for (TreeListIterator itr = new TreeListIterator(view.getStructure().getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            AbstractNode nodeCopy = new AbstractNode(node.getNodeData(), viewCopy.getViewId());
            nodeCopy.setEnabled(node.isEnabled());
            nodeCopy.setEnabledInDegree(node.getEnabledInDegree());
            nodeCopy.setEnabledOutDegree(node.getEnabledOutDegree());
            nodeCopy.setEnabledMutualDegree(node.getEnabledMutualDegree());
            AbstractNode parentCopy = node.parent != null ? newStructure.getNodeAt(node.parent.getPre()) : null;
            newStructure.insertAsChild(nodeCopy, parentCopy);
        }

        //Edges
        ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
        for (TreeListIterator itr = new TreeListIterator(view.getStructure().getTree(), 1); itr.hasNext();) {
            AbstractNode node = itr.next();
            if (!node.getEdgesOutTree().isEmpty()) {
                for (edgeIterator.setNode(node.getEdgesOutTree()); edgeIterator.hasNext();) {
                    AbstractEdge edge = edgeIterator.next();
                    AbstractNode sourceCopy = newStructure.getNodeAt(node.getPre());
                    AbstractNode targetCopy = newStructure.getNodeAt(((AbstractNode) edge.getTarget().getNodeData().getNode(view.getViewId())).getPre());
                    sourceCopy.getEdgesOutTree().add(edge);
                    targetCopy.getEdgesInTree().add(edge);
                    addToDictionnary(edge);
                }
            }
        }
        viewCopy.setNodesEnabled(view.getNodesEnabled());
        viewCopy.setEdgesCountTotal(view.getEdgesCountTotal());
        viewCopy.setEdgesCountEnabled(view.getEdgesCountEnabled());
        viewCopy.setMutualEdgesTotal(view.getMutualEdgesTotal());
        viewCopy.setMutualEdgesEnabled(view.getMutualEdgesEnabled());

        //Metaedges
        viewCopy.getStructureModifier().getEdgeProcessor().computeMetaEdges();

        views.add(viewCopy);
        dhns.writeUnlock();
        dhns.getEventManager().fireEvent(new ViewEvent(EventType.NEW_VIEW, viewCopy));
        return viewCopy;
    }

    public void destroyView(final GraphViewImpl view) {
        if (views.contains(view)) {
            destroyQueue.add(view);
            synchronized (this.lock) {
                lock.notify();
            }
        }
    }

    public void addToDictionnary(AbstractNode node) {
        dictionnary.addNode(node);
    }

    public void removeFromDictionnary(AbstractNode node) {
        dictionnary.removeNode(node);
    }

    public void addToDictionnary(AbstractEdge edge) {
        dictionnary.addEdge(edge);
    }

    public void removeFromDictionnary(AbstractEdge edge) {
        dictionnary.removeEdge(edge);
    }

    public AbstractEdge getEdgeFromDictionnary(int id) {
        return dictionnary.getEdge(id);
    }

    public AbstractEdge getEdgeFromDictionnary(String id) {
        return dictionnary.getEdge(id);
    }

    public AbstractNode getNodeFromDictionnary(int id, int viewId) {
        return dictionnary.getNode(id, viewId);
    }

    public AbstractNode getNodeFromDictionnary(String id, int viewId) {
        return dictionnary.getNode(id, viewId);
    }

    public void setNodeId(NodeDataImpl node, String id) {
        String oldId = node.setId(id);
        dictionnary.setNodeId(oldId, id, node);
    }

    public void setEdgeId(AbstractEdge edge, String id) {
        String oldId = edge.getEdgeData().setId(id);
        dictionnary.setEdgeId(oldId, id, edge);
    }

    public GraphViewImpl getVisibleView() {
        return visibleView;
    }

    public void setVisibleView(GraphViewImpl visibleView) {
        if (this.visibleView == visibleView) {
            return;
        }
        if (visibleView == null) {
            this.visibleView = mainView;
        } else {
            this.visibleView = visibleView;
        }
        dhns.getEventManager().fireEvent(new ViewEvent(EventType.VISIBLE_VIEW, this.visibleView));
    }

    private static class ViewDestructorThread extends Thread {

        private final WeakReference<GraphStructure> structureReference;
        private final int STD_TIMER = 300;
        private final int UNDESTRO_TIMER = 2000;
        private boolean running = true;

        public ViewDestructorThread(GraphStructure graphStructure) {
            super("DHNS View Destructor");
            setDaemon(true);
            structureReference = new WeakReference<GraphStructure>(graphStructure);
        }

        @Override
        public void run() {
            GraphStructure structure = null;
            while (running && (structure = structureReference.get()) != null) {
                while (structure.destroyQueue.isEmpty()) {
                    try {
                        synchronized (structure.lock) {
                            structure.lock.wait();
                        }
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
                boolean undestroyableViews = false;
                for (GraphViewImpl v : structure.destroyQueue.toArray(new GraphViewImpl[0])) {
                    if (!v.hasGraphReference()) {
                        destroyView(structure, v);
                        structure.destroyQueue.remove(v);
                    } else {
                        undestroyableViews = true;
                    }
                }
                try {
                    synchronized (structure.lock) {
                        structure.lock.wait(undestroyableViews ? UNDESTRO_TIMER : STD_TIMER);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void destroyView(GraphStructure structure, GraphViewImpl view) {
            //Logger.getLogger("").log(Level.WARNING, "Destroy view {0}", view.getViewId());
            structure.dhns.writeLock();
            ParamAVLIterator<AbstractEdge> edgeIterator = new ParamAVLIterator<AbstractEdge>();
            for (TreeListIterator itr = new TreeListIterator(structure.mainView.getStructure().getTree(), 1); itr.hasNext();) {
                AbstractNode node = itr.next();
                AbstractNode nodeInView = node.getNodeData().getNodes().get(view.getViewId());
                if (nodeInView != null) {
                    node.getNodeData().getNodes().remove(view.getViewId());
                    if (!nodeInView.getEdgesOutTree().isEmpty()) {
                        for (edgeIterator.setNode(nodeInView.getEdgesOutTree()); edgeIterator.hasNext();) {
                            AbstractEdge edge = edgeIterator.next();
                            structure.removeFromDictionnary(edge);
                        }
                    }
                }
            }
            structure.views.remove(view);
            //System.out.println("Destroy view finished");           
            structure.dhns.writeUnlock();
            structure.dhns.getEventManager().fireEvent(new ViewEvent(EventType.DESTROY_VIEW, view));
            if (structure.visibleView == view) {
                structure.visibleView = structure.mainView;
                structure.dhns.getEventManager().fireEvent(new ViewEvent(EventType.VISIBLE_VIEW, structure.mainView));
            }
        }
    }

    private static class GraphDictionnary {

        private final TObjectIntHashMap<String> nodesMap;
        private final TIntObjectHashMap<NodeDataImpl> nodesIntMap;
        private final TIntObjectHashMap<EdgeCounter> edgesRefCount;
        private final TObjectIntHashMap<String> edgesMap;

        public GraphDictionnary() {
            nodesMap = new TObjectIntHashMap<String>();
            nodesIntMap = new TIntObjectHashMap<NodeDataImpl>();
            edgesRefCount = new TIntObjectHashMap<EdgeCounter>();
            edgesMap = new TObjectIntHashMap<String>();
        }

        public synchronized void addNode(AbstractNode node) {
            if (node.getNodeData().getId() != null) {
                nodesMap.put(node.getNodeData().getId(), node.getId());
            }
            nodesIntMap.put(node.getId(), node.getNodeData());
        }

        public synchronized void removeNode(AbstractNode node) {
            if (node.getNodeData().getNodes().getCount() == 1) {
                if (node.getNodeData().getId() != null) {
                    nodesMap.remove(node.getNodeData().getId());
                }
                nodesIntMap.remove(node.getId());
            }
        }

        public synchronized void addEdge(AbstractEdge edge) {

            EdgeCounter edgeCounter = edgesRefCount.get(edge.getId());
            if (edgeCounter != null) {
                edgeCounter.inc();
            } else {
                edgeCounter = new EdgeCounter(edge);
                edgesRefCount.put(edge.getId(), edgeCounter);
                String id = edge.getEdgeData().getId();
                if (id != null) {
                    edgesMap.put(id, edge.getId());
                }
            }
        }

        public synchronized void removeEdge(AbstractEdge edge) {
            EdgeCounter edgeCounter = edgesRefCount.get(edge.getId());
            int count = edgeCounter.decAndGet();
            if (count == 0) {
                edgesRefCount.remove(edge.getId());
                String id = edge.getEdgeData().getId();
                if (id != null) {
                    edgesMap.remove(id);
                }
            }
        }

        public synchronized AbstractNode getNode(int id, int viewId) {
            NodeDataImpl nodeDataImpl = nodesIntMap.get(id);
            if (nodeDataImpl != null) {
                return (AbstractNode) nodeDataImpl.getNode(viewId);
            }
            return null;
        }

        public synchronized AbstractNode getNode(String id, int viewId) {
            int natId = nodesMap.get(id);
            if (natId != 0) {
                return getNode(natId, viewId);
            }
            return null;
        }

        public synchronized AbstractEdge getEdge(int id) {
            EdgeCounter edgeCounter = edgesRefCount.get(id);
            if (edgeCounter != null) {
                return edgeCounter.edge;
            }
            return null;
        }

        public synchronized AbstractEdge getEdge(String id) {
            int natId = edgesMap.get(id);
            if (natId != 0) {
                return getEdge(natId);
            }
            return null;
        }

        public synchronized void setNodeId(String oldId, String newId, NodeDataImpl node) {
            if (oldId != null) {
                int val = nodesMap.remove(oldId);
                nodesMap.put(newId, val);
            } else {
                nodesMap.put(newId, node.getID());
            }
        }

        public synchronized void setEdgeId(String oldId, String newId, AbstractEdge edge) {
            if (oldId != null) {
                int val = edgesMap.remove(oldId);
                edgesMap.put(newId, val);
            } else {
                edgesMap.put(newId, edge.getId());
            }
        }

        private static class EdgeCounter {

            protected final AbstractEdge edge;
            private int counter = 1;

            public EdgeCounter(AbstractEdge edge) {
                this.edge = edge;
            }

            private void inc() {
                counter++;
            }

            private int decAndGet() {
                return --counter;
            }

            @Override
            public boolean equals(Object obj) {
                if (obj != null && obj instanceof EdgeCounter) {
                    EdgeCounter e = (EdgeCounter) obj;
                    return e.edge.equals(edge);
                } else if (obj != null && obj instanceof AbstractEdge) {
                    return obj.equals(edge);
                }
                return false;
            }

            @Override
            public int hashCode() {
                return edge.hashCode();
            }
        }
    }
}
