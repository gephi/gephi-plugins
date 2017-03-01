/*
  Copyright (C) 2016 Gary Briggs

  This software is provided 'as-is', without any express or implied
  warranty.  In no event will the authors be held liable for any damages
  arising from the use of this software.

  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:

  1. The origin of this software must not be misrepresented; you must not
     claim that you wrote the original software. If you use this software
     in a product, an acknowledgment in the product documentation would be
     appreciated but is not required.
  2. Altered source versions must be plainly marked as such, and must not be
     misrepresented as being the original software.
  3. This notice may not be removed or altered from any source distribution.

        Gary Briggs <chunky@icculus.org>
*/

package org.icculus.chunky.gephigraphviz;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = LayoutBuilder.class)
public class Graphviz implements LayoutBuilder {

    private GraphvizLayoutUI ui = new GraphvizLayoutUI();

    public String getName() {
        return NbBundle.getMessage(Graphviz.class, "graphviz.name");
    }

    public GraphvizLayout buildLayout() {
        return new GraphvizLayout(this);
    }

    public LayoutUI getUI() {
        return ui;
    }

    private static class GraphvizLayoutUI implements LayoutUI {

        public String getDescription() {
            return NbBundle.getMessage(Graphviz.class, "graphviz.description");
        }

        public Icon getIcon() {
            return new ImageIcon("gvicon.png");
        }

        public JPanel getSimplePanel(Layout layout) {
            GraphvizLayout gvl = (GraphvizLayout)layout;
            return new GraphvizUIPanel(gvl);
        }

        public int getQualityRank() {
            return -1;
        }

        public int getSpeedRank() {
            return -1;
        }
    }
}
