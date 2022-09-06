/*
 Copyright 2008-2010 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 Gephi is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 Gephi is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.gephi.plugins.positionranking;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.RankingFunction;
import org.gephi.appearance.spi.TransformerCategory;
import org.gephi.appearance.spi.TransformerUI;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Alexis Jacomy, Mathieu Bastian
 */
@ServiceProvider(service = TransformerUI.class, position = 1000)
public class PositionTransformerUI implements TransformerUI {

    private PositionTransformerPanel panel;

    protected static final TransformerCategory CATEGORY = new TransformerCategory() {
        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(PositionTransformerUI.class, "PositionTransformerUI.category");
        }

        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("org/gephi/plugins/positionranking/xyz.png", false);
        }
    };

    @Override
    public TransformerCategory getCategory() {
        return CATEGORY;
    }

    @Override
    public synchronized JPanel getPanel(Function function) {
        if (panel == null) {
            panel = new PositionTransformerPanel();
        }
        panel.setup((RankingFunction) function);
        return panel;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(PositionTransformerUI.class, "PositionTransformerUI.name");
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public AbstractButton[] getControlButton() {
        return null;
    }

    @Override
    public Class getTransformerClass() {
        return PositionTransformer.class;
    }
}
