/*
 Copyright 2008-2016 Gephi
 Authors : Clement Levallois
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2016 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2016 Gephi Consortium.
 */
package net.clementlevallois.givecolortoedges;

import java.awt.Color;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.project.api.ProjectController;
import org.gephi.tools.spi.Tool;
import org.gephi.tools.spi.ToolEventListener;
import org.gephi.tools.spi.ToolSelectionType;
import org.gephi.tools.spi.ToolUI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = Tool.class)
public class AddEdgesTool implements Tool {

    private final AddEdgesToolUI ui = new AddEdgesToolUI();
    private Column color;

    @Override
    public void select() {
        NotifyDescriptor d = new NotifyDescriptor.Message("Trying to find a column containing \"color\"  or \"colour\" in your edges attributes, it will then color the edges accordingly");
        DialogDisplayer.getDefault().notify(d);

        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        if (pc.getCurrentProject() == null) {
            d = new NotifyDescriptor.Message("No project opened in Gephi.");
            DialogDisplayer.getDefault().notify(d);
            return;
        }

        GraphController gc = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = gc.getGraphModel();
        Graph graph = graphModel.getGraph();

        for (Column c : graphModel.getEdgeTable().toArray()) {
            if (c.getTypeClass().equals(String.class) && (c.getId().toLowerCase().contains("color") || c.getId().toLowerCase().contains("colour"))) {
                color = c;
                break;
            }
        }

        if (color == null) {
            d = new NotifyDescriptor.Message("No String edge attribute containing \"color\" or \"colour\" could be found");
            DialogDisplayer.getDefault().notify(d);
            return;
        }

        Color hex;
        String[] rgb;

        for (Edge edge : graph.getEdges().toArray()) {
            String colorString = (String) edge.getAttribute(color);
            if (colorString == null) {
                colorString = "";
            }
            if (colorString.contains(",") && (rgb = colorString.split(",")).length == 3) {
                edge.setR(Float.valueOf(rgb[0]) / 255f);
                edge.setG(Float.valueOf(rgb[1]) / 255f);
                edge.setB(Float.valueOf(rgb[2]) / 255f);
            } else if (colorString.contains("#")) {
                hex = Color.decode(colorString);
                edge.setColor(hex);
            } else {
                d = new NotifyDescriptor.Message("No rgb or hex color format detected in " + color.getTitle() + " for edge " + edge.getId());
                DialogDisplayer.getDefault().notify(d);
                return;

            }
        }
    }

    @Override
    public void unselect() {
    }

    @Override
    public ToolEventListener[] getListeners() {
        return new ToolEventListener[]{};
    }

    @Override
    public ToolUI getUI() {
        return ui;
    }

    @Override
    public ToolSelectionType getSelectionType() {
        return ToolSelectionType.NONE;
    }

    private static class AddEdgesToolUI implements ToolUI {

        @Override
        public JPanel getPropertiesBar(Tool tool) {
            JPanel panel = new JPanel();

            return panel;
        }

        @Override
        public Icon getIcon() {
            return new ImageIcon(getClass().getResource("/resources/colorwheel.png"));
        }

        @Override
        public String getName() {
            return "Color nodes";
        }

        @Override
        public String getDescription() {
            return "Color nodes based on a RGB or hex color attribute";
        }

        @Override
        public int getPosition() {
            return 1000;
        }
    }
}
