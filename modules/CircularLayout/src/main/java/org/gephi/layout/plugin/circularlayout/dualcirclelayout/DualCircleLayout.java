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
package org.gephi.layout.plugin.circularlayout.dualcirclelayout;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import org.gephi.graph.api.*;
import org.gephi.graph.spi.LayoutData;
import org.gephi.layout.plugin.AbstractLayout;
import org.gephi.layout.plugin.circularlayout.layouthelper.*;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Matt Groeninger
 */
public class DualCircleLayout extends LayoutHelper implements Layout
{
   private Graph   graph;
   private boolean highdegreeoutside;
   private int     secondarynodecount;
   static double   TWO_PI = (2 * Math.PI);
   private String  attribute;

   public DualCircleLayout(LayoutBuilder layoutBuilder, int secondarynodecount)
   {
      super(layoutBuilder);
      this.secondarynodecount = secondarynodecount;
   }

   @Override
   public void initAlgo()
   {
      this.setConverged(false);
      this.graph = graphModel.getGraphVisible();
      this.graph.readLock();
      float[] nodeCoords       = new float[2];
      double  tmpsecondarycirc = 0;
      double  tmpprimarycirc   = 0;
      int     index            = 0;
      double  twopi            = TWO_PI;
      double  lasttheta        = 0;
      double  primary_theta    = 0;
      double  secondary_theta  = 0;
      double  primary_scale    = 1;
      double  secondary_scale  = 1;
      double  correct_theta    = 0;

      if (this.isCW())
      {
         twopi = -twopi;
      }
      Node[] nodes = this.graph.getNodes().toArray();

      nodes = sortNodes(nodes, this.attribute, false);

      for (Node n : nodes)
      {
         if (!n.isFixed())
         {
            if (index < this.secondarynodecount)
            {
               tmpsecondarycirc += (n.size() * 2);
            }
            else
            {
               tmpprimarycirc += (n.size() * 2);
            }
            index++;
         }
      }
      index = 0;  //reset index

      double circum_ratio = tmpprimarycirc / tmpsecondarycirc;

      if (circum_ratio < 2)
      {
         primary_scale  = (2 / circum_ratio);
         tmpprimarycirc = 2 * tmpsecondarycirc;
      }

      if (this.isHighDegreeOutside())
      {
         secondary_scale  = ((2 * tmpprimarycirc) / tmpsecondarycirc); //Need to know how much the circumference has changed from the original
         tmpsecondarycirc = tmpprimarycirc * 2;                        //Scale to a better relationship
      }
      else
      {
         secondary_scale  = (tmpprimarycirc / (2 * tmpsecondarycirc)); //Need to know how much the circumference has changed from the original
         tmpsecondarycirc = tmpprimarycirc / 2;                        //Scale to a better relationship
      }

      tmpprimarycirc = tmpprimarycirc * 1.2;
      primary_theta  = (twopi / tmpprimarycirc);
      double primaryradius = (tmpprimarycirc / Math.PI) / 2;


      tmpsecondarycirc = tmpsecondarycirc * 1.2;
      secondary_theta  = (twopi / tmpsecondarycirc);
      double secondaryradius = (tmpsecondarycirc / Math.PI) / 2;

      for (Node n : nodes)
      {
         TempLayoutData posData = new TempLayoutData();
         if (!n.isFixed())
         {
            if (index < this.secondarynodecount)
            {
               //Draw secondary circle
               double noderadius = (n.size());
               //This step is hackish... but it makes small numbers of nodes symetrical on both the secondary circles.
               if (secondary_scale > 2)
               {
                  noderadius = (tmpsecondarycirc / (2 * this.secondarynodecount * secondary_scale * 1.2));
               }
               double noderadian = (secondary_theta * noderadius * 1.2 * secondary_scale);
               if (index == 0)
               {
                  correct_theta = noderadian;       //correct for cosmetics... overlap prevention offsets the first node by it's radius which looks weird.
               }
               nodeCoords = this.cartCoors(secondaryradius, 1, lasttheta + noderadian - correct_theta);
               lasttheta += (noderadius * 2 * secondary_theta * 1.2 * secondary_scale);
            }
            else
            {
               double noderadius = (n.size());
               double noderadian = (primary_theta * noderadius * 1.2 * primary_scale);
               if (index == this.secondarynodecount)
               {
                  lasttheta     = 0;
                  correct_theta = noderadian;       //correct for cosmetics... overlap prevention offsets the first node by it's radius which looks weird.
               }
               //Draw primary circle
               nodeCoords = this.cartCoors(primaryradius, 1, lasttheta + noderadian - correct_theta);
               lasttheta += (noderadius * 2 * primary_theta * 1.2 * primary_scale);
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
      final String TRANSITION_CATEGORY = NbBundle.getMessage(getClass(), "DualCircleLayout.Category.Transition.name");

      List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
      try {
         properties.add(LayoutProperty.createProperty(
                           this, Boolean.class,
                           NbBundle.getMessage(getClass(), "DualCircleLayout.HighDegreeOutside.name"),
                           NbBundle.getMessage(getClass(), "DualCircleLayout.Category.NodePlacement.name"),
                           NbBundle.getMessage(getClass(), "DualCircleLayout.HighDegreeOutside.desc"),
                           "isHighDegreeOutside", "setHighDegreeOutside"));
         properties.add(LayoutProperty.createProperty(
                           this, Integer.class,
                           NbBundle.getMessage(getClass(), "DualCircleLayout.InnerNodeCount.name"),
                           NbBundle.getMessage(getClass(), "DualCircleLayout.Category.NodePlacement.name"),
                           NbBundle.getMessage(getClass(), "DualCircleLayout.InnerNodeCount.desc"),
                           "getInnerNodeCount", "setInnerNodeCount"));
         properties.add(LayoutProperty.createProperty(
                           this, String.class,
                           NbBundle.getMessage(getClass(), "DualCircleLayout.attribue.name"),
                           NbBundle.getMessage(getClass(), "DualCircleLayout.Category.Sorting.name"),
                           NbBundle.getMessage(getClass(), "DualCircleLayout.attribue.desc"),
                           "getAttribute", "setAttribute", LayoutComboBoxEditor.class));
         properties.add(LayoutProperty.createProperty(
                           this, CircularDirection.class,
                           NbBundle.getMessage(getClass(), "DualCircleLayout.NodePlacement.Direction.name"),
                           NbBundle.getMessage(getClass(), "DualCircleLayout.Category.NodePlacement.name"),
                           NbBundle.getMessage(getClass(), "DualCircleLayout.NodePlacement.Direction.desc"),
                           "getNodePlacementDirection", "setNodePlacementDirection", RotationComboBoxEditor.class));
         properties.add(LayoutProperty.createProperty(
                           this, Boolean.class,
                           NbBundle.getMessage(getClass(), "DualCircleLayout.Transition.EnableTransition.name"),
                           TRANSITION_CATEGORY,
                           NbBundle.getMessage(getClass(), "DualCircleLayout.Transition.EnableTransition.desc"),
                           "isNodePlacementTransition", "setNodePlacementTransition"));
         properties.add(LayoutProperty.createProperty(
                           this, Double.class,
                           NbBundle.getMessage(getClass(), "DualCircleLayout.Transition.TransitionSteps.name"),
                           TRANSITION_CATEGORY,
                           NbBundle.getMessage(getClass(), "DualCircleLayout.Transition.TransitionSteps.desc"),
                           "getTransitionSteps", "setTransitionSteps"));
      }
      catch (Exception e) {
         Logger.getLogger(DualCircleLayout.class.getName()).log(Level.SEVERE, null, e);
      }
      return properties.toArray(new LayoutProperty[0]);
   }

   @Override
   public void resetPropertiesValues()
   {
      setInnerNodeCount(4);
      setHighDegreeOutside(false);
      setNodePlacementDirection(CircularDirection.CCW);
      setAttribute("NodeID");
      setNodePlacementTransition(false);
      setTransitionSteps(100000.0);
   }

   public void setInnerNodeCount(Integer intsecondarynodecount)
   {
      if (graphModel != null)
      {
         graph = this.graphModel.getGraphVisible();
         if (intsecondarynodecount > graph.getNodeCount())
         {
            JOptionPane.showMessageDialog(null,
                                          NbBundle.getMessage(getClass(), "DualCircleLayout.setInnerNodeCount.TooHigh.message"),
                                          NbBundle.getMessage(getClass(), "DualCircleLayout.setInnerNodeCount.TooHigh.title"),
                                          JOptionPane.WARNING_MESSAGE);
         }
         else if (intsecondarynodecount < 1)
         {
            JOptionPane.showMessageDialog(null,
                                          NbBundle.getMessage(getClass(), "DualCircleLayout.setInnerNodeCount.TooLow.message"),
                                          NbBundle.getMessage(getClass(), "DualCircleLayout.setInnerNodeCount.TooLow.title"),
                                          JOptionPane.WARNING_MESSAGE);
         }
         else
         {
            //TODO: add node count check to do boundary checking on user input
            this.secondarynodecount = intsecondarynodecount;
         }
      }
      else
      {
         this.secondarynodecount = 0;
      }
   }

   public Integer getInnerNodeCount()
   {
      return secondarynodecount;
   }

   public Boolean isHighDegreeOutside()
   {
      return highdegreeoutside;
   }

   public void setHighDegreeOutside(Boolean highdegreeoutside)
   {
      this.highdegreeoutside = highdegreeoutside;
   }

   public String getAttribute()
   {
      return this.attribute;
   }

   public void setAttribute(String attribute)
   {
      this.attribute = attribute;
   }
}
