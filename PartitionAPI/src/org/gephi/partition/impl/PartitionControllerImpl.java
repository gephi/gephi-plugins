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
package org.gephi.partition.impl;

import java.util.ArrayList;
import java.util.List;
import org.gephi.data.attributes.api.AttributeEvent;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeListener;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeUtils;
import org.gephi.data.attributes.api.Estimator;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.dynamic.api.DynamicModel;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphEvent;
import org.gephi.graph.api.GraphListener;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.HierarchicalGraph;
import org.gephi.graph.api.Node;
import org.gephi.partition.api.EdgePartition;
import org.gephi.partition.api.NodePartition;
import org.gephi.partition.api.Part;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionController;
import org.gephi.partition.api.PartitionModel;
import org.gephi.partition.spi.Transformer;
import org.gephi.partition.spi.TransformerBuilder;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = PartitionController.class)
public class PartitionControllerImpl implements PartitionController, AttributeListener {

    private PartitionModelImpl model;
    private boolean refreshPartitions = true;

    public PartitionControllerImpl() {

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
                workspace.add(new PartitionModelImpl());
            }

            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(PartitionModelImpl.class);
                if (model == null) {
                    model = new PartitionModelImpl();
                    workspace.add(model);
                }
                refreshPartitions = true;
                GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel(workspace);
                trachViewChange(gm);
                AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(workspace);
                attributeModel.addAttributeListener(PartitionControllerImpl.this);
            }

            public void unselect(Workspace workspace) {
                GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel(workspace);
                untrackViewChange(gm);
                model = null;
                AttributeModel attributeModel = workspace.getLookup().lookup(AttributeModel.class);
                attributeModel.removeAttributeListener(PartitionControllerImpl.this);
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                untrackViewChange(null);
            }
        });
        if (pc.getCurrentWorkspace() != null) {
            refreshPartitions = true;
            model = pc.getCurrentWorkspace().getLookup().lookup(PartitionModelImpl.class);
            if (model == null) {
                model = new PartitionModelImpl();
                Workspace workspace = pc.getCurrentWorkspace();
                pc.getCurrentWorkspace().add(model);
                GraphModel gm = Lookup.getDefault().lookup(GraphController.class).getModel(workspace);
                trachViewChange(gm);

                AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel(workspace);
                attributeModel.addAttributeListener(PartitionControllerImpl.this);
            }
        }
    }
    private GraphListener graphListener;

    private void trachViewChange(final GraphModel graphModel) {
        untrackViewChange(graphModel);
        if (model.getVisibleViewId() == -1) {
            model.setVisibleViewId(graphModel.getVisibleView().getViewId());
        }
        graphListener = new GraphListener() {

            public void graphChanged(GraphEvent event) {
                if (event.is(GraphEvent.EventType.VISIBLE_VIEW)) {
                    if (model.getVisibleViewId() != graphModel.getVisibleView().getViewId()) {
                        //View has been updated
                        model.setVisibleViewId(graphModel.getVisibleView().getViewId());
                        setSelectedPartition(null);
                    }
                }
            }
        };

        graphModel.addGraphListener(graphListener);
    }

    private void untrackViewChange(GraphModel graphModel) {
        if (graphListener != null && graphModel != null) {
            graphModel.removeGraphListener(graphListener);
        }
        graphListener = null;
    }

    public void attributesChanged(AttributeEvent event) {
        refreshPartitions = true;
    }

    public void setSelectedPartition(final Partition partition) {
        if (partition == model.getSelectedPartition()) {
            return;
        }
        model.setWaiting(true);
        if (model.getSelectedPartitioning() == PartitionModel.NODE_PARTITIONING) {
            Thread t = new Thread(new Runnable() {

                public void run() {
                    if (partition != null) {
                        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

                        DynamicModel dynamicModel = model.getDynamicModel();
                        TimeInterval timeInterval = dynamicModel != null ? dynamicModel.getVisibleInterval() : null;
                        Estimator estimator = AttributeUtils.getDefault().isDynamicNumberColumn(partition.getColumn()) ? model.getNumberEstimator() : model.getEstimator();
                        PartitionFactory.buildNodePartition((NodePartition) partition, graphModel.getGraphVisible(), timeInterval, estimator);
                    }
                    model.setNodePartition(partition);
                    if (model.getNodeTransformerBuilder() == null) {
                        //Select the first transformer
                        TransformerBuilder[] builders = Lookup.getDefault().lookupAll(TransformerBuilder.class).toArray(new TransformerBuilder[0]);
                        for (int i = 0; i < builders.length; i++) {
                            TransformerBuilder t = builders[i];
                            if (t instanceof TransformerBuilder.Node) {
                                model.setNodeBuilder(t);
                                break;
                            }
                        }
                    }
                    model.setWaiting(false);
                }
            }, "Partition Model refresh");
            t.start();
        } else {
            Thread t = new Thread(new Runnable() {

                public void run() {
                    if (partition != null) {
                        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

                        DynamicModel dynamicModel = model.getDynamicModel();
                        TimeInterval timeInterval = dynamicModel != null ? dynamicModel.getVisibleInterval() : null;
                        Estimator estimator = AttributeUtils.getDefault().isDynamicNumberColumn(partition.getColumn()) ? model.getNumberEstimator() : model.getEstimator();
                        PartitionFactory.buildEdgePartition((EdgePartition) partition, graphModel.getGraphVisible(), timeInterval, estimator);
                    }
                    model.setEdgePartition(partition);
                    if (model.getEdgeTransformerBuilder() == null) {
                        //Select the first transformer
                        TransformerBuilder[] builders = Lookup.getDefault().lookupAll(TransformerBuilder.class).toArray(new TransformerBuilder[0]);
                        for (int i = 0; i < builders.length; i++) {
                            TransformerBuilder t = builders[i];
                            if (t instanceof TransformerBuilder.Edge) {
                                model.setEdgeBuilder(t);
                                break;
                            }
                        }
                    }
                    model.setWaiting(false);
                }
            }, "Partition Model refresh");
            t.start();
        }
    }

    public Partition buildPartition(AttributeColumn column, Graph graph) {
        DynamicModel dynamicModel = model.getDynamicModel();
        TimeInterval timeInterval = dynamicModel != null ? dynamicModel.getVisibleInterval() : null;

        if (AttributeUtils.getDefault().isNodeColumn(column)) {
            NodePartition partition = PartitionFactory.createNodePartition(column);
            Estimator estimator = AttributeUtils.getDefault().isDynamicNumberColumn(column) ? model.getNumberEstimator() : model.getEstimator();
            PartitionFactory.buildNodePartition(partition, graph, timeInterval, estimator);
            return partition;
        } else {
            EdgePartition partition = PartitionFactory.createEdgePartition(column);
            Estimator estimator = AttributeUtils.getDefault().isDynamicNumberColumn(column) ? model.getNumberEstimator() : model.getEstimator();
            PartitionFactory.buildEdgePartition(partition, graph, timeInterval, estimator);
            return partition;
        }
    }

    public void setSelectedPartitioning(final int partitioning) {
        model.setWaiting(true);

        Thread t = new Thread(new Runnable() {

            public void run() {
                model.setSelectedPartitioning(partitioning);
                model.setWaiting(false);
            }
        }, "Partition Model refresh");
        t.start();
    }

    public void setSelectedTransformerBuilder(final TransformerBuilder builder) {
        model.setWaiting(true);
        Thread t = new Thread(new Runnable() {

            public void run() {
                if (model.getSelectedPartitioning() == PartitionModel.NODE_PARTITIONING) {
                    model.setNodeBuilder(builder);
                } else {
                    model.setEdgeBuilder(builder);
                }
                model.setWaiting(false);
            }
        }, "Partition Model refresh");
        t.start();
    }

    public void refreshPartitions() {
        if (refreshPartitions) {
            refreshPartitions = false;
            AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
            GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();

            //Nodes
            List<NodePartition> nodePartitions = new ArrayList<NodePartition>();
            AttributeTable nodeTable = ac.getModel().getNodeTable();
            Graph graph = graphModel.getGraphVisible();
            for (AttributeColumn column : nodeTable.getColumns()) {
                if (PartitionFactory.isPartitionColumn(column) && PartitionFactory.isNodePartitionColumn(column, graph)) {
                    nodePartitions.add(PartitionFactory.createNodePartition(column));
                } else if (PartitionFactory.isDynamicPartitionColumn(column)) {
                    DynamicModel dynamicModel = model.getDynamicModel();
                    TimeInterval timeInterval = dynamicModel != null ? dynamicModel.getVisibleInterval() : null;
                    Estimator estimator = AttributeUtils.getDefault().isDynamicNumberColumn(column) ? model.getNumberEstimator() : model.getEstimator();
                    if (PartitionFactory.isDynamicNodePartitionColumn(column, graph, timeInterval, estimator)) {
                        nodePartitions.add(PartitionFactory.createNodePartition(column));
                    }
                }
            }
            model.setNodePartitions(nodePartitions.toArray(new NodePartition[0]));

            //Edges
            List<EdgePartition> edgePartitions = new ArrayList<EdgePartition>();
            AttributeTable edgeClass = ac.getModel().getEdgeTable();
            for (AttributeColumn column : edgeClass.getColumns()) {
                if (PartitionFactory.isPartitionColumn(column) && PartitionFactory.isEdgePartitionColumn(column, graph)) {
                    edgePartitions.add(PartitionFactory.createEdgePartition(column));
                } else if (PartitionFactory.isDynamicPartitionColumn(column)) {
                    DynamicModel dynamicModel = model.getDynamicModel();
                    TimeInterval timeInterval = dynamicModel != null ? dynamicModel.getVisibleInterval() : null;
                    Estimator estimator = AttributeUtils.getDefault().isDynamicNumberColumn(column) ? model.getNumberEstimator() : model.getEstimator();
                    if (PartitionFactory.isDynamicEdgePartitionColumn(column, graph, timeInterval, estimator)) {
                        edgePartitions.add(PartitionFactory.createEdgePartition(column));
                    }
                }
            }
            model.setEdgePartitions(edgePartitions.toArray(new EdgePartition[0]));
        }
    }

    public void transform(Partition partition, Transformer transformer) {
        if (transformer != null && partition != null) {
            transformer.transform(partition);
        }
    }

    public boolean isGroupable(Partition partition) {
        if (partition instanceof NodePartition) {
            if (partition.getPartsCount() > 0) {
                NodePartition nodePartition = (NodePartition) partition;
                Node n0 = nodePartition.getParts()[0].getObjects()[0];
                GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
                HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
                if (graph.contains(n0) && graph.getParent(n0) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUngroupable(Partition partition) {
        if (partition instanceof NodePartition) {
            if (partition.getPartsCount() > 0) {
                NodePartition nodePartition = (NodePartition) partition;
                Node n0 = nodePartition.getParts()[0].getObjects()[0];
                GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
                HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
                if (graph.contains(n0) && graph.getParent(n0) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public void group(Partition partition) {
        NodePartition nodePartition = (NodePartition) partition;
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        for (Part<Node> p : nodePartition.getParts()) {
            Node[] nodes = p.getObjects();
            List<Node> validNodes = new ArrayList<Node>();
            for (Node n : nodes) {
                if (graph.contains(n)) {
                    validNodes.add(n);
                }
            }
            if (!validNodes.isEmpty() && graph.getParent(validNodes.get(0)) == null) {
                float centroidX = 0;
                float centroidY = 0;
                float sizes = 0;
                float r = 0;
                float g = 0;
                float b = 0;
                int len = 0;
                for (Node n : validNodes) {
                    centroidX += n.getNodeData().x();
                    centroidY += n.getNodeData().y();
                    sizes += n.getNodeData().getSize();
                    r += n.getNodeData().r();
                    g += n.getNodeData().g();
                    b += n.getNodeData().b();
                    len++;
                }
                Node metaNode = graph.groupNodes(validNodes.toArray(new Node[0]));
                metaNode.getNodeData().setX(centroidX / len);
                metaNode.getNodeData().setY(centroidY / len);
                metaNode.getNodeData().setLabel(p.getDisplayName());
                metaNode.getNodeData().setSize(sizes / graph.getNodeCount() * 5f);
                metaNode.getNodeData().setColor(r / len, g / len, b / len);
            }
        }
    }

    public void ungroup(Partition partition) {
        NodePartition nodePartition = (NodePartition) partition;
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        HierarchicalGraph graph = graphModel.getHierarchicalGraphVisible();
        for (Part<Node> p : nodePartition.getParts()) {
            Node[] nodes = p.getObjects();
            List<Node> validNodes = new ArrayList<Node>();
            for (Node n : nodes) {
                if (graph.contains(n)) {
                    validNodes.add(n);
                }
            }
            if (!validNodes.isEmpty()) {
                Node metaNode = graph.getParent(validNodes.get(0));
                if (metaNode != null) {
                    graph.ungroupNodes(metaNode);
                }
            }
        }
    }

    public void showPie(boolean showPie) {
        model.setPie(showPie);
    }

    public PartitionModel getModel() {
        return model;
    }
}
