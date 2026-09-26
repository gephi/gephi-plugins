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

import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Matt
 */
@ServiceProvider(service = LayoutBuilder.class)
public class CircleLayoutBuilder implements LayoutBuilder
{
   private CircleLayoutUI ui = new CircleLayoutUI();

   @Override
   public String getName()
   {
      return NbBundle.getMessage(CircleLayoutBuilder.class, "CircleLayout.name");
   }

   @Override
   public Layout buildLayout()
   {
      return new CircleLayout(this, 500.0, false);
   }

   @Override
   public LayoutUI getUI()
   {
      return ui;
   }

   private static class CircleLayoutUI implements LayoutUI {
      @Override
      public String getDescription()
      {
         return NbBundle.getMessage(CircleLayoutBuilder.class, "CircleLayout.description");
      }

      @Override
      public Icon getIcon()
      {
         return null;
      }

      @Override
      public JPanel getSimplePanel(Layout layout)
      {
         return null;
      }

      @Override
      public int getQualityRank()
      {
         return -1;
      }

      @Override
      public int getSpeedRank()
      {
         return -1;
      }
   }
}
