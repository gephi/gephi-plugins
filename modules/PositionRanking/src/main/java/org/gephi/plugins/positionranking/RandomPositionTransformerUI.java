package org.gephi.plugins.positionranking;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.spi.TransformerCategory;
import org.gephi.appearance.spi.TransformerUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = TransformerUI.class, position = 999)
public class RandomPositionTransformerUI implements TransformerUI {

    @Override
    public TransformerCategory getCategory() {
        return PositionTransformerUI.CATEGORY;
    }

    @Override
    public JPanel getPanel(Function function) {
        return null;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(PositionTransformerUI.class, "RandomPositionTransformerUI.name");
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
        return new AbstractButton[0];
    }

    @Override
    public Class getTransformerClass() {
        return RandomPositionTransformer.class;
    }
}
