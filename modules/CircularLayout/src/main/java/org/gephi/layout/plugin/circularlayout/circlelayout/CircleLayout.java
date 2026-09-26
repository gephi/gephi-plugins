/*
 * Copyright (c) 2010, Matt Groeninger
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.gephi.layout.plugin.circularlayout.circlelayout;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.gephi.graph.api.*;
import org.gephi.graph.spi.LayoutData;
import org.gephi.layout.plugin.circularlayout.layouthelper.*;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Matt
 */
public class CircleLayout extends LayoutHelper implements Layout
{
   private Graph       graph;
   private double      diameter;
   private boolean     boolfixeddiameter;
   private String      strNodeplacement;
   static final double TWO_PI = (2 * Math.PI);

   public CircleLayout(LayoutBuilder layoutBuilder, double diameter, boolean boolfixeddiameter)
   {
      super(layoutBuilder);
      this.diameter          = diameter;
      this.boolfixeddiameter = boolfixeddiameter;
   }

   @Override
   public void initAlgo()
   {
      this.setConverged(false);
      this.graph = graphModel.getGraphVisible();
      this.graph.readLock();
      float[]        nodeCoords  = new float[2];
      double         tmpcirc     = 0;
      double         tmpdiameter = 0;
      int            index       = 0;
      Node[] nodes = this.graph.getNodes().toArray();
      int            nodecount   = this.graph.getNodeCount();
      double         theta       = TWO_PI / nodecount;
      double         noderadius  = 0;
      double         lasttheta   = 0;
      TempLayoutData posData;

      for (Node n : nodes)
      {
         if (!n.isFixed())
         {
            tmpcirc += (n.size() * 2);
         }
      }
      tmpcirc     = (tmpcirc * 1.2);
      tmpdiameter = tmpcirc / Math.PI;
      if (this.isNodePlacementNoOverlap())
      {
         theta = (TWO_PI / tmpcirc);
      }

      if (this.isBoolFixedDiameter() && (tmpdiameter < this.diameter))
      {
         tmpdiameter = this.diameter;
      }
      double radius = tmpdiameter / 2;

      //determine Node placement
      nodes = sortNodes(nodes, this.strNodeplacement, false);

      if (this.isCW())
      {
         theta = -theta;
      }

      for (Node n : nodes)
      {
         posData = new TempLayoutData();
         if (!n.isFixed())
         {
            if (this.isNodePlacementNoOverlap())
            {
               noderadius = (n.size());
               double noderadian = (theta * noderadius * 1.2);
               nodeCoords = this.cartCoors(radius, 1, lasttheta + noderadian);
               lasttheta += (noderadius * 2 * theta * 1.2);
            }
            else
            {
               nodeCoords = this.cartCoors(radius, index, theta);
            }
            posData.finishx = nodeCoords[0];
            posData.finishy = nodeCoords[1];
            index++;
         }
         else
         {
            posData.finishx = n.x();
            posData.finishy = n.y();
         }
         posData.xdistance = (float)(1 / this.getTransitionSteps()) * (nodeCoords[0] - n.x());
         posData.ydistance = (float)(1 / this.getTransitionSteps()) * (nodeCoords[1] - n.y());
         n.setLayoutData(posData);
      }
      this.graph.readUnlock();
   }

   @Override
   public void goAlgo()
   {
      this.graph.readLock();
      this.setConverged(true);
      TempLayoutData position = null;
      Node[]         nodes    = this.graph.getNodes().toArray();
      for (Node n : nodes)
      {
         if (n.getLayoutData() != null)
         {
            position = n.getLayoutData();
            if (this.isNodePlacementTransition())
            {
               float currentDistance = Math.abs(n.x() - position.finishx);
               float nextDistance    = Math.abs(n.x() + position.xdistance - position.finishx);
               if (nextDistance < currentDistance)
               {
                  n.setX(n.x() + position.xdistance);
                  this.setConverged(false);
               }
               else
               {
                  n.setX(position.finishx);
               }
               currentDistance = Math.abs(n.y() - position.finishy);
               nextDistance    = Math.abs(n.y() + position.ydistance - position.finishy);
               if (nextDistance < currentDistance)
               {
                  n.setY(n.y() + position.ydistance);
                  this.setConverged(false);
               }
               else
               {
                  n.setY(position.finishy);
               }
               if ((n.y() == position.finishy) && (n.x() == position.finishx))
               {
                  n.setLayoutData(null);
               }
            }
            else
            {
               n.setX(position.finishx);
               n.setY(position.finishy);
               n.setLayoutData(null);
            }
         }
      }
      this.graph.readUnlock();
   }

   @Override
   public LayoutProperty[] getProperties()
   {
      List<LayoutProperty> properties          = new ArrayList<LayoutProperty>();
      final String         PROPERTIES_CATEGORY = NbBundle.getMessage(getClass(), "CircleLayout.Category.CircleProperties.name");
      final String         PLACEMENT_CATEGORY  = NbBundle.getMessage(getClass(), "CircleLayout.Category.NodePlacement.name");
      final String         TRANSITION_CATEGORY = NbBundle.getMessage(getClass(), "CircleLayout.Category.Transition.name");
      try {
         properties.add(LayoutProperty.createProperty(
                           this, Boolean.class,
                           NbBundle.getMessage(getClass(), "CircleLayout.BoolFixedDiameter.name"),
                           PROPERTIES_CATEGORY,
                           NbBundle.getMessage(getClass(), "CircleLayout.BoolFixedDiameter.desc"),
                           "isBoolFixedDiameter", "setBoolFixedDiameter"));
         properties.add(LayoutProperty.createProperty(
                           this, Double.class,
                           NbBundle.getMessage(getClass(), "CircleLayout.Diameter.name"),
                           PROPERTIES_CATEGORY,
                           NbBundle.getMessage(getClass(), "CircleLayout.Diameter.desc"),
                           "getDiameter", "setDiameter"));
         properties.add(LayoutProperty.createProperty(
                           this, String.class,
                           NbBundle.getMessage(getClass(), "CircleLayout.NodePlacement.NodeOrdering.name"),
                           PROPERTIES_CATEGORY,
                           NbBundle.getMessage(getClass(), "CircleLayout.NodePlacement.NodeOrdering.desc"),
                           "getNodePlacement", "setNodePlacement", LayoutComboBoxEditor.class));
         properties.add(LayoutProperty.createProperty(
                           this, CircularDirection.class,
                           NbBundle.getMessage(getClass(), "CircleLayout.NodePlacement.Direction.name"),
                           PLACEMENT_CATEGORY,
                           NbBundle.getMessage(getClass(), "CircleLayout.NodePlacement.Direction.desc"),
                           "getNodePlacementDirection", "setNodePlacementDirection", RotationComboBoxEditor.class));
         properties.add(LayoutProperty.createProperty(
                           this, Boolean.class,
                           NbBundle.getMessage(getClass(), "CircleLayout.NodePlacement.NoOverlap.name"),
                           PLACEMENT_CATEGORY,
                           NbBundle.getMessage(getClass(), "CircleLayout.NodePlacement.NoOverlap.desc"),
                           "isNodePlacementNoOverlap", "setNodePlacementNoOverlap"));
         properties.add(LayoutProperty.createProperty(
                           this, Boolean.class,
                           NbBundle.getMessage(getClass(), "CircleLayout.Transition.EnableTransition.name"),
                           TRANSITION_CATEGORY,
                           NbBundle.getMessage(getClass(), "CircleLayout.Transition.EnableTransition.desc"),
                           "isNodePlacementTransition", "setNodePlacementTransition"));
         properties.add(LayoutProperty.createProperty(
                           this, Double.class,
                           NbBundle.getMessage(getClass(), "CircleLayout.Transition.TransitionSteps.name"),
                           TRANSITION_CATEGORY,
                           NbBundle.getMessage(getClass(), "CircleLayout.Transition.TransitionSteps.desc"),
                           "getTransitionSteps", "setTransitionSteps"));
      }
      catch (Exception e) {
        Logger.getLogger(CircleLayout.class.getName()).log(Level.SEVERE, null, e);
      }
      return properties.toArray(new LayoutProperty[0]);
   }

   @Override
   public void resetPropertiesValues()
   {
      setDiameter(500.0);
      setBoolFixedDiameter(false);
      setNodePlacement("NodeID");
      setNodePlacementNoOverlap(true);
      setNodePlacementDirection(CircularDirection.CCW);
      setNodePlacementTransition(false);
      setTransitionSteps(100000.0);
   }

   public void setNodePlacement(String strNodeplacement)
   {
      this.strNodeplacement = strNodeplacement;
   }

   public String getNodePlacement()
   {
      return this.strNodeplacement;
   }

   public void setBoolFixedDiameter(Boolean boolfixeddiameter)
   {
      this.boolfixeddiameter = boolfixeddiameter;
   }

   public boolean isBoolFixedDiameter()
   {
      return boolfixeddiameter;
   }

   public void setDiameter(Double diameter)
   {
      this.diameter = diameter;
   }

   public Double getDiameter()
   {
      return diameter;
   }
}
