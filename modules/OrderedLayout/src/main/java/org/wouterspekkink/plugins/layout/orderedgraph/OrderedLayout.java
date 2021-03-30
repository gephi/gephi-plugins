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

 Contributor(s): Wouter Spekkink
 Contribution: Most of the code is from the original Force Atlas 2 source code
 Some changes were made to change the way that nodes are laid out in the x-axis
 The x-axis layout is now based on a variable that should be submitted by the
 user. The approach used to implement this was inspired by the source dode
 of the GeoLayout plugin.

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.wouterspekkink.plugins.layout.orderedgraph;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.gephi.ui.propertyeditor.NodeColumnNumbersEditor;
import org.openide.util.Exceptions;
import org.wouterspekkink.plugins.layout.orderedgraph.ForceFactory.AttractionForce;
import org.wouterspekkink.plugins.layout.orderedgraph.ForceFactory.RepulsionForce;

/**
 *
 * @author wouterspekkink
 */
public class OrderedLayout implements Layout {

    private GraphModel graphModel;
    private Graph graph;
    private final OrderedLayoutBuilder layoutBuilder;
    private double jitterTolerance;
    private double scalingRatio;
    private double orderScale;
    private Column order;
    private double gravity;
    private double speed;
    private boolean strongGravityMode;
    private int threadCount;
    private int currentThreadCount;
    private ExecutorService pool;
    private boolean force;
    private boolean vertical;
    private boolean inverted;
    private boolean center;

    public OrderedLayout(OrderedLayoutBuilder layoutBuilder) {
        this.layoutBuilder = layoutBuilder;
        this.threadCount = Math.min(4, Math.max(1, Runtime.getRuntime().availableProcessors() - 1));
        resetPropertiesValues();
    }

    @Override
    public void initAlgo() {
        speed = 1.;

        graph = graphModel.getGraphVisible();

        graph.readLock();
        Node[] nodes = graph.getNodes().toArray();

        // Initialise layout data
        for (Node n : nodes) {
            if (n.getLayoutData() == null || !(n.getLayoutData() instanceof OrderedLayoutData)) {
                OrderedLayoutData nLayout = new OrderedLayoutData();
                n.setLayoutData(nLayout);
            }
            OrderedLayoutData nLayout = n.getLayoutData();
            nLayout.mass = 1 + graph.getDegree(n);
            nLayout.old_dx = 0;
            nLayout.old_dy = 0;
            nLayout.dx = 0;
            nLayout.dy = 0;
        }
        pool = Executors.newFixedThreadPool(threadCount);
        currentThreadCount = threadCount;

    }

    @Override
    public void goAlgo() {
        // Initialize graph data
        if (graphModel == null) {
            return;
        }
        graph = graphModel.getGraphVisible();

        double ord;
        
        graph.readLock();
        Node[] nodes = graph.getNodes().toArray();
        Edge[] edges = graph.getEdges().toArray();

        ArrayList<Node> validNodes = new ArrayList<Node>();

        for (Node n : nodes) {
            if (n.getAttribute(order) != null) {
                validNodes.add(n);
            }
        }

        // Initialise layout data
        for (Node n : nodes) {
            if (n.getLayoutData() == null || !(n.getLayoutData() instanceof OrderedLayoutData)) {
                OrderedLayoutData nLayout = new OrderedLayoutData();
                n.setLayoutData(nLayout);
            }
            OrderedLayoutData nLayout = n.getLayoutData();
            nLayout.mass = 1 + graph.getDegree(n);
            nLayout.dx = 0;
            nLayout.dy = 0;
            nLayout.old_dx = nLayout.dx;
            nLayout.old_dy = nLayout.dy;

        }
        // Repulsion (and gravity)
        // NB: Muti-threaded
        RepulsionForce Repulsion = ForceFactory.builder.buildRepulsion(getScalingRatio());

        int taskCount = 8 * currentThreadCount;  // The threadPool Executor Service will manage the fetching of tasks and threads.
        // We make more tasks than threads because some tasks may need more time to compute.
        ArrayList<Future> threads = new ArrayList();
        for (int t = taskCount; t > 0; t--) {
            int from = (int) Math.floor(nodes.length * (t - 1) / taskCount);
            int to = (int) Math.floor(nodes.length * t / taskCount);
            Future future = pool.submit(new NodesThread(nodes, from, to, getGravity(), (isStrongGravityMode()) ? (ForceFactory.builder.getStrongGravity(getScalingRatio())) : (Repulsion), getScalingRatio(), Repulsion));
            threads.add(future);
        }
        for (Future future : threads) {
            try {
                future.get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        //Attraction
        AttractionForce Attraction = ForceFactory.builder.buildAttraction(1);
        for (Edge e : edges) {
            Attraction.apply(e.getSource(), e.getTarget(), 1);
        }
        // Auto adjust speed
        double totalSwinging = 0d;  // How much irregular movement
        double totalEffectiveTraction = 0d;  // Hom much useful movement
        for (Node n : nodes) {
            OrderedLayoutData nLayout = n.getLayoutData();
            if (!n.isFixed()) {
                double swinging = Math.sqrt(Math.pow(nLayout.old_dx - nLayout.dx, 2) + Math.pow(nLayout.old_dy - nLayout.dy, 2));
                totalSwinging += nLayout.mass * swinging;   // If the node has a burst change of direction, then it's not converging.
                totalEffectiveTraction += nLayout.mass * 0.5 * Math.sqrt(Math.pow(nLayout.old_dx + nLayout.dx, 2) + Math.pow(nLayout.old_dy + nLayout.dy, 2));
            }
        }
        // We want that swingingMovement < tolerance * convergenceMovement
        double targetSpeed = getJitterTolerance() * getJitterTolerance() * totalEffectiveTraction / totalSwinging;

        // But the speed shoudn't rise too much too quickly, since it would make the convergence drop dramatically.
        double maxRise = 0.5;   // Max rise: 50%
        speed = speed + Math.min(targetSpeed - speed, maxRise * speed);

        // Apply Forces 
        for (Node n : validNodes) {
            //AttributeRow row = (AttributeRow) n.getNodeData().getAttributes();
            OrderedLayoutData nLayout = n.getLayoutData();

            if (!n.isFixed()) {

                // Adaptive auto-speed: the speed of each node is lowered
                // when the node swings.
                double swinging = Math.sqrt((nLayout.old_dx - nLayout.dx) * (nLayout.old_dx - nLayout.dx) + (nLayout.old_dy - nLayout.dy) * (nLayout.old_dy - nLayout.dy));
                //double factor = speed / (1f + Math.sqrt(speed * swinging));
                double factor = speed / (1f + speed * Math.sqrt(swinging));

                double x = n.x() + nLayout.dx * factor;
                double y = n.y() + nLayout.dy * factor;

                //ord = (Double) n.getAttribute(order, graph.getView());
                ord = ((Number) n.getAttribute(order)).doubleValue();

                double averageX = 0;
                double averageY = 0;

                ord = ord * (double) orderScale;
                float ordFloat = (float) ord;

                if (vertical)
                {
                    if (inverted) {
                        n.setY(ordFloat);
                    } else {
                        n.setY(-ordFloat);
                    }
                    if (force) {
                        n.setX((float) x);
                    } else {
                        n.setX(n.x());
                    }
                } else {
                    if (inverted) {
                        n.setX(-ordFloat);
                    } else {
                        n.setX(ordFloat);
                    }
                    if (force) {
                        n.setY((float) y);
                    } else {
                        n.setY(n.y());
                    }
                }
                if (center) {
                    averageX += x;
                    averageY += y;

                    averageX = averageX / nodes.length;
                    averageY = averageY / nodes.length;

                    x = n.x() - averageX;
                    y = n.y() - averageY;

                    n.setX((float) x);
                    n.setY((float) y);

                }

            }

        }

        graph.readUnlockAll();
    }

    @Override
    public boolean canAlgo() {
        return graphModel != null && order != null;
    }

    @Override
    public void endAlgo() {
        for (Node n : graph.getNodes()) {
            n.setLayoutData(null);
        }
        pool.shutdown();
        graph.readUnlockAll();
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String ORDEREDLAYOUT = "Ordered Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    "Vertical layout",
                    ORDEREDLAYOUT,
                    "Sets the layout to a vertical orientation",
                    "isVertical", "setVertical"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    "Inverted layout",
                    ORDEREDLAYOUT,
                    "Inverts the layout",
                    "isInverted", "setInverted"));
            properties.add(LayoutProperty.createProperty(
                    this, Column.class,
                    "Order",
                    ORDEREDLAYOUT,
                    "Selects the attribute that indicates the ordering of nodes",
                    "getOrder", "setOrder", NodeColumnNumbersEditor.class));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    "Scale of Order",
                    ORDEREDLAYOUT,
                    "Determines the separation of the nodes on the axis that is fixed",
                    "getOrderScale", "setOrderScale"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    "Set Force on free axis",
                    ORDEREDLAYOUT,
                    "Used to push unconnected groups of nodes away from each other",
                    "isForce", "setForce"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    "Force strength",
                    ORDEREDLAYOUT,
                    "Sets the strength of the force",
                    "getScalingRatio", "setScalingRatio"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    "Strong Gravity Mode",
                    ORDEREDLAYOUT,
                    "Sets the strong gravity mode",
                    "isStrongGravityMode", "setStrongGravityMode"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    "Gravity",
                    ORDEREDLAYOUT,
                    "Pulls nodes to origin of the free axis. Prevents islands from drifting away.",
                    "getGravity", "setGravity"));
            properties.add(LayoutProperty.createProperty(
                    this, Double.class,
                    "Jitter Tolerance",
                    ORDEREDLAYOUT,
                    "How much swiging you allow.",
                    "getJitterTolerance", "setJitterTolerance"));
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    "Threads",
                    ORDEREDLAYOUT,
                    "Possibility to use more threads if your cores can handle it.",
                    "getThreadsCount", "setThreadsCount"));
            properties.add(LayoutProperty.createProperty(
                    this, Boolean.class,
                    "Center",
                    ORDEREDLAYOUT,
                    "Centers the graph",
                    "isCenter", "setCenter"));

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public void resetPropertiesValues() {
        order = null;
        if (graphModel != null) {
            for (Column c : graphModel.getNodeTable()) {
                if (c.getId().equalsIgnoreCase("order")
                        || c.getId().equalsIgnoreCase("ord")
                        || c.getId().equalsIgnoreCase("order")
                        || c.getId().equalsIgnoreCase("trophic_level")) {
                    order = c;
                }
            }
        }
        int nodesCount = 0;
        setOrderScale(10.0);
        if (graphModel != null) {
            nodesCount = graphModel.getGraphVisible().getNodeCount();
            if (order != null) {
                double min = 0.0;
                double max = 0.0;
                Node[] nodes = graphModel.getGraphVisible().getNodes().toArray();
                boolean initialized = false;
                for (int i = 0; i !=  nodes.length; i++) {
                    if (nodes[i].getAttribute(order) != null) {
                        if (!initialized) {
                            min = ((Number) nodes[i].getAttribute(order)).doubleValue();
                            max = ((Number) nodes[i].getAttribute(order)).doubleValue();
                            initialized = true;
                        } else {
                            if (((Number) nodes[i].getAttribute(order)).doubleValue() < min) {
                                min = ((Number) nodes[i].getAttribute(order)).doubleValue();
                            }
                            if (((Number) nodes[i].getAttribute(order)).doubleValue() > max) {
                                max = ((Number) nodes[i].getAttribute(order)).doubleValue();
                            }
                        }
                    } 
                }
                double range = max - min;
                if (range < 10.0) {
                    setOrderScale(1000.0);
                } else if (range < 100.0) {
                    setOrderScale(100.0);
                } else if (range < 1000) {
                    setOrderScale(10.0);
                } else if (range < 10000) {
                    setOrderScale(1.0);
                }
            }
        }

        setVertical(false);
        setInverted(false);
        
        // Tuning
        setScalingRatio(4.0);

        setStrongGravityMode(false);
        setGravity(1.);

        setForce(false);
        setCenter(false);

        // Performance
        if (nodesCount >= 50000) {
            setJitterTolerance(10d);
        } else if (nodesCount >= 5000) {
            setJitterTolerance(1d);
        } else {
            setJitterTolerance(0.3d);
        }
        setThreadsCount(2);
    }

    @Override
    public LayoutBuilder getBuilder() {
        return layoutBuilder;
    }

    @Override
    public void setGraphModel(GraphModel graphModel) {
        this.graphModel = graphModel;
        // Trick: reset here to take the profile of the graph in account for default values
        resetPropertiesValues();
    }

    public Double getJitterTolerance() {
        return jitterTolerance;
    }

    public void setJitterTolerance(Double jitterTolerance) {
        this.jitterTolerance = jitterTolerance;
    }

    public Double getScalingRatio() {
        return scalingRatio;
    }

    public void setScalingRatio(Double scalingRatio) {
        this.scalingRatio = scalingRatio;
    }

    public Boolean isStrongGravityMode() {
        return strongGravityMode;
    }

    public void setStrongGravityMode(Boolean strongGravityMode) {
        this.strongGravityMode = strongGravityMode;
    }

    public Boolean isForce() {
        return force;
    }

    public void setForce(Boolean force) {
        this.force = force;
    }
    
    public Boolean isVertical() {
        return vertical;
    }

    public void setVertical(Boolean vertical) {
        this.vertical = vertical;
    }
    
    public Boolean isInverted() {
        return inverted;
    }
    
    public void setInverted (Boolean inverted) {
        this.inverted = inverted;
    }
    
    public Boolean isCenter() {
        return center;
    }

    public void setCenter(Boolean center) {
        this.center = center;
    }

    public Double getGravity() {
        return gravity;
    }

    public void setGravity(Double gravity) {
        this.gravity = gravity;
    }

    public Integer getThreadsCount() {
        return threadCount;
    }

    public void setThreadsCount(Integer threadCount) {
        if (threadCount < 1) {
            setThreadsCount(1);
        } else {
            this.threadCount = threadCount;
        }
    }

    public Double getOrderScale() {
        return orderScale;
    }

    public void setOrderScale(Double orderScale) {
        this.orderScale = orderScale;
    }

    public Column getOrder() {
        return order;
    }

    public void setOrder(Column order) {
        this.order = order;
    }

}
